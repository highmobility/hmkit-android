package com.high_mobility.digitalkey.HMLink.Broadcasting;

import android.util.Log;

import com.high_mobility.btcore.HMBTCoreInterface;
import com.high_mobility.btcore.HMDevice;
import com.high_mobility.digitalkey.HMLink.LinkException;
import com.high_mobility.digitalkey.HMLink.Shared.AccessCertificate;

/**
 * Created by ttiganik on 20/04/16.
 */
public class BTCoreInterface implements HMBTCoreInterface {
    LocalDevice device;

    BTCoreInterface(LocalDevice device) {
        this.device = device;
    }

    @Override
    public int HMBTHalInit() {
        return 0;
    }

    @Override
    public int HMBTHalScanStart() {
        return 0;
    }

    @Override
    public int HMBTHalScanStop() {
        return 0;
    }

    @Override
    public int HMBTHalAdvertisementStart() {
        try {
            device.startBroadcasting();
        } catch (LinkException e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    @Override
    public int HMBTHalAdvertisementStop() {
        device.stopBroadcasting();
        return 0;
    }

    @Override
    public int HMBTHalConnect(byte[] mac) {
        return 0;
    }

    @Override
    public int HMBTHalDisconnect(byte[] mac) {
        return 0;
    }

    @Override
    public int HMBTHalServiceDiscovery(byte[] mac) {
        return 0;
    }

    @Override
    public int HMBTHalWriteData(byte[] mac, int length, byte[] data) {
        device.writeData(mac, data);
        return 0;
    }

    @Override
    public int HMBTHalReadData(byte[] mac, int offset) {
        return 0;
    }

    @Override
    public int HMPersistenceHalgetSerial(byte[] serial) {
        copyBytesToJNI(device.certificate.getSerial(), serial);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetLocalPublicKey(byte[] publicKey) {
        copyBytesToJNI(device.certificate.getPublicKey(), publicKey);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetLocalPrivateKey(byte[] privateKey) {
        copyBytesToJNI(device.privateKey, privateKey);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetDeviceCertificate(byte[] cert) {
        //TODO
        /*uint8_t deviceCertificate[153] = {
          0x48, 0x49, 0x4D, 0x4C,
          0xBB, 0x81, 0x49, 0xAB, 0xBE, 0x90, 0x6F, 0x25, 0xD7, 0x16, 0xE8, 0xDE,
          0x01, 0x23, 0x19, 0x10, 0xD6, 0x2C, 0xA5, 0x71, 0xEE,
          0x36, 0x4B, 0x85, 0x24, 0x23, 0xF4, 0x5F, 0x09, 0x81, 0x34, 0x95, 0xDC, 0x28, 0x1C, 0x26, 0xC3, 0xFB, 0xBA, 0x83, 0x5D, 0xE0, 0x4A, 0x48, 0xFC, 0x06, 0x6A, 0xFD, 0xDF, 0x19, 0x0B, 0xE3, 0x40, 0xB2, 0x88, 0xC0, 0xB2, 0x8E, 0xFA, 0x1C, 0xA6, 0x9E, 0xD0, 0xD2, 0x3A, 0xED, 0x83, 0x53, 0x33, 0xFE, 0xE7, 0x11, 0xD6, 0x23, 0x8E, 0xB1, 0xE4, 0x9B, 0x59, 0x35, 0x83, 0x36, 0xDE, 0xE1, 0xDD,
          0x5f, 0x9a, 0xb5, 0x6f, 0x31, 0x53, 0x49, 0x6b, 0x06, 0xf1, 0x33, 0x6a, 0x36, 0xf6, 0x60, 0xd1, 0xa6, 0xc2, 0x67, 0xaf, 0x76, 0x06, 0x86, 0x42, 0xd6, 0x44, 0x6d, 0x22, 0x9b, 0x75, 0x49, 0xb5, 0xee, 0x34, 0x49, 0x1d, 0x1a, 0x5b, 0x39, 0xf3, 0xe8, 0xbc, 0xc9, 0x1a, 0x1a, 0xed, 0x1a, 0xb2, 0xeb, 0x96, 0xe1, 0x97, 0xa9, 0xf9, 0x2e, 0xf2, 0x81, 0x23, 0x04, 0xff, 0xf0, 0x0b, 0x45, 0xc8
            };*/
        return 0;
    }

    @Override
    public int HMPersistenceHaladdPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int commandSize, byte[] command) {
        AccessCertificate cert = new AccessCertificate(serial, publicKey, device.certificate.getSerial(), startDate, endDate, command);

        try {
            device.storage.storeCertificate(cert);
        } catch (LinkException e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    @Override
    public int HMPersistenceHalgetPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int[] commandSize, byte[] command) {
        AccessCertificate certificate = device.storage.certWithGainingSerial(serial);
        if (certificate == null) {
            return 1;
        }
        copyBytesToJNI(certificate.getGainerPublicKey(), publicKey);
        copyBytesToJNI(certificate.getStartDateBytes(), startDate);
        copyBytesToJNI(certificate.getEndDateBytes(), endDate);
        byte[] permissions = certificate.getPermissions();
        copyBytesToJNI(permissions, command);
        commandSize[0] = permissions.length;

        return 0;
    }

    @Override
    public int HMPersistenceHalgetPublicKeyByIndex(int index, byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int[] commandSize, byte[] command) {
        AccessCertificate[] certificates = device.storage.getRegisteredCertificates(device.certificate.getSerial());
        if (certificates.length >= index) {
            AccessCertificate certificate = certificates[index];
            copyBytesToJNI(certificate.getGainerPublicKey(), publicKey);
            copyBytesToJNI(certificate.getStartDateBytes(), startDate);
            copyBytesToJNI(certificate.getEndDateBytes(), endDate);
            byte[] permissions = certificate.getPermissions();
            copyBytesToJNI(permissions, command);
            commandSize[0] = permissions.length;

            return 0;
        }

        return 1;
    }

    @Override
    public int HMPersistenceHalgetPublicKeyCount(int[] count) {
        count[0] = device.storage.getRegisteredCertificates(device.certificate.getSerial()).length;
        return 0;
    }

    @Override
    public int HMPersistenceHalremovePublicKey(byte[] serial) {
        device.storage.deleteCertificateWithGainingSerial(serial);
        return 0;
    }

    @Override
    public int HMPersistenceHaladdStoredCertificate(byte[] cert, int size) {
        AccessCertificate certificate = new AccessCertificate(cert);
        try {
            device.storage.storeCertificate(certificate);
        } catch (LinkException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    @Override
    public int HMPersistenceHalgetStoredCertificate(byte[] cert, int[] size) {
        AccessCertificate certificate = device.storage.certWithProvidingSerial(device.certificate.getSerial());
        copyBytesToJNI(certificate.getBytes(), cert);
        size[0] = certificate.getBytes().length;
        return 0;
    }

    @Override
    public int HMPersistenceHaleraseStoredCertificate() {
        device.storage.deleteCertificateWithProvidingSerial(device.certificate.getSerial());
        return 0;
    }

    @Override
    public void HMCtwEnteredProximity(HMDevice device) {
        // TODO: this means core has finished identification of the device (might me authenticated or not) - show device info on screen
        Log.i(LocalDevice.TAG, "HMCtwEnteredProximity");
        this.device.didResolveDevice(device);
    }

    @Override
    public void HMCtwExitedProximity(HMDevice device) {
        // TODO: hide the device
        Log.i(LocalDevice.TAG, "HMCtwExitedProximity");
        this.device.didLoseLink(device);
    }

    @Override
    public void HMCtwCustomCommandReceived(HMDevice device, byte[] data, int[] length, int[] error) {
        // TODO: copy bytes with loop -- this is not the command response
        this.device.didReceiveCustomCommand(device, data, length[0], error[0]);
    }

    @Override
    public int HMCtwGetDeviceCertificateFailed(HMDevice device, byte[] nonce) {
        //ret false on, et ei jätka // TODO:
        return 0;
    }

    @Override
    public int HMCtwPairingRequested(HMDevice device, byte[] serial) {
        this.device.didReceivePairingRequest(device, serial);
        return 0;
    }

    private void copyBytesToJNI(byte[] from, byte[] to) {
        for (int i = 0; i < from.length; i++) {
            to[i] = from[i];
        }
    }
}