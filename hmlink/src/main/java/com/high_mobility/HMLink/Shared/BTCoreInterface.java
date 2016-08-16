package com.high_mobility.HMLink.Shared;

import android.util.Log;

import com.high_mobility.HMLink.AccessCertificate;
import com.high_mobility.HMLink.Crypto;
import com.high_mobility.HMLink.LinkException;
import com.high_mobility.btcore.HMBTCoreInterface;
import com.high_mobility.btcore.HMDevice;

import java.util.Arrays;

/**
 * Created by ttiganik on 03/08/16.
 */
class BTCoreInterface implements HMBTCoreInterface {
    Shared shared;
    BTCoreInterface(Shared shared) {
        this.shared = shared;
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
        shared.getExternalDeviceManager().connect(mac); // TODO: what if fails?
        return 0;
    }

    @Override
    public int HMBTHalDisconnect(byte[] mac) {
        Log.d(ExternalDeviceManager.TAG, new Object(){}.getClass().getEnclosingMethod().getName());
        shared.getExternalDeviceManager().disconnect(mac);
        return 0;
    }

    @Override
    public int HMBTHalServiceDiscovery(byte[] mac) {
        shared.getExternalDeviceManager().startServiceDiscovery(mac);
        return 0;
    }

    @Override
    public int HMBTHalWriteData(byte[] mac, int length, byte[] data) {
        Link link = shared.getLocalDevice().getLinkForMac(mac);
        if (link != null) {
            shared.getLocalDevice().writeData(link, data);
        }
        else {
            ExternalDevice device = shared.getExternalDeviceManager().getDeviceForMac(mac);
            if (device == null) return 1;
            device.writeValue(data);
        }

        return 0;
    }

    @Override
    public int HMBTHalReadData(byte[] mac, int offset) {
        ExternalDevice device = shared.getExternalDeviceManager().getDeviceForMac(mac);
        if (device == null) return 1;
        device.readValue();
        return 0;
    }

    @Override
    public int HMPersistenceHalgetSerial(byte[] serial) {
        copyBytesToJNI(shared.getLocalDevice().getCertificate().getSerial(), serial);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetLocalPublicKey(byte[] publicKey) {
        copyBytesToJNI(shared.getLocalDevice().getCertificate().getPublicKey(), publicKey);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetLocalPrivateKey(byte[] privateKey) {
        copyBytesToJNI(shared.getLocalDevice().privateKey, privateKey);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetDeviceCertificate(byte[] cert) {
        copyBytesToJNI(shared.getLocalDevice().getCertificate().getBytes(), cert);
        return 0;
    }

    @Override
    public int HMPersistenceHaladdPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int commandSize, byte[] command) {
        AccessCertificate cert = new AccessCertificate(serial, publicKey, shared.getLocalDevice().getCertificate().getSerial(), startDate, endDate, command);

        try {
            shared.getLocalDevice().storage.storeCertificate(cert);
            if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
                Log.d(ExternalDeviceManager.TAG, "Cant store certificate.");
        } catch (LinkException e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    @Override
    public int HMPersistenceHalgetPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int[] commandSize, byte[] command) {
        AccessCertificate certificate = shared.getLocalDevice().storage.certWithGainingSerial(serial);
        if (certificate == null) {
            if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
                Log.d(ExternalDeviceManager.TAG, "No cert with gaining serial " + ByteUtils.hexFromBytes(serial));
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
        AccessCertificate[] certificates = shared.getLocalDevice().storage.getCertificatesWithProvidingSerial(shared.getLocalDevice().getCertificate().getSerial());

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

        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
            Log.d(ExternalDeviceManager.TAG, "No cert for index " + index);

        return 1;
    }

    @Override
    public int HMPersistenceHalgetPublicKeyCount(int[] count) {
        count[0] = shared.getLocalDevice().storage.getCertificatesWithProvidingSerial(shared.getLocalDevice().getCertificate().getSerial()).length;
        return 0;
    }

    @Override
    public int HMPersistenceHalremovePublicKey(byte[] serial) {
        if (shared.getLocalDevice().storage.deleteCertificateWithGainingSerial(serial)) return 0;
        else return 1;
    }

    @Override
    public int HMPersistenceHaladdStoredCertificate(byte[] cert, int size) {
        AccessCertificate certificate = new AccessCertificate(cert);

        try {
            shared.getLocalDevice().storage.storeCertificate(certificate);
            if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
                Log.d(ExternalDeviceManager.TAG, "Cant store certificate.");
        } catch (LinkException e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    @Override
    public int HMPersistenceHalgetStoredCertificate(byte[] serial, byte[] cert, int[] size) {
        AccessCertificate[] storedCerts = shared.getLocalDevice().storage.getCertificatesWithoutProvidingSerial(shared.getLocalDevice().getCertificate().getSerial());

        for (AccessCertificate storedCert : storedCerts) {
            if (Arrays.equals(storedCert.getProviderSerial(), serial)) {
                copyBytesToJNI(storedCert.getBytes(), cert);
                size[0] = storedCert.getBytes().length;
                if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
                    Log.d(LocalDevice.TAG, "Returned stored cert for serial " + ByteUtils.hexFromBytes(serial));
                return 0;
            }
        }

        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
            Log.d(LocalDevice.TAG, "No stored cert for serial " + ByteUtils.hexFromBytes(serial));

        return 1;
    }

    @Override
    public int HMPersistenceHaleraseStoredCertificate(byte[] serial) {
        AccessCertificate[] storedCerts = shared.getLocalDevice().storage.getCertificatesWithoutProvidingSerial(shared.getLocalDevice().getCertificate().getSerial());

        for (AccessCertificate cert : storedCerts) {
            if (Arrays.equals(cert.getProviderSerial(), serial)) {
                if (shared.getLocalDevice().storage.deleteCertificate(cert)) {
                    if (Device.loggingLevel.getValue() >= Device.LoggingLevel.All.getValue())
                        Log.d(LocalDevice.TAG, "Erased stored cert for serial " + ByteUtils.hexFromBytes(serial));

                    return 0;
                }
                else {
                    if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
                        Log.d(LocalDevice.TAG, "Could not erase cert for serial " + ByteUtils.hexFromBytes(serial));
                    return 1;
                }
            }
        }
        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
            Log.d(LocalDevice.TAG, "No cert to erase for serial " + ByteUtils.hexFromBytes(serial));

        return 1;
    }

    @Override
    public void HMApiCallbackEnteredProximity(HMDevice device) {
        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.All.getValue())
            Log.d(LocalDevice.TAG, "HMCtwEnteredProximity");

        // this means core has finished identification of the device (might me authenticated or not) - show device info on screen
        // always update the device with this, auth state might have changed later with this callback as well
        boolean scanningDevice = shared.getExternalDeviceManager().isAuthenticating(device.getMac());
        if (scanningDevice) {
            shared.getExternalDeviceManager().didAuthenticateDevice(device);
        }
        else {
            shared.getLocalDevice().didResolveDevice(device);
        }
    }

    @Override
    public void HMApiCallbackExitedProximity(HMDevice device) {
        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.All.getValue())
            Log.d(LocalDevice.TAG, "HMCtwExitedProximity");

        if (shared.getLocalDevice().didLoseLink(device) == false) {
            shared.getExternalDeviceManager().deviceExitedProximity(device.getMac());
        }
    }

    @Override
    public void HMApiCallbackCustomCommandIncoming(HMDevice device, byte[] data, int[] length, int[] error) {
        byte[] response = shared.getLocalDevice().onCommandReceived(device, trimmedBytes(data, length[0]));

        if (response == null)
            response = shared.getExternalDeviceManager().onCommandReceived(device, trimmedBytes(data, length[0]));

        if (response != null) {
            copyBytesToJNI(response, data);
            length[0] = response.length;
            error[0] = 0;
        }
        else {
            length[0] = 0;
            error[0] = 0;
        }
    }

    @Override
    public void HMApiCallbackCustomCommandResponse(HMDevice device, byte[] data, int length) {
        shared.getLocalDevice().onCommandResponseReceived(device, trimmedBytes(data, length));
    }

    @Override
    public int HMApiCallbackGetDeviceCertificateFailed(HMDevice device, byte[] nonce) {
        Log.d(ExternalDeviceManager.TAG, "HMApiCallbackGetDeviceCertificateFailed ");
        // Sensing: should ask for CA sig for the nonce
        // if ret false getting the sig start failed
        // if ret true started acquiring signature

        byte[] CaPrivKey = new byte[] {0x1B, (byte)0x85, (byte)0x93, (byte)0xD0, 0x47, (byte)0x8B, (byte)0x90, 0x17, (byte)0xC2, 0x42, 0x72, 0x56, (byte)0xAA, (byte)0xEE, 0x25, (byte)0xFF, (byte)0x8A, 0x4E, 0x20, (byte)0xEC, 0x66, 0x11, (byte)0xAF, (byte)0xE3, 0x1D, 0x52, (byte)0xB3, 0x2C, (byte)0xE0, (byte)0xBE, (byte)0xCC, (byte)0xA2};
        byte[] signature = Crypto.sign(nonce, CaPrivKey);

        shared.core.HMBTCoreSendReadDeviceCertificate(shared.coreInterface, device.getMac(), nonce, signature);
        return 1;
    }

    @Override
    public int HMApiCallbackPairingRequested(HMDevice device) {
        return shared.getLocalDevice().didReceivePairingRequest(device);
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
