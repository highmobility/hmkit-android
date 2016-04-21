package com.high_mobility.digitalkey.HMLink.Broadcasting.Core;

public interface HMBTCoreInterface {

    //BT HAL

    //Initialize central or peripheral
    //TT
    int HMBTHalInit();

    //Start stop central scanning
    int HMBTHalscan_start();
    int HMBTHalscan_stop();

    //Start stop peripheral advertisement
    //TT
    int HMBTHalAdvertisementStart();
    int HMBTHalAdvertisementStop();

    //Connect disconnect to peripheral
    int HMBTHalConnect(byte[] mac);
    int HMBTHalDisconnect(byte[] mac);

    //Start peripheral service discovery
    int HMBTHalServiceDiscovery(byte[] mac);

    //Write data to peripheral or central
    //TT
    int HMBTHalWriteData(byte[] mac, int length, byte[] data);
    //Read data from peripheral
    int HMBTHalReadData(byte[] mac, int offset);

    //PERSISTENCE HAL

    //Get current device serial number
    //TT
    int HMPersistenceHalgetSerial(byte[] serial);
    //Get current device publick key
    //TT
    int HMPersistenceHalgetLocalPublicKey(byte[] publicKey);

    //Add remote device publick key to storagr
    //TT
    int HMPersistenceHaladdPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int commandSize, byte[] command);
    //Get remote device publick key from storage
    //TT
    int HMPersistenceHalgetPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int commandSize, byte[] command);
    //Remove remote device publick key from storage
    //TT
    int HMPersistenceHalremovePublicKey(byte[] serial);

    //Add certificate to storage
    //TT
    int HMPersistenceHaladdStoredCertificate(byte[] cert, int size);
    //Get certificate from storage
    //TT
    int HMPersistenceHalgetStoredCertificate(byte[] cert, int size);
    //Delete certificate from storage
    //TT
    int HMPersistenceHaleraseStoredCertificate();

    //CRYPTO HAL

    //TT
    int HMCryptoaesEcbBlockEncrypt(byte[] key, byte[] cleartext, byte[] ciphertext);

    //TT
    int HMCryptoeccGetEcdh(byte[] serial, byte[] ecdh);
    //TT
    int HMCryptoeccAddSignature(byte[] data, int size, byte[] signature);
    //TT
    int HMCryptoeccValidateSignature(byte[] data, int size, byte[] signature, byte[] serial);
    //TT
    int HMCryptoeccValidateAllSignatures(byte[] data, int size, byte[] signature);
    //TT
    int HMCryptoeccValidateCaSignature(byte[] data, int size, byte[] signature);
    //TT
    int HMCryptohmac(byte[] key, byte[] data, byte[] hmac);
    //TT
    int HMCryptogenerateNonce(byte[] nonce);

    //DEBUG HAL

    //Debug text
    //TT
    void HMDebug(String str);

    //CTW
    void HMCtwInit();
    void HMCtwPing();

    //Proximity
    //TT
    void HMCtwEnteredProximity(HMDevice device);
    //TT
    void HMCtwExitedProximity(HMDevice device);

    //Callback
    //TT
    void HMCtwCommandReceived(HMDevice device, int data, int length, int error); // received custom command
    int HMCtwGetDeviceCertificateFailed(HMDevice device, int nonce); //ret false on, et ei jätka
}