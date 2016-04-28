package com.high_mobility.digitalkey.HMLink.Broadcasting;

import com.high_mobility.btcore.HMBTCoreInterface;
import com.high_mobility.btcore.HMDevice;
import com.high_mobility.digitalkey.HMLink.LinkException;
import com.high_mobility.digitalkey.HMLink.Shared.AccessCertificate;

/**
 * Created by ttiganik on 20/04/16.
 */
public class BTCoreInterface implements HMBTCoreInterface {
    private static final String TAG = "BTCoreInterface";
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
    public int HMPersistenceHalgetStoredCertificate(byte[] cert, int size) {
        AccessCertificate certificate = device.storage.certWithProvidingSerial(device.certificate.getSerial());
        copyBytesToJNI(certificate.getBytes(), cert);
        size = certificate.getBytes().length;
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
//        this.device.didReceiveLink(device);
    }

    @Override
    public void HMCtwExitedProximity(HMDevice device) {
        this.device.didLoseLink(device);
        // TODO: hide the device
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