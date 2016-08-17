package com.high_mobility.HMLink.Shared;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import com.high_mobility.HMLink.LinkException;
import com.high_mobility.btcore.HMDevice;

import java.util.ArrayList;
import java.util.Arrays;
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

    Map<byte[], byte[]> CaPublicKeyMap = new HashMap<>();

    List<ExternalDevice> devices = new ArrayList<>();  // TODO: test if devices pointer is ok for adapter dataSetChanged
    ExternalDeviceManagerListener listener;

    State state = State.IDLE;

    Shared shared;
    BluetoothLeScanner bleScanner;

    ArrayList<byte[]> authenticatingMacs = new ArrayList<>();

    ExternalDeviceManager(Shared shared) {
        this.shared = shared;
    }

    public List<ExternalDevice> getDevices() {
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

    public void startScanning() throws LinkException {
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
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        bleScanner.startScan(null, settings, scanCallback);
        setState(State.SCANNING);
        shared.core.HMBTCoreSensingScanStart(shared.coreInterface);
    }

    public void stopScanning() {
        if (getState() != State.SCANNING) return;
        bleScanner.stopScan(scanCallback);
        setState(State.IDLE);
    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            onScanResult(result);
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                onScanResult(result);
            }
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            setState(State.IDLE);
            super.onScanFailed(errorCode);
        }

        void onScanResult(ScanResult result) {
            final BluetoothDevice device = result.getDevice();
            final byte[] advBytes = result.getScanRecord().getBytes();

            shared.mainThread.post(new Runnable() {
                @Override
                public void run() {
            shared.core.HMBTCoreSensingProcessAdvertisement(shared.coreInterface,
                    ByteUtils.bytesFromMacString(device.getAddress()),
                    advBytes, advBytes.length);
                }
            });
        }
    };

    void connect(byte[] mac) {
        for (byte[] authenticatingMac : authenticatingMacs) {
            if (Arrays.equals(authenticatingMac, mac)) return; // already connecting
        }

        addAuthenticatingMac(mac);
        BluetoothDevice bluetoothDevice = shared.ble.getAdapter().getRemoteDevice(mac);

        for (ExternalDevice existingDevice : devices) {
            if (existingDevice.btDevice.getAddress().equals(bluetoothDevice.getAddress())) {
                existingDevice.connect();
                return;
            }
        }

        ExternalDevice device = new ExternalDevice(this, bluetoothDevice);
        devices.add(device);
        device.connect();
    }

    void disconnect(byte[] mac) {
        ExternalDevice device = getDeviceForMac(mac);
        if (device == null) return;

        device.disconnect();
        removeAuthenticatingMac(mac);
        devices.remove(device);
    }

    boolean deviceExitedProximity(byte[] mac) {
        ExternalDevice device = getDeviceForMac(mac);
        if (device == null) return false;
        device.onDeviceExitedProximity();

        if (listener != null) {
            listener.onDeviceExitedProximity(device);
        }

        removeAuthenticatingMac(device.getAddressBytes());
        devices.remove(device);
        return true;
    }

    void startServiceDiscovery(byte[] mac) {
        ExternalDevice device = getDeviceForMac(mac);
        device.discoverServices();
    }

    ExternalDevice getDeviceForMac(byte[] mac) {
        for (ExternalDevice existingDevice : devices) {
            if (Arrays.equals(ByteUtils.bytesFromMacString(existingDevice.btDevice.getAddress()), mac)) {
                return existingDevice;
            }
        }

        return null;
    }

    void didAuthenticateDevice(HMDevice device) {
        removeAuthenticatingMac(device.getMac());
        final ExternalDevice externalDevice = getDeviceForMac(device.getMac());
        if (externalDevice != null) {
            externalDevice.hmDevice = device;
            externalDevice.didAuthenticate();
            if (listener != null) {
                shared.mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDeviceEnteredProximity(externalDevice);
                    }
                });
            }
        }
        else {
            Log.e(TAG, "Invalid authenticated device");
        }
    }

    boolean isAuthenticating(byte[] mac) {
        for (int i = 0; i < authenticatingMacs.size(); i++) {
            byte[] existingMac = authenticatingMacs.get(i);
            if (Arrays.equals(existingMac, mac)) {
                return true;
            }
        }
        return false;
    }

    byte[] onCommandReceived(HMDevice device, byte[] data) {
        ExternalDevice externalDevice = getDeviceForMac(device.getMac());
        if (externalDevice == null) return null;
        return externalDevice.onCommandReceived(data);
    }

    boolean onCommandResponseReceived(HMDevice device, byte[] data) {
        ExternalDevice externalDevice = getDeviceForMac(device.getMac());
        if (externalDevice == null) return false;
        externalDevice.onCommandResponseReceived(data);
        return true;
    }


    private void setState(final State state) {
        if (this.state != state) {
            final State oldState = this.state;
            this.state = state;

            if (listener != null) {
                shared.mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStateChanged(oldState);
                    }
                });
            }
        }
    }

    private void removeAuthenticatingMac(byte[] mac) {
        for (int i = 0; i < authenticatingMacs.size(); i++) {
            byte[] existingMac = authenticatingMacs.get(i);
            if (Arrays.equals(existingMac, mac)) {
                authenticatingMacs.remove(i);
                return;
            }
        }
    }

    private boolean addAuthenticatingMac(byte[] mac) {
        for (byte[] existingAuthMac : authenticatingMacs) {
            if (Arrays.equals(existingAuthMac, mac)) {
                return false;
            }
        }
        authenticatingMacs.add(mac);
        return true;
    }
}
