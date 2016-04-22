package com.high_mobility.digitalkey.HMLink.Broadcasting;

import android.util.Log;

import com.high_mobility.digitalkey.HMLink.Broadcasting.Core.HMBTCoreInterface;
import com.high_mobility.digitalkey.HMLink.Broadcasting.Core.HMDevice;
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
    public int HMBTHalscan_start() {
        return 0;
    }

    @Override
    public int HMBTHalscan_stop() {
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
        serial = device.certificate.getSerial();
        return 0;
    }

    @Override
    public int HMPersistenceHalgetLocalPublicKey(byte[] publicKey) {
        publicKey = device.certificate.getPublicKey();
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
    public int HMPersistenceHalgetPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int commandSize, byte[] command) {
        AccessCertificate certificate = device.storage.certWithGainingSerial(serial);
        if (certificate == null) {
            return 1;
        }

        publicKey = certificate.getGainerPublicKey();
        startDate = certificate.getStartDateBytes();
        endDate = certificate.getEndDateBytes();
        command = certificate.getPermissions();
        commandSize = command.length;

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
        cert = certificate.getBytes();
        size = cert.length;
        return 0;
    }

    @Override
    public int HMPersistenceHaleraseStoredCertificate() {
        device.storage.deleteCertificateWithProvidingSerial(device.certificate.getSerial());
        return 0;
    }

    @Override
    public int HMCryptoaesEcbBlockEncrypt(byte[] key, byte[] cleartext, byte[] ciphertext) {
        return 0;
    }

    @Override
    public int HMCryptoeccGetEcdh(byte[] serial, byte[] ecdh) {
        return 0;
    }

    @Override
    public int HMCryptoeccAddSignature(byte[] data, int size, byte[] signature) {
        return 0;
    }

    @Override
    public int HMCryptoeccValidateSignature(byte[] data, int size, byte[] signature, byte[] serial) {
        return 0;
    }

    @Override
    public int HMCryptoeccValidateAllSignatures(byte[] data, int size, byte[] signature) {
        return 0;
    }

    @Override
    public int HMCryptoeccValidateCaSignature(byte[] data, int size, byte[] signature) {
        return 0;
    }

    @Override
    public int HMCryptohmac(byte[] key, byte[] data, byte[] hmac) {
        return 0;
    }

    @Override
    public int HMCryptogenerateNonce(byte[] nonce) {
        return 0;
    }

    @Override
    public void HMDebug(String str) {
        Log.i(TAG, str);
    }

    @Override
    public void HMCtwInit() {

    }

    @Override
    public void HMCtwPing() {

    }

    @Override
    public void HMCtwEnteredProximity(HMDevice device) {
        this.device.didReceiveLink(device);
    }

    @Override
    public void HMCtwExitedProximity(HMDevice device) {
        this.device.didLoseLink(device);
    }

    @Override
    public void HMCtwCommandReceived(HMDevice device, byte[] data, int length, int error) {
        this.device.didReceiveCustomCommand(device, data, length, error);
    }

    @Override
    public int HMCtwGetDeviceCertificateFailed(HMDevice device, int nonce) {
        //ret false on, et ei jätka // TODO:
        return 0;
    }

    @Override
    public int HMCtwPairingRequested(HMDevice device, byte[] serial) {
        this.device.didReceivePairingRequest(device, serial);
        return 0;
    }
}