package com.high_mobility.HMLink.Shared;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import com.high_mobility.HMLink.LinkException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ttiganik on 01/06/16.
 */
public class ExternalDeviceManager {
    public static final String TAG = "ExternalDeviceManager";

    public enum State {
        BLUETOOTH_UNAVAILABLE, IDLE, SCANNING
    }

    byte[] serialNumber;
    byte[] publicKey;
    byte[] privateKey;

    Map<byte[], byte[]> CaPublicKeyMap = new HashMap<>();

    ExternalDevice[] devices = new ExternalDevice[0];
    ExternalDeviceManagerListener listener;
    BTCoreInterface coreInterface;

    State state = State.IDLE;
    static ExternalDeviceManager instance;
    Context ctx;
    Shared shared;
    BluetoothLeScanner bleScanner;
    byte[][] scannedIdentifiers;

    public static ExternalDeviceManager getInstance(Context applicationContext) {
        if (instance == null) {
            instance = new ExternalDeviceManager(applicationContext);

        }
        return  instance;
    }

    ExternalDeviceManager(Context applicationContext) {
        ctx = applicationContext;
        shared = Shared.getInstance(applicationContext);
        shared.externalDeviceManager = this;
    }

    public byte[] getSerialNumber() {
        return serialNumber;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public ExternalDevice[] getDevices() {
        return devices;
    }

    public State getState() {
        return state;
    }

    public void setListener(ExternalDeviceManagerListener listener) {
        this.listener = listener;
    }

    public void addTrustedCertificateAuthority(byte[] issuer, byte[] publicKey) {
        CaPublicKeyMap.put(issuer, publicKey);
    }

    public void setProperties(byte[] serialNumber, byte[] publicKey, byte[] privateKey) {
        this.serialNumber = serialNumber;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public void startScanning(byte[][] appIdentifiers) throws LinkException {
        // = new byte[][] { array1, array2, array3, array4, array5 };
        if (getState() == State.SCANNING) return;

        if (!shared.ble.isBluetoothSupported()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            throw new LinkException(LinkException.LinkExceptionCode.UNSUPPORTED);
        }

        if (!shared.ble.isBluetoothOn()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            throw new LinkException(LinkException.LinkExceptionCode.BLUETOOTH_OFF);
        }

        if (bleScanner == null) bleScanner = shared.ble.getAdapter().getBluetoothLeScanner();

        scannedIdentifiers = appIdentifiers;
        bleScanner.startScan(scanCallback); // TODO: could use filter if useful to our scan preferences
        setState(State.SCANNING);
        shared.startClock();
        shared.core.HMBTCoreSensingScanStart(coreInterface);
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult " + result);
            onScanResult(result);
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d(TAG, "onBatchScanResults " + results);
            for (ScanResult result : results) {
                onScanResult(result);
            }
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            setState(State.IDLE);
            Log.e(TAG, "onScanFailed " + errorCode);
            super.onScanFailed(errorCode);
        }

        void onScanResult(ScanResult result) {
            // TODO: create/sync devices
            /*
            public native void HMBTCoreSensingProcessAdvertisement(HMBTCoreInterface forwardInterface, byte[] mac, byte[] data, int size);


             */
//            core.HMBTCoreSensingProcessAdvertisement(forwardInterface, );
        }
    };

    public void stopScanning() {
        if (getState() != State.SCANNING) return;
        bleScanner.stopScan(scanCallback);
    }

    void startServiceDiscovery(byte[] mac) {
        // TODO: HMBTCoreSensingDiscoveryEvent(HMBTCoreInterface forwardInterface, byte[] mac); // call when services have been discovered
    }

    private void setState(final State state) {
        if (this.state != state) {
            final State oldState = this.state;
            this.state = state;

            if (listener != null) {
                shared.ble.mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStateChanged(oldState);
                    }
                });
            }
        }
    }
}
