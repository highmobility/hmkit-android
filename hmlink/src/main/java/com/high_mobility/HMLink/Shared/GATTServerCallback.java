package com.high_mobility.HMLink.Shared;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by ttiganik on 15/04/16.
 */
class GATTServerCallback extends BluetoothGattServerCallback {
    Broadcaster broadcaster;
    GATTServerCallback(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.All.getValue()) Log.d(Broadcaster.TAG, "onConnectionStateChange " + newState);
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            broadcaster.manager.core.HMBTCorelinkDisconnect(this.broadcaster.manager.coreInterface, ByteUtils.bytesFromMacString(device.getAddress()));
        }
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device,
                                            int requestId,
                                            int offset,
                                            BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

        if (characteristic.getUuid().equals(Constants.READ_CHAR_UUID)) {
            // response to read here
            byte[] value = characteristic.getValue();
//            Log.d(Broadcaster.TAG, "onCharacteristicReadRequest " + ByteUtils.hexFromBytes(value) + " " + offset + " " + value.length);
            byte[] offsetBytes = Arrays.copyOfRange(value, offset, value.length);

            broadcaster.GATTServer.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    offsetBytes);
            
            return;
        }

        broadcaster.GATTServer.sendResponse(device,
            requestId,
            BluetoothGatt.GATT_FAILURE,
            0,
            null);
    }

    @Override
    public void onCharacteristicWriteRequest(final BluetoothDevice device,
                                             int requestId,
                                             BluetoothGattCharacteristic characteristic,
                                             boolean preparedWrite,
                                             boolean responseNeeded,
                                             int offset,
                                             final byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.All.getValue())
            Log.d(Broadcaster.TAG, "incoming data " + ByteUtils.hexFromBytes(value) + " from "
                    + ByteUtils.hexFromBytes(ByteUtils.bytesFromMacString(device.getAddress())));

        if (responseNeeded) {
            broadcaster.GATTServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            final Broadcaster devicePointer = this.broadcaster;
            broadcaster.manager.mainThread.post(new Runnable() {
                @Override
                public void run() {
                    devicePointer.manager.core.HMBTCorelinkIncomingData(devicePointer.manager.coreInterface, value, value.length, ByteUtils.bytesFromMacString(device.getAddress()));
                }
            });
        }
    }

    @Override
    public void onDescriptorWriteRequest(final BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        if (responseNeeded) {
            broadcaster.GATTServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);

            final Broadcaster devicePointer = this.broadcaster;
            broadcaster.manager.mainThread.post(new Runnable() {
                @Override
                public void run() {
                    devicePointer.linkDidConnect(device);
                    devicePointer.manager.core.HMBTCorelinkConnect(devicePointer.manager.coreInterface, ByteUtils.bytesFromMacString(device.getAddress()));
                }
            });
        }
    }
}
