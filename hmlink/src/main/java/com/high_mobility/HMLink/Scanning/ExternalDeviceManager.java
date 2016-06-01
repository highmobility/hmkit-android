package com.high_mobility.HMLink.Scanning;

import com.high_mobility.HMLink.LinkException;

/**
 * Created by ttiganik on 01/06/16.
 */
public class ExternalDeviceManager {
    public enum State {
        BLUETOOTH_UNAVAILABLE, IDLE, SCANNING
    }

    ExternalDevice[] devices;
    ExternalDeviceListener listener;
    State state;
    static ExternalDeviceManager instance;

    public static ExternalDeviceManager getInstance() {
        if (instance == null) {
            instance = new ExternalDeviceManager();
        }
        return  instance;
    }

    public ExternalDevice[] getDevices() {
        return devices;
    }

    public State getState() {
        return state;
    }

    public void setListener(ExternalDeviceListener listener) {
        this.listener = listener;
    }

    public void addTrustedCertificateAuthority(byte[] issuer, byte[] publicKey) {
        // TODO:
    }

    public void startScanning(byte[][] appIdentifiers) throws LinkException {
        if (getState() == State.SCANNING) return;
        // = new byte[][] { array1, array2, array3, array4, array5 };

        // TODO:
    }

    public void stopScanning() {
        if (getState() == State.IDLE) return;
        // TODO:
    }
}
