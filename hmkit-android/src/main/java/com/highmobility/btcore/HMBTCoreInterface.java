package com.highmobility.btcore;

public interface HMBTCoreInterface {

    //Characteristics id's
    int hm_characteristic_link_read     =   0x02;
    int hm_characteristic_link_write    =   0x03;
    int hm_characteristic_alive         =   0x04;
    int hm_characteristic_info          =   0x05;
    int hm_characteristic_sensing_read  =   0x06;
    int hm_characteristic_sensing_write =   0x07;

    //BT HAL

    //Initialize central or peripheral
    //TT
    int HMBTHalInit();

    //Start stop central scanning
    int HMBTHalScanStart();
    int HMBTHalScanStop();

    //Start stop peripheral advertisement
    //TT
    int HMBTHalAdvertisementStart(byte[] issuer, byte[] appID);
    int HMBTHalAdvertisementStop();

    //Connect disconnect to peripheral
    int HMBTHalConnect(byte[] mac);
    int HMBTHalDisconnect(byte[] mac);

    //Start peripheral service discovery
    int HMBTHalServiceDiscovery(byte[] mac);

    //Write data to peripheral or central
    //TT
    int HMBTHalWriteData(byte[] mac, int length, byte[] data, int characteristic);
    //Read data from peripheral
    int HMBTHalReadData(byte[] mac, int offset, int characteristic);

    int HMBTHalTelematicsSendData(byte[] issuer, byte[] serial, int length, byte[] data);

    //PERSISTENCE HAL

    //Get current device serial number
    int HMPersistenceHalgetSerial(byte[] serial);
    //Get current device public key
    int HMPersistenceHalgetLocalPublicKey(byte[] publicKey);
    //Get current device public key
    int HMPersistenceHalgetLocalPrivateKey(byte[] privateKey);
    int HMPersistenceHalgetDeviceCertificate(byte[] cert);

    int HMPersistenceHalgetCaPublicKey(byte[] cert);

    int HMPersistenceHalgetOEMCaPublicKey(byte[] cert);

    //Add remote device public key to storage
    int HMPersistenceHaladdPublicKey(byte[] serial, byte[] cert, int size);
    //Get remote device public key from storage
    int HMPersistenceHalgetPublicKey(byte[] serial, byte[] cert, int[] size);
    int HMPersistenceHalgetPublicKeyByIndex(int index, byte[] cert, int[] size);
    int HMPersistenceHalgetPublicKeyCount(int[] count);
    //Remove remote device public key from storage
    int HMPersistenceHalremovePublicKey(byte[] serial);

    //Add certificate to storage
    int HMPersistenceHaladdStoredCertificate(byte[] cert, int size);
    //Get certificate from storage
    int HMPersistenceHalgetStoredCertificate(byte[] serial, byte[] cert, int[] size);
    //Delete certificate from storage
    int HMPersistenceHaleraseStoredCertificate(byte[] serial);


    //Proximity
    void HMApiCallbackEnteredProximity(HMDevice device);
    void HMApiCallbackExitedProximity(HMDevice device);

    //Callback
    void HMApiCallbackCustomCommandIncoming(HMDevice device, byte[] data, int length); // received custom command
    void HMApiCallbackCustomCommandResponse(HMDevice device, byte[] data, int length);
    int HMApiCallbackGetDeviceCertificateFailed(HMDevice device, byte[] nonce); //ret false on, et ei jätka
    int HMApiCallbackPairingRequested(HMDevice device); //ret false on, et ei jätka

    void HMApiCallbackTelematicsCommandIncoming(HMDevice device, int id, int length, byte[] data);

    //Crypto
    void HMCryptoHalGenerateNonce(byte[] nonce);

    void HMApiCallbackRevokeResponse(HMDevice device, byte[] data, int length, int status);
}