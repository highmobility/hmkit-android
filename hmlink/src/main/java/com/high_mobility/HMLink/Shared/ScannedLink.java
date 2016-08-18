package com.high_mobility.HMLink.Shared;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.high_mobility.HMLink.LinkException;
import com.high_mobility.btcore.HMDevice;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Created by ttiganik on 01/06/16.
 */
public class ScannedLink extends Link {
    private static final String TAG = "ScannedLink";

    Scanner scanner;

    BluetoothGatt gatt;

    BluetoothGattCharacteristic readCharacteristic;
    BluetoothGattCharacteristic writeCharacteristic;

    Constants.RSSICallback rssiCallback;

    public void readRSSI(Constants.RSSICallback rssiCallback) {
        this.rssiCallback = rssiCallback;
        gatt.readRemoteRssi();
    }

    public void setListener(LinkListener listener) {
        this.listener = listener;
    }

    public void registerCertificate() {
        // TODO:
    }

    public void storeCertificate() {
        // TODO:
    }

    public void getAccessCertificate() {
        // TODO:
    }

    public void revokeCertificate() {
        // TODO:
    }

    public void sendCommand(byte[] bytes, Constants.DataResponseCallback responseCallback) {
        if (state != State.AUTHENTICATED) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.All.getValue())
                Log.d(Broadcaster.TAG, "cant send command, not authenticated");
            responseCallback.response(null, new LinkException(LinkException.LinkExceptionCode.UNAUTHORISED));
            return;
        }

        if (sentCommand != null && sentCommand.finished == false) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.All.getValue())
                Log.d(Broadcaster.TAG, "cant send command, custom command in progress");
            responseCallback.response(null, new LinkException(LinkException.LinkExceptionCode.CUSTOM_COMMAND_IN_PROGRESS));
            return;
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.Debug.getValue())
            Log.d(Broadcaster.TAG, "send command " + ByteUtils.hexFromBytes(bytes)
                    + " to " + ByteUtils.hexFromBytes(hmDevice.getMac()));

        sentCommand = new SentCommand(responseCallback, scanner.manager.mainThread);
        scanner.manager.core.HMBTCoreSendCustomCommand(scanner.manager.coreInterface, bytes, bytes.length, getAddressBytes());
    }

    public void reset() {
        // TODO:
    }

    ScannedLink(Scanner scanner, BluetoothDevice btDevice) {
        super(scanner.manager, btDevice);
        this.scanner = scanner;
        this.btDevice = btDevice;
    }

    void writeValue(byte[] value) {
        if (writeCharacteristic != null){
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.Debug.getValue())
                Log.d(Scanner.TAG, "write value " + ByteUtils.hexFromBytes(value));

            writeCharacteristic.setValue(value);
            gatt.writeCharacteristic(writeCharacteristic);
        }
    }

    void readValue() {
        gatt.readCharacteristic(readCharacteristic);
    }

    void didAuthenticate() {
        setState(State.AUTHENTICATED);
    }

    void onDeviceExitedProximity() {
        setState(State.DISCONNECTED);
    }

    void connect() {
        Log.d(Scanner.TAG, "connect " + btDevice.getAddress() + " " + this);
        gatt = btDevice.connectGatt(scanner.manager.ctx, false, gattCallback);
    }

    void disconnect() {
        Log.d(Scanner.TAG, "disconnect " + btDevice.getAddress() + " " + this);
        if (gatt != null) gatt.disconnect();
    }

    void discoverServices() {
        gatt.discoverServices();
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.Debug.getValue())
                        Log.d(Scanner.TAG, "STATE_CONNECTED " + this);
                    scanner.manager.mainThread.post(new Runnable() {
                        @Override
                        public void run() {
                        scanner.manager.core.HMBTCoreSensingConnect(scanner.manager.coreInterface, getAddressBytes());
                        }
                    });
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.Debug.getValue())
                        Log.d(Scanner.TAG, "STATE_DISCONNECTED " + this);

                    scanner.manager.mainThread.post(new Runnable() {
                        @Override
                        public void run() {
                        scanner.manager.core.HMBTCoreSensingDisconnect(scanner.manager.coreInterface, getAddressBytes());
                        }
                    });
                    break;
                default:
                    Log.e(Scanner.TAG, "INVALID CONNECTION STATE " + this);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                // service UUID is reversed
                UUID uuid = service.getUuid();

                byte[] msb = ByteUtils.longToBytes(uuid.getMostSignificantBits());
                byte[] lsb = ByteUtils.longToBytes(uuid.getLeastSignificantBits());

                ByteUtils.reverse(msb);
                ByteUtils.reverse(lsb);
                UUID reverseUUID = new UUID(ByteUtils.bytesToLong(lsb), ByteUtils.bytesToLong(msb));

                if (reverseUUID.equals(Constants.SERVICE_UUID)) {
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        if (characteristic.getUuid().equals(Constants.READ_CHAR_UUID)) {
                            readCharacteristic = characteristic;
                        }
                        else if (characteristic.getUuid().equals(Constants.WRITE_CHAR_UUID)) {
                            writeCharacteristic = characteristic;
                        }
                    }
                    break;
                }
            }

            Log.i(Scanner.TAG, "onServicesDiscovered " + (readCharacteristic != null && writeCharacteristic != null));

            if (readCharacteristic != null && writeCharacteristic != null) {
                gatt.setCharacteristicNotification(readCharacteristic, true);
                BluetoothGattDescriptor descriptor = readCharacteristic.getDescriptor(Constants.NOTIFY_DESC_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
            else {
                // TODO: how failed service discovery/connection should be handled
                // also remove authenticatingMac from scanner
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic, int status) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.Debug.getValue())
                Log.d(Scanner.TAG, "onCharacteristicRead " + ByteUtils.hexFromBytes(characteristic.getValue()));
            scanner.manager.mainThread.post(new Runnable() {
                @Override
                public void run() {
            scanner.manager.core.HMBTCoreSensingReadResponse(scanner.manager.coreInterface, characteristic.getValue(), characteristic.getValue().length, 0, getAddressBytes());

                }
            });
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            scanner.manager.mainThread.post(new Runnable() {
                @Override
                public void run() {
                scanner.manager.core.HMBTCoreSensingWriteResponse(scanner.manager.coreInterface, getAddressBytes());
                }
            });
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.Debug.getValue())
                Log.d(Scanner.TAG, "onCharacteristicChanged " + ByteUtils.hexFromBytes(characteristic.getValue()));
            scanner.manager.mainThread.post(new Runnable() {
                @Override
                public void run() {
                    scanner.manager.core.HMBTCoreSensingReadResponse(scanner.manager.coreInterface, characteristic.getValue(), characteristic.getValue().length, 0, getAddressBytes());

                }
            });
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            scanner.manager.mainThread.post(new Runnable() {
                @Override
                public void run() {
                    scanner.manager.core.HMBTCoreSensingDiscoveryEvent(scanner.manager.coreInterface, getAddressBytes());
                }
            });
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (rssiCallback != null) rssiCallback.onRSSIRead(rssi);
        }
    };
}
