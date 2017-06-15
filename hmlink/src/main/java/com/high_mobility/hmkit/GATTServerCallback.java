package com.high_mobility.hmkit;

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
    static final String TAG = "GATTServerCallback";
    Broadcaster broadcaster;
    GATTServerCallback(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    public void onConnectionStateChange(final BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "connecting failed with status" + status);
            // TODO: this should be handled
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue()) Log.d(TAG, "onConnectionStateChange " + newState);
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            final Broadcaster broadcaster = this.broadcaster;
            broadcaster.manager.workHandler.post(new Runnable() {
                @Override
                public void run() {
                    broadcaster.manager.core.HMBTCorelinkDisconnect(broadcaster.manager.coreInterface, ByteUtils.bytesFromMacString(device.getAddress()));
                }
            });
        }
    }

    @Override
    public void onCharacteristicReadRequest(final BluetoothDevice device,
                                            int requestId,
                                            int offset,
                                            BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

        byte[] value = characteristic.getValue();
        byte[] offsetBytes = Arrays.copyOfRange(value, offset, value.length);
        final int characteristicId = getCharacteristicIdForCharacteristic(characteristic);

        broadcaster.GATTServer.sendResponse(device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                offsetBytes);
        broadcaster.manager.workHandler.post(
                new Runnable() {
                     @Override
                     public void run() {
                         broadcaster.manager.core.HMBTCorelinkWriteResponse(broadcaster.manager.coreInterface,
                                 ByteUtils.bytesFromMacString(device.getAddress()),
                                 characteristicId);
                     }
                 });
    }

    @Override
    public void onCharacteristicWriteRequest(final BluetoothDevice device,
                                             int requestId,
                                             final BluetoothGattCharacteristic characteristic,
                                             boolean preparedWrite,
                                             boolean responseNeeded,
                                             int offset,
                                             final byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

        final int characteristicId = getCharacteristicIdForCharacteristic(characteristic);
        if (characteristicId == -1) {
            Log.e(TAG, "incoming data from invalid characteristic");
            return;
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(TAG, "incoming data " + ByteUtils.hexFromBytes(value) + " from "
                    + ByteUtils.hexFromBytes(ByteUtils.bytesFromMacString(device.getAddress())));

        if (responseNeeded) {
            broadcaster.GATTServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            final Broadcaster devicePointer = this.broadcaster;
            broadcaster.manager.workHandler.post(new Runnable() {
                @Override
                public void run() {
                    devicePointer.manager.core.HMBTCorelinkIncomingData(
                            devicePointer.manager.coreInterface,
                            value,
                            value.length,
                            ByteUtils.bytesFromMacString(device.getAddress()),
                            characteristicId);
                }
            });
        }
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);

        byte[] value = descriptor.getValue();
        byte[] offsetBytes = value == null ? new byte[] {} : Arrays.copyOfRange(value, offset, value.length);

        broadcaster.GATTServer.sendResponse(device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                offsetBytes);
    }

    @Override
    public void onDescriptorWriteRequest(final BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        if (responseNeeded) {
            broadcaster.GATTServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);

            if (descriptor.getCharacteristic().getUuid().equals(Constants.READ_CHAR_UUID)) {
                final Broadcaster broadcaster = this.broadcaster;
                broadcaster.manager.workHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        broadcaster.linkDidConnect(device);
                        broadcaster.manager.core.HMBTCorelinkConnect(broadcaster.manager.coreInterface, ByteUtils.bytesFromMacString(device.getAddress()));
                    }
                });
            }
        }
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        super.onNotificationSent(device, status);
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(TAG, "onNotificationSent " + status);
    }

    int getCharacteristicIdForCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (characteristic == broadcaster.writeCharacteristic) {
            return 0x03;
        }
        else if (characteristic == broadcaster.sensingWriteCharacteristic) {
            return 0x07;
        }
        else if (characteristic == broadcaster.readCharacteristic) {
            return 0x02;
        }
        else if (characteristic == broadcaster.sensingReadCharacteristic) {
            return 0x06;
        }
        else if (characteristic == broadcaster.aliveCharacteristic) {
            return 0x04;
        }
        else if (characteristic == broadcaster.infoCharacteristic) {
            return 0x05;
        }

        return -1;
    }
}
