package com.high_mobility.HMLink.Shared;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;

import com.high_mobility.HMLink.Constants;
import com.high_mobility.HMLink.LinkException;
import com.high_mobility.btcore.HMDevice;

import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by ttiganik on 01/06/16.
 */
public class ExternalDevice extends Device {
    private static final String TAG = "ExternalDevice";
    public enum State {
        DISCONNECTED, CONNECTED, AUTHENTICATED
    }

    State state;
    ExternalDeviceListener listener;

    ExternalDeviceManager manager;

    BluetoothDevice btDevice;
    HMDevice hmDevice;
    BluetoothGatt gatt;

    BluetoothGattCharacteristic readCharacteristic;
    BluetoothGattCharacteristic writeCharacteristic;

    SentCommand sentCommand;
    Constants.RSSICallback rssiCallback;

    public State getState() {
        return state;
    }

    public void readRSSI(Constants.RSSICallback rssiCallback) {
        this.rssiCallback = rssiCallback;
        gatt.readRemoteRssi();
    }

    public void setListener(ExternalDeviceListener listener) {
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
            if (Device.loggingLevel.getValue() >= Device.LoggingLevel.All.getValue())
                Log.d(LocalDevice.TAG, "cant send command, not authenticated");
            responseCallback.response(null, new LinkException(LinkException.LinkExceptionCode.UNAUTHORISED));
            return;
        }

        if (sentCommand != null && sentCommand.finished == false) {
            if (Device.loggingLevel.getValue() >= Device.LoggingLevel.All.getValue())
                Log.d(LocalDevice.TAG, "cant send command, custom command in progress");
            responseCallback.response(null, new LinkException(LinkException.LinkExceptionCode.CUSTOM_COMMAND_IN_PROGRESS));
            return;
        }

        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
            Log.d(LocalDevice.TAG, "send command " + ByteUtils.hexFromBytes(bytes)
                    + " to " + ByteUtils.hexFromBytes(hmDevice.getMac()));

        sentCommand = new SentCommand(responseCallback, manager.shared.mainThread);
        manager.shared.core.HMBTCoreSendCustomCommand(manager.shared.coreInterface, bytes, bytes.length, getAddressBytes());
    }

    public void reset() {
        // TODO:
    }

    ExternalDevice(ExternalDeviceManager manager, BluetoothDevice btDevice) {
        this.manager = manager;
        this.btDevice = btDevice;
    }

    @Override
    public String getName() {
        return btDevice.getName();
    }

    void setState(State state) {
        State oldState = state;
        this.state = state;
        if (listener != null) listener.onStateChanged(oldState);
    }

    void writeValue(byte[] value) {
        if (writeCharacteristic != null){
            if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
                Log.d(ExternalDeviceManager.TAG, "write value " + ByteUtils.hexFromBytes(value));

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
        Log.d(ExternalDeviceManager.TAG, "connect " + btDevice.getAddress() + " " + this);
        gatt = btDevice.connectGatt(manager.shared.ctx, false, gattCallback);
    }

    void disconnect() {
        Log.d(ExternalDeviceManager.TAG, "disconnect " + btDevice.getAddress() + " " + this);
        if (gatt != null) gatt.disconnect();
    }

    void discoverServices() {
        gatt.discoverServices();
    }

    byte[] getAddressBytes() {
        return ByteUtils.bytesFromMacString(btDevice.getAddress());
    }


    byte[] onCommandReceived(byte[] bytes) {
        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
            Log.d(LocalDevice.TAG, "did receive command " + ByteUtils.hexFromBytes(bytes)
                    + " from " + btDevice.getAddress());

        if (listener == null) {
            Log.d(LocalDevice.TAG, "can't dispatch notification: no listener set");
            return null;
        }

        return listener.onCommandReceived(bytes);
    }

    public void onCommandResponseReceived(byte[] data) {
        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
            Log.d(LocalDevice.TAG, "did receive command response " + ByteUtils.hexFromBytes(data)
                    + " from " + btDevice.getAddress() + " in " +
                    (Calendar.getInstance().getTimeInMillis() - sentCommand.commandStartTime) + "ms");

        if (sentCommand == null) {
            if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
                Log.d(LocalDevice.TAG, "can't dispatch command response: sentCommand = null");
            return;
        }

        sentCommand.dispatchResult(data, null);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    if (Device.loggingLevel.getValue() >= LoggingLevel.Debug.getValue())
                        Log.d(ExternalDeviceManager.TAG, "STATE_CONNECTED " + this);
                    manager.shared.mainThread.post(new Runnable() {
                        @Override
                        public void run() {
                        manager.shared.core.HMBTCoreSensingConnect(manager.shared.coreInterface, getAddressBytes());
                        }
                    });
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    if (Device.loggingLevel.getValue() >= LoggingLevel.Debug.getValue())
                        Log.d(ExternalDeviceManager.TAG, "STATE_DISCONNECTED " + this);

                    manager.shared.mainThread.post(new Runnable() {
                        @Override
                        public void run() {
                        manager.shared.core.HMBTCoreSensingDisconnect(manager.shared.coreInterface, getAddressBytes());
                        }
                    });
                    break;
                default:
                    Log.e(ExternalDeviceManager.TAG, "INVALID CONNECTION STATE " + this);
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

                if (reverseUUID.equals(Device.SERVICE_UUID)) {
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        if (characteristic.getUuid().equals(Device.READ_CHAR_UUID)) {
                            readCharacteristic = characteristic;
                        }
                        else if (characteristic.getUuid().equals(Device.WRITE_CHAR_UUID)) {
                            writeCharacteristic = characteristic;
                        }
                    }
                    break;
                }
            }

            Log.i(ExternalDeviceManager.TAG, "onServicesDiscovered " + (readCharacteristic != null && writeCharacteristic != null));

            if (readCharacteristic != null && writeCharacteristic != null) {
                gatt.setCharacteristicNotification(readCharacteristic, true);
                BluetoothGattDescriptor descriptor = readCharacteristic.getDescriptor(NOTIFY_DESC_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
            else {
                // TODO: how failed service discovery/connection should be handled
                // also remove authenticatingMac from manager
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic, int status) {
            if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
                Log.d(ExternalDeviceManager.TAG, "onCharacteristicRead " + ByteUtils.hexFromBytes(characteristic.getValue()));
            manager.shared.mainThread.post(new Runnable() {
                @Override
                public void run() {
            manager.shared.core.HMBTCoreSensingReadResponse(manager.shared.coreInterface, characteristic.getValue(), characteristic.getValue().length, 0, getAddressBytes());

                }
            });
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            manager.shared.mainThread.post(new Runnable() {
                @Override
                public void run() {
            manager.shared.core.HMBTCoreSensingWriteResponse(manager.shared.coreInterface, getAddressBytes());

                }
            });
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
                Log.d(ExternalDeviceManager.TAG, "onCharacteristicChanged " + ByteUtils.hexFromBytes(characteristic.getValue()));
            manager.shared.mainThread.post(new Runnable() {
                @Override
                public void run() {
                    manager.shared.core.HMBTCoreSensingReadResponse(manager.shared.coreInterface, characteristic.getValue(), characteristic.getValue().length, 0, getAddressBytes());

                }
            });
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            manager.shared.mainThread.post(new Runnable() {
                @Override
                public void run() {
                    manager.shared.core.HMBTCoreSensingDiscoveryEvent(manager.shared.coreInterface, getAddressBytes());
                }
            });
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (rssiCallback != null) rssiCallback.onRSSIRead(rssi);
        }
    };
}
