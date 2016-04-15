package com.high_mobility.digitalkey.MajesticLink.Broadcasting;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.high_mobility.digitalkey.MajesticLink.Constants;
import com.high_mobility.digitalkey.Utils;

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
        Log.i(TAG, "onConnectionStateChange "
                + Utils.getStatusDescription(status) + " "
                + Utils.getStateDescription(newState));
    // TODO:
        /*
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            postDeviceChange(device, true);

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i(TAG, "remove device");
            mDevices.remove(device);
            postDeviceChange(device, false);
        }
        */
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device,
                                            int requestId,
                                            int offset,
                                            BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        Log.i(TAG, "onCharacteristicReadRequest " + characteristic.getUuid().toString());
// TODO:
        /*if (Constants.READ_CHAR_UUID.equals(characteristic.getUuid())) {
            mGattServer.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    readCharacteristic.getValue());
        }


//              Unless the characteristic supports WRITE_NO_RESPONSE,
//              always send a response back for any request.

        mGattServer.sendResponse(device,
                requestId,
                BluetoothGatt.GATT_FAILURE,
                0,
                null);
        */
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
        Log.i(TAG, "onCharacteristicWriteRequest : " + characteristic.getUuid().toString() + " v: " + Utils.hexFromBytes(value));
/*
        if (responseNeeded) {
            mGattServer.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    value);
        }
        */ // TODO:
    }

    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        Log.i(TAG, device.getAddress());
        // TODO:
        /*
        if (responseNeeded) {
            if (!mDevices.contains(device)) {
                mDevices.add(device);
            }

            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }
        */
    }

}
