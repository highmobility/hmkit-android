package com.high_mobility.digitalkey.MajesticLink.Broadcasting;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import com.high_mobility.digitalkey.MajesticLink.Constants;
import com.high_mobility.digitalkey.MajesticLink.Shared.AccessCertificate;
import com.high_mobility.digitalkey.MajesticLink.Shared.DeviceCertificate;
import com.high_mobility.digitalkey.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by ttiganik on 12/04/16.
 */
public class LocalDevice {
    private static final String TAG = LocalDevice.class.getSimpleName();

    private static final boolean ALLOWS_MULTIPLE_LINKS = false;

    public enum State { BLUETOOTH_UNAVAILABLE, IDLE, BROADCASTING}

    Context ctx;
    Storage storage;
    byte[] privateKey;
    byte[] CAPublicKey;
    LocalDeviceCallback callback;

    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    BluetoothGattServer mGattServer;
    GATTServerCallback gattServerCallback;
    List<BluetoothDevice> mDevices = new ArrayList<>();
    BluetoothGattCharacteristic readCharacteristic;
    BluetoothGattCharacteristic writeCharacteristic;

    static LocalDevice instance = null;
    public State state;

    public static LocalDevice getInstance() {
        if (instance == null) {
            instance = new LocalDevice();
        }

        return instance;
    }

    public void registerCallback(LocalDeviceCallback callback) {
        this.callback = callback;
    }

    public void setDeviceCertificate(DeviceCertificate certificate, byte[] privateKey, byte[] CAPublicKey, Context ctx) {
        this.ctx = ctx;
        storage = new Storage(ctx);
        storage.deviceCertificate = certificate;
        this.privateKey = privateKey;
        this.CAPublicKey = CAPublicKey;
        storage = new Storage(ctx);
    }

    public AccessCertificate[] getRegisteredCertificates() {
        return storage.getRegisteredCertificates();
    }

    public AccessCertificate[] getStoredCertificates() {
        return storage.getStoredCertificates();
    }

    private Link[] links = new Link[0];
    public Link[] getLinks() {
        return links;
    }

    public void startBroadcasting() throws Exception{
        if (ALLOWS_MULTIPLE_LINKS == false && links.length != 0) {
            return;
        }

        // TODO: check for bt preconditions (on, available etc)
        // create the adapter
        mBluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothAdapter.setName("666666A5"); // TODO: use random name
        gattServerCallback = new GATTServerCallback(this);

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mGattServer = mBluetoothManager.openGattServer(ctx, gattServerCallback);

        // create the service
        // TODO: dont recreate this after stop -> start advertise
        BluetoothGattService service =new BluetoothGattService(Constants.SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        readCharacteristic =
                new BluetoothGattCharacteristic(Constants.READ_CHAR_UUID,
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_READ);

        UUID confUUUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
        readCharacteristic.addDescriptor(new BluetoothGattDescriptor(confUUUID, BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));

        writeCharacteristic =
                new BluetoothGattCharacteristic(Constants.WRITE_CHAR_UUID,
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(readCharacteristic);
        service.addCharacteristic(writeCharacteristic);

        mGattServer.addService(service);

        // start advertising
        if (mBluetoothLeAdvertiser == null) return;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(Utils.ADVERTISE_UUID))
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback);

        // TODO:
    }

    public void stopBroadcasting() {
        if (mBluetoothLeAdvertiser == null) return;

        mBluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
        if (mGattServer == null) return;
        mGattServer.close();
    }

    public void registerCertificate(AccessCertificate certificate) {
        // TODO:
    }

    public void storeCertificate(AccessCertificate certificate) {
        // TODO:
    }

    public void revokeCertificate(AccessCertificate certificate) {
        // TODO:
    }

    public void reset() {
        // TODO:
    }

    private boolean bluetoothEnabled() throws Exception {
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // TODO: throw not enabled exception
            /*//Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
            */
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // TODO: throw no support exception
/*
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
            */
        }

        return true;
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "Device Advertise Started.");
            setState(State.BROADCASTING);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "DEVICE Advertise Failed: "+errorCode);
            setState(State.IDLE);
        }
    };

    private void setState(State state) {
        if (state != state) {
            State oldState = this.state;
            this.state = state; // TODO: test that old state is correct

            if (callback != null) {
                callback.localDeviceStateChanged(state, oldState);
            }
        }


    }
}
