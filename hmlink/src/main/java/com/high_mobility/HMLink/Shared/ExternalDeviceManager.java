package com.high_mobility.HMLink.Shared;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
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

    ExternalDevice[] devices = new ExternalDevice[0];  // TODO: test if devices pointer is ok for adapter dataSetChanged
    ExternalDeviceManagerListener listener;

    State state = State.IDLE;

    Shared shared;
    BluetoothLeScanner bleScanner;
    byte[][] scannedIdentifiers;
    ArrayList<byte[]> authenticatingMacs = new ArrayList<>();

    ExternalDeviceManager(Shared shared) {
        this.shared = shared;
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
        bleScanner.startScan(scanCallback);
        setState(State.SCANNING);
        shared.core.HMBTCoreSensingScanStart(shared.coreInterface);
    }

    private ScanCallback scanCallback = new ScanCallback() {
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
            BluetoothDevice device = result.getDevice();
            byte[] advBytes = result.getScanRecord().getBytes();
            shared.core.HMBTCoreSensingProcessAdvertisement(shared.coreInterface,
                    ByteUtils.bytesFromMacString(device.getAddress()),
                    advBytes, advBytes.length);
        }
    };

    public void stopScanning() {
        if (getState() != State.SCANNING) return;
        bleScanner.stopScan(scanCallback);
    }

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
        addDevice(device);
        device.connect();
    }

    void disconnect(byte[] mac) {
        ExternalDevice device = getDeviceForMac(mac);
        if (device == null) return;

        device.disconnect();
        removeAuthenticatingMac(mac);
        removeDevice(device);
    }

    boolean deviceExitedProximity(byte[] mac) {
        ExternalDevice device = getDeviceForMac(mac);
        if (device == null) return false;

        if (listener != null) {
            device.onDeviceExitedProximity();
            listener.onDeviceExitedProximity(device);
        }

        removeAuthenticatingMac(device.getAddressBytes());
        removeDevice(device);
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
        Log.d(TAG, "didAuthenticateDevice " + ByteUtils.hexFromBytes(device.getMac()));
        removeAuthenticatingMac(device.getMac());
        ExternalDevice externalDevice = getDeviceForMac(device.getMac());
        if (externalDevice != null) {
            externalDevice.hmDevice = device;
            externalDevice.didAuthenticate();
            if (listener != null) listener.onDeviceEnteredProximity(externalDevice);
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

    private void addDevice(ExternalDevice device) {
        ExternalDevice[] newDevices = new ExternalDevice[devices.length + 1];
        for (int i = 0; i < devices.length; i++) {
            newDevices[i] = devices[i];
        }

        newDevices[devices.length] = device;
        devices = newDevices;
    }

    private void removeDevice(ExternalDevice device) {
        if (device == null) return;
        int removedIndex = -1;
        for (int i = 0; i < devices.length; i++) {
            if (device == devices[i]) {
                removedIndex = i;
                break;
            }
        }

        if (removedIndex >= 0) {
            ExternalDevice[] newDevices = new ExternalDevice[devices.length - 1];
            for (int i = 0; i < devices.length - 1; i++) {
                if (i >= removedIndex) {
                    newDevices[i] = devices[i + 1]; // if you crash here the initial size is not big enough
                } else {
                    newDevices[i] = devices[i];
                }
            }
            devices = newDevices;
        }
    }
}
