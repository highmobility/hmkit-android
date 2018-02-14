package com.highmobility.hmkit;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.highmobility.utils.Bytes;
import com.highmobility.crypto.AccessCertificate;

import java.util.List;
import java.util.UUID;

/**
 * The ScannedLink is a representation of the connection between the Scanner and a device that the
 * Scanner has connected to and validated as a High-Mobility broadcaster.
 *
 * The ScannedLinks's interface provides the ability
 * to send commands and handle incoming requests from the ConnectedLink.
 *
 * Created by ttiganik on 01/06/16.
 */
class ScannedLink extends Link {
    private static final String TAG = "ScannedLink";

    Scanner scanner;

    BluetoothGatt gatt;

    BluetoothGattCharacteristic readCharacteristic;
    BluetoothGattCharacteristic writeCharacteristic;
    BluetoothGattCharacteristic aliveCharacteristic;
    BluetoothGattCharacteristic infoCharacteristic;

    Constants.ResponseCallback rssiCallback;
    int rssi;

    Constants.ResponseCallback infoCallback;
    String info;


    /**
     * Read the RSSI of the Link's underlying Bluetooth device.
     *
     * @param callback The callback that is invoked after receiving the RSSI.
     */
    public void readRSSI(Constants.ResponseCallback callback) {
        this.rssiCallback = callback;
        gatt.readRemoteRssi();
    }

    public int rssi() {
        return rssi;
    }

    public void readVersionInfo(Constants.ResponseCallback callback) {
        if (infoCharacteristic != null) {
            infoCallback = callback;
            gatt.readCharacteristic(infoCharacteristic);
        }
    }

    public String versionInfo() {
        return versionInfo();
    }

    /**
     * Set the Link listener to receive commands and state change events.
     *
     * @param listener The object that implements LinkListener.
     */
    public void setListener(LinkListener listener) {
        this.listener = listener;
    }


    void registerCertificate(AccessCertificate certificate, Constants.ResponseCallback callback) {
        // TODO:
    }

    void storeCertificate(AccessCertificate certificate, Constants.ResponseCallback callback) {
        // TODO:
    }

    void getAccessCertificate(AccessCertificate certificate, Constants.ResponseCallback callback) {
        // TODO:
    }

    void revokeCertificate(byte[] serial, Constants.ResponseCallback callback) {
        // TODO:
    }

    void reset(Constants.ResponseCallback callback) {
        // TODO:
    }

    ScannedLink(Scanner scanner, BluetoothDevice btDevice) {
        super(scanner.manager, btDevice);
        this.scanner = scanner;
        this.btDevice = btDevice;
    }

    void writeValue(byte[] value) {
        if (writeCharacteristic != null){
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "write value " + Bytes.hexFromBytes(value));

            if (writeCharacteristic.setValue(value) == false || gatt.writeCharacteristic(writeCharacteristic) == false) {
                // TODO: fail auth or command
            }
        }
    }

    void readValue() {
        if (gatt.readCharacteristic(readCharacteristic) == false) {
            // TODO: fail auth or command
        }
    }

    void onDeviceExitedProximity() {
        setState(State.DISCONNECTED);
    }

    void connect() {
        Log.d(TAG, "connect " + btDevice.getAddress() + " " + this);
        gatt = btDevice.connectGatt(scanner.manager.context, false, gattCallback);
    }

    void disconnect() {
        Log.d(TAG, "disconnect " + btDevice.getAddress() + " " + this);
        if (gatt != null) gatt.disconnect();
    }

    void discoverServices() {
        if (gatt.discoverServices() == false) {
            // TODO: how to failed connection should be handled
            // also remove authenticatingMac from scanner

            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "cannot initiate discoverServices");
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // TODO: handle status.. 
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                        Log.d(TAG, "STATE_CONNECTED " + this);
                    scanner.manager.workHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            scanner.manager.core.HMBTCoreSensingConnect(scanner.manager.coreInterface, getAddressBytes());
                        }
                    });
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                        Log.d(TAG, "STATE_DISCONNECTED " + this);

                    scanner.manager.workHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            scanner.manager.core.HMBTCoreSensingDisconnect(scanner.manager.coreInterface, getAddressBytes());
                        }
                    });
                    break;
                default:
                    Log.e(TAG, "INVALID CONNECTION STATE " + this);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                // TODO: how failed service discovery/connection should be handled
                // also remove authenticatingMac from scanner
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                    Log.d(TAG, "onServicesDiscovered failure " + status);
                return;
            }

            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                // service UUID is reversed
                UUID uuid = service.getUuid();

                byte[] msb = longToBytes(uuid.getMostSignificantBits());
                byte[] lsb = longToBytes(uuid.getLeastSignificantBits());

                Bytes.reverse(msb);
                Bytes.reverse(lsb);
                UUID reverseUUID = new UUID(getLong(lsb), getLong(msb));

                if (reverseUUID.equals(Constants.SERVICE_UUID)) {
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        if (characteristic.getUuid().equals(Constants.READ_CHAR_UUID)) {
                            readCharacteristic = characteristic;
                        } else if (characteristic.getUuid().equals(Constants.WRITE_CHAR_UUID)) {
                            writeCharacteristic = characteristic;
                        } else if (characteristic.getUuid().equals(Constants.ALIVE_CHAR_UUID)) {
                            aliveCharacteristic = characteristic;
                        } else if (characteristic.getUuid().equals(Constants.INFO_CHAR_UUID)) {
                            infoCharacteristic = characteristic;
                        }
                    }
                    break;
                }
            }

            Log.i(TAG, "onServicesDiscovered " + (readCharacteristic != null && writeCharacteristic != null));

            if (readCharacteristic != null && writeCharacteristic != null) {
                if (gatt.setCharacteristicNotification(readCharacteristic, true) == false) {
                    // TODO: how to failed connection should be handled
                    // also remove authenticatingMac from scanner

                    if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                        Log.d(TAG, "cannot initiate setCharacteristicNotification");
                    return;
                }

                BluetoothGattDescriptor descriptor = readCharacteristic.getDescriptor(Constants.NOTIFY_DESCRIPTOR_UUID);
                if (descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) == false) {
                    // TODO: how to failed connection should be handled
                    // also remove authenticatingMac from scanner
                    if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                        Log.d(TAG, "cannot initiate descriptor.setValue");
                    return;
                }

                if (gatt.writeDescriptor(descriptor) == false) {
                    // TODO: how to failed connection should be handled
                    // also remove authenticatingMac from scanner
                    if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                        Log.d(TAG, "cannot initiate writeDescriptor");
                }
                // TODO: What to do with ping here?
            } else {
                // TODO: how failed service discovery/connection should be handled
                // also remove authenticatingMac from scanner
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                    Log.d(TAG, "onCharacteristicRead " + Bytes.hexFromBytes(characteristic.getValue()));
                if (characteristic.getUuid().equals(Constants.READ_CHAR_UUID)) {
                    scanner.manager.workHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //TODO add proper characteristic
                            scanner.manager.core.HMBTCoreSensingReadResponse(scanner.manager.coreInterface, characteristic.getValue(), characteristic.getValue().length, 0, getAddressBytes(), 0);
                        }
                    });
                }
                else if (characteristic.getUuid().equals(Constants.INFO_CHAR_UUID)) {
                    info = characteristic.getStringValue(0);
                    Log.v(TAG, "read info " + info);
                }
            }
            else {
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                    Log.d(TAG, "onCharacteristicRead failed " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                scanner.manager.workHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //TODO add proper characteristic
                        scanner.manager.core.HMBTCoreSensingWriteResponse(scanner.manager.coreInterface, getAddressBytes(),0);
                    }
                });
            }
            else {
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                    Log.d(TAG, "onCharacteristicWrite failure " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "onCharacteristicChanged " + Bytes.hexFromBytes(characteristic.getValue()));
            scanner.manager.workHandler.post(new Runnable() {
                @Override
                public void run() {
                    //TODO add proper characteristic
                    scanner.manager.core.HMBTCoreSensingReadResponse(scanner.manager.coreInterface, characteristic.getValue(), characteristic.getValue().length, 0, getAddressBytes(),0);
                }
            });
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                scanner.manager.workHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scanner.manager.core.HMBTCoreSensingDiscoveryEvent(scanner.manager.coreInterface, getAddressBytes());
                    }
                });
            }
            else {
                // TODO: fail connection
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                    Log.d(TAG, "onDescriptorWrite failure " + status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            ScannedLink.this.rssi = rssi;

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (rssiCallback != null) rssiCallback.response(0);
            }
            else {
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                    Log.d(TAG, "read rssi failure " + status);
                if (rssiCallback != null) rssiCallback.response(1); // TODO: use correct error code
            }
        }
    };

    static long getLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }
}
