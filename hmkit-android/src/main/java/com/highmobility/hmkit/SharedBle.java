/*
 * The MIT License
 *
 * Copyright (c) 2014- High-Mobility GmbH (https://high-mobility.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.highmobility.hmkit;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.highmobility.hmkit.error.BleNotSupportedException;
import com.highmobility.utils.ByteUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Used to access shared BLE resources.
 */
public class SharedBle {
    final Context context; // the only place where need to store context: on start broadcasting and
    // terminate
    private boolean receiverRegistered;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private final ArrayList<SharedBleListener> listeners = new ArrayList<>();

    public boolean addListener(SharedBleListener listener) {
        if (listeners.contains(listener) == false) {
            listeners.add(listener);
            return true;
        }
        return false;
    }

    public void removeListener(SharedBleListener listener) {
        listeners.remove(listener);
    }

    public BluetoothManager getManager() {
        return mBluetoothManager;
    }

    public BluetoothAdapter getAdapter() {
        return mBluetoothAdapter;
    }

    public String getName() {
        return mBluetoothAdapter.getName();
    }

    // devices connected to the Broadcaster
    List<BluetoothDevice> getConnectedDevices() {
        return getManager().getConnectedDevices(BluetoothProfile.GATT_SERVER);
    }

    String getInfoString() {
        PackageManager packageManager = context.getPackageManager();

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            return HMKit.infoStringPrefix() + "w"; // wearable
        } else if (packageManager.hasSystemFeature(PackageManager.FEATURE_EMBEDDED)) {
            return HMKit.infoStringPrefix() + "t"; // android things
        } else {
            return HMKit.infoStringPrefix() + "m";
        }
    }

    public boolean isBluetoothOn() {
        return (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothAdapter
                .getState() ==
                BluetoothAdapter.STATE_ON);
    }

    SharedBle(Context context) throws BleNotSupportedException {
        this.context = context;

        Object bleService = context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bleService == null ||
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                        == false)
            throw new BleNotSupportedException();

        mBluetoothManager = (BluetoothManager) bleService;
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        initialise();
    }

    /**
     * @return true if context receiver was registered.
     */
    boolean initialise() {
        if (receiverRegistered == false) {
            context.registerReceiver(receiver, new IntentFilter(BluetoothAdapter
                    .ACTION_STATE_CHANGED));
            receiverRegistered = true;
            return true;
        }

        return false;
    }

    void terminate() {
        if (mBluetoothAdapter != null && receiverRegistered) {
            context.unregisterReceiver(receiver);
            receiverRegistered = false;
            // don't clear listeners here because broadcaster is never nulled.
        }
    }

    BluetoothGattServer openGattServer(BluetoothGattServerCallback callback) {
        return getManager().openGattServer(context, callback);
    }

    void setRandomAdapterName(boolean overrideLocalName) {
        if (overrideLocalName == false) return;
        String name = "HM ";
        byte[] serialBytes = new byte[3];
        new Random().nextBytes(serialBytes);
        String randomBytesString = ByteUtils.hexFromBytes(serialBytes);
        name += randomBytesString.substring(1);
        mBluetoothAdapter.setName(name);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter
                        .STATE_OFF) {
                    for (SharedBleListener listener : listeners) {
                        listener.bluetoothChangedToAvailable(false);
                    }
                } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) ==
                        BluetoothAdapter.STATE_ON) {
                    for (SharedBleListener listener : listeners) {
                        listener.bluetoothChangedToAvailable(true);
                    }
                }
            }
        }
    };
}
