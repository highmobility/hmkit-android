//
// Created by Maidu Ule on 20/04/16.
//
#ifndef ANDROID_WEAR_DIGITAL_KEY_HMBTCORE_H
#define ANDROID_WEAR_DIGITAL_KEY_HMBTCORE_H

#include <jni.h>

jclass interfaceClassRef;
jobject coreInterfaceRef;
JNIEnv *envRef;

jmethodID *interfaceMethodHMBTHalInit;
jmethodID *interfaceMethodHMBTHalScanStart;
jmethodID *interfaceMethodHMBTHalScanStop;
jmethodID *interfaceMethodHMBTHalAdvertisementStart;
jmethodID *interfaceMethodHMBTHalAdvertisementStop;
jmethodID *interfaceMethodHMBTHalConnect;
jmethodID *interfaceMethodHMBTHalDisconnect;
jmethodID *interfaceMethodHMBTHalServiceDiscovery;
jmethodID *interfaceMethodHMBTHalWriteData;
jmethodID *interfaceMethodHMBTHalReadData;

jmethodID *interfaceMethodHMPersistenceHalgetSerial;
jmethodID *interfaceMethodHMPersistenceHalgetLocalPublicKey;
jmethodID *interfaceMethodHMPersistenceHaladdPublicKey;
jmethodID *interfaceMethodHMPersistenceHalgetPublicKey;
jmethodID *interfaceMethodHMPersistenceHalremovePublicKey;
jmethodID *interfaceMethodHMPersistenceHaladdStoredCertificate;
jmethodID *interfaceMethodHMPersistenceHalgetStoredCertificate;
jmethodID *interfaceMethodHMPersistenceHaleraseStoredCertificate;

#endif //ANDROID_WEAR_DIGITAL_KEY_HMBTCORE_H
