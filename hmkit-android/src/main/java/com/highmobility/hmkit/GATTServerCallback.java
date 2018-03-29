package com.highmobility.hmkit;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import com.highmobility.utils.Bytes;

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
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "connecting failed with status" + status);
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue()) Log.d(TAG, "onConnectionStateChange: " + getConnectionState(newState)+ " " + device.getAddress());

        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            final Broadcaster broadcaster = this.broadcaster;
            broadcaster.manager.workHandler.post(new Runnable() {
                @Override
                public void run() {
                    broadcaster.manager.core.HMBTCorelinkDisconnect(broadcaster.manager.coreInterface, Bytes.bytesFromMacString(device.getAddress()));
                }
            });
        }
    }

    @Override
    public void onCharacteristicReadRequest(final BluetoothDevice device,
                                            final int requestId,
                                            final int offset,
                                            final BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue();
        byte[] offsetBytes = Arrays.copyOfRange(value, offset, value.length);
        final int characteristicId = getCharacteristicIdForCharacteristic(characteristic);
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(TAG, "onCharacteristicReadRequest " + characteristicId + ": "
                + Bytes.hexFromBytes(offsetBytes));
        boolean result = broadcaster.GATTServer.sendResponse(device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                offsetBytes);

        if (result == false) {
            Log.e(TAG, "onCharacteristicReadRequest: failed to send response");
        }

        broadcaster.manager.workHandler.postDelayed(
            new Runnable() {
                @Override
                public void run() {
                    broadcaster.manager.core.HMBTCorelinkWriteResponse(broadcaster.manager.coreInterface,
                            Bytes.bytesFromMacString(device.getAddress()),
                            characteristicId);
                }
            }, 1);
    }

    @Override
    public void onCharacteristicWriteRequest(final BluetoothDevice device,
                                             int requestId,
                                             final BluetoothGattCharacteristic characteristic,
                                             boolean preparedWrite,
                                             boolean responseNeeded,
                                             int offset,
                                             final byte[] value) {
        final int characteristicId = getCharacteristicIdForCharacteristic(characteristic);
        if (characteristicId == -1) {
            Log.e(TAG, "incoming data from invalid characteristic");
            return;
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(TAG, "incoming data " + characteristicId + ": " + Bytes.hexFromBytes(value) + " from "
                    + Bytes.hexFromBytes(Bytes.bytesFromMacString(device.getAddress())));

        if (responseNeeded) {
            boolean result = broadcaster.GATTServer.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    null);

            if (result == false) {
                Log.e(TAG, "onCharacteristicWriteRequest: failed to send response");
            }

            final Broadcaster devicePointer = this.broadcaster;
            broadcaster.manager.workHandler.post(new Runnable() {
                @Override
                public void run() {
                    devicePointer.manager.core.HMBTCorelinkIncomingData(
                            devicePointer.manager.coreInterface,
                            value,
                            value.length,
                            Bytes.bytesFromMacString(device.getAddress()),
                            characteristicId);
                }
            });
        }
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        byte[] value = descriptor.getValue();
        byte[] offsetBytes = value == null ? new byte[] {} : Arrays.copyOfRange(value, offset, value.length);

        boolean result = broadcaster.GATTServer.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                offsetBytes);

        if (result == false) {
            Log.e(TAG, "onDescriptorReadRequest: failed to send response");
        }
    }

    @Override
    public void onDescriptorWriteRequest(final BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        if (responseNeeded) {
            boolean result = broadcaster.GATTServer.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    value);

            if (result == false) {
                Log.e(TAG, "onDescriptorWriteRequest: failed to send response");
            }

            if (descriptor.getCharacteristic().getUuid().equals(Constants.READ_CHAR_UUID)) {
                // if notifications don't start, try restarting bluetooth on android / other device
                final Broadcaster broadcaster = this.broadcaster;

                if (broadcaster.getLinkForMac(Bytes.bytesFromMacString(device.getAddress())) != null) return;

                broadcaster.manager.workHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        broadcaster.linkDidConnect(device);
                        broadcaster.manager.core.HMBTCorelinkConnect(broadcaster.manager.coreInterface, Bytes.bytesFromMacString(device.getAddress()));
                    }
                });
            }
        }
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue()) {
            Log.d(TAG, "onNotificationSent: " + (status == BluetoothGatt.GATT_SUCCESS ? "success" : "failed"));
        }
    }

    int getCharacteristicIdForCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(broadcaster.writeCharacteristic.getUuid())) {
            return 0x03;
        }
        else if (characteristic.getUuid().equals(broadcaster.sensingWriteCharacteristic.getUuid())) {
            return 0x07;
        }
        else if (characteristic.getUuid().equals(broadcaster.readCharacteristic.getUuid())) {
            return 0x02;
        }
        else if (characteristic.getUuid().equals(broadcaster.sensingReadCharacteristic.getUuid())) {
            return 0x06;
        }
        else if (characteristic.getUuid().equals(broadcaster.aliveCharacteristic.getUuid())) {
            return 0x04;
        }
        else if (characteristic.getUuid().equals(broadcaster.infoCharacteristic.getUuid())) {
            return 0x05;
        }

        return -1;
    }

    private String getConnectionState(int state) {
        switch (state) {
            case BluetoothProfile.STATE_DISCONNECTED: return "STATE_DISCONNECTED";
            case BluetoothProfile.STATE_CONNECTING: return "STATE_CONNECTING";
            case BluetoothProfile.STATE_CONNECTED: return "STATE_CONNECTED";
            case BluetoothProfile.STATE_DISCONNECTING: return "STATE_DISCONNECTING";
        }

        return "Unknown";
    }
}
