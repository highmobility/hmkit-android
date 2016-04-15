package com.high_mobility.digitalkey.MajesticLink.Broadcasting;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.high_mobility.digitalkey.MajesticLink.Shared.AccessCertificate;
import com.high_mobility.digitalkey.MajesticLink.Shared.DeviceCertificate;
import com.high_mobility.digitalkey.Utils;

/**
 * Created by ttiganik on 12/04/16.
 */
public class LocalDevice {
    public enum State { BLUETOOTH_UNAVAILABLE, IDLE, BROADCASTING}

    Storage storage;
    byte[] privateKey;
    byte[] CAPublicKey;

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

    public void setDeviceCertificate(DeviceCertificate certificate, byte[] privateKey, byte[] CAPublicKey, Context ctx) {
        storage = new Storage(ctx);
        storage.deviceCertificate = certificate;
        this.privateKey = privateKey;
        this.CAPublicKey = CAPublicKey;
        storage = new Storage(ctx);
    }

    public AccessCertificate[] getRegisteredCertificates() {
        return storage.getRegisteredCertificates();
    }

    public AccessCertificate[] getStoredCertificates() {
        return storage.getStoredCertificates();
    }

    private Link[] links;
    public Link[] getLinks() {
        return links;
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
