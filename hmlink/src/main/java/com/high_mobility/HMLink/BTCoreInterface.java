package com.high_mobility.HMLink;

import android.util.Log;

import com.high_mobility.HMLink.Crypto.AccessCertificate;
import com.high_mobility.HMLink.Crypto.Crypto;
import com.high_mobility.btcore.HMBTCoreInterface;
import com.high_mobility.btcore.HMDevice;

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
    public int HMBTHalWriteData(byte[] mac, int length, byte[] data) {
        if (manager.getBroadcaster().writeData(mac, data) == false) {
            if (manager.getScanner().writeData(mac, data) == false) {
                return 1;
            }
        }

        return 0;
    }

    @Override
    public int HMBTHalReadData(byte[] mac, int offset) {
        return manager.getScanner().readValue(mac) == true ? 0 : 1;
    }

    @Override
    public int HMPersistenceHalgetSerial(byte[] serial) {
        copyBytesToJNI(manager.getCertificate().getSerial(), serial);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetLocalPublicKey(byte[] publicKey) {
        copyBytesToJNI(manager.getCertificate().getPublicKey(), publicKey);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetLocalPrivateKey(byte[] privateKey) {
        copyBytesToJNI(manager.privateKey, privateKey);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetDeviceCertificate(byte[] cert) {
        copyBytesToJNI(manager.getCertificate().getBytes(), cert);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetCaPublicKey(byte[] publicKey) {
        //TODO;
        return 0;
    }

    @Override
    public int HMPersistenceHaladdPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int commandSize, byte[] command) {
        AccessCertificate cert = new AccessCertificate(serial, publicKey, manager.getCertificate().getSerial(), startDate, endDate, command);

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(TAG, "HMPersistenceHaladdPublicKey: " + ByteUtils.hexFromBytes(serial));


            int errorCode = manager.getBroadcaster().storage.storeCertificate(cert);
            if (errorCode != 0) {
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "Cant register certificate: " + errorCode);
            }

        return 0;
    }

    @Override
    public int HMPersistenceHalgetPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int[] commandSize, byte[] command) {
        AccessCertificate certificate = manager.getBroadcaster().storage.certWithGainingSerial(serial);

        if (certificate == null) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "No registered cert with gaining serial " + ByteUtils.hexFromBytes(serial));
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
        AccessCertificate[] certificates = manager.getBroadcaster().storage.getCertificatesWithProvidingSerial(manager.getCertificate().getSerial());

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

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "No registered cert for index " + index);

        return 1;
    }

    @Override
    public int HMPersistenceHalgetPublicKeyCount(int[] count) {
        int certCount = manager.getBroadcaster().storage.getCertificatesWithProvidingSerial(manager.getCertificate().getSerial()).length;
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
            Log.d(TAG, "HMPersistenceHalgetPublicKeyCount " + certCount);
        count[0] = certCount;
        return 0;
    }

    @Override
    public int HMPersistenceHalremovePublicKey(byte[] serial) {
        if (manager.getBroadcaster().storage.deleteCertificateWithGainingSerial(serial)){
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

        int errorCode = manager.getBroadcaster().storage.storeCertificate(certificate);
        if (errorCode != 0) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "Cant store certificate: " + errorCode);
        }
        else {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "HMPersistenceHaladdStoredCertificate " + ByteUtils.hexFromBytes(certificate.getGainerSerial()) + " success");
        }

        return 0;
    }

    @Override
    public int HMPersistenceHalgetStoredCertificate(byte[] serial, byte[] cert, int[] size) {
        AccessCertificate[] storedCerts = manager.getBroadcaster().storage.getCertificatesWithoutProvidingSerial(manager.getCertificate().getSerial());

        for (AccessCertificate storedCert : storedCerts) {
            if (Arrays.equals(storedCert.getProviderSerial(), serial)) {
                copyBytesToJNI(storedCert.getBytes(), cert);
                size[0] = storedCert.getBytes().length;
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                    Log.d(Broadcaster.TAG, "Returned stored cert for serial " + ByteUtils.hexFromBytes(serial));
                return 0;
            }
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(Broadcaster.TAG, "No stored cert for serial " + ByteUtils.hexFromBytes(serial));

        return 1;
    }

    @Override
    public int HMPersistenceHaleraseStoredCertificate(byte[] serial) {
        AccessCertificate[] storedCerts = manager.getBroadcaster().storage.getCertificatesWithoutProvidingSerial(manager.getCertificate().getSerial());

        for (AccessCertificate cert : storedCerts) {
            if (Arrays.equals(cert.getProviderSerial(), serial)) {
                if (manager.getBroadcaster().storage.deleteCertificate(cert)) {
                    if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                        Log.d(Broadcaster.TAG, "Erased stored cert for serial " + ByteUtils.hexFromBytes(serial));

                    return 0;
                }
                else {
                    if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                        Log.d(Broadcaster.TAG, "Could not erase cert for serial " + ByteUtils.hexFromBytes(serial));
                    return 1;
                }
            }
        }
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(Broadcaster.TAG, "No cert to erase for serial " + ByteUtils.hexFromBytes(serial));

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
    public void HMApiCallbackCustomCommandIncoming(HMDevice device, byte[] data, int[] length, int[] error) {
        if (manager.getBroadcaster().onCommandReceived(device, trimmedBytes(data, length[0])) == false) {
            manager.getScanner().onCommandReceived(device, trimmedBytes(data, length[0]));
        }

        length[0] = 0;
        error[0] = 0;
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
        // Sensing: should ask for CA sig for the nonce
        // if ret false getting the sig start failed
        // if ret true started acquiring signature

        byte[] CaPrivKey = new byte[] {0x1B, (byte)0x85, (byte)0x93, (byte)0xD0, 0x47, (byte)0x8B, (byte)0x90, 0x17, (byte)0xC2, 0x42, 0x72, 0x56, (byte)0xAA, (byte)0xEE, 0x25, (byte)0xFF, (byte)0x8A, 0x4E, 0x20, (byte)0xEC, 0x66, 0x11, (byte)0xAF, (byte)0xE3, 0x1D, 0x52, (byte)0xB3, 0x2C, (byte)0xE0, (byte)0xBE, (byte)0xCC, (byte)0xA2};
        byte[] signature = Crypto.sign(nonce, CaPrivKey);

        manager.core.HMBTCoreSendReadDeviceCertificate(manager.coreInterface, device.getMac(), nonce, signature);
        return 1;
    }

    @Override
    public int HMApiCallbackPairingRequested(HMDevice device) {
        return manager.getBroadcaster().didReceivePairingRequest(device);
    }

    void copyBytesToJNI(byte[] from, byte[] to) {
        for (int i = 0; i < from.length; i++) {
            to[i] = from[i];
        }
    }

    byte[] trimmedBytes(byte[] bytes, int length) {
        byte[] trimmedBytes = new byte[length];

        for (int i = 0; i < length; i++) {
            trimmedBytes[i] = bytes[i];
        }

        return trimmedBytes;
    }
}
