package com.high_mobility.HMLink.Scanning;

import android.bluetooth.BluetoothDevice;

import com.high_mobility.HMLink.AccessCertificate;
import com.high_mobility.HMLink.Device;
import com.high_mobility.HMLink.DeviceCertificate;

/**
 * Created by ttiganik on 01/06/16.
 */
public class ExternalDevice extends Device {
    public enum State {
        Disconnected, Disconnecting, Connecting, Connected, Authenticated
    }
    State state;
    ExternalDeviceListener listener;
    int RSSI;

    ExternalDeviceManager manager;

    BluetoothDevice btDevice;

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

    public void getAccessCertificate() {
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

    ExternalDevice(ExternalDeviceManager manager, BluetoothDevice btDevice) {
        this.manager = manager;
        this.btDevice = btDevice;
    }

    @Override
    public String getName() {
        return btDevice.getName();
    }
}
