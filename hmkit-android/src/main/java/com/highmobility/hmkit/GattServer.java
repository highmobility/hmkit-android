package com.highmobility.hmkit;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import com.highmobility.btcore.HMBTCoreInterface;
import com.highmobility.utils.ByteUtils;

import java.util.Arrays;
import java.util.List;

import static com.highmobility.hmkit.HMLog.d;
import static com.highmobility.hmkit.HMLog.e;

/**
 * This is the Broadcaster's GATT server.
 */
class GattServer extends BluetoothGattServerCallback {
    private final Core core;
    private final ThreadManager threadManager;
    private final SharedBle ble;
    private final Callback broadcaster;

    private BluetoothGattServer gattServer;

    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic aliveCharacteristic;
    private BluetoothGattCharacteristic infoCharacteristic;
    private BluetoothGattCharacteristic sensingReadCharacteristic;
    private BluetoothGattCharacteristic sensingWriteCharacteristic;

    GattServer(Core core, ThreadManager threadManager, SharedBle ble, Callback broadcaster) {
        this.core = core;
        this.threadManager = threadManager;
        this.ble = ble;
        this.broadcaster = broadcaster;
    }

    boolean isOpen() {
        return gattServer != null && gattServer.getServices().size() > 0;
    }

    void open() {
        if (isOpen()) {
            d("gatt service already exists");
            broadcaster.onServiceAdded(true);
            return;
        }

        if (gattServer == null) {
            // first server open
            gattServer = ble.openGattServer(this);

            if (gattServer == null) {
                e("Cannot create gatt server");
                onServiceAdded(BluetoothGatt.GATT_FAILURE, null);
                return;
            }
        }

        d("createGattServer()");
        BluetoothGattService service = createGattService();

        if (service == null) {
            onServiceAdded(BluetoothGatt.GATT_FAILURE, null);
        } else if (gattServer.addService(service) == false) {
            e("Cannot add service to GATT server");
            onServiceAdded(BluetoothGatt.GATT_FAILURE, null);
        }
        // now gatt server started adding the service and will call onServiceAdded.
    }

    void close() {
        if (gattServer == null) return;
        gattServer.clearServices();
        gattServer.close();
        gattServer = null;
    }

    void disconnectAllLinks() {
        if (gattServer == null) return;
        List<BluetoothDevice> devices = ble.getConnectedDevices();

        for (BluetoothDevice device : devices) {
            // just to make sure all of the devices are tried to be disconnected. disconnect
            // callback should find the one in this.links if it exists.
            gattServer.cancelConnection(device);
        }

        // cant close service here, we wont get disconnect callback
    }

    boolean writeData(BluetoothDevice device, byte[] value, int characteristicId) {
        /*if (value[1] == 0x01 && value[2] == 0x35) {
            // fail auth with invalid answer
            d("writeData(): %s", "set random byte in data for error testing");
            value[value.length-10] = (byte) 0xAB;
            value[value.length-11] = (byte) 0xAB;
        }*/

        // fail revoke with invalid answer
        Link link = HMKit.getInstance().getBroadcaster().getLinks().get(0);
        if (link.getState() == Link.State.REVOKING) {
            d("writeData(): %s", "set random byte in data for error testing");
            value[2] = (byte) 0xAB;
            value[3] = (byte) 0xAB;
        }

        d("write %s to %s, char: %s", ByteUtils.hexFromBytes(value),
                device.getAddress().replaceAll(":", ""), characteristicId);

        BluetoothGattCharacteristic characteristic = getCharacteristicForId(characteristicId);
        if (characteristic == null) {
            e("no characteristic for write");
            return false;
        }

        if (characteristic.setValue(value) == false) {
            e("can't set read char value");
            return false;
        }

        if (notifyCharacteristicChanged(device, characteristic) == false) {
            e("can't notify characteristic changed");
            return false;
        }

        return true;
    }

    void sendAlivePing(BluetoothDevice btDevice) {
        notifyCharacteristicChanged(btDevice, aliveCharacteristic);
    }

    private boolean notifyCharacteristicChanged(BluetoothDevice device,
                                                BluetoothGattCharacteristic characteristic) {
        // could be that device is disconnected when this is called and android will crash then.
        // catch the exception.
        try {
            return gattServer.notifyCharacteristicChanged(device, characteristic, false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return true if created the server with the service and characteristics.
     */
    private BluetoothGattService createGattService() {
        // create the service
        BluetoothGattService service = new BluetoothGattService(Constants.SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // add characteristics to the service
        readCharacteristic = new BluetoothGattCharacteristic(Constants.READ_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic
                        .PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);

        sensingReadCharacteristic = new BluetoothGattCharacteristic(Constants
                .SENSING_READ_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic
                        .PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);

        writeCharacteristic =
                new BluetoothGattCharacteristic(Constants.WRITE_CHAR_UUID,
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        sensingWriteCharacteristic = new BluetoothGattCharacteristic(Constants
                .SENSING_WRITE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        aliveCharacteristic = new BluetoothGattCharacteristic(Constants.ALIVE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);

        infoCharacteristic = new BluetoothGattCharacteristic(Constants.INFO_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        if (readCharacteristic.addDescriptor(new BluetoothGattDescriptor(Constants
                .NOTIFY_DESCRIPTOR_UUID,
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor
                        .PERMISSION_READ)) == false) {
            e("Cannot add read descriptor");
            return null;
        }

        if (sensingReadCharacteristic.addDescriptor(new BluetoothGattDescriptor(Constants
                .NOTIFY_DESCRIPTOR_UUID,
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor
                        .PERMISSION_READ)) == false) {
            e("Cannot add sensing read descriptor");
            return null;
        }

        if (aliveCharacteristic.addDescriptor(new BluetoothGattDescriptor(Constants
                .NOTIFY_DESCRIPTOR_UUID,
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor
                        .PERMISSION_READ)) == false) {
            e("Cannot add alive descriptor");
            return null;
        }

        if (aliveCharacteristic.setValue(new byte[]{}) == false) {
            e("Cannot set alive char value");
            return null;
        }

        if (infoCharacteristic.setValue(ble.getInfoString()) == false) {
            e("Cannot set info char value");
            return null;
        }

        if (service.addCharacteristic(readCharacteristic) == false) {
            e("Cannot add read char");
            return null;
        }

        if (service.addCharacteristic(sensingReadCharacteristic) == false) {
            e("Cannot add sensing read char");
            return null;
        }

        if (service.addCharacteristic(writeCharacteristic) == false) {
            e("Cannot add write char");
            return null;
        }

        if (service.addCharacteristic(sensingWriteCharacteristic) == false) {
            e("Cannot add sensing write char");
            return null;
        }

        if (service.addCharacteristic(aliveCharacteristic) == false) {
            e("Cannot add alive char");
            return null;
        }

        if (service.addCharacteristic(infoCharacteristic) == false) {
            e("Cannot add info char");
            return null;
        }

        return service;
    }

    // MARK: BluetoothGattServerCallback

    @Override
    public void onConnectionStateChange(final BluetoothDevice device, final int status,
                                        final int newState) {
        threadManager.postToWork(new Runnable() {
            // this needs to go straight to work thread, so core is blocked with handling of the
            // disconnecting of the device. Otherwise a ping or write could come in before
            // disconnect is finished.
            @Override
            public void run() {
                // even the log needs to be in work thread, otherwise could have a racing condition.
                d("onConnectionStateChange: %s %s", getConnectionState(newState)
                        , device.getAddress());

                if (status != BluetoothGatt.GATT_SUCCESS) {
                    e("connecting failed with status" + status);
                }

                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    core.HMBTCorelinkDisconnect(ByteUtils.bytesFromMacString(device.getAddress()));
                }
            }
        });
    }

    @Override public void onServiceAdded(int status, BluetoothGattService service) {
        // continue start broadcast after this
        broadcaster.onServiceAdded(status == BluetoothGatt.GATT_SUCCESS);
    }

    @Override
    public void onCharacteristicReadRequest(final BluetoothDevice device,
                                            final int requestId,
                                            final int offset,
                                            final BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue();
        if (value == null) value = new byte[0];
        byte[] offsetBytes = Arrays.copyOfRange(value, offset, value.length);
        final int characteristicId = getCharacteristicIdForCharacteristic(characteristic);

        d("onCharacteristicReadRequest %s %s", characteristicId,
                ByteUtils.hexFromBytes(offsetBytes));

        boolean result = gattServer.sendResponse(device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                offsetBytes);

        if (result == false) {
            e("onCharacteristicReadRequest: failed to send response");
            // TODO: 30/10/2019 if the read failed, core should be notified
            return;
        }

        d("onCharacteristicReadRequest %s %s %b", characteristicId,
                ByteUtils.hexFromBytes(offsetBytes), result);

        // give a chance for the other side to read the data?
        threadManager.postDelayed(new Runnable() {
            @Override
            public void run() {
                core.HMBTCorelinkWriteResponse(ByteUtils.bytesFromMacString(device
                        .getAddress()), characteristicId);
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
            e("incoming data from invalid characteristic");
            return;
        }

        d("incoming data %s: %s from %s", characteristicId, ByteUtils
                .hexFromBytes(value), ByteUtils.hexFromBytes(ByteUtils.bytesFromMacString(device
                .getAddress())));

        if (responseNeeded) {
            boolean result = gattServer.sendResponse(device, requestId, BluetoothGatt
                    .GATT_SUCCESS, 0, null);

            if (result == false) {
                e("onCharacteristicWriteRequest: failed to send response");
            }

            threadManager.postToWork(new Runnable() {
                @Override
                public void run() {
                    core.HMBTCorelinkIncomingData(value, value.length, ByteUtils
                            .bytesFromMacString(device.getAddress()), characteristicId);
                }
            });
        }
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                        BluetoothGattDescriptor descriptor) {
        byte[] value = descriptor.getValue();
        byte[] offsetBytes = value == null ? new byte[]{} : Arrays.copyOfRange(value, offset,
                value.length);

        boolean result = gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, offsetBytes);

        if (result == false) e("onDescriptorReadRequest: failed to send response");
    }

    @Override
    public void onDescriptorWriteRequest(final BluetoothDevice device, int requestId,
                                         BluetoothGattDescriptor descriptor, boolean
                                                 preparedWrite, boolean responseNeeded, int
                                                 offset, byte[] value) {
        if (responseNeeded) {
            boolean result = gattServer.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    value);

            if (result == false) {
                e("onDescriptorWriteRequest: failed to send response");
            }

            if (descriptor.getCharacteristic().getUuid().equals(Constants.READ_CHAR_UUID) == false)
                return;

            // if notifications don't start, try restarting bluetooth on android / other device

            threadManager.postToWork(new Runnable() {
                @Override
                public void run() {
                    final byte[] deviceMac = ByteUtils.bytesFromMacString(device.getAddress());
                    // check if broadcaster already has this device as a link because according to
                    // BLE spec a new descriptor write can come in any time and this would
                    // create a duplicate link

                    if (broadcaster.onNotificationsStartedForReadCharacteristic(device,
                            deviceMac)) {
                        // this is not authenticated but connected state. Can call
                        // HMBTCorelinkConnect after dispatching new link from Broadcaster.
                        core.HMBTCorelinkConnect(deviceMac);
                    }
                }
            });
        }
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        d("onNotificationSent: %s", (status == BluetoothGatt.GATT_SUCCESS ? "success" : "failed"));
    }

    @Override public void onMtuChanged(BluetoothDevice device, int mtu) {
        core.HMBTCoreSetMTU(ByteUtils.bytesFromMacString(device.getAddress()), mtu);

        String mtuCharValue = " MTU" + String.format("%03d", mtu);
        String currentCharValue = infoCharacteristic.getStringValue(0);

        if (currentCharValue.contains("MTU")) {
            infoCharacteristic.setValue(currentCharValue.split("MTU")[0] + mtuCharValue);
        } else {
            infoCharacteristic.setValue(currentCharValue + mtuCharValue);
        }
    }

    private int getCharacteristicIdForCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(writeCharacteristic.getUuid())) {
            return 0x03;
        } else if (characteristic.getUuid().equals(sensingWriteCharacteristic.getUuid())) {
            return 0x07;
        } else if (characteristic.getUuid().equals(readCharacteristic.getUuid())) {
            return 0x02;
        } else if (characteristic.getUuid().equals(sensingReadCharacteristic.getUuid())) {
            return 0x06;
        } else if (characteristic.getUuid().equals(aliveCharacteristic.getUuid())) {
            return 0x04;
        } else if (characteristic.getUuid().equals(infoCharacteristic.getUuid())) {
            return 0x05;
        }

        return -1;
    }

    private String getConnectionState(int state) {
        switch (state) {
            case BluetoothProfile.STATE_DISCONNECTED:
                return "STATE_DISCONNECTED";
            case BluetoothProfile.STATE_CONNECTING:
                return "STATE_CONNECTING";
            case BluetoothProfile.STATE_CONNECTED:
                return "STATE_CONNECTED";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "STATE_DISCONNECTING";
        }

        return "Unknown";
    }

    private BluetoothGattCharacteristic getCharacteristicForId(int id) {
        switch (id) {
            case HMBTCoreInterface.hm_characteristic_alive: {
                return aliveCharacteristic;
            }
            case HMBTCoreInterface.hm_characteristic_info: {
                return infoCharacteristic;
            }
            case HMBTCoreInterface.hm_characteristic_link_read: {
                return readCharacteristic;
            }
            case HMBTCoreInterface.hm_characteristic_link_write: {
                return writeCharacteristic;
            }
            case HMBTCoreInterface.hm_characteristic_sensing_read: {
                return sensingReadCharacteristic;
            }
            case HMBTCoreInterface.hm_characteristic_sensing_write: {
                return sensingWriteCharacteristic;
            }
            default:
                return null;
        }
    }

    static abstract class Callback {
        abstract void onServiceAdded(boolean success);

        // return false if already exists.
        abstract boolean onNotificationsStartedForReadCharacteristic(BluetoothDevice device,
                                                                     byte[] mac);
    }
}
