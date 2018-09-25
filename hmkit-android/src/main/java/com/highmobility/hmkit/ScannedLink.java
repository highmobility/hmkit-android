package com.highmobility.hmkit;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import com.highmobility.crypto.AccessCertificate;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.utils.ByteUtils;

import java.util.List;
import java.util.UUID;

/**
 * The ScannedLink is a representation of the connection between the Scanner and a device that the
 * Scanner has connected to and validated as a High-Mobility broadcaster.
 * <p>
 * The ScannedLinks's interface provides the ability to send commands and handle incoming requests
 * from the ConnectedLink.
 * <p>
 */
class ScannedLink extends Link {
    private final SharedBle ble;

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
        return "";
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
        // TSODO:
    }

    void storeCertificate(AccessCertificate certificate, Constants.ResponseCallback callback) {
        // TSODO:
    }

    void getAccessCertificate(AccessCertificate certificate, Constants.ResponseCallback callback) {
        // TSODO:
    }

    void revokeCertificate(DeviceSerial serial, Constants.ResponseCallback callback) {
        // TSODO:
    }

    void reset(Constants.ResponseCallback callback) {
        // TSODO:
    }

    ScannedLink(SharedBle ble, Core core, ThreadManager threadManager, BluetoothDevice btDevice) {
        super(core, threadManager, btDevice);
        this.btDevice = btDevice;
        this.ble = ble;
    }

    void writeValue(byte[] value) {
        if (writeCharacteristic != null) {
            if (HmKit.loggingLevel.getValue() >= HmKit.LoggingLevel.DEBUG.getValue())
                HmLog.d("write value " + ByteUtils.hexFromBytes(value));

            if (writeCharacteristic.setValue(value) == false || gatt.writeCharacteristic
                    (writeCharacteristic) == false) {
                // TSODO: fail auth or command
            }
        }
    }

    void readValue() {
        if (gatt.readCharacteristic(readCharacteristic) == false) {
            // TSODO: fail auth or command
        }
    }

    void onDeviceExitedProximity() {
        setState(State.DISCONNECTED);
    }

    void connect() {
        HmLog.d("connect %s %s", btDevice.getAddress(), this);
        gatt = btDevice.connectGatt(ble.context, false, gattCallback);
    }

    void disconnect() {
        HmLog.d("disconnect " + btDevice.getAddress() + " " + this);
        if (gatt != null) gatt.disconnect();
    }

    void discoverServices() {
        if (gatt.discoverServices() == false) {
            // TSODO: how to failed connection should be handled
            // also remove authenticatingMac from scanner

            if (HmKit.loggingLevel.getValue() >= HmKit.LoggingLevel.DEBUG.getValue())
                HmLog.d("cannot initiate discoverServices");
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // TSODO: handle status.. 
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:

                    HmLog.d("STATE_CONNECTED " + this);
                    threadManager.postToWork(new Runnable() {
                        @Override
                        public void run() {
                            core.HMBTCoreSensingConnect(getAddressBytes());
                        }
                    });
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:

                    HmLog.d("STATE_DISCONNECTED " + this);

                    threadManager.postToWork(new Runnable() {
                        @Override
                        public void run() {
                            core.HMBTCoreSensingDisconnect(getAddressBytes());
                        }
                    });
                    break;
                default:
                    HmLog.e("INVALID CONNECTION STATE " + this);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                // TSODO: how failed service discovery/connection should be handled
                // also remove authenticatingMac from scanner

                HmLog.d("onServicesDiscovered failure " + status);
                return;
            }

            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                // service UUID is reversed
                UUID uuid = service.getUuid();

                byte[] msb = longToBytes(uuid.getMostSignificantBits());
                byte[] lsb = longToBytes(uuid.getLeastSignificantBits());

                ByteUtils.reverse(msb);
                ByteUtils.reverse(lsb);
                UUID reverseUUID = new UUID(getLong(lsb), getLong(msb));

                if (reverseUUID.equals(Constants.SERVICE_UUID)) {
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics
                            ()) {
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

            HmLog.d("onServicesDiscovered " + (readCharacteristic != null &&
                    writeCharacteristic != null));

            if (readCharacteristic != null && writeCharacteristic != null) {
                if (gatt.setCharacteristicNotification(readCharacteristic, true) == false) {
                    // TSODO: how to failed connection should be handled
                    // also remove authenticatingMac from scanner

                    HmLog.d("cannot initiate " +
                            "setCharacteristicNotification");
                    return;
                }

                BluetoothGattDescriptor descriptor = readCharacteristic.getDescriptor(Constants
                        .NOTIFY_DESCRIPTOR_UUID);
                if (descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) ==
                        false) {
                    // TSODO: how to failed connection should be handled
                    // also remove authenticatingMac from scanner

                    HmLog.d("cannot initiate descriptor.setValue");
                    return;
                }

                if (gatt.writeDescriptor(descriptor) == false) {
                    // TSODO: how to failed connection should be handled
                    // also remove authenticatingMac from scanner

                    HmLog.d("cannot initiate writeDescriptor");
                }
                // TSODO: What to do with ping here?
            } else {
                // TSODO: how failed service discovery/connection should be handled
                // also remove authenticatingMac from scanner
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic, int
                                                 status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                HmLog.d("onCharacteristicRead " + ByteUtils
                        .hexFromBytes(characteristic
                                .getValue()));
                if (characteristic.getUuid().equals(Constants.READ_CHAR_UUID)) {
                    threadManager.postToWork(new Runnable() {
                        @Override
                        public void run() {
                            //TSODO add proper characteristic
                            core.HMBTCoreSensingReadResponse(characteristic.getValue(),
                                    characteristic
                                            .getValue().length, 0, getAddressBytes(), 0);
                        }
                    });
                } else if (characteristic.getUuid().equals(Constants.INFO_CHAR_UUID)) {
                    info = characteristic.getStringValue(0);
                    HmLog.d("read info " + info);
                }
            } else {
                HmLog.d("onCharacteristicRead failed " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                threadManager.postToWork(new Runnable() {
                    @Override
                    public void run() {
                        //TSODO add proper characteristic
                        core.HMBTCoreSensingWriteResponse(getAddressBytes(), 0);
                    }
                });
            } else {
                HmLog.d("onCharacteristicWrite failure " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic
                characteristic) {
            HmLog.d("onCharacteristicChanged " + ByteUtils
                    .hexFromBytes(characteristic
                            .getValue()));
            threadManager.postToWork(new Runnable() {
                @Override
                public void run() {
                    //TSODO add proper characteristic
                    core.HMBTCoreSensingReadResponse(characteristic.getValue(), characteristic
                            .getValue()
                            .length, 0, getAddressBytes(), 0);
                }
            });
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int
                status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                threadManager.postToWork(new Runnable() {
                    @Override
                    public void run() {
                        core.HMBTCoreSensingDiscoveryEvent(getAddressBytes());
                    }
                });
            } else {
                // TSODO: fail connection
                HmLog.d("onDescriptorWrite failure " + status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            ScannedLink.this.rssi = rssi;

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (rssiCallback != null) rssiCallback.response(0);
            } else {
                HmLog.d("read rssi failure " + status);
                if (rssiCallback != null) rssiCallback.response(1); // TSODO: use correct error code
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
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }
}
