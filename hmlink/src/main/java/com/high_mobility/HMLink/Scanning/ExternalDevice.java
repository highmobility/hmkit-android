package com.high_mobility.HMLink.Scanning;

/**
 * Created by ttiganik on 01/06/16.
 */
public class ExternalDevice {
    public enum State {
        Disconnected, Disconnecting, Connecting, Connected, Authenticated
    }
    State state;
    ExternalDeviceListener listener;
    int RSSI;

    public State getState() {
        return state;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setListener(ExternalDeviceListener listener) {
        this.listener = listener;
    }

    public void connect() {
        // TODO:
    }

    public void disconnect() {
        // TODO:
    }

    public void getNonce() {
        // TODO:
    }

    public void getDeviceCertificate() {
        // TODO:
    }

    public void getDeviceCertificateWithNonce() {
        // TODO:
    }

    public void registerCertificate() {
        // TODO:
    }

    public void authenticate() {
        // TODO:
    }

    public void storeCertificate() {
        // TODO:
    }

    public void getCertificate() {
        // TODO:
    }

    public void revokeCertificate() {
        // TODO:
    }

    public void sendCommand() {
        // TODO:
    }

    public void reset() {
        // TODO:
    }
}
