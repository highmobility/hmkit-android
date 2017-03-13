package com.high_mobility.HMLink;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

import com.high_mobility.btcore.HMDevice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ttiganik on 01/06/16.
 */
class Scanner {
    static final String TAG = "Scanner";

    public enum State {
        BLUETOOTH_UNAVAILABLE, IDLE, SCANNING
    }

    Map<byte[], byte[]> CaPublicKeyMap = new HashMap<>();

    List<ScannedLink> devices = new ArrayList<>();
    ScannerListener listener;

    State state = State.IDLE;

    Manager manager;
    BluetoothLeScanner bleScanner;

    ArrayList<byte[]> authenticatingMacs = new ArrayList<>();

    Scanner(Manager manager) {
        this.manager = manager;
    }

    /**
     *
     * @return The links currently in proximity
     */
    public List<ScannedLink> getLinks() {
        return devices;
    }

    /**
     *
     * @return The Scanner state
     * @see State
     */
    public State getState() {
        return state;
    }

    /**
     * Set the ScannerListener to receive state change and Link proximity events.
     *
     * @param listener The object that implements as the ScannerListener
     */
    public void setListener(ScannerListener listener) {
        this.listener = listener;
    }

    /**
     * Add a trusted Certificate Authority to the scan list. This is used to recognize
     * Link-enabled devices when scanning.
     *
     * @param issuer The CA issuer identifier
     * @param publicKey The CA public key
     */
    public void addTrustedCertificateAuthority(byte[] issuer, byte[] publicKey) {
        CaPublicKeyMap.put(issuer, publicKey);
    }

    /**
     * Start scanning for nearby links.
     *
     * @return 0 if succeeded or    Link.UNSUPPORTED if BLE is not supported with this device
     *                              Link.BLUETOOTH_OFF if BLE is turned off
     */
    public int startScanning() {
        // = new byte[][] { array1, array2, array3, array4, array5 };
        if (getState() == State.SCANNING) return 0;

        if (!manager.ble.isBluetoothSupported()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            return Link.UNSUPPORTED;
        }

        if (!manager.ble.isBluetoothOn()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            return Link.BLUETOOTH_OFF;
        }

        if (bleScanner == null) bleScanner = manager.ble.getAdapter().getBluetoothLeScanner();
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        bleScanner.startScan(null, settings, scanCallback);
        setState(State.SCANNING);
        manager.workHandler.post(new Runnable() {
            @Override
            public void run() {
                manager.core.HMBTCoreSensingScanStart(manager.coreInterface);
            }
        });

        return 0;
    }

    /**
     * Stop scanning for nearby devices
     */
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

            manager.workHandler.post(new Runnable() {
                @Override
                public void run() {
            manager.core.HMBTCoreSensingProcessAdvertisement(manager.coreInterface,
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
        BluetoothDevice bluetoothDevice = manager.ble.getAdapter().getRemoteDevice(mac);

        for (ScannedLink existingDevice : devices) {
            if (existingDevice.btDevice.getAddress().equals(bluetoothDevice.getAddress())) {
                existingDevice.connect();
                return;
            }
        }

        ScannedLink device = new ScannedLink(this, bluetoothDevice);
        devices.add(device);
        device.connect();
    }

    void disconnect(byte[] mac) {
        ScannedLink device = getLinkForMac(mac);
        if (device == null) return;

        device.disconnect();
        removeAuthenticatingMac(mac);
        devices.remove(device);
    }

    boolean deviceExitedProximity(byte[] mac) {
        final ScannedLink device = getLinkForMac(mac);
        if (device == null) return false;
        device.onDeviceExitedProximity();

        if (listener != null) {
            manager.mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onDeviceExitedProximity(device);
                }
            });
        }

        removeAuthenticatingMac(device.getAddressBytes());
        devices.remove(device);
        return true;
    }

    void startServiceDiscovery(byte[] mac) {
        ScannedLink device = getLinkForMac(mac);
        device.discoverServices();
    }

    boolean didResolveDevice(HMDevice device) {
        removeAuthenticatingMac(device.getMac());
        final ScannedLink scannedLink = getLinkForMac(device.getMac());
        if (scannedLink != null) {
            scannedLink.setHmDevice(device);
            if (listener != null) {
                manager.mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDeviceEnteredProximity(scannedLink);
                    }
                });
            }
            return true;
        }

        return false;
    }

    boolean onCommandReceived(HMDevice device, byte[] data) {
        ScannedLink scannedLink = getLinkForMac(device.getMac());
        if (scannedLink == null) return false;
        scannedLink.onCommandReceived(data);
        return true;
    }

    boolean onCommandResponseReceived(HMDevice device, byte[] data) {
        ScannedLink scannedLink = getLinkForMac(device.getMac());
        if (scannedLink == null) return false;
        scannedLink.onCommandResponseReceived(data);
        return true;
    }

    boolean writeData(byte[] mac, byte[] value, int characteristic) {
        ScannedLink link = getLinkForMac(mac);
        if (link == null) return false;
        // TODO: use characteristic from id
        link.writeValue(value);

        return true;
    }

    boolean readValue(byte[] mac, int characteristic) {
        ScannedLink link = getLinkForMac(mac);
        if (link == null) return false;
        // TODO: use the characteristic id
        link.readValue();
        return true;
    }

    private ScannedLink getLinkForMac(byte[] mac) {
        for (ScannedLink existingDevice : devices) {
            if (Arrays.equals(ByteUtils.bytesFromMacString(existingDevice.btDevice.getAddress()), mac)) {
                return existingDevice;
            }
        }

        return null;
    }

    private void setState(final State state) {
        if (this.state != state) {
            final State oldState = this.state;
            this.state = state;

            if (listener != null) {
                manager.mainHandler.post(new Runnable() {
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
