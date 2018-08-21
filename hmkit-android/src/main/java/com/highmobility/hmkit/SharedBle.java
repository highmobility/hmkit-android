package com.highmobility.hmkit;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.highmobility.utils.ByteUtils;

import java.util.ArrayList;
import java.util.Random;

public class SharedBle {
    Context context;

    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;

    private ArrayList<SharedBleListener> listeners = new ArrayList<>();

    public void addListener(SharedBleListener listener) {
        if (listeners.contains(listener) == false) listeners.add(listener);
    }

    public void removeListener(SharedBleListener listener) {
        listeners.remove(listener);
    }

    public BluetoothManager getManager() {
        return mBluetoothManager;
    }

    public BluetoothAdapter getAdapter() {
        if (mBluetoothAdapter == null) {
            createAdapter();
        }

        return mBluetoothAdapter;
    }

    public boolean isBluetoothSupported() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean isBluetoothOn() {
        return (getAdapter() != null && getAdapter().isEnabled() && getAdapter().getState() ==
                BluetoothAdapter.STATE_ON);
    }

    SharedBle(Context context) {
        this.context = context;
        initialise();
    }

    void initialise() {
        context.registerReceiver(receiver, new IntentFilter(BluetoothAdapter
                .ACTION_STATE_CHANGED));
    }

    void setRandomAdapterName(boolean overrideLocalName) {
        if (overrideLocalName == false) return;
        String name = "HM ";
        byte[] serialBytes = new byte[3];
        new Random().nextBytes(serialBytes);
        String randomBytesString = ByteUtils.hexFromBytes(serialBytes);
        name += randomBytesString.substring(1);
        getAdapter().setName(name);
    }

    void createAdapter() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context
                    .BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
    }

    void terminate() {
        if (mBluetoothAdapter != null) {
            context.unregisterReceiver(receiver);
            // don't clear listeners here because broadcaster is never nulled.
        }
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
