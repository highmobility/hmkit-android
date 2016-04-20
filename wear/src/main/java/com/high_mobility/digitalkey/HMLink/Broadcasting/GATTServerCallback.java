package com.high_mobility.digitalkey.HMLink.Broadcasting;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.high_mobility.digitalkey.HMLink.Constants;
import com.high_mobility.digitalkey.HMLink.LinkException;
import com.high_mobility.digitalkey.Utils;

import java.util.Arrays;

/**
 * Created by ttiganik on 15/04/16.
 */
// TODO: test if main thread is necessary
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
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            loseLink(device);
        }
    }

    @Override
    public void onCharacteristicReadRequest(final BluetoothDevice device,
                                            final int requestId,
                                            final int offset,
                                            final BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        Log.i(TAG, "onCharacteristicReadRequest " + characteristic.getUuid().toString());


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
    public void onCharacteristicWriteRequest(final BluetoothDevice device,
                                             final int requestId,
                                             BluetoothGattCharacteristic characteristic,
                                             boolean preparedWrite,
                                             boolean responseNeeded,
                                             final int offset,
                                             final byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        Log.i(TAG, "onCharacteristicWriteRequest : " + device.getAddress() + " v: " + Utils.hexFromBytes(value));

        if (responseNeeded) {
            final LocalDevice devicePointer = this.device;
            devicePointer.GATTServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            Log.i(TAG, "sent response");

            byte[] testResponseBytes = Utils.bytesFromHex("***REMOVED***");
            devicePointer.readCharacteristic.setValue(testResponseBytes);

            devicePointer.GATTServer.notifyCharacteristicChanged(device, devicePointer.readCharacteristic, false);
            Log.i(TAG, "sent data");
        }
    }

    @Override
    public void onDescriptorWriteRequest(final BluetoothDevice device, final int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, final int offset, final byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        Log.i(TAG, "onDescriptorWriteRequest: " + device.getAddress());

        if (responseNeeded) {
            if (LocalDevice.ALLOWS_MULTIPLE_LINKS == false) {
                this.device.stopBroadcasting();
            }

            // add a new link to the array
            final Link link = new Link(device, this.device);
            Link[] newLinks = new Link[this.device.links.length + 1];

            for (int i = 0; i < this.device.links.length; i++) {
                newLinks[i] = this.device.links[i];
            }

            newLinks[this.device.links.length] = link;
            this.device.links = newLinks;

            this.device.GATTServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);

            final LocalDevice devicePointer = this.device;
            devicePointer.mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (devicePointer.callback != null) {
                        devicePointer .callback.localDeviceDidReceiveLink(link);
                    }
                }
            });
        }
    }

    private void loseLink(final BluetoothDevice device) {
        Log.i(TAG, "lose link");

        int linkIndex = linkIndexForBTDevice(device);
        if (linkIndex > -1) {
            final Link link = this.device.links[linkIndex];
            // remove the link from the array
            Link[] newLinks = new Link[this.device.links.length - 1];

            for (int i = 0; i < this.device.links.length; i++) {
                if (i < linkIndex) {
                    newLinks[i] = this.device.links[i];
                }
                else if (i > linkIndex) {
                    newLinks[i - 1] = this.device.links[i];
                }
            }

            this.device.links = newLinks;

            // set new adapter name
            if (LocalDevice.ALLOWS_MULTIPLE_LINKS == false && this.device.getLinks() == null) {
                this.device.setAdapterName();
            }

            // invoke the listener callback
            if (this.device.callback != null) {
                final LocalDevice devicePointer = this.device;
                devicePointer.mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        devicePointer.callback.localDeviceDidLoseLink(link);
                    }
                });
            }

            if (this.device.state != LocalDevice.State.BROADCASTING) {
                try {
                    this.device.startBroadcasting();
                } catch (LinkException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            Log.e(TAG, "Internal lose link error");
        }
    }

    private int linkIndexForBTDevice(BluetoothDevice device) {
        for (int i = 0; i < this.device.links.length; i++) {
            Link link = this.device.links[i];

            if (link.btDevice.getAddress().equals(device.getAddress())) {
                return i;
            }
        }

        return -1;
    }
}
