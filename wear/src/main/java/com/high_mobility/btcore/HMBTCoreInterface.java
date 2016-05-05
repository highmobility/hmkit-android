package com.high_mobility.btcore;

public interface HMBTCoreInterface {

    //BT HAL

    //Initialize central or peripheral
    //TT
    int HMBTHalInit();

    //Start stop central scanning
    int HMBTHalScanStart();
    int HMBTHalScanStop();

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
    //Get current device publick key
    //TT
    int HMPersistenceHalgetLocalPrivateKey(byte[] privateKey);
    //TT
    int HMPersistenceHalgetDeviceCertificate(byte[] cert);

    //Add remote device publick key to storagr
    //TT
    int HMPersistenceHaladdPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int commandSize, byte[] command);
    //Get remote device publick key from storage
    //TT
    int HMPersistenceHalgetPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int[] commandSize, byte[] command);
    //TT
    int HMPersistenceHalgetPublicKeyByIndex(int index, byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int[] commandSize, byte[] command);
    //TT
    int HMPersistenceHalgetPublicKeyCount(int[] count);
    //Remove remote device publick key from storage
    //TT
    int HMPersistenceHalremovePublicKey(byte[] serial);

    //Add certificate to storage
    //TT
    int HMPersistenceHaladdStoredCertificate(byte[] cert, int size);
    //Get certificate from storage
    //TT
    int HMPersistenceHalgetStoredCertificate(byte[] cert, int[] size);
    //Delete certificate from storage
    //TT
    int HMPersistenceHaleraseStoredCertificate();


    //Proximity
    //TT
    void HMCtwEnteredProximity(HMDevice device);
    //TT
    void HMCtwExitedProximity(HMDevice device);

    //Callback
    //TT
    void HMCtwCustomCommandReceived(HMDevice device, byte[] data, int[] length, int[] error); // received custom command
    int HMCtwGetDeviceCertificateFailed(HMDevice device, byte[] nonce); //ret false on, et ei jätka
    int HMCtwPairingRequested(HMDevice device, byte[] serial); //ret false on, et ei jätka
}