#include <jni.h>
#include <stddef.h>
extern "C" {
#include "hm_bt_core.h"
#include "Crypto.h"
#include "hm_bt_debug_hal.h"
#include "hm_cert.h"
#include "hm_api.h"
#include "hm_bt_log.h"
}
#include "hmbtcore.h"

#define CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION              \
                                                                  \
  catch (...)                                                     \
  {                                                               \
    /* Oops I missed identifying this exception! */               \
    jclass jc = env->FindClass("java/lang/Error");                \
    if(jc) env->ThrowNew (jc, "unidentified exception");          \
  }

void prepareCallbackFunctions(JNIEnv *env, jobject instance, jobject coreInterface){

    interfaceClassRef = env->GetObjectClass(coreInterface);

    interfaceMethodHMBTHalInit = env->GetMethodID(interfaceClassRef, "HMBTHalInit","()I");
    interfaceMethodHMBTHalScanStart = env->GetMethodID(interfaceClassRef, "HMBTHalScanStart","()I");
    interfaceMethodHMBTHalScanStop = env->GetMethodID(interfaceClassRef, "HMBTHalScanStop","()I");
    interfaceMethodHMBTHalAdvertisementStart = env->GetMethodID(interfaceClassRef, "HMBTHalAdvertisementStart","([B[B)I");
    interfaceMethodHMBTHalAdvertisementStop = env->GetMethodID(interfaceClassRef, "HMBTHalAdvertisementStop","()I");
    interfaceMethodHMBTHalConnect = env->GetMethodID(interfaceClassRef, "HMBTHalConnect","([B)I");
    interfaceMethodHMBTHalDisconnect = env->GetMethodID(interfaceClassRef, "HMBTHalDisconnect","([B)I");
    interfaceMethodHMBTHalServiceDiscovery = env->GetMethodID(interfaceClassRef, "HMBTHalServiceDiscovery","([B)I");
    interfaceMethodHMBTHalWriteData = env->GetMethodID(interfaceClassRef, "HMBTHalWriteData","([BI[BI)I");
    interfaceMethodHMBTHalReadData = env->GetMethodID(interfaceClassRef, "HMBTHalReadData","([BII)I");
    interfaceMethodHMBTHalTelematicsSendData = env->GetMethodID(interfaceClassRef, "HMBTHalTelematicsSendData","([B[BI[B)I");

    interfaceMethodHMPersistenceHalgetSerial = env->GetMethodID(interfaceClassRef, "HMPersistenceHalgetSerial","([B)I");
    interfaceMethodHMPersistenceHalgetLocalPublicKey = env->GetMethodID(interfaceClassRef, "HMPersistenceHalgetLocalPublicKey","([B)I");
    interfaceMethodHMPersistenceHalgetLocalPrivateKey = env->GetMethodID(interfaceClassRef, "HMPersistenceHalgetLocalPrivateKey","([B)I");
    interfaceMethodHMPersistenceHalgetDeviceCertificate = env->GetMethodID(interfaceClassRef, "HMPersistenceHalgetDeviceCertificate","([B)I");
    interfaceMethodHMPersistenceHalgetCaPublicKey = env->GetMethodID(interfaceClassRef, "HMPersistenceHalgetCaPublicKey","([B)I");
    interfaceMethodHMPersistenceHalgetOEMCaPublicKey = env->GetMethodID(interfaceClassRef, "HMPersistenceHalgetOEMCaPublicKey","([B)I");
    interfaceMethodHMPersistenceHaladdPublicKey = env->GetMethodID(interfaceClassRef, "HMPersistenceHaladdPublicKey","([B[BI)I");
    interfaceMethodHMPersistenceHalgetPublicKey = env->GetMethodID(interfaceClassRef, "HMPersistenceHalgetPublicKey","([B[B[I)I");
    interfaceMethodHMPersistenceHalgetPublicKeyByIndex = env->GetMethodID(interfaceClassRef, "HMPersistenceHalgetPublicKeyByIndex","(I[B[I)I");
    interfaceMethodHMPersistenceHalgetPublicKeyCount = env->GetMethodID(interfaceClassRef, "HMPersistenceHalgetPublicKeyCount","([I)I");
    interfaceMethodHMPersistenceHalremovePublicKey = env->GetMethodID(interfaceClassRef, "HMPersistenceHalremovePublicKey","([B)I");
    interfaceMethodHMPersistenceHaladdStoredCertificate = env->GetMethodID(interfaceClassRef, "HMPersistenceHaladdStoredCertificate","([BI)I");
    interfaceMethodHMPersistenceHalgetStoredCertificate = env->GetMethodID(interfaceClassRef, "HMPersistenceHalgetStoredCertificate","([B[B[I)I");
    interfaceMethodHMPersistenceHaleraseStoredCertificate = env->GetMethodID(interfaceClassRef, "HMPersistenceHaleraseStoredCertificate","([B)I");

    interfaceMethodHMApiCallbackEnteredProximity = env->GetMethodID(interfaceClassRef, "HMApiCallbackEnteredProximity","(Lcom/highmobility/btcore/HMDevice;)V");
    interfaceMethodHMApiCallbackExitedProximity = env->GetMethodID(interfaceClassRef, "HMApiCallbackExitedProximity","(Lcom/highmobility/btcore/HMDevice;)V");
    interfaceMethodHMApiCallbackCustomCommandIncoming = env->GetMethodID(interfaceClassRef, "HMApiCallbackCustomCommandIncoming","(Lcom/highmobility/btcore/HMDevice;[BI)V");
    interfaceMethodHMApiCallbackCustomCommandResponse = env->GetMethodID(interfaceClassRef, "HMApiCallbackCustomCommandResponse","(Lcom/highmobility/btcore/HMDevice;[BI)V");
    interfaceMethodHMApiCallbackGetDeviceCertificateFailed = env->GetMethodID(interfaceClassRef, "HMApiCallbackGetDeviceCertificateFailed","(Lcom/highmobility/btcore/HMDevice;[B)I");
    interfaceMethodHMApiCallbackPairingRequested = env->GetMethodID(interfaceClassRef, "HMApiCallbackPairingRequested","(Lcom/highmobility/btcore/HMDevice;)I");
    interfaceMethodHMApiCallbackTelematicsCommandIncoming = env->GetMethodID(interfaceClassRef, "HMApiCallbackTelematicsCommandIncoming","(Lcom/highmobility/btcore/HMDevice;II[B)V");

    interfaceMethodHMCryptoHalGenerateNonce  = env->GetMethodID(interfaceClassRef, "HMCryptoHalGenerateNonce","([B)V");

    interfaceMethodHMApiCallbackRevokeResponse = env->GetMethodID(interfaceClassRef, "HMApiCallbackRevokeResponse","(Lcom/highmobility/btcore/HMDevice;[BII)V");

    interfaceMethodHMApiCallbackErrorCommandIncoming = env->GetMethodID(interfaceClassRef, "HMApiCallbackErrorCommandIncoming","(Lcom/highmobility/btcore/HMDevice;II)V");

    envRef = env;
    coreInterfaceRef = coreInterface;
}

extern "C" {

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreInit(JNIEnv *env, jobject instance,
                                                     jobject coreInterface) {

    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        hm_bt_core_init();
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreClock(JNIEnv *env, jobject instance,jobject coreInterface) {

    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        hm_bt_core_clock();
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingReadNotification(JNIEnv *env,
                                                                        jobject instance,
                                                                        jobject coreInterface,
                                                                        jbyteArray mac_,
                                                                        jint characteriistic_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);

        hm_bt_core_sensing_read_notification(0, (uint8_t*)mac, (hm_characteristic)characteriistic_);

        env->ReleaseByteArrayElements( mac_, mac, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingReadResponse(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                    jbyteArray data_, jint size,
                                                                    jint offset, jbyteArray mac_,
                                                                    jint characteriistic_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *data = env->GetByteArrayElements( data_, NULL);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);

        hm_bt_core_sensing_read_response(0, (uint8_t*)data,size,offset,(uint8_t*)mac,(hm_characteristic)characteriistic_);

        env->ReleaseByteArrayElements( data_, data, 0);
        env->ReleaseByteArrayElements( mac_, mac, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingWriteResponse(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                     jbyteArray mac_,
                                                                     jint characteriistic_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);

        hm_bt_core_sensing_write_response(0, (uint8_t*)mac,(hm_characteristic)characteriistic_);

        env->ReleaseByteArrayElements( mac_, mac, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingPingNotification(JNIEnv *env,
                                                                        jobject instance,jobject coreInterface,
                                                                        jbyteArray mac_,
                                                                        jint characteriistic_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);

        hm_bt_core_sensing_ping_notification(0, (uint8_t*)mac,(hm_characteristic)characteriistic_);

        env->ReleaseByteArrayElements( mac_, mac, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingProcessAdvertisement(JNIEnv *env,
                                                                            jobject instance,jobject coreInterface,
                                                                            jbyteArray mac_,
                                                                            jbyteArray data_,
                                                                            jint size) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);
        jbyte *data = env->GetByteArrayElements( data_, NULL);

        hm_bt_core_sensing_process_advertisement((uint8_t*)mac,0, (uint8_t*)data,size);

        env->ReleaseByteArrayElements( mac_, mac, 0);
        env->ReleaseByteArrayElements( data_, data, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingDiscoveryEvent(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                      jbyteArray mac_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);

        hm_bt_core_sensing_discovery_event((uint8_t*)mac);

        env->ReleaseByteArrayElements( mac_, mac, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingScanStart(JNIEnv *env, jobject instance, jobject coreInterface) {

    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        hm_bt_core_sensing_scan_start();
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION


}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingConnect(JNIEnv *env, jobject instance,jobject coreInterface,
                                                               jbyteArray mac_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);

        hm_bt_core_sensing_connect(0, (uint8_t*)mac);

        env->ReleaseByteArrayElements( mac_, mac, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSensingDisconnect(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                  jbyteArray mac_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);

        hm_bt_core_sensing_disconnect((uint8_t*)mac);

        env->ReleaseByteArrayElements( mac_, mac, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCorelinkConnect(JNIEnv *env, jobject instance,jobject coreInterface,
                                                            jbyteArray mac_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);

        hm_bt_core_link_connect(0, (uint8_t*)mac);

        env->ReleaseByteArrayElements( mac_, mac, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCorelinkDisconnect(JNIEnv *env, jobject instance,jobject coreInterface,
                                                               jbyteArray mac_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);

        hm_bt_core_link_disconnect((uint8_t*)mac);

        env->ReleaseByteArrayElements( mac_, mac, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCorelinkIncomingData(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                 jbyteArray data_, jint size,
                                                                 jbyteArray mac_,jint characteriistic_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *data = env->GetByteArrayElements( data_, NULL);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);

        hm_bt_core_link_incoming_data(0, (uint8_t*)data,size,(uint8_t*)mac,(hm_characteristic)characteriistic_);

        env->ReleaseByteArrayElements( data_, data, 0);
        env->ReleaseByteArrayElements( mac_, mac, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCorelinkWriteResponse(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                 jbyteArray mac_,jint characteriistic_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);

        hm_bt_core_link_write_response(0, (uint8_t*)mac,(hm_characteristic)characteriistic_);

        env->ReleaseByteArrayElements( mac_, mac, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSendCustomCommand(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                  jbyteArray data_, jint size,
                                                                  jbyteArray mac_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *data = env->GetByteArrayElements( data_, NULL);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);

        sendSecureContainerUsingMac(0, (uint8_t*)mac, (uint8_t*)data, size, 0, 0, 1);

        env->ReleaseByteArrayElements( data_, data, 0);
        env->ReleaseByteArrayElements( mac_, mac, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSendReadDeviceCertificate(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                  jbyteArray mac_, jbyteArray nonce_,
                                                                  jbyteArray caSignature_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *mac = env->GetByteArrayElements( mac_, NULL);
        jbyte *nonce = env->GetByteArrayElements( nonce_, NULL);
        jbyte *caSignature = env->GetByteArrayElements( caSignature_, NULL);

        hm_api_send_read_device_certificate(0, (uint8_t*)mac, (uint8_t*)nonce, (uint8_t*)caSignature);

        env->ReleaseByteArrayElements( mac_, mac, 0);
        env->ReleaseByteArrayElements( nonce_, nonce, 0);
        env->ReleaseByteArrayElements( caSignature_, caSignature, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSendRegisterAccessCertificate(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                          jbyteArray certificate_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);
        jbyte *certificate = env->GetByteArrayElements( certificate_, NULL);

        hm_certificate_t cert;
        hm_cert_get_as_struct((uint8_t*)certificate, &cert);
        hm_api_send_register_access_certificate(0, &cert);

        env->ReleaseByteArrayElements( certificate_, certificate, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreCryptoCreateKeys(JNIEnv *env, jobject instance,
                                                                  jbyteArray privateKey_,
                                                                  jbyteArray publicKey_) {
    try{
        jbyte *privatek = env->GetByteArrayElements( privateKey_, NULL);
        jbyte *publick = env->GetByteArrayElements( publicKey_, NULL);

        hm_crypto_openssl_create_keys((uint8_t*)privatek, (uint8_t*)publick, true);

        env->ReleaseByteArrayElements( privateKey_, privatek, 0);
        env->ReleaseByteArrayElements( publicKey_, publick, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreCryptoAddSignature(JNIEnv *env, jobject instance,
                                                                 jbyteArray data_, jint size,
                                                                 jbyteArray privateKey_, jbyteArray signature_) {

    try{
        jbyte *data = env->GetByteArrayElements( data_, NULL);
        jbyte *privatek = env->GetByteArrayElements( privateKey_, NULL);
        jbyte *signature = env->GetByteArrayElements( signature_, NULL);

        hm_crypto_openssl_signature((uint8_t*)data, size, (uint8_t*)privatek, (uint8_t*)signature);

        env->ReleaseByteArrayElements( data_, data, 0);
        env->ReleaseByteArrayElements( privateKey_, privatek, 0);
        env->ReleaseByteArrayElements( signature_, signature, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT jint JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreCryptoValidateSignature(JNIEnv *env, jobject instance,
        jbyteArray data_, jint size,
        jbyteArray pubKey_, jbyteArray signature_) {

    try{
        jbyte *data = env->GetByteArrayElements( data_, NULL);
        jbyte *public_key = env->GetByteArrayElements( pubKey_, NULL);
        jbyte *signature = env->GetByteArrayElements( signature_, NULL);

        jint retvalue = hm_crypto_openssl_verify((uint8_t*)data, size, (uint8_t*)public_key, (uint8_t*)signature);

        env->ReleaseByteArrayElements( data_, data, 0);
        env->ReleaseByteArrayElements( pubKey_, public_key, 0);
        env->ReleaseByteArrayElements( signature_, signature, 0);
        return retvalue;
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

    return 0;
}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreTelematicsReceiveData(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                      jint length, jbyteArray data_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);

        jbyte *data = env->GetByteArrayElements( data_, NULL);

        hm_bt_core_telematics_receive_data(0, length, (uint8_t*)data);

        env->ReleaseByteArrayElements( data_, data, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSendTelematicsCommand(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                      jbyteArray serial_, jbyteArray nonce_, jint length, jbyteArray data_) {
    try{
        prepareCallbackFunctions(env,instance,coreInterface);

        jbyte *serial = env->GetByteArrayElements( serial_, NULL);
        jbyte *nonce = env->GetByteArrayElements( nonce_, NULL);
        jbyte *data = env->GetByteArrayElements( data_, NULL);

        hm_api_send_telematics_command(0, (uint8_t*)serial, (uint8_t*)nonce, length, (uint8_t*)data, 0, 0, 1);

        env->ReleaseByteArrayElements( data_, data, 0);
        env->ReleaseByteArrayElements( nonce_, nonce, 0);
        env->ReleaseByteArrayElements( serial_, serial, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSendRevoke(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                      jbyteArray serial_) {
    try{
          prepareCallbackFunctions(env,instance,coreInterface);

          jbyte *serial = env->GetByteArrayElements( serial_, NULL);

          sendRevoke(0, (uint8_t*)serial);

          env->ReleaseByteArrayElements( serial_, serial, 0);
    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSetLogLevel(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                      jint level) {
    try{
          prepareCallbackFunctions(env,instance,coreInterface);

          hm_bt_log_set_level(level);

    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

JNIEXPORT void JNICALL
Java_com_highmobility_btcore_HMBTCore_HMBTCoreSetMTU(JNIEnv *env, jobject instance,jobject coreInterface,
                                                                      jbyteArray mac_, jint mtu) {
    try{
          prepareCallbackFunctions(env,instance,coreInterface);

          jbyte *mac = env->GetByteArrayElements( mac_, NULL);

          hm_bt_core_set_mtu((uint8_t*)mac, mtu);

          env->ReleaseByteArrayElements( mac_, mac, 0);

    }
    CATCH_CPP_EXCEPTION_AND_THROW_JAVA_EXCEPTION

}

}