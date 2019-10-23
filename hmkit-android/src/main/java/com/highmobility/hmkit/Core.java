package com.highmobility.hmkit;

import com.highmobility.btcore.HMBTCore;
import com.highmobility.btcore.HMBTCoreInterface;
import com.highmobility.btcore.HMDevice;
import com.highmobility.crypto.AccessCertificate;
import com.highmobility.crypto.DeviceCertificate;
import com.highmobility.crypto.value.PrivateKey;
import com.highmobility.crypto.value.PublicKey;
import com.highmobility.utils.ByteUtils;
import com.highmobility.value.Bytes;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

import static com.highmobility.hmkit.HMLog.d;
import static com.highmobility.hmkit.HMLog.e;
import static com.highmobility.hmkit.HMLog.w;

/**
 * Interface to the C core. Keeps a reference to the device certificate as well because core only
 * handles one device certificate.
 */
class Core implements HMBTCoreInterface {
    private final HMBTCore core = new HMBTCore();
    private final Storage storage;
    private final ThreadManager threadManager;

    private Timer coreClockTimer;

    private DeviceCertificate deviceCertificate;
    private PrivateKey privateKey;
    private PublicKey caPublicKey;

    // 3x listeners. telematics, broadcaster and scanner
    @Nullable public Telematics telematics;
    @Nullable public Broadcaster broadcaster;
    @Nullable public Scanner scanner;

    private byte[] issuer, appId; // these are set from HMBTCoreInterface HMBTHalAdvertisementStart.

    DeviceCertificate getDeviceCertificate() {
        return deviceCertificate;
    }

    PrivateKey getPrivateKey() {
        return privateKey;
    }

    PublicKey getCaPublicKey() {
        return caPublicKey;
    }

    byte[] getIssuer() {
        return issuer;
    }

    byte[] getAppId() {
        return appId;
    }

    Core(Storage storage, ThreadManager threadManager, DeviceCertificate
            deviceCertificate, PrivateKey privateKey, PublicKey issuerPublicKey, HMLog.Level logLevel) {
        setDeviceCertificate(deviceCertificate, privateKey, issuerPublicKey);
        this.storage = storage;
        this.threadManager = threadManager;

        core.HMBTCoreSetLogLevel(this, logLevel.getValue());
        // core init needs to be done once, only initialises structs(but requires device cert)
        core.HMBTCoreInit(this);
    }

    void setDeviceCertificate(DeviceCertificate deviceCertificate, PrivateKey privateKey,
                              PublicKey issuerPublicKey) {
        this.deviceCertificate = deviceCertificate;
        this.privateKey = privateKey;
        this.caPublicKey = issuerPublicKey;
    }

    /**
     * Start core clock.
     */
    void start() {
        // start the core clock if is not running already
        if (coreClockTimer == null) {
            coreClockTimer = new Timer();
            coreClockTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    threadManager.postToWork(new Runnable() {
                        @Override
                        public void run() {
                            core.HMBTCoreClock(Core.this);
                        }
                    });
                }
            }, 0, 1000);
        }
    }

    /**
     * Stop core clock.
     */
    void stop() {
        if (coreClockTimer != null) {
            coreClockTimer.cancel();
            coreClockTimer = null;
        }
    }

    // MARK: sensing

    void HMBTCoreSensingReadNotification(byte[] mac, int characteristic) {
        core.HMBTCoreSensingReadNotification(this, mac, characteristic);
    }

    void HMBTCoreSensingReadResponse(byte[] data, int size, int offset, byte[] mac, int
            characteristic) {
        core.HMBTCoreSensingReadResponse(this, data, size, offset, mac, characteristic);
    }

    void HMBTCoreSensingWriteResponse(byte[] mac, int characteristic) {
        core.HMBTCoreSensingWriteResponse(this, mac, characteristic);
    }

    void HMBTCoreSensingPingNotification(byte[] mac, int characteristic) {
        core.HMBTCoreSensingPingNotification(this, mac, characteristic);
    }

    void HMBTCoreSensingProcessAdvertisement(byte[] mac, byte[] data, int size) {
        core.HMBTCoreSensingProcessAdvertisement(this, mac, data, size);
    }

    void HMBTCoreSensingDiscoveryEvent(byte[] mac) {
        core.HMBTCoreSensingDiscoveryEvent(this, mac);
    }

    void HMBTCoreSensingScanStart() {
        core.HMBTCoreSensingScanStart(this);
    }

    void HMBTCoreSensingConnect(byte[] mac) {
        core.HMBTCoreSensingConnect(this, mac);
    }

    void HMBTCoreSensingDisconnect(byte[] mac) {
        core.HMBTCoreSensingDisconnect(this, mac);
    }

    // MARK: link

    //Initialize link object in core
    void HMBTCorelinkConnect(byte[] mac) {
        core.HMBTCorelinkConnect(this, mac);
    }

    //Delete link object in core
    void HMBTCorelinkDisconnect(byte[] mac) {
        core.HMBTCorelinkDisconnect(this, mac);
    }

    //Forward link incoming data to core
    void HMBTCorelinkIncomingData(byte[] data, int size, byte[] mac, int characteristic) {
        core.HMBTCorelinkIncomingData(this, data, size, mac, characteristic);
    }

    void HMBTCorelinkWriteResponse(byte[] mac, int characteristic) {
        core.HMBTCorelinkWriteResponse(this, mac, characteristic);
    }

    void HMBTCoreSendCustomCommand(byte[] data, int size, byte[] mac) {
        core.HMBTCoreSendCustomCommand(this, data, size, mac);
    }

    void HMBTCoreSendReadDeviceCertificate(byte[] mac, byte[] nonce, byte[] caSignature) {
        core.HMBTCoreSendReadDeviceCertificate(this, mac, nonce, caSignature);
    }

    void HMBTCoreSendRegisterAccessCertificate(byte[] certificate) {
        core.HMBTCoreSendRegisterAccessCertificate(this, certificate);
    }

    // MARK: crypto
    void HMBTCoreCryptoCreateKeys(byte[] privateKey, byte[] publicKey) {
        core.HMBTCoreCryptoCreateKeys(privateKey, publicKey);
    }

    void HMBTCoreCryptoAddSignature(byte[] data, int size, byte[] privateKey, byte[] signature) {
        core.HMBTCoreCryptoAddSignature(data, size, privateKey, signature);
    }

    int HMBTCoreCryptoValidateSignature(byte[] data, int size, byte[] pubKey, byte[] signature) {
        return core.HMBTCoreCryptoValidateSignature(data, size, pubKey, signature);
    }

    // MARK: telematics

    void HMBTCoreTelematicsReceiveData(int length, byte[] data) {
        core.HMBTCoreTelematicsReceiveData(this, length, data);
    }

    void HMBTCoreSendTelematicsCommand(byte[] serial, byte[] nonce, int length, byte[] data) {
        core.HMBTCoreSendTelematicsCommand(this, serial, nonce, length, data);
    }

    // MARK: other

    void HMBTCoreSendRevoke(byte[] serial) {
        core.HMBTCoreSendRevoke(this, serial);
    }

    void HMBTCoreSetLogLevel(int level) {
        core.HMBTCoreSetLogLevel(this, level);
    }

    void HMBTCoreSetMTU(byte[] mac, int mtu) {
        // TODO: 18/10/2019 core should probably accept int not byte[]
        core.HMBTCoreSetMTU(this, mac, new byte[]{(byte) mtu});
    }

    // MARK: HMBTCoreInterface

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
        // Called straight after init. Has issuer and app id parsed from the device cert.
        // advertise will not be started because it is controlled by the user.
        this.issuer = issuer;
        this.appId = appID;
        return 0;
    }

    @Override
    public int HMBTHalAdvertisementStop() {
        // ignored, controlled by the user
        return 0;
    }

    @Override
    public int HMBTHalConnect(byte[] mac) {
        if (scanner != null) scanner.connect(mac);
        return 0;
    }

    @Override
    public int HMBTHalDisconnect(byte[] mac) {
        if (scanner != null) scanner.disconnect(mac);
        return 0;
    }

    @Override
    public int HMBTHalServiceDiscovery(byte[] mac) {
        if (scanner != null) scanner.startServiceDiscovery(mac);
        return 0;
    }

    @Override
    public int HMBTHalWriteData(byte[] mac, int length, byte[] data, int characteristic) {
        if (broadcaster != null && broadcaster.writeData(mac, data, characteristic))
            return 1;

        if (scanner != null && scanner.writeData(mac, data, characteristic))
            return 1;

        return 0;
    }

    @Override
    public int HMBTHalReadData(byte[] mac, int offset, int characteristic) {
        if (scanner != null)
            return scanner.readValue(mac, characteristic) == true ? 0 : 1;

        return 0;
    }

    @Override
    public int HMBTHalTelematicsSendData(byte[] issuer, byte[] serial, int length, byte[] data) {
        if (telematics != null)
            telematics.onTelematicsCommandEncrypted(serial, issuer, trimmedBytes(data, length));

        return 0;
    }

    @Override
    public int HMPersistenceHalgetSerial(byte[] serial) {
        copyBytes(getDeviceCertificate().getSerial(), serial);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetLocalPublicKey(byte[] publicKey) {
        copyBytes(this.deviceCertificate.getPublicKey(), publicKey);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetLocalPrivateKey(byte[] privateKey) {
        copyBytes(this.privateKey, privateKey);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetDeviceCertificate(byte[] cert) {
        copyBytes(this.deviceCertificate.getBytes(), cert);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetCaPublicKey(byte[] publicKey) {
        copyBytes(this.caPublicKey, publicKey);
        return 0;
    }

    @Override
    public int HMPersistenceHalgetOEMCaPublicKey(byte[] publicKey) {
        copyBytes(this.caPublicKey, publicKey);
        return 0;
    }

    @Override
    public int HMPersistenceHaladdPublicKey(byte[] serial, byte[] cert, int size) {
        AccessCertificate certificate = new AccessCertificate(new Bytes(trimmedBytes(cert, size)));

        d("HMPersistenceHaladdPublicKey: %s", ByteUtils.hexFromBytes(serial));

        int errorCode = storage.storeCertificate(certificate).getValue();
        if (errorCode != 0) {
            e("Cant register certificate %s: %d", ByteUtils.hexFromBytes(serial), errorCode);
        }

        return 0;
    }

    @Override
    public int HMPersistenceHalgetPublicKey(byte[] serial, byte[] cert, int[] size) {
        AccessCertificate certificate = storage.certWithGainingSerial(serial);

        if (certificate == null) {
            w("No registered cert with gaining serial %s", ByteUtils.hexFromBytes(serial));
            return 1;
        }

        copyBytes(certificate.getBytes(), cert);
        size[0] = certificate.getBytes().getLength();

        return 0;
    }

    @Override
    public int HMPersistenceHalgetPublicKeyByIndex(int index, byte[] cert, int[] size) {
        AccessCertificate[] certificates = storage.getCertificatesWithProvidingSerial
                (getDeviceCertificate().getSerial().getByteArray());

        if (certificates.length >= index) {
            AccessCertificate certificate = certificates[index];
            copyBytes(certificate.getBytes(), cert);
            size[0] = certificate.getBytes().getLength();
            return 0;
        }

        w("No registered cert for index %d", index);

        return 1;
    }

    @Override
    public int HMPersistenceHalgetPublicKeyCount(int[] count) {
        int certCount = storage.getCertificatesWithProvidingSerial(getDeviceCertificate()
                .getSerial().getByteArray()).length;

        d("HMPersistenceHalgetPublicKeyCount %d", +certCount);

        count[0] = certCount;
        return 0;
    }

    @Override
    public int HMPersistenceHalremovePublicKey(byte[] serial) {
        if (storage.deleteCertificate(serial, null)) {
            d("HMPersistenceHalremovePublicKey success: %s", ByteUtils.hexFromBytes(serial));
            return 0;
        } else {
            d("HMPersistenceHalremovePublicKey failure: %s", ByteUtils.hexFromBytes(serial));
            return 1;
        }
    }

    @Override
    public int HMPersistenceHaladdStoredCertificate(byte[] cert, int size) {
        AccessCertificate certificate = new AccessCertificate(new Bytes(cert));

        int errorCode = storage.storeCertificate(certificate).getValue();
        if (errorCode != 0) {
            e("Cant store certificate: %d", errorCode);
        } else {
            d("HMPersistenceHaladdStoredCertificate %s success", certificate.getGainerSerial());
        }

        return 0;
    }

    @Override
    public int HMPersistenceHalgetStoredCertificate(byte[] serial, byte[] cert, int[] size) {
        AccessCertificate[] storedCerts = storage.getCertificatesWithoutProvidingSerial
                (getDeviceCertificate().getSerial().getByteArray());

        for (AccessCertificate storedCert : storedCerts) {
            if (storedCert.getProviderSerial().equals(serial)) {
                copyBytes(storedCert.getBytes(), cert);
                size[0] = storedCert.getBytes().getLength();
                d("Returned stored cert for serial %s", ByteUtils.hexFromBytes(serial));
                return 0;
            }
        }

        d("No stored cert for serial %s", ByteUtils.hexFromBytes(serial));
        return 1;
    }

    @Override
    public int HMPersistenceHaleraseStoredCertificate(byte[] serial) {
        AccessCertificate[] storedCerts = storage.getCertificatesWithoutProvidingSerial
                (getDeviceCertificate().getSerial().getByteArray());

        for (AccessCertificate cert : storedCerts) {
            if (cert.getProviderSerial().equals(serial)) {
                if (storage.deleteCertificate(cert.getGainerSerial().getByteArray(),
                        cert.getProviderSerial().getByteArray())) {
                    d("Erased stored cert: %s", ByteUtils.hexFromBytes(serial));
                    return 0;
                } else {
                    d("Could not erase cert: %s", ByteUtils.hexFromBytes(serial));
                    return 1;
                }
            }
        }

        d("No cert to erase: %s", ByteUtils.hexFromBytes(serial));

        return 1;
    }

    @Override
    public void HMApiCallbackEnteredProximity(HMDevice device) {
        d("HMApiCallbackEnteredProximity. authenticated: %d", device.getIsAuthenticated());

        // core has finished identification of the link: its now authenticated or the
        // authentication failed.
        // also, auth state might change from authenticated > connected with this call as well
        if (broadcaster != null && broadcaster.onChangedAuthenticationState(device)) return;
        if (scanner != null) scanner.onChangedAuthenticationState(device);
    }

    @Override
    public void HMApiCallbackExitedProximity(HMDevice device) {
        d("HMApiCallbackExitedProximity");

        if (broadcaster != null && broadcaster.onDeviceExitedProximity(device.getMac())) return;
        if (scanner != null) scanner.onDeviceExitedProximity(device.getMac());
    }

    @Override
    public void HMApiCallbackCustomCommandIncoming(HMDevice device, byte[] data, int length) {
        byte[] trimmedBytes = trimmedBytes(data, length);

        if (broadcaster != null && broadcaster.onCommandReceived(device, trimmedBytes)) return;
        if (scanner != null) scanner.onCommandReceived(device, trimmedBytes);
    }

    @Override
    public void HMApiCallbackCustomCommandResponse(HMDevice device, byte[] data, int length) {
        byte[] trimmedBytes = trimmedBytes(data, length);

        if (broadcaster != null && broadcaster.onCommandResponseReceived(device, trimmedBytes))
            return;

        if (scanner != null) scanner.onCommandResponseReceived(device, trimmedBytes);
    }

    @Override
    public int HMApiCallbackGetDeviceCertificateFailed(HMDevice device, byte[] nonce) {
        d("HMApiCallbackGetDeviceCertificateFailed ");
        // should ask the CA for the signature for the nonce
        // return false getting the sig start failed
        // return true started acquiring signature
        return 0;
    }

    @Override
    public int HMApiCallbackPairingRequested(HMDevice device) {
        if (broadcaster != null) return broadcaster.onReceivedPairingRequest(device);
        return 1;
    }

    @Override
    public void HMApiCallbackTelematicsCommandIncoming(HMDevice device, int id, int length,
                                                       byte[] data) {
        if (telematics != null) {
            telematics.onTelematicsResponseDecrypted(device.getSerial(), id, trimmedBytes(data,
                    length));
        }
    }

    @Override
    public void HMCryptoHalGenerateNonce(byte[] nonce) {
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);
    }

    @Override
    public void HMApiCallbackRevokeResponse(HMDevice device, byte[] data, int length, int status) {
        d("HMApiCallbackRevokeResponse(): %s, %s, %d, %d",
                ByteUtils.hexFromBytes(device.getSerial()),
                ByteUtils.hexFromBytes(data),
                length,
                status);

        byte[] trimmedBytes = trimmedBytes(data, length);

        if (broadcaster != null && broadcaster.onRevokeResult(device, trimmedBytes, status) ==
                false) {
            if (scanner != null) scanner.onRevokeResult(device, trimmedBytes, status);
        }
    }

    @Override
    public void HMApiCallbackErrorCommandIncoming(HMDevice device, int commandId, int errorType) {
        d("HMApiCallbackErrorCommandIncoming %s, %s, %s",
                ByteUtils.hexFromBytes(device.getMac()), commandId, errorType);

        if (broadcaster != null && broadcaster.onErrorCommand(device, commandId, errorType) == false) {
            if (scanner != null) scanner.onErrorCommand(device, commandId, errorType);
        }
    }

    void copyBytes(byte[] from, byte[] to) {
        System.arraycopy(from, 0, to, 0, from.length);
    }

    void copyBytes(Bytes fromBytes, byte[] to) {
        copyBytes(fromBytes.getByteArray(), to);
    }

    byte[] trimmedBytes(byte[] bytes, int length) {
        if (bytes.length == length) return bytes;
        return Arrays.copyOfRange(bytes, 0, length);
    }

    // MARK: listeners

    abstract static class Telematics {
        abstract void onTelematicsCommandEncrypted(byte[] serial, byte[] issuer, byte[] data);

        abstract void onTelematicsResponseDecrypted(byte[] serial, int resultCode, byte[] data);
    }

    abstract static class Broadcaster {
        abstract boolean writeData(byte[] mac, byte[] data, int characteristic);

        abstract boolean onChangedAuthenticationState(HMDevice device);

        abstract boolean onDeviceExitedProximity(byte[] mac);

        abstract boolean onCommandReceived(HMDevice device, byte[] bytes);

        abstract boolean onCommandResponseReceived(HMDevice device, byte[] trimmedBytes);

        abstract int onReceivedPairingRequest(HMDevice device);

        abstract boolean onRevokeResult(HMDevice device, byte[] bytes, int status);

        public abstract boolean onErrorCommand(HMDevice device, int commandId, int errorType);
    }

    abstract static class Scanner {

        abstract void connect(byte[] bytes);

        abstract void disconnect(byte[] bytes);

        abstract void startServiceDiscovery(byte[] bytes);

        abstract boolean writeData(byte[] mac, byte[] data, int characteristic);

        abstract boolean readValue(byte[] mac, int characteristic);

        abstract boolean onChangedAuthenticationState(HMDevice device);

        abstract boolean onDeviceExitedProximity(byte[] mac);

        abstract boolean onCommandReceived(HMDevice device, byte[] bytes);

        abstract boolean onCommandResponseReceived(HMDevice device, byte[] bytes);

        abstract boolean onRevokeResult(HMDevice device, byte[] bytes, int status);

        public abstract boolean onErrorCommand(HMDevice device, int commandId, int errorType);
    }
}
