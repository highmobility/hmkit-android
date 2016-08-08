package com.high_mobility.HMLink.Shared;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.high_mobility.btcore.HMDevice;

import java.util.List;

/**
 * Created by ttiganik on 01/06/16.
 */
public class ExternalDevice extends Device {
    public enum State {
        Disconnected, Connected, Authenticated
    }

    State state;
    ExternalDeviceListener listener;
    int RSSI;

    ExternalDeviceManager manager;

    BluetoothDevice btDevice;
    HMDevice hmDevice;
    BluetoothGatt gatt;

    BluetoothGattCharacteristic readCharacteristic;
    BluetoothGattCharacteristic writeCharacteristic;

    public State getState() {
        return state;
    }

    public int getRSSI() {
        return RSSI;
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

    public void sendCommand() {
        // TODO: HMBTCoreSendCustomCommand
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
        if (listener != null) listener.onStateChanged(oldState);
    }

    void writeData(byte[] value) {
        Log.d(ExternalDeviceManager.TAG, "TODO: write data");
        if (writeCharacteristic != null) writeCharacteristic.setValue(value);
    }

    void readData() {
        gatt.readCharacteristic(readCharacteristic);
    }

    void didAuthenticate() {
        setState(State.Authenticated);
    }

    void onDeviceExitedProximity() {
        setState(State.Disconnected);
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

    // TODO: wait for notification, if there is data in read notif, then can call readResponse instantly
    // otherwise send to core that notification with data appeared, core decices if needs to call readData again

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(ExternalDeviceManager.TAG, "onConnectionStateChange status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(ExternalDeviceManager.TAG, "STATE_CONNECTED " + this);
                    manager.shared.core.HMBTCoreSensingConnect(manager.shared.coreInterface, getAddressBytes());
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(ExternalDeviceManager.TAG, "STATE_DISCONNECTED " + this);
                    manager.shared.core.HMBTCoreSensingDisconnect(manager.shared.coreInterface, getAddressBytes());
                    break;
                default:
                    Log.e(ExternalDeviceManager.TAG, "STATE_OTHER " + this);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service : services) {
                if (service.getUuid().equals(Device.SERVICE_UUID)) {
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
                manager.shared.core.HMBTCoreSensingDiscoveryEvent(manager.shared.coreInterface, getAddressBytes());
            }
            else {
                // TODO: ask how failed service discovery should be handled
//                disconnect();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
//            manager.shared.core.HMBTCoreSensingReadResponse();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(ExternalDeviceManager.TAG, new Object(){}.getClass().getEnclosingMethod().getName());
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

}
