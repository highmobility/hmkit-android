package com.highmobility.hmkit;

import android.util.Log;

import com.highmobility.utils.Bytes;
import com.highmobility.crypto.AccessCertificate;
import com.highmobility.btcore.HMBTCoreInterface;
import com.highmobility.btcore.HMDevice;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Created by ttiganik on 03/08/16.
 */
class BTCoreInterface implements HMBTCoreInterface {
    static final String TAG = "HMBTCoreInterface";
    Manager manager;

    BTCoreInterface(Manager manager) {
        this.manager = manager;
    }

    @Override
    public int HMBTHalInit() {
        return 0;
    }

    @Override
    public int HMBTHalScanStart() {
        // ignored, controlled by the user
        return 0;
    }

    @Override
    public int HMBTHalScanStop() {
        // ignored, controlled by the user
        return 0;
    }

    @Override
    public int HMBTHalAdvertisementStart(byte[] issuer, byte[] appID) {
        // ignored, controlled by the user
        manager.getBroadcaster().issuer = issuer;
        manager.getBroadcaster().appId = appID;
        return 0;
    }

    @Override
    public int HMBTHalAdvertisementStop() {
        // ignored, controlled by the user
        return 0;
    }

    @Override
    public int HMBTHalConnect(byte[] mac) {
        manager.getScanner().connect(mac);
        return 0;
    }

    @Override
    public int HMBTHalDisconnect(byte[] mac) {
        Log.d(TAG, new Object(){}.getClass().getEnclosingMethod().getName());
        manager.getScanner().disconnect(mac);
        return 0;
    }

    @Override
    public int HMBTHalServiceDiscovery(byte[] mac) {
        manager.getScanner().startServiceDiscovery(mac);
        return 0;
    }

    @Override
    public int HMBTHalWriteData(byte[] mac, int length, byte[] data, int characteristic) {
        if (manager.getBroadcaster().writeData(mac, data, characteristic) == false) {
            if (manager.getScanner().writeData(mac, data, characteristic) == false) {
                return 1;
            }
        }

        return 0;
    }

    @Override
    public int HMBTHalReadData(byte[] mac, int offset, int characteristic) {
        return manager.getScanner().readValue(mac, characteristic) == true ? 0 : 1;
    }

    @Override
    public int HMBTHalTelematicsSendData(byte[] issuer, byte[] serial, int length, byte[] data) {
        manager.telematics.onTelematicsCommandEncrypted(serial, issuer, data);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetSerial(byte[] serial) {
        copyBytes(manager.getDeviceCertificate().getSerial(), serial);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetLocalPublicKey(byte[] publicKey) {
        copyBytes(manager.getDeviceCertificate().getPublicKey(), publicKey);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetLocalPrivateKey(byte[] privateKey) {
        copyBytes(manager.privateKey, privateKey);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetDeviceCertificate(byte[] cert) {
        copyBytes(manager.getDeviceCertificate().getBytes(), cert);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetCaPublicKey(byte[] publicKey) {
        copyBytes(manager.caPublicKey, publicKey);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetOEMCaPublicKey(byte[] publicKey) {
        copyBytes(manager.caPublicKey, publicKey);
        return 0;
    }

    @Override
    public int HMPersistenceHaladdPublicKey(byte[] serial, byte[] cert, int size) {
        AccessCertificate certificate = new AccessCertificate(trimmedBytes(cert, size));

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(TAG, "HMPersistenceHaladdPublicKey: " + Bytes.hexFromBytes(serial));


        int errorCode = manager.storage.storeCertificate(certificate).getValue();
        if (errorCode != 0) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "Cant register certificate " + Bytes.hexFromBytes(serial) + ": " + errorCode);
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(TAG, "HMPersistenceHaladdPublicKey: " + Bytes.hexFromBytes(serial));

        return 0;
    }

    @Override
    public int HMPersistenceHalgetPublicKey(byte[] serial, byte[] cert, int[] size) {
        AccessCertificate certificate = manager.storage.certWithGainingSerial(serial);

        if (certificate == null) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "No registered cert with gaining serial " + Bytes.hexFromBytes(serial));
            return 1;
        }

        copyBytes(certificate.getBytes(), cert);
        size[0] = certificate.getBytes().length;

        return 0;
    }

    @Override
    public int HMPersistenceHalgetPublicKeyByIndex(int index, byte[] cert, int[] size) {
        AccessCertificate[] certificates = manager.storage.getCertificatesWithProvidingSerial(manager.getDeviceCertificate().getSerial());

        if (certificates.length >= index) {
            AccessCertificate certificate = certificates[index];
            copyBytes(certificate.getBytes(), cert);
            size[0] = certificate.getBytes().length;
            return 0;
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "No registered cert for index " + index);

        return 1;
    }

    @Override
    public int HMPersistenceHalgetPublicKeyCount(int[] count) {
        int certCount = manager.storage.getCertificatesWithProvidingSerial(manager.getDeviceCertificate().getSerial()).length;
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(TAG, "HMPersistenceHalgetPublicKeyCount " + certCount);
        count[0] = certCount;
        return 0;
    }

    @Override
    public int HMPersistenceHalremovePublicKey(byte[] serial) {
        if (manager.storage.deleteCertificateWithGainingSerial(serial)){
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "HMPersistenceHalremovePublicKey success");

            return 0;
        }
        else {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "HMPersistenceHalremovePublicKey failure");

            return 1;
        }
    }

    @Override
    public int HMPersistenceHaladdStoredCertificate(byte[] cert, int size) {
        AccessCertificate certificate = new AccessCertificate(cert);

        int errorCode = manager.storage.storeCertificate(certificate).getValue();
        if (errorCode != 0) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "Cant store certificate: " + errorCode);
        }
        else {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "HMPersistenceHaladdStoredCertificate " + Bytes.hexFromBytes(certificate.getGainerSerial()) + " success");
        }

        return 0;
    }

    @Override
    public int HMPersistenceHalgetStoredCertificate(byte[] serial, byte[] cert, int[] size) {
        AccessCertificate[] storedCerts = manager.storage.getCertificatesWithoutProvidingSerial(manager.getDeviceCertificate().getSerial());

        for (AccessCertificate storedCert : storedCerts) {
            if (Arrays.equals(storedCert.getProviderSerial(), serial)) {
                copyBytes(storedCert.getBytes(), cert);
                size[0] = storedCert.getBytes().length;
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                    Log.d(Broadcaster.TAG, "Returned stored cert for serial " + Bytes.hexFromBytes(serial));
                return 0;
            }
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(Broadcaster.TAG, "No stored cert for serial " + Bytes.hexFromBytes(serial));

        return 1;
    }

    @Override
    public int HMPersistenceHaleraseStoredCertificate(byte[] serial) {
        AccessCertificate[] storedCerts = manager.storage.getCertificatesWithoutProvidingSerial(manager.getDeviceCertificate().getSerial());

        for (AccessCertificate cert : storedCerts) {
            if (Arrays.equals(cert.getProviderSerial(), serial)) {
                if (manager.storage.deleteCertificate(cert.getGainerSerial(), cert.getProviderSerial())) {
                    if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                        Log.d(Broadcaster.TAG, "Erased stored cert for serial " + Bytes.hexFromBytes(serial));

                    return 0;
                }
                else {
                    if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                        Log.d(Broadcaster.TAG, "Could not erase cert for serial " + Bytes.hexFromBytes(serial));
                    return 1;
                }
            }
        }
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(Broadcaster.TAG, "No cert to erase for serial " + Bytes.hexFromBytes(serial));

        return 1;
    }

    @Override
    public void HMApiCallbackEnteredProximity(HMDevice device) {
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(Broadcaster.TAG, "HMCtwEnteredProximity");

        // this means core has finished identification of the broadcaster (might me authenticated or not) - show broadcaster info on screen
        // always update the broadcaster with this, auth state might have changed later with this callback as well
        if (manager.getBroadcaster().didResolveDevice(device) == false) {
            manager.getScanner().didResolveDevice(device);
        }
    }

    @Override
    public void HMApiCallbackExitedProximity(HMDevice device) {
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(Broadcaster.TAG, "HMCtwExitedProximity");

        if (manager.getBroadcaster().deviceExitedProximity(device) == false) {
            manager.getScanner().deviceExitedProximity(device.getMac());
        }
    }

    @Override
    public void HMApiCallbackCustomCommandIncoming(HMDevice device, byte[] data, int length) {
        if (manager.getBroadcaster().onCommandReceived(device, trimmedBytes(data, length)) == false) {
            manager.getScanner().onCommandReceived(device, trimmedBytes(data, length));
        }
    }

    @Override
    public void HMApiCallbackCustomCommandResponse(HMDevice device, byte[] data, int length) {
        byte[] trimmedBytes = trimmedBytes(data, length);
        if (manager.getBroadcaster().onCommandResponseReceived(device, trimmedBytes) == false) {
            manager.getScanner().onCommandResponseReceived(device, trimmedBytes);
        }
    }

    @Override
    public int HMApiCallbackGetDeviceCertificateFailed(HMDevice device, byte[] nonce) {
        Log.d(TAG, "HMApiCallbackGetDeviceCertificateFailed ");
        // should ask the CA for the signature for the nonce
        // return false getting the sig start failed
        // return true started acquiring signature
        return 0;
    }

    @Override
    public int HMApiCallbackPairingRequested(HMDevice device) {
        return manager.getBroadcaster().didReceivePairingRequest(device);
    }

    @Override
    public void HMApiCallbackTelematicsCommandIncoming(HMDevice device, int id, int length, byte[] data) {
        manager.telematics.onTelematicsResponseDecrypted(device.getSerial(), (byte)id, trimmedBytes(data, length));
    }

    @Override
    public void HMCryptoHalGenerateNonce(byte[] nonce) {
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);
    }

    void copyBytes(byte[] from, byte[] to) {
        for (int i = 0; i < from.length; i++) {
            to[i] = from[i];
        }
    }

    byte[] trimmedBytes(byte[] bytes, int length) {
        if (bytes.length == length) return bytes;

        byte[] trimmedBytes = new byte[length];

        for (int i = 0; i < length; i++) {
            trimmedBytes[i] = bytes[i];
        }

        return trimmedBytes;
    }
}



