package com.high_mobility.HMLink;

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
import com.high_mobility.HMLink.Crypto.AccessCertificate;

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
    BluetoothGattCharacteristic aliveCharacteristic;
    BluetoothGattCharacteristic infoCharacteristic;

    boolean isAlivePinging;
    State state = State.IDLE;

    ArrayList<ConnectedLink> links = new ArrayList<>();

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
     * @throws Link.    An exception with either UNSUPPORTED or BLUETOOTH_OFF code.
     */
    public int startBroadcasting() {
        if (state == State.BROADCASTING) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "will not start broadcasting: already broadcasting");

            return 0;
        }

        if (!manager.ble.isBluetoothSupported()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            return Link.UNSUPPORTED;
        }

        if (!manager.ble.isBluetoothOn()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            return Link.BLUETOOTH_OFF;
        }

        if (createGATTServer() == false) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            return Link.BLUETOOTH_FAILURE;
        }

        // start advertising
        if (mBluetoothLeAdvertiser == null) {
            mBluetoothLeAdvertiser = manager.ble.getAdapter().getBluetoothLeAdvertiser();
            if (mBluetoothLeAdvertiser == null) {
                // for unsupported devices the system does not return an advertiser
                setState(State.BLUETOOTH_UNAVAILABLE);
                return Link.UNSUPPORTED;
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
        return 0;
    }

    /**
     * Stops the advertisements and disconnects all the links.
     */
    public void stopBroadcasting() {
        if (getState() != State.BROADCASTING) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "already not broadcasting");
        }
        // stopAdvertising clears the GATT server as well.
        // This causes all connection to fail with the link because there is no GATT server.
        try {
            for (int i = getLinks().size() - 1; i >= 0; i--) {
                GATTServer.cancelConnection(getLinks().get(i).btDevice);
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
     * Activate or disable the alive ping mode.
     *
     * @param alivePinging Whether the alive ping mode should be actived or stopped
     */
    public void setIsAlivePinging(boolean alivePinging) {
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
     * @throws Link.When this broadcaster's certificate hasn't been set, the given certificates
     *                       providing serial doesn't match with this broadcaster's serial or
     *                       the storage is full.
     */
    // TODO: update comment
    public int registerCertificate(AccessCertificate certificate)  {
        if (manager.certificate == null) {
            return Link.INTERNAL_ERROR;
        }

        if (Arrays.equals(manager.certificate.getSerial(), certificate.getProviderSerial()) == false) {
            return Link.INTERNAL_ERROR;
        }

        return storage.storeCertificate(certificate);
    }

    /**
     * Stores a Certificate to Device's storage. This certificate is usually read by other Devices.
     *
     * @param certificate The certificate that will be saved to the database
     * @throws Link.When the storage is full or certificate has not been set
     */
    // TODO: comment
    public int storeCertificate(AccessCertificate certificate) {
        return storage.storeCertificate(certificate);
    }

    /**
     * Revokes a stored certificate from Device's storage. The stored certificate and its
     * accompanying registered certificate are deleted from the storage.
     *
     * @param serial The 9-byte serial number of the access providing broadcaster
     * @throws Link.When there are no matching certificate pairs for this serial.
     */
    // TODO: comment
    public int revokeCertificate(byte[] serial) {
        if (storage.certWithGainingSerial(serial) == null
                || storage.certWithProvidingSerial(serial) == null) {
            return Link.INTERNAL_ERROR;
        }

        if (storage.deleteCertificateWithGainingSerial(serial) == false) return Link.INTERNAL_ERROR;
        if (storage.deleteCertificateWithProvidingSerial(serial) == false) return Link.INTERNAL_ERROR;

        return 0;
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
        manager.ble.addListener(this);
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
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue()) Log.d(TAG, "lose link " + ByteUtils.hexFromBytes(device.getMac()));

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

    boolean writeData(byte[] mac, byte[] value) {
        ConnectedLink link = getLinkForMac(mac);
        if (link == null) return false;

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "write " + ByteUtils.hexFromBytes(value) + " to " + ByteUtils.hexFromBytes(link.getAddressBytes()));

        readCharacteristic.setValue(value);
        GATTServer.notifyCharacteristicChanged(link.btDevice, readCharacteristic, false);

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
        if (GATTServer == null) {
            gattServerCallback = new GATTServerCallback(this);
            GATTServer = manager.ble.getManager().openGattServer(manager.ctx, gattServerCallback);

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

            writeCharacteristic =
                    new BluetoothGattCharacteristic(Constants.WRITE_CHAR_UUID,
                            BluetoothGattCharacteristic.PROPERTY_WRITE,
                            BluetoothGattCharacteristic.PERMISSION_WRITE);

            aliveCharacteristic = new BluetoothGattCharacteristic(Constants.ALIVE_CHAR_UUID,
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ);

            infoCharacteristic = new BluetoothGattCharacteristic(Constants.INFO_CHAR_UUID,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);

            if (readCharacteristic.addDescriptor(new BluetoothGattDescriptor(Constants.NOTIFY_DESC_UUID,
                    BluetoothGattDescriptor.PERMISSION_WRITE)) == false) {
                Log.e(TAG, "Cannot add read descriptor"); return false;
            }
            if (aliveCharacteristic.setValue(new byte[]{}) == false) {
                Log.e(TAG, "Cannot set alive char value"); return false;
            }

            if (aliveCharacteristic.addDescriptor(new BluetoothGattDescriptor(Constants.NOTIFY_DESC_UUID,
                    BluetoothGattDescriptor.PERMISSION_WRITE)) == false) {
                Log.e(TAG, "Cannot add alive descriptor"); return false;
            }
            if (infoCharacteristic.setValue(manager.getInfoString()) == false) {
                Log.e(TAG, "Cannot set info char value"); return false;
            }

            if (service.addCharacteristic(readCharacteristic) == false) {
                Log.e(TAG, "Cannot add read char"); return false;
            }

            if (service.addCharacteristic(writeCharacteristic) == false) {
                Log.e(TAG, "Cannot add write char"); return false;
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
            manager.workHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendAlivePing();
                }
            }, 55);
        }
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue()) Log.d(TAG, "Start advertise " + manager.ble.getAdapter().getName());
            setState(State.BROADCASTING);
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
                manager.mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStateChanged(oldState);
                    }
                });
            }
        }
    }
}
