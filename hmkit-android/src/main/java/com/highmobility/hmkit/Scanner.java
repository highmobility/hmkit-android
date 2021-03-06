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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

import com.highmobility.btcore.HMDevice;
import com.highmobility.utils.ByteUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.highmobility.hmkit.HMLog.d;

class Scanner extends Core.Scanner {
    private final Core core;
    private final SharedBle ble;
    private final ThreadManager threadManager;
    private final Storage storage;

    private final BleListener bleListener = new BleListener();

    private class BleListener implements SharedBleListener {
        // we don't want this method to be publicly available, so we create the class here
        @Override public void bluetoothChangedToAvailable(boolean available) {
            d("bluetoothChangedToAvailable(): available = %b", available);

        }
    }

    public enum State {
        BLUETOOTH_UNAVAILABLE, IDLE, SCANNING
    }

    Map<byte[], byte[]> CaPublicKeyMap = new HashMap<>();

    List<ScannedLink> links = new ArrayList<>();
    ScannerListener listener;

    State state = State.IDLE;

    BluetoothLeScanner bleScanner;

    ArrayList<byte[]> authenticatingMacs = new ArrayList<>();

    Scanner(Core core, Storage storage, ThreadManager threadManager, SharedBle ble) {
        this.core = core;
        this.ble = ble;
        this.storage = storage;
        this.threadManager = threadManager;
        initialise();
    }

    boolean initialise() {
        ble.addListener(bleListener);
        return true;
    }

    /**
     * @return The links currently in proximity
     */
    public List<ScannedLink> getLinks() {
        return links;
    }

    /**
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
     * Add a trusted Certificate Authority to the scan list. This is used to recognize Link-enabled
     * devices when scanning.
     *
     * @param issuer    The CA issuer identifier
     * @param publicKey The CA public key
     */
    public void addTrustedCertificateAuthority(byte[] issuer, byte[] publicKey) {
        CaPublicKeyMap.put(issuer, publicKey);
    }

    /**
     * Start scanning for nearby links.
     *
     * @return 0 if succeeded or Link.BLUETOOTH_OFF if BLE is turned off
     */
    public int startScanning() {
        // = new byte[][] { array1, array2, array3, array4, array5 };
        if (getState() == State.SCANNING) return 0;

        core.start();
        startBle();

        if (!ble.isBluetoothOn()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
//            return Link.BLUETOOTH_OFF;
            return 2; // use some descriptive error method
        }

        if (bleScanner == null) bleScanner = ble.getAdapter().getBluetoothLeScanner();
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        bleScanner.startScan(null, settings, scanCallback);
        setState(State.SCANNING);

        threadManager.postToWork(new Runnable() {
            @Override
            public void run() {
                core.HMBTCoreSensingScanStart();
            }
        });

        return 0;
    }

    private void startBle() {
        // we need ble on ctor to listen to state change from IDLE to BLE_UNAVAILABLE.
        // ble is stopped on terminate. could be started again on startBroadcasting.
        ble.initialise();

        // scanner could have already initialised the ble, then we need to add the listener and
        // check for initial ble state.
        ble.addListener(bleListener);
        if (ble.isBluetoothOn() == false) setState(State.BLUETOOTH_UNAVAILABLE);
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

            threadManager.postToWork(new Runnable() {
                @Override
                public void run() {
                    core.HMBTCoreSensingProcessAdvertisement(ByteUtils.bytesFromMacString(device
                            .getAddress()), advBytes, advBytes.length);
                }
            });
        }
    };

    void connect(byte[] mac) {
        for (byte[] authenticatingMac : authenticatingMacs) {
            if (Arrays.equals(authenticatingMac, mac)) return; // already connecting
        }

        addAuthenticatingMac(mac);
        BluetoothDevice bluetoothDevice = ble.getAdapter().getRemoteDevice(mac);

        for (ScannedLink existingDevice : links) {
            if (existingDevice.btDevice.getAddress().equals(bluetoothDevice.getAddress())) {
                existingDevice.connect();
                return;
            }
        }

        ScannedLink device = new ScannedLink(ble, core, threadManager, bluetoothDevice);
        links.add(device);
        device.connect();
    }

    void disconnect(byte[] mac) {
        ScannedLink device = getLinkForMac(mac);
        if (device == null) return;

        device.disconnect();
        removeAuthenticatingMac(mac);
        links.remove(device);
    }

    boolean onDeviceExitedProximity(byte[] mac) {
        final ScannedLink device = getLinkForMac(mac);
        if (device == null) return false;
        device.onDeviceExitedProximity();

        if (listener != null) {
            threadManager.postToMain(new Runnable() {
                @Override
                public void run() {
                    if (listener == null) return;
                    listener.onDeviceExitedProximity(device);
                }
            });
        }

        removeAuthenticatingMac(device.getAddressBytes());
        links.remove(device);
        return true;
    }

    void startServiceDiscovery(byte[] mac) {
        ScannedLink device = getLinkForMac(mac);
        device.discoverServices();
    }

    boolean onChangedAuthenticationState(HMDevice device) {
        removeAuthenticatingMac(device.getMac());
        final ScannedLink scannedLink = getLinkForMac(device.getMac());
        if (scannedLink != null) {
            scannedLink.onChangedAuthenticationState(device);
            if (listener != null) {
                threadManager.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (listener == null) return;
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

    boolean onCommandResponse(HMDevice device, byte[] data) {
        ScannedLink scannedLink = getLinkForMac(device.getMac());
        if (scannedLink == null) return false;
        scannedLink.onCommandResponse(data);
        return true;
    }

    @Override boolean onCommandErrorResponse(HMDevice device, int errorType) {
        Link link = getLinkForMac(device.getMac());
        if (link == null) return false;
        link.onCommandError(errorType);
        return true;
    }

    boolean onRevokeResult(HMDevice device, byte[] data, int result) {
        Link link = getLinkForMac(device.getMac());
        if (link == null) return false;
        link.onRevokeResponse(data, result);
        return true;
    }

    @Override boolean onErrorCommand(HMDevice device, int commandId, int errorType) {
        Link link = getLinkForMac(device.getMac());
        if (link == null) return false;
        link.onErrorCommand(commandId, errorType);
        return true;
    }

    @Override boolean onRegisterCertificate(byte[] serial) {
        Link link = getLinkForSerial(serial);
        if (link == null) return false;
        link.onRegisterCertificate();
        return false;
    }

    @Override boolean onRevokeIncoming(HMDevice device) {
        Link link = getLinkForMac(device.getMac());
        if (link == null) return false;
        link.onRevokeIncoming();
        return true;
    }

    boolean writeData(byte[] mac, byte[] value, int characteristic) {
        ScannedLink link = getLinkForMac(mac);
        if (link == null) return false;
        // TSODO: use characteristic from id
        link.writeValue(value);

        return true;
    }

    boolean readValue(byte[] mac, int characteristic) {
        ScannedLink link = getLinkForMac(mac);
        if (link == null) return false;
        // TSODO: use the characteristic id
        link.readValue();
        return true;
    }

    private ScannedLink getLinkForMac(byte[] mac) {
        for (int i = 0; i < links.size(); i++) {
            ScannedLink link = links.get(i);
            if (Arrays.equals(link.getAddressBytes(), mac)) return link;
        }

        return null;
    }

    private ScannedLink getLinkForSerial(byte[] serial) {
        for (int i = 0; i < links.size(); i++) {
            ScannedLink link = links.get(i);
            if (link.getSerial().equals(serial)) return link;
        }

        return null;
    }

    private void setState(final State state) {
        if (this.state != state) {
            final State oldState = this.state;
            this.state = state;

            if (listener != null) {
                threadManager.postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (listener == null) return;
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
