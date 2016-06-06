package com.high_mobility.HMLink.Scanning;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import com.high_mobility.HMLink.Broadcasting.*;
import com.high_mobility.HMLink.LinkException;
import com.high_mobility.HMLink.SharedBle;
import com.high_mobility.btcore.HMBTCore;

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

    HMBTCore core = new HMBTCore();
    BTCoreInterface coreInterface;

    State state = State.IDLE;
    static ExternalDeviceManager instance;
    Context ctx;
    SharedBle ble;
    BluetoothLeScanner bleScanner;

    public static ExternalDeviceManager getInstance(Context applicationContext) {
        if (instance == null) {
            instance = new ExternalDeviceManager(applicationContext);

        }
        return  instance;
    }

    ExternalDeviceManager(Context applicationContext) {
        ctx = applicationContext;
        ble = SharedBle.getInstance(applicationContext);
        coreInterface = new BTCoreInterface(this);
        core.HMBTCoreInit(coreInterface);
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

        if (!ble.isBluetoothSupported()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            throw new LinkException(LinkException.LinkExceptionCode.UNSUPPORTED);
        }

        if (!ble.isBluetoothOn()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            throw new LinkException(LinkException.LinkExceptionCode.BLUETOOTH_OFF);
        }

        if (bleScanner == null) bleScanner = ble.getAdapter().getBluetoothLeScanner(); // TODO: use filter if useful to our scan filter

        bleScanner.startScan(scanCallback);
        setState(State.SCANNING);
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // TODO: create/sync devices
            Log.i(TAG, "onScanResult " + result);
            onScanResult(result);
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            // TODO: create/sync devices
            Log.i(TAG, "onBatchScanResults " + results);
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
//            core.HMBTCoreSensingProcessAdvertisement(coreInterface, );
        }
    };

    public void stopScanning() {
        if (getState() != State.SCANNING) return;
        bleScanner.stopScan(scanCallback);
    }

    private void setState(final State state) {
        if (this.state != state) {
            final State oldState = this.state;
            this.state = state;

            if (listener != null) {
                ble.mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStateChanged(oldState);
                    }
                });
            }
        }
    }
}
