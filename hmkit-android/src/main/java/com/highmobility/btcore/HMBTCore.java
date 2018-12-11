package com.highmobility.btcore;

public class HMBTCore {

    static {
        System.loadLibrary("hmbtcore");
    }

    //Init core
    //interface is class reference what implements HMBTCoreInterface
    public native void HMBTCoreInit(HMBTCoreInterface coreInterface);
    //Send clock beat to core
    public native void HMBTCoreClock(HMBTCoreInterface coreInterface);

    //CORE SENSING

    public native void HMBTCoreSensingReadNotification(HMBTCoreInterface coreInterface, byte[] mac, int characteristic);
    public native void HMBTCoreSensingReadResponse(HMBTCoreInterface coreInterface, byte[] data, int size, int offset, byte[] mac, int characteristic);

    public native void HMBTCoreSensingWriteResponse(HMBTCoreInterface coreInterface, byte[] mac, int characteristic);

    public native void HMBTCoreSensingPingNotification(HMBTCoreInterface coreInterface, byte[] mac, int characteristic);

    public native void HMBTCoreSensingProcessAdvertisement(HMBTCoreInterface coreInterface, byte[] mac, byte[] data, int size);
    public native void HMBTCoreSensingDiscoveryEvent(HMBTCoreInterface coreInterface, byte[] mac);
    public native void HMBTCoreSensingScanStart(HMBTCoreInterface coreInterface);

    public native void HMBTCoreSensingConnect(HMBTCoreInterface coreInterface, byte[] mac);
    public native void HMBTCoreSensingDisconnect(HMBTCoreInterface coreInterface, byte[] mac);

    //CORE LINK

    //Initialize link object in core
    public native void HMBTCorelinkConnect(HMBTCoreInterface coreInterface, byte[] mac);
    //Delete link object in core
    public native void HMBTCorelinkDisconnect(HMBTCoreInterface coreInterface, byte[] mac);

    //Forward link incoming data to core
    public native void HMBTCorelinkIncomingData(HMBTCoreInterface coreInterface, byte[] data, int size, byte[] mac, int characteristic);

    public native void HMBTCorelinkWriteResponse(HMBTCoreInterface coreInterface, byte[] mac, int characteristic);

    public native void HMBTCoreSendCustomCommand(HMBTCoreInterface coreInterface, byte[] data, int size, byte[] mac);

    public native void HMBTCoreSendReadDeviceCertificate(HMBTCoreInterface coreInterface, byte[] mac, byte[] nonce, byte[] caSignature);
    public native void HMBTCoreSendRegisterAccessCertificate(HMBTCoreInterface coreInterface, byte[] certificate);

    //Crypto
    public native void HMBTCoreCryptoCreateKeys(byte[] privateKey, byte[] publicKey);

    public native void HMBTCoreCryptoAddSignature(byte[] data, int size, byte[] privateKey, byte[] signature);
    public native int HMBTCoreCryptoValidateSignature(byte[] data, int size, byte[] pubKey, byte[] signature);

    public native int HMBTCoreCryptoJWTAddSignature(byte[] message, int size, byte[] private_key, byte[] signature);
    public native int HMBTCoreCryptoJWTsha(byte[] nonce, int size, byte[] hash);

    //Telematics
    public native void HMBTCoreTelematicsReceiveData(HMBTCoreInterface coreInterface, int length, byte[] data);
    public native void HMBTCoreSendTelematicsCommand(HMBTCoreInterface coreInterface, byte[] serial, byte[] nonce, int length, byte[] data);

    public native void HMBTCoreSendRevoke(HMBTCoreInterface coreInterface, byte[] serial);
}
