package com.high_mobility.digitalkey.HMLink.Broadcasting;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.high_mobility.digitalkey.HMLink.Constants;
import com.high_mobility.digitalkey.Utils;

import java.util.Arrays;

/**
 * Created by ttiganik on 15/04/16.
 */
public class GATTServerCallback extends BluetoothGattServerCallback {
    private static final String TAG = "GATTServerCallback";

    LocalDevice device;
    public GATTServerCallback(LocalDevice device) {
        this.device = device;
    }

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);

        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            this.device.core.HMBTCorelinkDisconnect(this.device.coreInterface,Utils.bytesFromMacString(device.getAddress()));
        }
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device,
                                            int requestId,
                                            int offset,
                                            BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

        if (Constants.READ_CHAR_UUID.equals(characteristic.getUuid())) {
            // response to read here
            byte[] value = characteristic.getValue();
            byte[] offsetBytes = Arrays.copyOfRange(value, offset, value.length);

            this.device.GATTServer.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    offsetBytes);
            
            return;
        }

        this.device.GATTServer.sendResponse(device,
            requestId,
            BluetoothGatt.GATT_FAILURE,
            0,
            null);
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device,
                                             int requestId,
                                             BluetoothGattCharacteristic characteristic,
                                             boolean preparedWrite,
                                             boolean responseNeeded,
                                             int offset,
                                             byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

        if (responseNeeded) {
            this.device.GATTServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            this.device.core.HMBTCorelinkIncomingData(this.device.coreInterface,value, value.length, Utils.bytesFromMacString(device.getAddress()));
        }
    }

    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        if (responseNeeded) {
            this.device.didReceiveLink(device);
            this.device.GATTServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            this.device.core.HMBTCorelinkConnect(this.device.coreInterface,Utils.bytesFromMacString(device.getAddress()));
        }
    }
}
