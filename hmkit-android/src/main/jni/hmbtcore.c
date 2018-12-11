#include <jni.h>
#include <stddef.h>
#include "hm_bt_core.h"
#include "hmbtcore.h"
#include "Crypto.h"
#include "hm_bt_debug_hal.h"
#include "hm_cert.h"
#include "hm_api.h"

void prepareCallbackFunctions(JNIEnv *env, jobject instance, jobject coreInterface){

    interfaceClassRef = (*env)->GetObjectClass(env, coreInterface);

    interfaceMethodHMBTHalInit = (*env)->GetMethodID(env,interfaceClassRef, "HMBTHalInit","()I");
    interfaceMethodHMBTHalScanStart = (*env)->GetMethodID(env,interfaceClassRef, "HMBTHalScanStart","()I");
    interfaceMethodHMBTHalScanStop = (*env)->GetMethodID(env,interfaceClassRef, "HMBTHalScanStop","()I");
    interfaceMethodHMBTHalAdvertisementStart = (*env)->GetMethodID(env,interfaceClassRef, "HMBTHalAdvertisementStart","([B[B)I");
    interfaceMethodHMBTHalAdvertisementStop = (*env)->GetMethodID(env,interfaceClassRef, "HMBTHalAdvertisementStop","()I");
    interfaceMethodHMBTHalConnect = (*env)->GetMethodID(env,interfaceClassRef, "HMBTHalConnect","([B)I");
    interfaceMethodHMBTHalDisconnect = (*env)->GetMethodID(env,interfaceClassRef, "HMBTHalDisconnect","([B)I");
    interfaceMethodHMBTHalServiceDiscovery = (*env)->GetMethodID(env,interfaceClassRef, "HMBTHalServiceDiscovery","([B)I");
    interfaceMethodHMBTHalWriteData = (*env)->GetMethodID(env,interfaceClassRef, "HMBTHalWriteData","([BI[BI)I");
    interfaceMethodHMBTHalReadData = (*env)->GetMethodID(env,interfaceClassRef, "HMBTHalReadData","([BII)I");
    interfaceMethodHMBTHalTelematicsSendData = (*env)->GetMethodID(env,interfaceClassRef, "HMBTHalTelematicsSendData","([B[BI[B)I");

    interfaceMethodHMPersistenceHalgetSerial = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHalgetSerial","([B)I");
    interfaceMethodHMPersistenceHalgetLocalPublicKey = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHalgetLocalPublicKey","([B)I");
    interfaceMethodHMPersistenceHalgetLocalPrivateKey = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHalgetLocalPrivateKey","([B)I");
    interfaceMethodHMPersistenceHalgetDeviceCertificate = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHalgetDeviceCertificate","([B)I");
    interfaceMethodHMPersistenceHalgetCaPublicKey = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHalgetCaPublicKey","([B)I");
    interfaceMethodHMPersistenceHalgetOEMCaPublicKey = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHalgetOEMCaPublicKey","([B)I");
    interfaceMethodHMPersistenceHaladdPublicKey = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHaladdPublicKey","([B[BI)I");
    interfaceMethodHMPersistenceHalgetPublicKey = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHalgetPublicKey","([B[B[I)I");
    interfaceMethodHMPersistenceHalgetPublicKeyByIndex = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHalgetPublicKeyByIndex","(I[B[I)I");
    interfaceMethodHMPersistenceHalgetPublicKeyCount = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHalgetPublicKeyCount","([I)I");
    interfaceMethodHMPersistenceHalremovePublicKey = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHalremovePublicKey","([B)I");
    interfaceMethodHMPersistenceHaladdStoredCertificate = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHaladdStoredCertificate","([BI)I");
    interfaceMethodHMPersistenceHalgetStoredCertificate = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHalgetStoredCertificate","([B[B[I)I");
    interfaceMethodHMPersistenceHaleraseStoredCertificate = (*env)->GetMethodID(env,interfaceClassRef, "HMPersistenceHaleraseStoredCertificate","([B)I");

    interfaceMethodHMApiCallbackEnteredProximity = (*env)->GetMethodID(env,interfaceClassRef, "HMApiCallbackEnteredProximity","(Lcom/highmobility/btcore/HMDevice;)V");
    interfaceMethodHMApiCallbackExitedProximity = (*env)->GetMethodID(env,interfaceClassRef, "HMApiCallbackExitedProximity","(Lcom/highmobility/btcore/HMDevice;)V");
    interfaceMethodHMApiCallbackCustomCommandIncoming = (*env)->GetMethodID(env,interfaceClassRef, "HMApiCallbackCustomCommandIncoming","(Lcom/highmobility/btcore/HMDevice;[BI)V");
    interfaceMethodHMApiCallbackCustomCommandResponse = (*env)->GetMethodID(env,interfaceClassRef, "HMApiCallbackCustomCommandResponse","(Lcom/highmobility/btcore/HMDevice;[BI)V");
    interfaceMethodHMApiCallbackGetDeviceCertificateFailed = (*env)->GetMethodID(env,interfaceClassRef, "HMApiCallbackGetDeviceCertificateFailed","(Lcom/highmobility/btcore/HMDevice;[B)I");
    interfaceMethodHMApiCallbackPairingRequested = (*env)->GetMethodID(env,interfaceClassRef, "HMApiCallbackPairingRequested","(Lcom/highmobility/btcore/HMDevice;)I");
    interfaceMethodHMApiCallbackTelematicsCommandIncoming = (*env)->GetMethodID(env,interfaceClassRef, "HMApiCallbackTelematicsCommandIncoming","(Lcom/highmobility/btcore/HMDevice;II[B)V");

    interfaceMethodHMCryptoHalGenerateNonce  = (*env)->GetMethodID(env,interfaceClassRef, "HMCryptoHalGenerateNonce","([B)V");

    interfaceMethodHMApiCallbackRevokeResponse = (*env)->GetMethodID(env,interfaceClassRef, "HMApiCallbackRevokeResponse","(Lcom/highmobility/btcore/HMDevice;[BII)V");

    envRef = env;
    coreInterfaceRef = coreInterface;
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreInit(JNIEnv *env, jobject instance,
                                                     jobject coreInterface) {

    prepareCallbackFunctions(env,instance,coreInterface);

    hm_bt_core_init();
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreClock(JNIEnv *env, jobject instance,jobject coreInterface) {

    prepareCallbackFunctions(env,instance,coreInterface);
    hm_bt_core_clock();
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingReadNotification(JNIEnv *env,
                                                                        jobject instance,
                                                                        jobject coreInterface,
                                                                        jbyteArray mac_,
                                                                        jint characteriistic_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_read_notification(mac, characteriistic_);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingReadResponse(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                    jbyteArray data_, jint size,
                                                                    jint offset, jbyteArray mac_,
                                                                    jint characteriistic_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_read_response(data,size,offset,mac,characteriistic_);

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingWriteResponse(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                     jbyteArray mac_,
                                                                     jint characteriistic_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_write_response(mac,characteriistic_);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingPingNotification(JNIEnv *env,
                                                                        jobject instance,jobject coreInterface,
                                                                        jbyteArray mac_,
                                                                        jint characteriistic_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_ping_notification(mac,characteriistic_);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingProcessAdvertisement(JNIEnv *env,
                                                                            jobject instance,jobject coreInterface,
                                                                            jbyteArray mac_,
                                                                            jbyteArray data_,
                                                                            jint size) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);

    hm_bt_core_sensing_process_advertisement(mac,0, data,size);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingDiscoveryEvent(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                      jbyteArray mac_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_discovery_event(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingScanStart(JNIEnv *env, jobject instance, jobject coreInterface) {
    prepareCallbackFunctions(env,instance,coreInterface);

    hm_bt_core_sensing_scan_start();

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingConnect(JNIEnv *env, jobject instance,jobject coreInterface,
                                                               jbyteArray mac_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_connect(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingDisconnect(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                  jbyteArray mac_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_sensing_disconnect(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCorelinkConnect(JNIEnv *env, jobject instance,jobject coreInterface,
                                                            jbyteArray mac_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_link_connect(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCorelinkDisconnect(JNIEnv *env, jobject instance,jobject coreInterface,
                                                               jbyteArray mac_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_link_disconnect(mac);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCorelinkIncomingData(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                 jbyteArray data_, jint size,
                                                                 jbyteArray mac_,jint characteriistic_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_link_incoming_data(data,size,mac,characteriistic_);

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCorelinkWriteResponse(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                 jbyteArray mac_,jint characteriistic_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    hm_bt_core_link_write_response(mac,characteriistic_);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSendCustomCommand(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                  jbyteArray data_, jint size,
                                                                  jbyteArray mac_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);

    sendSecureContainerUsingMac(mac, data, size);

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSendReadDeviceCertificate(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                  jbyteArray mac_, jbyteArray nonce_,
                                                                  jbyteArray caSignature_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *mac = (*env)->GetByteArrayElements(env, mac_, NULL);
    jbyte *nonce = (*env)->GetByteArrayElements(env, nonce_, NULL);
    jbyte *caSignature = (*env)->GetByteArrayElements(env, caSignature_, NULL);

    hm_api_send_read_device_certificate(mac, nonce, caSignature);

    (*env)->ReleaseByteArrayElements(env, mac_, mac, 0);
    (*env)->ReleaseByteArrayElements(env, nonce_, nonce, 0);
    (*env)->ReleaseByteArrayElements(env, caSignature_, caSignature, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSendRegisterAccessCertificate(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                          jbyteArray certificate_) {
    prepareCallbackFunctions(env,instance,coreInterface);
    jbyte *certificate = (*env)->GetByteArrayElements(env, certificate_, NULL);

    hm_certificate_t cert;
    hm_cert_get_as_struct(certificate, &cert);
    hm_api_send_register_access_certificate(&cert);

    (*env)->ReleaseByteArrayElements(env, certificate_, certificate, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreCryptoCreateKeys(JNIEnv *env, jobject instance,
                                                                  jbyteArray privateKey_,
                                                                  jbyteArray publicKey_) {
    jbyte *private = (*env)->GetByteArrayElements(env, privateKey_, NULL);
    jbyte *public = (*env)->GetByteArrayElements(env, publicKey_, NULL);

    hm_crypto_openssl_create_keys(private, public, true);

    (*env)->ReleaseByteArrayElements(env, privateKey_, private, 0);
    (*env)->ReleaseByteArrayElements(env, publicKey_, public, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreCryptoAddSignature(JNIEnv *env, jobject instance,
                                                                 jbyteArray data_, jint size,
                                                                 jbyteArray privateKey_, jbyteArray signature_) {

    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);
    jbyte *private = (*env)->GetByteArrayElements(env, privateKey_, NULL);
    jbyte *signature = (*env)->GetByteArrayElements(env, signature_, NULL);

    hm_crypto_openssl_signature(data, size, private, signature);

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    (*env)->ReleaseByteArrayElements(env, privateKey_, private, 0);
    (*env)->ReleaseByteArrayElements(env, signature_, signature, 0);
}

JNIEXPORT jint JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreCryptoValidateSignature(JNIEnv *env, jobject instance,
        jbyteArray data_, jint size,
        jbyteArray pubKey_, jbyteArray signature_) {

jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);
jbyte *public_key = (*env)->GetByteArrayElements(env, pubKey_, NULL);
jbyte *signature = (*env)->GetByteArrayElements(env, signature_, NULL);

jint retvalue = hm_crypto_openssl_verify(data, size, public_key, signature);

(*env)->ReleaseByteArrayElements(env, data_, data, 0);
(*env)->ReleaseByteArrayElements(env, pubKey_, public_key, 0);
(*env)->ReleaseByteArrayElements(env, signature_, signature, 0);

return retvalue;
}

JNIEXPORT jint JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreCryptoJWTAddSignature(JNIEnv *env, jobject instance,
        jbyteArray message_, jint size,
        jbyteArray privateKey_, jbyteArray signature_) {

jbyte *message = (*env)->GetByteArrayElements(env, message_, NULL);
jbyte *private_key = (*env)->GetByteArrayElements(env, privateKey_, NULL);
jbyte *signature = (*env)->GetByteArrayElements(env, signature_, NULL);

jint retvalue =hm_crypto_openssl_jwt_signature(message, size, private_key, signature);

(*env)->ReleaseByteArrayElements(env, message_, message, 0);
(*env)->ReleaseByteArrayElements(env, privateKey_, private_key, 0);
(*env)->ReleaseByteArrayElements(env, signature_, signature, 0);

return retvalue;
}

JNIEXPORT jint JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreCryptoJWTsha(JNIEnv *env, jobject instance,
        jbyteArray nonce_, jint size,
        jbyteArray hash_) {

jbyte *nonce = (*env)->GetByteArrayElements(env, nonce_, NULL);
jbyte *hash = (*env)->GetByteArrayElements(env, hash_, NULL);

jint retvalue = hm_crypto_openssl_jwt_sha(nonce, size, hash);

(*env)->ReleaseByteArrayElements(env, nonce_, nonce, 0);
(*env)->ReleaseByteArrayElements(env, hash_, hash, 0);

return retvalue;
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreTelematicsReceiveData(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                      jint length, jbyteArray data_) {
    prepareCallbackFunctions(env,instance,coreInterface);

    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);

    hm_bt_core_telematics_receive_data(length, data);

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSendTelematicsCommand(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                      jbyteArray serial_, jbyteArray nonce_, jint length, jbyteArray data_) {
    prepareCallbackFunctions(env,instance,coreInterface);

    jbyte *serial = (*env)->GetByteArrayElements(env, serial_, NULL);
    jbyte *nonce = (*env)->GetByteArrayElements(env, nonce_, NULL);
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);

    hm_api_send_telematics_command(serial, nonce, length, data);

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    (*env)->ReleaseByteArrayElements(env, nonce_, nonce, 0);
    (*env)->ReleaseByteArrayElements(env, serial_, serial, 0);
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSendRevoke(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                      jbyteArray serial_) {
    prepareCallbackFunctions(env,instance,coreInterface);

    jbyte *serial = (*env)->GetByteArrayElements(env, serial_, NULL);

    sendRevoke(serial);

    (*env)->ReleaseByteArrayElements(env, serial_, serial, 0);
}