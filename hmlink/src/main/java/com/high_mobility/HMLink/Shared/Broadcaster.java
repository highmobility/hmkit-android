package com.high_mobility.HMLink.Shared;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;
import android.util.Log;

import com.high_mobility.btcore.HMDevice;
import com.high_mobility.HMLink.LinkException;
import com.high_mobility.HMLink.AccessCertificate;

import java.util.Arrays;
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

    static int advertiseMode = AdvertiseSettings.ADVERTISE_MODE_BALANCED;
    static int txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;

    Storage storage;
    BroadcasterListener listener;

    Manager manager;

    BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    BluetoothGattServer GATTServer;
    GATTServerCallback gattServerCallback;

    BluetoothGattCharacteristic readCharacteristic;
    BluetoothGattCharacteristic writeCharacteristic;

    State state = State.IDLE;
    ConnectedLink[] links = new ConnectedLink[0];
    static Broadcaster instance = null;

    /**
     * Sets the advertise mode for the AdvertiseSettings
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
     * Sets the TX power level for the AdvertiseSettings
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
     * In order to receive Broadcaster events, a listener must be set.
     *
     * @param listener The listener instance to receive Broadcaster events.
     */
    public void setListener(BroadcasterListener listener) {
        this.listener = listener;
    }



    /**
     * @return The certificates that are registered on the Broadcaster.
     */
    public AccessCertificate[] getRegisteredCertificates() {
        return storage.getCertificatesWithProvidingSerial(manager.certificate.getSerial());
    }

    /**
     * @return The certificates that are stored in the broadcaster's database for other devices.
     */
    public AccessCertificate[] getStoredCertificates() {
        return storage.getCertificatesWithoutProvidingSerial(manager.certificate.getSerial());
    }

    /**
     * @return The Links currently connected to the Broadcaster.
     */
    public ConnectedLink[] getLinks() {
        return links;
    }

    /**
     * Start broadcasting the Broadcaster via BLE advertising.
     *
     * @throws LinkException	    An exception with either UNSUPPORTED or BLUETOOTH_OFF code.
     */
    public void startBroadcasting() throws LinkException {
        if (state == State.BROADCASTING) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.All.getValue())
                Log.d(TAG, "will not start broadcasting: already broadcasting");

            return;
        }

        if (!manager.ble.isBluetoothSupported()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            throw new LinkException(LinkException.LinkExceptionCode.UNSUPPORTED);
        }

        if (!manager.ble.isBluetoothOn()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            throw new LinkException(LinkException.LinkExceptionCode.BLUETOOTH_OFF);
        }

        createGATTServer();

        // start advertising
        if (mBluetoothLeAdvertiser == null) {
            mBluetoothLeAdvertiser = manager.ble.getAdapter().getBluetoothLeAdvertiser();
            if (mBluetoothLeAdvertiser == null) {
                // for unsupported devices the system does not return an advertiser
                setState(State.BLUETOOTH_UNAVAILABLE);
                throw new LinkException(LinkException.LinkExceptionCode.UNSUPPORTED);
            }
        }

        final AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(advertiseMode)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(txPowerLevel)
                .build();

        final UUID advertiseUUID = ByteUtils.UUIDFromByteArray(ByteUtils.concatBytes(manager.certificate.getIssuer(), manager.certificate.getAppIdentifier()));

        final AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(advertiseUUID))
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback);
    }

    /**
     * Stops the advertisements and disconnects all the links.
     */
    public void stopBroadcasting() {
        if (getState() != State.BROADCASTING) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.All.getValue())
                Log.d(TAG, "already not broadcasting");
        }
        // stopAdvertising clears the GATT server as well.
        // This causes all connection to fail with the link because there is no GATT server.
        try {
            for (int i = getLinks().length - 1; i >= 0; i--) {
                GATTServer.cancelConnection(getLinks()[i].btDevice);
            }

            if (mBluetoothLeAdvertiser != null) {
                mBluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
            }

            setState(State.IDLE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers the AccessCertificate for the broadcaster, enabling authenticated
     * connection to another broadcaster.
     *
     * @param certificate The certificate that can be used by the Device to authorised Links
     * @throws LinkException When this broadcaster's certificate hasn't been set, the given certificates
     *                       providing serial doesn't match with this broadcaster's serial or
     *                       the storage is full.
     */
    public void registerCertificate(AccessCertificate certificate) throws LinkException {
        if (manager.certificate == null) {
            throw new LinkException(LinkException.LinkExceptionCode.INTERNAL_ERROR);
        }

        if (Arrays.equals(manager.certificate.getSerial(), certificate.getProviderSerial()) == false) {
            throw new LinkException(LinkException.LinkExceptionCode.INTERNAL_ERROR);
        }

        storage.storeCertificate(certificate);
    }

    /**
     * Stores a Certificate to Device's storage. This certificate is usually read by other Devices.
     *
     * @param certificate The certificate that will be saved to the database
     * @throws LinkException When the storage is full or certificate has not been set
     */
    public void storeCertificate(AccessCertificate certificate) throws LinkException {
        storage.storeCertificate(certificate);
    }

    /**
     * Revokes a stored certificate from Device's storage. The stored certificate and its
     * accompanying registered certificate are deleted from the storage.
     *
     * @param serial The 9-byte serial number of the access providing broadcaster
     * @throws LinkException When there are no matching certificate pairs for this serial.
     */
    public void revokeCertificate(byte[] serial) throws LinkException {
        if (storage.certWithGainingSerial(serial) == null
                || storage.certWithProvidingSerial(serial) == null) {
            throw new LinkException(LinkException.LinkExceptionCode.INTERNAL_ERROR);
        }

        storage.deleteCertificateWithGainingSerial(serial);
        storage.deleteCertificateWithProvidingSerial(serial);
    }

    /**
     * Deletes the saved certificates, resets the Bluetooth connection and stops broadcasting.
     */
    public void reset() {
        storage.resetStorage();
        stopBroadcasting();

        if (GATTServer != null) {
            GATTServer.clearServices();
            GATTServer.close();
            GATTServer = null;
        }
    }

    Broadcaster(Manager manager) {
        this.manager = manager;
        manager.ble.addListener(instance);
        storage = new Storage(manager.ctx);
    }

    @Override
    public void bluetoothChangedToAvailable(boolean available) {
        if (available && getState() == State.BLUETOOTH_UNAVAILABLE) {
            setState(State.IDLE);
        }
        else if (!available && getState() != State.BLUETOOTH_UNAVAILABLE) {
            if (GATTServer != null) {
                GATTServer.clearServices();
                GATTServer.close();
                GATTServer = null;
            }

            setState(State.BLUETOOTH_UNAVAILABLE);
        }
    }

    int didResolveDevice(HMDevice device) {
        for (int i = 0; i < links.length; i++) {
            ConnectedLink link = links[i];
            if (Arrays.equals(link.getAddressBytes(), device.getMac())) {
                link.setHmDevice(device);
                return i;
            }
        }

        return -1;
    }

    byte[] onCommandReceived(HMDevice device, byte[] data) {
        BluetoothDevice btDevice = manager.ble.getAdapter().getRemoteDevice(device.getMac());
        int linkIndex = linkIndexForBTDevice(btDevice);

        if (linkIndex > -1) {
            ConnectedLink link = links[linkIndex];
            return link.onCommandReceived(data);
        }

        return null;
    }

    boolean onCommandResponseReceived(HMDevice device, byte[] data) {
        BluetoothDevice btDevice = manager.ble.getAdapter().getRemoteDevice(device.getMac());
        int linkIndex = linkIndexForBTDevice(btDevice);

        if (linkIndex < 0) return false;

        ConnectedLink link = links[linkIndex];
        link.onCommandResponseReceived(data);
        return true;
    }

    void didReceiveLink(BluetoothDevice device) {
        // add a new link to the array

        final ConnectedLink link = new ConnectedLink(device, this);
        ConnectedLink[] newLinks = new ConnectedLink[links.length + 1];

        for (int i = 0; i < links.length; i++) {
            newLinks[i] = links[i];
        }

        newLinks[links.length] = link;
        links = newLinks;

        link.setState(ConnectedLink.State.CONNECTED);

        if (listener != null) {
            final Broadcaster devicePointer = this;
            devicePointer.manager.mainThread.post(new Runnable() {
                @Override
                public void run() {
                    devicePointer.listener.onLinkReceived(link);
                }
            });
        }
    }

    boolean didLoseLink(HMDevice device) {
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.All.getValue()) Log.d(TAG, "lose link " + ByteUtils.hexFromBytes(device.getMac()));

        BluetoothDevice btDevice = manager.ble.getAdapter().getRemoteDevice(device.getMac());
        int linkIndex = linkIndexForBTDevice(btDevice);

        if (linkIndex < 0) return false;

        // remove the link from the array
        final ConnectedLink link = links[linkIndex];

        if (link.getState() != Link.State.DISCONNECTED) {
            GATTServer.cancelConnection(link.btDevice);
        }

        ConnectedLink[] newLinks = new ConnectedLink[links.length - 1];

        for (int i = 0; i < links.length; i++) {
            if (i < linkIndex) {
                newLinks[i] = links[i];
            }
            else if (i > linkIndex) {
                newLinks[i - 1] = links[i];
            }
        }

        links = newLinks;

        // set new adapter name
        if (links.length == 0) {
            manager.ble.setRandomAdapterName();
        }

        link.setState(Link.State.DISCONNECTED);

        // invoke the listener listener
        if (listener != null) {
            final Broadcaster devicePointer = this;
            devicePointer.manager.mainThread.post(new Runnable() {
                @Override
                public void run() {
                    devicePointer.listener.onLinkLost(link);
                }
            });
        }

        return true;
    }

    int didReceivePairingRequest(HMDevice device) {
        int linkIndex = didResolveDevice(device);

        if (linkIndex > -1) {
            final ConnectedLink link = links[linkIndex];
            return link.didReceivePairingRequest();
        }
        else {
            Log.e(TAG, "no link for pairingResponse");
            return 1;
        }
    }

    boolean writeData(byte[] mac, byte[] value) {
        ConnectedLink link = getLinkForMac(mac);
        if (link == null) return false;
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.Debug.getValue())
            Log.d(TAG, "write " + ByteUtils.hexFromBytes(value) + " to " + ByteUtils.hexFromBytes(link.getAddressBytes()));

        readCharacteristic.setValue(value);
        GATTServer.notifyCharacteristicChanged(link.btDevice, readCharacteristic, false);

        return true;
    }

    ConnectedLink getLinkForMac(byte[] mac) {
        for (int i = 0; i < links.length; i++) {
            ConnectedLink link = links[i];

            if (Arrays.equals(link.getAddressBytes(), mac)) {
                return link;
            }
        }

        return null;
    }

    private int linkIndexForBTDevice(BluetoothDevice device) {
        for (int i = 0; i < links.length; i++) {
            ConnectedLink link = links[i];

            if (link.btDevice.getAddress().equals(device.getAddress())) {
                return i;
            }
        }

        return -1;
    }

    private void createGATTServer() {
        if (GATTServer == null) {
            gattServerCallback = new GATTServerCallback(this);
            GATTServer = manager.ble.getManager().openGattServer(manager.ctx, gattServerCallback);

            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.All.getValue()) Log.d(TAG, "createGATTServer");

            /// bluez hack service
            UUID BLUEZ_HACK_SERVICE_UUID = UUID.fromString("48494D4F-BB81-49AB-BE90-6F25D716E8DE");
            BluetoothGattService bluezHackService = new BluetoothGattService(BLUEZ_HACK_SERVICE_UUID,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);
            GATTServer.addService(bluezHackService);
            ///

            // create the service
            BluetoothGattService service = new BluetoothGattService(Constants.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

            readCharacteristic = new BluetoothGattCharacteristic(Constants.READ_CHAR_UUID,
                            BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                            BluetoothGattCharacteristic.PERMISSION_READ);

            readCharacteristic.addDescriptor(new BluetoothGattDescriptor(Constants.NOTIFY_DESC_UUID,
                    BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));

            writeCharacteristic =
                    new BluetoothGattCharacteristic(Constants.WRITE_CHAR_UUID,
                            BluetoothGattCharacteristic.PROPERTY_WRITE,
                            BluetoothGattCharacteristic.PERMISSION_WRITE);

            service.addCharacteristic(readCharacteristic);
            service.addCharacteristic(writeCharacteristic);

            GATTServer.addService(service);
        }
        else {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.All.getValue()) Log.d(TAG, "createGATTServer: already exists");
        }
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.All.getValue()) Log.d(TAG, "Start advertise " + manager.ble.getAdapter().getName());
            setState(State.BROADCASTING);
        }

        @Override
        public void onStartFailure(int errorCode) {
            switch (errorCode) {
                case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                    setState(State.BROADCASTING);
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                    Log.e(TAG, "Advertise failed: data too large");
                    setState(State.IDLE);
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    Log.e(TAG, "Advertise failed: feature unsupported");
                    setState(State.IDLE);
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                    Log.e(TAG, "Advertise failed: internal error");
                    setState(State.IDLE);
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    Log.e(TAG, "Advertise failed: too many advertisers");
                    setState(State.IDLE);
                    break;
            }

            if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED) {
                setState(State.BROADCASTING);
            } else {
                setState(State.IDLE);
            }
        }
    };

    private void setState(final State state) {
        if (this.state != state) {
            final State oldState = this.state;
            this.state = state;

            if (listener != null) {
                manager.mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStateChanged(state, oldState);
                    }
                });
            }
        }
    }
}
