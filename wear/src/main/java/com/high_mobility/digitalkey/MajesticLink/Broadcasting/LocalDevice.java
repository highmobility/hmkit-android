package com.high_mobility.digitalkey.MajesticLink.Broadcasting;

import android.bluetooth.BluetoothGattCharacteristic;

import com.high_mobility.digitalkey.MajesticLink.Shared.AccessCertificate;
import com.high_mobility.digitalkey.MajesticLink.Shared.DeviceCertificate;

/**
 * Created by ttiganik on 12/04/16.
 */
public class LocalDevice {
    public enum State { BLUETOOTH_UNAVAILABLE, IDLE, BROADCASTING}

    public AccessCertificate[] registeredCertificates;
    public AccessCertificate[] storedCertificates;

    public Link[] links;

    BluetoothGattCharacteristic readCharacteristic;
    BluetoothGattCharacteristic writeCharacteristic;

    LocalDeviceCallback callback;

    static LocalDevice instance = null;

    public static LocalDevice getInstance() {
        if (instance == null) {
            instance = new LocalDevice();
        }

        return instance;
    }

    public void registerCallback(LocalDeviceCallback callback) {
        this.callback = callback;
    }

    public void setDeviceCertificate(DeviceCertificate certificate, byte[] privateKey, byte[] publicKey) {
        // TODO:
        Storage.getInstance().deviceCertificate = certificate;
    }

    public void startBroadcasting() {
        // TODO:
    }

    public void stopBroadcasting() {
        // TODO:
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

}
