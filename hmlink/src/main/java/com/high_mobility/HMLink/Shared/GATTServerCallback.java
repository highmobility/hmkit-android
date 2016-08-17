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
    LocalDevice device;
    GATTServerCallback(LocalDevice device) {
        this.device = device;
    }

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);
        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.All.getValue()) Log.d(LocalDevice.TAG, "onConnectionStateChange " + newState);
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            this.device.shared.core.HMBTCorelinkDisconnect(this.device.shared.coreInterface, ByteUtils.bytesFromMacString(device.getAddress()));
        }
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device,
                                            int requestId,
                                            int offset,
                                            BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

        if (this.device.isReadCharacteristic(characteristic.getUuid())) {
            // response to read here
            byte[] value = characteristic.getValue();
//            Log.d(LocalDevice.TAG, "onCharacteristicReadRequest " + ByteUtils.hexFromBytes(value) + " " + offset + " " + value.length);
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
    public void onCharacteristicWriteRequest(final BluetoothDevice device,
                                             int requestId,
                                             BluetoothGattCharacteristic characteristic,
                                             boolean preparedWrite,
                                             boolean responseNeeded,
                                             int offset,
                                             final byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.All.getValue())
            Log.d(LocalDevice.TAG, "incoming data " + ByteUtils.hexFromBytes(value) + " from "
                    + ByteUtils.hexFromBytes(ByteUtils.bytesFromMacString(device.getAddress())));

        if (responseNeeded) {
            this.device.GATTServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            final LocalDevice devicePointer = this.device;
            this.device.shared.mainThread.post(new Runnable() {
                @Override
                public void run() {
                    devicePointer.shared.core.HMBTCorelinkIncomingData(devicePointer.shared.coreInterface, value, value.length, ByteUtils.bytesFromMacString(device.getAddress()));
                }
            });
        }
    }

    @Override
    public void onDescriptorWriteRequest(final BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        if (responseNeeded) {
            this.device.didReceiveLink(device);
            this.device.GATTServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);

            final LocalDevice devicePointer = this.device;
            this.device.shared.mainThread.post(new Runnable() {
                @Override
                public void run() {
                    devicePointer.shared.core.HMBTCorelinkConnect(devicePointer.shared.coreInterface, ByteUtils.bytesFromMacString(device.getAddress()));
                }
            });
        }
    }
}
