package com.highmobility.hmkit;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import com.highmobility.hmkit.Crypto.AccessCertificate;
import com.highmobility.btcore.HMDevice;
import com.highmobility.hmkit.Error.BroadcastError;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by ttiganik on 12/04/16.
 *
 * Broadcaster acts as a gateway to the application's capability to broadcast itself and handle ConnectedLink connectivity.
 *
 */
public class Broadcaster implements SharedBleListener {
    static final String TAG = "HMLink";

    public enum State { BLUETOOTH_UNAVAILABLE, IDLE, BROADCASTING }

    public interface StartCallback {
        void onBroadcastingStarted();
        void onBroadcastingFailed(BroadcastError error);
    }

    static int advertiseMode = AdvertiseSettings.ADVERTISE_MODE_BALANCED;
    static int txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;

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
    State state = State.IDLE;

    ArrayList<ConnectedLink> links = new ArrayList<>();

    byte[] issuer; // these are set from BTCoreInterface HMBTHalAdvertisementStart.
    byte[] appId;
    byte[] advertisedSerial;

    /**
     * Sets the advertise mode for the Bluetooth's AdvertiseSettings. Default is ADVERTISE_MODE_BALANCED.
     *
     * @param advertiseMode the advertise mode
     * @see AdvertiseSettings
     */
    public static void setAdvertiseMode(int advertiseMode) {
        if (advertiseMode > AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
                || advertiseMode < AdvertiseSettings.ADVERTISE_MODE_LOW_POWER) return;
        Broadcaster.advertiseMode = advertiseMode;
    }

    /**
     * Sets the TX power level for the Bluetooth's AdvertiseSettings. Default is ADVERTISE_TX_POWER_HIGH.
     *
     * @param txPowerLevel the advertise mode
     * @see AdvertiseSettings
     */
    public static void setTxPowerLevel(int txPowerLevel) {
        if (txPowerLevel > AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
                || txPowerLevel < AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW) return;
        Broadcaster.txPowerLevel = txPowerLevel;
    }

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
     *
     * @return The name of the advertised peripheral
     */
    public String getName() {
        return manager.ble.getAdapter().getName();
    }

    /**
     *
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
        return manager.storage.getCertificatesWithoutProvidingSerial(manager.certificate.getSerial());
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
     * @param callback is invoked with the start broadcasting result
     *                 onBroadcastingStarted is invoked if the broadcasting started
     *                 onBroadcastingFailed is invoked if something went wrong.
     */
    public void startBroadcasting(StartCallback callback) {
        if (state == State.BROADCASTING) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "will not start broadcasting: already broadcasting");

            callback.onBroadcastingStarted();
        }

        if (!manager.ble.isBluetoothSupported()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            callback.onBroadcastingFailed(new BroadcastError(BroadcastError.Type.UNSUPPORTED
            , 0, "Bluetooth is no supported"));
            return;
        }

        if (!manager.ble.isBluetoothOn()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            callback.onBroadcastingFailed(new BroadcastError(BroadcastError.Type.BLUETOOTH_OFF
                    , 0, "Bluetooth is turned off"));
            return;
        }

        // start advertising
        if (mBluetoothLeAdvertiser == null) {
            mBluetoothLeAdvertiser = manager.ble.getAdapter().getBluetoothLeAdvertiser();
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
                .setAdvertiseMode(advertiseMode)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(txPowerLevel)
                .build();

        UUID advertiseUUID;
        if (advertisedSerial == null) {
            byte[] uuidBytes = ByteUtils.concatBytes(issuer, appId);
            ByteUtils.reverse(uuidBytes);
            advertiseUUID = ByteUtils.UUIDFromByteArray(uuidBytes);
        }
        else {
            byte[] uuidBytes = ByteUtils.concatBytes(new byte[] {0x00, 0x00, 0x00, 0x00}, advertisedSerial);
            uuidBytes = ByteUtils.concatBytes(uuidBytes, new byte[] {0x00, 0x00, 0x00});
            advertiseUUID = ByteUtils.UUIDFromByteArray(uuidBytes);
        }

        final AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
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
            mBluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
            mBluetoothLeAdvertiser = null;
        }

        setState(State.IDLE);
    }

    /**
     * Sets the given serial number in the broadcast info, so other devices know before connecting
     * if this device is interesting to them or not.
     *
     * Set this before calling startBroadcasting. Set this to null to use regular broadcast info.
     *
     * @param serial the serial set in the broadcast info
     */
    public void setBroadcastingTarget(byte[] serial) {
        advertisedSerial = serial;
    }

    /**
     * Activate or disable the alive ping mode.
     *
     * @param alivePinging Whether the alive ping mode should be actived or stopped
     */
    public void setIsAlivePinging(boolean alivePinging) {
        if (alivePinging == isAlivePinging) return;
        isAlivePinging = alivePinging;
        if (isAlivePinging) {
            sendAlivePing();
        }
    }

    /**
     * Registers the AccessCertificate for the broadcaster, enabling authenticated
     * connection to another broadcaster.
     *
     * @param certificate The certificate that can be used by the Device to authorised Links
     * @return Result code 0 on success or
     *         {@link Storage.Result#INTERNAL_ERROR} if the given certificates providing serial doesn't match with
     *                      broadcaster's serial or the certificate is null.
     *         {@link Storage.Result#STORAGE_FULL} if the storage is full.
     */
    public Storage.Result registerCertificate(AccessCertificate certificate)  {
        if (manager.certificate == null) {
            return Storage.Result.INTERNAL_ERROR;
        }

        if (Arrays.equals(manager.certificate.getSerial(), certificate.getProviderSerial()) == false) {
            return Storage.Result.INTERNAL_ERROR;
        }

        return manager.storage.storeCertificate(certificate);
    }

    /**
     * Stores a Certificate to Device's storage. This certificate is usually read by other Devices.
     *
     * @param certificate The certificate that will be saved to the database
     * @return Result code 0 on success or
     * {@link Storage.Result#STORAGE_FULL} if the storage is full
     * {@link Storage.Result#INTERNAL_ERROR} if certificate is null.
     */
    public Storage.Result storeCertificate(AccessCertificate certificate) {
        return manager.storage.storeCertificate(certificate);
    }

    /**
     * Revokes a stored certificate from Device's storage. The stored certificate and its
     * accompanying registered certificate are deleted from the storage.
     *
     *  @param serial The 9-byte serial number of the access providing broadcaster
     *  @return {@link Storage.Result#SUCCESS }
     *  {@link Storage.Result#INTERNAL_ERROR } if there are no matching certificate pairs for this serial.
     */
    public Storage.Result revokeCertificate(byte[] serial) {
        if (manager.storage.certWithGainingSerial(serial) == null
                || manager.storage.certWithProvidingSerial(serial) == null) {
            return Storage.Result.INTERNAL_ERROR;
        }

        if (manager.storage.deleteCertificateWithGainingSerial(serial) == false) return Storage.Result.INTERNAL_ERROR;
        if (manager.storage.deleteCertificateWithProvidingSerial(serial) == false) return Storage.Result.INTERNAL_ERROR;

        return Storage.Result.SUCCESS;
    }

    Broadcaster(Manager manager) {
        this.manager = manager;
        manager.ble.addListener(this);
    }

    @Override
    public void bluetoothChangedToAvailable(boolean available) {
        if (available && getState() == State.BLUETOOTH_UNAVAILABLE) {
            setState(State.IDLE);
        }
        else if (!available && getState() != State.BLUETOOTH_UNAVAILABLE) {
            setState(State.BLUETOOTH_UNAVAILABLE);
        }
    }

    void terminate() {
        stopBroadcasting();
        setListener(null);

        for (ConnectedLink link : getLinks()) {
            link.setListener(null);
            link.broadcaster = null;
        }

        manager.workHandler.removeCallbacks(clockRunnable);
        setIsAlivePinging(false);

        manager.ble.removeListener(this);

        if (GATTServer != null) {
            GATTServer.clearServices();
            GATTServer.close();
            GATTServer = null;
        }

        gattServerCallback = null;
        advertiseCallback = null;
        clockRunnable = null;
        startCallback = null;
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
            final Broadcaster devicePointer = this;
            devicePointer.manager.mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    devicePointer.listener.onLinkReceived(link);
                }
            });
        }
    }

    boolean deviceExitedProximity(HMDevice device) {
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(TAG, "lose link " + ByteUtils.hexFromBytes(device.getMac()));

        final ConnectedLink link = getLinkForMac(device.getMac());
        if (link == null) return false;

        if (link.getState() != Link.State.DISCONNECTED) {
            GATTServer.cancelConnection(link.btDevice);
            link.setState(Link.State.DISCONNECTED);
        }

        links.remove(link);

        // set new adapter name
        if (links.size() == 0) {
            manager.ble.setRandomAdapterName();
        }

        // invoke the listener listener
        if (listener != null) {
            final Broadcaster devicePointer = this;
            devicePointer.manager.mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    devicePointer.listener.onLinkLost(link);
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
        if (link == null) return false;

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "write " + ByteUtils.hexFromBytes(value) + " to " + ByteUtils.hexFromBytes(link.getAddressBytes()));

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
        if (GATTServer != null) return true;

        gattServerCallback = new GATTServerCallback(this);
        GATTServer = manager.ble.getManager().openGattServer(manager.context, gattServerCallback);

        if (GATTServer == null) {
            Log.e(TAG, "Cannot create gatt server"); return false;
        }
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue()) Log.d(TAG, "createGATTServer");

        /// bluez hack service
        /*UUID BLUEZ_HACK_SERVICE_UUID = UUID.fromString("48494D4F-BB81-49AB-BE90-6F25D716E8DE");
        BluetoothGattService bluezHackService = new BluetoothGattService(BLUEZ_HACK_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        GATTServer.addService(bluezHackService);*/
        ///

        // create the service
        BluetoothGattService service = new BluetoothGattService(Constants.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // add characteristics to the service
        readCharacteristic = new BluetoothGattCharacteristic(Constants.READ_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);

        sensingReadCharacteristic = new BluetoothGattCharacteristic(Constants.SENSING_READ_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);


        writeCharacteristic =
                new BluetoothGattCharacteristic(Constants.WRITE_CHAR_UUID,
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        sensingWriteCharacteristic = new BluetoothGattCharacteristic(Constants.SENSING_WRITE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        aliveCharacteristic = new BluetoothGattCharacteristic(Constants.ALIVE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);

        infoCharacteristic = new BluetoothGattCharacteristic(Constants.INFO_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        if (readCharacteristic.addDescriptor(new BluetoothGattDescriptor(Constants.NOTIFY_DESC_UUID,
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ)) == false) {
            Log.e(TAG, "Cannot add read descriptor"); return false;
        }

        if (sensingReadCharacteristic.addDescriptor(new BluetoothGattDescriptor(Constants.NOTIFY_DESC_UUID,
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ)) == false) {
            Log.e(TAG, "Cannot add sensing read descriptor"); return false;
        }

        if (aliveCharacteristic.setValue(new byte[]{}) == false) {
            Log.e(TAG, "Cannot set alive char value"); return false;
        }

        if (aliveCharacteristic.addDescriptor(new BluetoothGattDescriptor(Constants.NOTIFY_DESC_UUID,
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ)) == false) {
            Log.e(TAG, "Cannot add alive descriptor"); return false;
        }

        if (infoCharacteristic.setValue(manager.getInfoString()) == false) {
            Log.e(TAG, "Cannot set info char value"); return false;
        }

        if (service.addCharacteristic(readCharacteristic) == false) {
            Log.e(TAG, "Cannot add read char"); return false;
        }

        if (service.addCharacteristic(sensingReadCharacteristic) == false) {
            Log.e(TAG, "Cannot add sensing read char"); return false;
        }

        if (service.addCharacteristic(writeCharacteristic) == false) {
            Log.e(TAG, "Cannot add write char"); return false;
        }

        if (service.addCharacteristic(sensingWriteCharacteristic) == false) {
            Log.e(TAG, "Cannot add sensing write char"); return false;
        }

        if (service.addCharacteristic(aliveCharacteristic) == false) {
            Log.e(TAG, "Cannot add alive char"); return false;
        }

        if (service.addCharacteristic(infoCharacteristic) == false) {
            Log.e(TAG, "Cannot add info char"); return false;
        }

        if (GATTServer.addService(service) == false) {
            Log.e(TAG, "Cannot add service to GATT server"); return false;
        }

        return true;
    }


    private void sendAlivePing() {
        if (aliveCharacteristic != null) {
            for (Link link : links) {
                GATTServer.notifyCharacteristicChanged(link.btDevice, aliveCharacteristic, false);
            }
        }
        else {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "need to start broadcasting before pinging");
        }

        if (isAlivePinging) {
            manager.workHandler.postDelayed(clockRunnable, 55);
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
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "Start advertise " + Manager.getInstance().ble.getAdapter().getName());
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

                    broadcaster.get().startCallback.onBroadcastingFailed(new BroadcastError(BroadcastError.Type.BLUETOOTH_FAILURE
                            , 0, "Failed to start BLE advertisements"));
                }
            }
        }
    }

    private void setState(final State state) {
        if (this.state != state) {
            final State oldState = this.state;
            this.state = state;

            Runnable broadcastStateChange = new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onStateChanged(oldState);
                    }
                }
            };

            if (Looper.myLooper() != manager.mainHandler.getLooper()) {
                manager.mainHandler.post(broadcastStateChange);
            }
            else {
                broadcastStateChange.run();
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
