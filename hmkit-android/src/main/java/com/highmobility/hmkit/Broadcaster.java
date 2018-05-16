package com.highmobility.hmkit;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;
import android.util.Log;

import com.highmobility.btcore.HMDevice;
import com.highmobility.crypto.AccessCertificate;
import com.highmobility.hmkit.error.BroadcastError;
import com.highmobility.utils.Bytes;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by ttiganik on 12/04/16.
 * <p>
 * Broadcaster acts as a gateway to the application's capability to broadcast itself and handle
 * ConnectedLink connectivity.
 */
public class Broadcaster implements SharedBleListener {
    static final String TAG = "HMLink";

    public enum State {BLUETOOTH_UNAVAILABLE, IDLE, BROADCASTING}

    /**
     * Startcallback is used to notify the user about the start broadcasting result
     */
    public interface StartCallback {
        /**
         * Invoked when the broadcasting was started.
         */
        void onBroadcastingStarted();

        /**
         * Invoked when there was an error with starting the broadcast.
         *
         * @param error The error
         */
        void onBroadcastingFailed(BroadcastError error);
    }

    BroadcasterListener listener;
    Manager manager;

    StartCallback startCallback;
    BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    BluetoothGattServer GATTServer;
    GATTServerCallback gattServerCallback;

    BluetoothGattCharacteristic readCharacteristic;
    BluetoothGattCharacteristic writeCharacteristic;
    BluetoothGattCharacteristic aliveCharacteristic;
    BluetoothGattCharacteristic infoCharacteristic;
    BluetoothGattCharacteristic sensingReadCharacteristic;
    BluetoothGattCharacteristic sensingWriteCharacteristic;

    boolean isAlivePinging;
    long alivePingInterval = 500;

    State state = State.IDLE;

    ArrayList<ConnectedLink> links = new ArrayList<>();

    byte[] issuer; // these are set from BTCoreInterface HMBTHalAdvertisementStart.
    byte[] appId;

    BroadcastConfiguration configuration;

    /**
     * The possible states of the local broadcaster are represented by the enum Broadcaster.State.
     *
     * @return The current state of the Broadcaster.
     * @see Broadcaster.State
     */
    public State getState() {
        return state;
    }

    /**
     * @return The name of the advertised peripheral
     */
    public String getName() {
        return manager.getBle().getAdapter().getName();
    }

    /**
     * @return indiation of whether the alive pinging is active or not
     */
    public boolean isAlivePinging() {
        return isAlivePinging;
    }

    /**
     * @return The certificates that are registered on the Broadcaster.
     */
    public AccessCertificate[] getRegisteredCertificates() {
        return manager.storage.getCertificatesWithProvidingSerial(manager.certificate.getSerial());
    }

    /**
     * @return The certificates that are stored in the broadcaster's database for other devices.
     */
    public AccessCertificate[] getStoredCertificates() {
        return manager.storage.getCertificatesWithoutProvidingSerial(manager.certificate
                .getSerial());
    }

    /**
     * @return The Links currently connected to the Broadcaster.
     */
    public List<ConnectedLink> getLinks() {
        return links;
    }

    /**
     * In order to receive Broadcaster events, a listener must be set.
     *
     * @param listener The listener instance to receive Broadcaster events.
     */
    public void setListener(BroadcasterListener listener) {
        this.listener = listener;
    }

    /**
     * Start broadcasting the Broadcaster via BLE advertising.
     *
     * @param callback      This is invoked with the start broadcasting result.
     *                      onBroadcastingStarted is invoked if the broadcasting started.
     *                      onBroadcastingFailed is invoked if something went wrong.
     * @param configuration The broadcast configuration.
     */
    public void startBroadcasting(StartCallback callback, BroadcastConfiguration configuration) {
        this.configuration = configuration;
        startBroadcasting(callback);
    }

    /**
     * Start broadcasting the Broadcaster via BLE advertising.
     *
     * @param callback is invoked with the start broadcasting result. onBroadcastingStarted is
     *                 invoked if the broadcasting started. onBroadcastingFailed is invoked if
     *                 something went wrong.
     */
    public void startBroadcasting(StartCallback callback) {
        if (state == State.BROADCASTING) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "will not start broadcasting: already broadcasting");

            callback.onBroadcastingStarted();
        }

        if (!manager.getBle().isBluetoothSupported()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            callback.onBroadcastingFailed(new BroadcastError(BroadcastError.Type.UNSUPPORTED
                    , 0, "Bluetooth is no supported"));
            return;
        }

        if (!manager.getBle().isBluetoothOn()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            callback.onBroadcastingFailed(new BroadcastError(BroadcastError.Type.BLUETOOTH_OFF
                    , 0, "Bluetooth is turned off"));
            return;
        }

        manager.getBle().addListener(this);
        if (this.configuration == null) this.configuration = new BroadcastConfiguration();
        manager.getBle().setRandomAdapterName(configuration.isOverridingAdvertisementName());

        // start advertising
        if (mBluetoothLeAdvertiser == null) {
            mBluetoothLeAdvertiser = manager.getBle().getAdapter().getBluetoothLeAdvertiser();

            if (mBluetoothLeAdvertiser == null) {
                // for unsupported devices the system does not return an advertiser
                setState(State.BLUETOOTH_UNAVAILABLE);
                callback.onBroadcastingFailed(new BroadcastError(BroadcastError.Type.UNSUPPORTED
                        , 0, "Bluetooth is no supported"));
                return;
            }
        }

        if (createGATTServer() == false) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            callback.onBroadcastingFailed(new BroadcastError(BroadcastError.Type.BLUETOOTH_FAILURE
                    , 0, "Bluetooth failed to start"));
            return;
        }

        final AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(configuration.getAdvertiseMode())
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(configuration.getTxPowerLevel())
                .build();

        UUID advertiseUUID;
        byte[] uuidBytes;

        if (configuration.getBroadcastTarget() == null) {
            uuidBytes = Bytes.concatBytes(issuer, appId);
        } else {
            uuidBytes = Bytes.concatBytes(new byte[]{0x00, 0x00, 0x00, 0x00}, configuration
                    .getBroadcastTarget());
            uuidBytes = Bytes.concatBytes(uuidBytes, new byte[]{0x00, 0x00, 0x00});
        }

        Bytes.reverse(uuidBytes);
        advertiseUUID = Bytes.UUIDFromByteArray(uuidBytes);

        final AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(configuration.isOverridingAdvertisementName() == true)
                .addServiceUuid(new ParcelUuid(advertiseUUID))
                .build();

        startCallback = callback;
        mBluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback);
    }

    /**
     * Stops the advertisements and disconnects all the links.
     */
    public void stopBroadcasting() {
        if (getState() != State.BROADCASTING) return; // we are not broadcasting

        // stopAdvertising cancels all the BT connections as well.
        if (mBluetoothLeAdvertiser != null) {
            Log.d(TAG, "stopBroadcasting: ");
            mBluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
            mBluetoothLeAdvertiser = null;
        }

        this.configuration = null;
        setState(State.IDLE);
    }

    /**
     * Start the alive ping mode. This can be done after broadcasting has started, even if there are
     * no connected links. Call {@link #stopAlivePinging()} to stop.
     *
     * @param interval Interval of the ping, in ms.
     */
    public void startAlivePinging(long interval) {
        alivePingInterval = interval;
        if (isAlivePinging == true) return;
        isAlivePinging = true;
        sendAlivePing();
    }

    /**
     * Stop the alive pinging.
     */
    public void stopAlivePinging() {
        isAlivePinging = false;
        manager.workHandler.removeCallbacks(clockRunnable);
    }

    /**
     * Registers the AccessCertificate for the broadcaster, enabling authenticated connection to
     * other devices.
     *
     * @param certificate The certificate that can be used by the Device to authorised Links
     * @return {@link Storage.Result#SUCCESS} on success or {@link Storage.Result#INTERNAL_ERROR} if
     * the given certificates providing serial doesn't match with broadcaster's serial or the
     * certificate is null. {@link Storage.Result#STORAGE_FULL} if the storage is full.
     */
    public Storage.Result registerCertificate(AccessCertificate certificate) {
        if (manager.certificate == null) {
            return Storage.Result.INTERNAL_ERROR;
        }

        if (Arrays.equals(manager.certificate.getSerial(), certificate.getProviderSerial()) ==
                false) {
            return Storage.Result.INTERNAL_ERROR;
        }

        return manager.storage.storeCertificate(certificate);
    }

    /**
     * Stores a Certificate to Device's storage. This certificate is usually read by other Devices.
     *
     * @param certificate The certificate that will be saved to the database
     * @return {@link Storage.Result#SUCCESS} on success or {@link Storage.Result#STORAGE_FULL} if
     * the storage is full. {@link Storage.Result#INTERNAL_ERROR} if certificate is null.
     */
    public Storage.Result storeCertificate(AccessCertificate certificate) {
        return manager.storage.storeCertificate(certificate);
    }

    /**
     * Revokes a stored certificate from Device's storage. The stored certificate and its
     * accompanying registered certificate are deleted from the storage.
     *
     * @param serial The 9-byte serial number of the access providing broadcaster
     * @return {@link Storage.Result#SUCCESS} on success or {@link Storage.Result#INTERNAL_ERROR }
     * if there are no matching certificate pairs for this serial.
     */
    public Storage.Result revokeCertificate(byte[] serial) {
        if (manager.storage.certWithGainingSerial(serial) == null
                || manager.storage.certWithProvidingSerial(serial) == null) {
            return Storage.Result.INTERNAL_ERROR;
        }

        if (manager.storage.deleteCertificateWithGainingSerial(serial) == false)
            return Storage.Result.INTERNAL_ERROR;
        if (manager.storage.deleteCertificateWithProvidingSerial(serial) == false)
            return Storage.Result.INTERNAL_ERROR;

        return Storage.Result.SUCCESS;
    }

    /**
     * Tries to cancel all Bluetooth connections and stop broadcasting. This has proven being slow
     * or not working at all. Success may be related to the specific device or it's Android
     * version.
     * <p>
     * If successful, the link's state will change to disconnected and {@link
     * com.highmobility.hmkit.BroadcasterListener#onLinkLost(ConnectedLink)}} will be called.
     * <p>
     * The user is responsible for releasing the Link's BroadcasterListener.
     */
    public void disconnectAllLinks() {
        if (GATTServer == null) return;

        List<BluetoothDevice> devices = manager.getBle().getManager().getConnectedDevices
                (BluetoothProfile.GATT_SERVER);

        for (BluetoothDevice device : devices) {
            // just to make sure all of the devices are tried to be disconnected. disconnect
            // callback
            // should find the one in this.links if it exists.
            GATTServer.cancelConnection(device);
        }

        stopBroadcasting();
        stopAlivePinging();

        // cant close service here, we wont get disconnect callback
    }

    void closeService() {
        // this should never be called. Once you create a service it should not be changed. All
        // communication should finish or be cancelled
        if (GATTServer == null) return;
        GATTServer.clearServices();
        GATTServer.close();
        GATTServer = null; // but with this we wont get link callbacks
        setListener(null); // there are no more callbacks coming
    }

    Broadcaster(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void bluetoothChangedToAvailable(boolean available) {
        if (available && getState() == State.BLUETOOTH_UNAVAILABLE) {
            setState(State.IDLE);
        } else if (!available && getState() != State.BLUETOOTH_UNAVAILABLE) {
            setState(State.BLUETOOTH_UNAVAILABLE);
        }
    }

    boolean didResolveDevice(HMDevice device) {
        final ConnectedLink link = getLinkForMac(device.getMac());
        if (link == null) return false;

        link.setHmDevice(device);

        return true;
    }

    void linkDidConnect(BluetoothDevice device) {
        // need to dispatch the link before authenticating to forward pairing request for instance
        final ConnectedLink link = new ConnectedLink(device, this);
        links.add(link);

        if (listener != null) {
            manager.postToMainThread(new Runnable() {
                @Override public void run() {
                    if (listener == null) return;
                    listener.onLinkReceived(link);
                }
            });
        }
    }

    boolean deviceExitedProximity(HMDevice device) {
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(TAG, "lose link " + Bytes.hexFromBytes(device.getMac()));

        final ConnectedLink link = getLinkForMac(device.getMac());
        if (link == null) return false;

        links.remove(link);
        link.setState(Link.State.DISCONNECTED);

        // invoke the listener listener
        if (listener != null) {
            manager.postToMainThread(new Runnable() {
                @Override public void run() {
                    if (listener == null) return;
                    listener.onLinkLost(link);
                    link.listener = null; // nothing to do with the link anymore
                    // pointless to set random adapter name here because if already broadcasting
                    // android will not change the name and if not broadcasting then startBroadcast
                    // will change the name
                }
            });
        }

        return true;
    }

    boolean onCommandResponseReceived(HMDevice device, byte[] data) {
        Link link = getLinkForMac(device.getMac());
        if (link == null) return false;

        link.onCommandResponseReceived(data);
        return true;
    }

    boolean onCommandReceived(HMDevice device, byte[] data) {
        Link link = getLinkForMac(device.getMac());
        if (link == null) return false;
        link.onCommandReceived(data);
        return true;
    }

    int didReceivePairingRequest(HMDevice device) {
        ConnectedLink link = getLinkForMac(device.getMac());
        if (link != null) {
            return link.didReceivePairingRequest();
        }

        return 1;
    }

    boolean writeData(byte[] mac, byte[] value, int characteristicId) {
        ConnectedLink link = getLinkForMac(mac);
        if (link == null || link.btDevice == null) return false;

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "write " + Bytes.hexFromBytes(value) + " to " + Bytes.hexFromBytes(link
                    .getAddressBytes()) + " char: " + characteristicId);

        BluetoothGattCharacteristic characteristic = getCharacteristicForId(characteristicId);
        if (characteristic == null) {
            Log.e(TAG, "no characteristic for write");
            return false;
        }

        if (characteristic.setValue(value) == false) {
            Log.e(TAG, "can't set read char value");
            return false;
        }

        if (GATTServer.notifyCharacteristicChanged(link.btDevice, characteristic, false) == false) {
            Log.e(TAG, "can't notify characteristic changed");
            return false;
        }

        return true;
    }

    ConnectedLink getLinkForMac(byte[] mac) {
        for (int i = 0; i < links.size(); i++) {
            ConnectedLink link = links.get(i);

            if (Arrays.equals(link.getAddressBytes(), mac)) {
                return link;
            }
        }

        return null;
    }

    private boolean createGATTServer() {
        if (GATTServer != null && GATTServer.getServices().size() > 0) return true;

        if (GATTServer == null) {
            gattServerCallback = new GATTServerCallback(this);
            GATTServer = manager.getBle().getManager().openGattServer(manager.context,
                    gattServerCallback);

            if (GATTServer == null) {
                Log.e(TAG, "Cannot create gatt server");
                return false;
            }
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(TAG, "createGATTServer");

        // create the service
        BluetoothGattService service = new BluetoothGattService(Constants.SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // add characteristics to the service
        readCharacteristic = new BluetoothGattCharacteristic(Constants.READ_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic
                        .PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);

        sensingReadCharacteristic = new BluetoothGattCharacteristic(Constants
                .SENSING_READ_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic
                        .PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);

        writeCharacteristic =
                new BluetoothGattCharacteristic(Constants.WRITE_CHAR_UUID,
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        sensingWriteCharacteristic = new BluetoothGattCharacteristic(Constants
                .SENSING_WRITE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        aliveCharacteristic = new BluetoothGattCharacteristic(Constants.ALIVE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);

        infoCharacteristic = new BluetoothGattCharacteristic(Constants.INFO_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        if (readCharacteristic.addDescriptor(new BluetoothGattDescriptor(Constants
                .NOTIFY_DESCRIPTOR_UUID,
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor
                        .PERMISSION_READ)) == false) {
            Log.e(TAG, "Cannot add read descriptor");
            return false;
        }

        if (sensingReadCharacteristic.addDescriptor(new BluetoothGattDescriptor(Constants
                .NOTIFY_DESCRIPTOR_UUID,
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor
                        .PERMISSION_READ)) == false) {
            Log.e(TAG, "Cannot add sensing read descriptor");
            return false;
        }

        if (aliveCharacteristic.addDescriptor(new BluetoothGattDescriptor(Constants
                .NOTIFY_DESCRIPTOR_UUID,
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor
                        .PERMISSION_READ)) == false) {
            Log.e(TAG, "Cannot add alive descriptor");
            return false;
        }

        if (aliveCharacteristic.setValue(new byte[]{}) == false) {
            Log.e(TAG, "Cannot set alive char value");
            return false;
        }

        if (infoCharacteristic.setValue(manager.getInfoString()) == false) {
            Log.e(TAG, "Cannot set info char value");
            return false;
        }

        if (service.addCharacteristic(readCharacteristic) == false) {
            Log.e(TAG, "Cannot add read char");
            return false;
        }

        if (service.addCharacteristic(sensingReadCharacteristic) == false) {
            Log.e(TAG, "Cannot add sensing read char");
            return false;
        }

        if (service.addCharacteristic(writeCharacteristic) == false) {
            Log.e(TAG, "Cannot add write char");
            return false;
        }

        if (service.addCharacteristic(sensingWriteCharacteristic) == false) {
            Log.e(TAG, "Cannot add sensing write char");
            return false;
        }

        if (service.addCharacteristic(aliveCharacteristic) == false) {
            Log.e(TAG, "Cannot add alive char");
            return false;
        }

        if (service.addCharacteristic(infoCharacteristic) == false) {
            Log.e(TAG, "Cannot add info char");
            return false;
        }

        if (GATTServer.addService(service) == false) {
            Log.e(TAG, "Cannot add service to GATT server");
            return false;
        }

        return true;
    }

    private void sendAlivePing() {
        if (aliveCharacteristic != null) {
            for (Link link : links) {
                GATTServer.notifyCharacteristicChanged(link.btDevice, aliveCharacteristic, false);
            }
        } else {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "need to start broadcasting before pinging");
        }

        if (isAlivePinging) {
            manager.workHandler.postDelayed(clockRunnable, alivePingInterval);
        }
    }

    ClockRunnable clockRunnable = new ClockRunnable(this);

    static class ClockRunnable implements Runnable {
        WeakReference<Broadcaster> broadcaster;

        ClockRunnable(Broadcaster broadcaster) {
            this.broadcaster = new WeakReference<>(broadcaster);
        }

        @Override
        public void run() {
            broadcaster.get().sendAlivePing();
        }
    }

    AdvertiseCb advertiseCallback = new AdvertiseCb(this);

    static class AdvertiseCb extends AdvertiseCallback {
        WeakReference<Broadcaster> broadcaster;

        AdvertiseCb(Broadcaster broadcaster) {
            this.broadcaster = new WeakReference<>(broadcaster);
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue()) {
                String name;
                if (broadcaster.get().configuration.isOverridingAdvertisementName()) {
                    name = broadcaster.get().getName();
                } else {
                    name = "not advertising name";
                }

                Log.d(TAG, "Start advertise: " + name);
            }
            broadcaster.get().setState(State.BROADCASTING);
            if (broadcaster.get().startCallback != null) {
                broadcaster.get().startCallback.onBroadcastingStarted();
            }
        }

        @Override
        public void onStartFailure(int errorCode) {
            switch (errorCode) {
                case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                    Log.e(TAG, "Advertise failed: data too large");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    Log.e(TAG, "Advertise failed: feature unsupported");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                    Log.e(TAG, "Advertise failed: internal error");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    Log.e(TAG, "Advertise failed: too many advertisers");
                    break;
            }

            if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED) {
                broadcaster.get().setState(State.BROADCASTING);
            } else {
                broadcaster.get().setState(State.IDLE);

                if (broadcaster.get().startCallback != null) {
                    broadcaster.get().startCallback.onBroadcastingFailed(new BroadcastError
                            (BroadcastError.Type.BLUETOOTH_FAILURE
                                    , 0, "Failed to start BLE advertisements"));
                }
            }
        }
    }

    private void setState(final State state) {
        if (this.state != state) {
            final State oldState = this.state;
            this.state = state;

            if (listener != null) {
                manager.postToMainThread(new Runnable() {
                    @Override public void run() {
                        if (listener == null) return;
                        listener.onStateChanged(oldState);
                    }
                });
            }
        }
    }

    BluetoothGattCharacteristic getCharacteristicForId(int id) {
        switch (id) {
            case BTCoreInterface.hm_characteristic_alive: {
                return aliveCharacteristic;
            }
            case BTCoreInterface.hm_characteristic_info: {
                return infoCharacteristic;
            }
            case BTCoreInterface.hm_characteristic_link_read: {
                return readCharacteristic;
            }
            case BTCoreInterface.hm_characteristic_link_write: {
                return writeCharacteristic;
            }
            case BTCoreInterface.hm_characteristic_sensing_read: {
                return sensingReadCharacteristic;
            }
            case BTCoreInterface.hm_characteristic_sensing_write: {
                return sensingWriteCharacteristic;
            }
            default:
                return null;
        }
    }
}
