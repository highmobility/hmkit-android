
#include "hm_api_callback.h"
#include "hm_cert.h"
#include <string.h>
#include "hm_config.h"
#include "hmbtcore.h"
#include "hm_api.h"

void hm_api_callback_init()
{

}

void hm_api_callback_ping()
{

}

void hm_api_callback_clock()
{

}

void hm_api_callback_authorised_device_added(hm_device_t *device, uint8_t error)
{

}

void hm_api_callback_authorised_device_updated(hm_device_t *device, uint8_t error)
{

}

void hm_api_callback_entered_proximity(uint64_t appContxtId, hm_device_t *device)
{
    jclass cls = (*envRef)->FindClass(envRef, "com/highmobility/btcore/HMDevice");
    jmethodID constructor = (*envRef)->GetMethodID(envRef,cls, "<init>", "()V");
    jmethodID setMac = (*envRef)->GetMethodID(envRef,cls, "setMac", "([B)V");
    jmethodID setSerial = (*envRef)->GetMethodID(envRef,cls, "setSerial", "([B)V");
    jmethodID setIsAuthenticated = (*envRef)->GetMethodID(envRef,cls, "setIsAuthenticated", "(I)V");
    jmethodID setAppId = (*envRef)->GetMethodID(envRef,cls, "setAppId", "([B)V");

    jobject obj = (*envRef)->NewObject(envRef,cls, constructor);

    jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
    (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jint*) device->mac );

    jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
    (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jint*) device->serial_number );

    jbyteArray appid_ = (*envRef)->NewByteArray(envRef,12);
    (*envRef)->SetByteArrayRegion(envRef, appid_, 0, 12, (const jint*) device->app_id );

    (*envRef)->CallVoidMethod(envRef, obj, setMac, mac_);
    (*envRef)->CallVoidMethod(envRef, obj, setSerial, serial_);
    (*envRef)->CallVoidMethod(envRef, obj, setIsAuthenticated, device->is_authorised);
    (*envRef)->CallVoidMethod(envRef, obj, setAppId, appid_);


    (*envRef)->CallVoidMethod(envRef, coreInterfaceRef, interfaceMethodHMApiCallbackEnteredProximity, obj);

    if ((*envRef)->ExceptionCheck(envRef)) {
        (*envRef)->ExceptionClear(envRef);
      }
}

void hm_api_callback_proximity_measured(uint64_t appContxtId, hm_device_t *device, uint8_t receiver_count, hm_receiver_t *receivers)
{

}

void hm_api_callback_exited_proximity(uint64_t appContxtId, hm_device_t *device)
{
    jclass cls = (*envRef)->FindClass(envRef, "com/highmobility/btcore/HMDevice");
    jmethodID constructor = (*envRef)->GetMethodID(envRef,cls, "<init>", "()V");
    jmethodID setMac = (*envRef)->GetMethodID(envRef,cls, "setMac", "([B)V");
    jmethodID setSerial = (*envRef)->GetMethodID(envRef,cls, "setSerial", "([B)V");
    jmethodID setIsAuthenticated = (*envRef)->GetMethodID(envRef,cls, "setIsAuthenticated", "(I)V");
    jmethodID setAppId = (*envRef)->GetMethodID(envRef,cls, "setAppId", "([B)V");

    jobject obj = (*envRef)->NewObject(envRef,cls, constructor);

    jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
    (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jint*) device->mac );

    jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
    (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jint*) device->serial_number );

    jbyteArray appid_ = (*envRef)->NewByteArray(envRef,12);
    (*envRef)->SetByteArrayRegion(envRef, appid_, 0, 12, (const jint*) device->app_id );

    (*envRef)->CallVoidMethod(envRef, obj, setMac, mac_);
    (*envRef)->CallVoidMethod(envRef, obj, setSerial, serial_);
    (*envRef)->CallVoidMethod(envRef, obj, setIsAuthenticated, device->is_authorised);
    (*envRef)->CallVoidMethod(envRef, obj, setAppId, appid_);

    (*envRef)->CallVoidMethod(envRef, coreInterfaceRef, interfaceMethodHMApiCallbackExitedProximity, obj);

    if ((*envRef)->ExceptionCheck(envRef)) {
        (*envRef)->ExceptionClear(envRef);
    }
}

void hm_api_callback_command_incoming(uint64_t appContxtId, hm_device_t *device, uint8_t *data, uint32_t length, uint8_t *respID, uint16_t respID_size, uint8_t version)
{
    jclass cls = (*envRef)->FindClass(envRef, "com/highmobility/btcore/HMDevice");
    jmethodID constructor = (*envRef)->GetMethodID(envRef,cls, "<init>", "()V");
    jmethodID setMac = (*envRef)->GetMethodID(envRef,cls, "setMac", "([B)V");
    jmethodID setSerial = (*envRef)->GetMethodID(envRef,cls, "setSerial", "([B)V");
    jmethodID setIsAuthenticated = (*envRef)->GetMethodID(envRef,cls, "setIsAuthenticated", "(I)V");
    jmethodID setAppId = (*envRef)->GetMethodID(envRef,cls, "setAppId", "([B)V");

    jobject obj = (*envRef)->NewObject(envRef,cls, constructor);

    jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
    (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jint*) device->mac );

    jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
    (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jint*) device->serial_number );

    jbyteArray appid_ = (*envRef)->NewByteArray(envRef,12);
    (*envRef)->SetByteArrayRegion(envRef, appid_, 0, 12, (const jint*) device->app_id );

    (*envRef)->CallVoidMethod(envRef, obj, setMac, mac_);
    (*envRef)->CallVoidMethod(envRef, obj, setSerial, serial_);
    (*envRef)->CallVoidMethod(envRef, obj, setIsAuthenticated, device->is_authorised);
    (*envRef)->CallVoidMethod(envRef, obj, setAppId, appid_);

    jbyteArray data_ = (*envRef)->NewByteArray(envRef,10024);
    (*envRef)->SetByteArrayRegion(envRef, data_, 0, length, (const jbyte*) data );

    (*envRef)->CallVoidMethod(envRef, coreInterfaceRef, interfaceMethodHMApiCallbackCustomCommandIncoming, obj,data_,length);

    if ((*envRef)->ExceptionCheck(envRef)) {
        (*envRef)->ExceptionClear(envRef);
      }
}

void hm_api_callback_command_response(uint64_t appContxtId, hm_device_t *device, uint8_t *data, uint32_t length, uint8_t *respID, uint16_t respID_size, uint8_t version)
{
    jclass cls = (*envRef)->FindClass(envRef, "com/highmobility/btcore/HMDevice");
    jmethodID constructor = (*envRef)->GetMethodID(envRef,cls, "<init>", "()V");
    jmethodID setMac = (*envRef)->GetMethodID(envRef,cls, "setMac", "([B)V");
    jmethodID setSerial = (*envRef)->GetMethodID(envRef,cls, "setSerial", "([B)V");
    jmethodID setIsAuthenticated = (*envRef)->GetMethodID(envRef,cls, "setIsAuthenticated", "(I)V");
    jmethodID setAppId = (*envRef)->GetMethodID(envRef,cls, "setAppId", "([B)V");

    jobject obj = (*envRef)->NewObject(envRef,cls, constructor);

    jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
    (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jint*) device->mac );

    jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
    (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jint*) device->serial_number );

    jbyteArray appid_ = (*envRef)->NewByteArray(envRef,12);
    (*envRef)->SetByteArrayRegion(envRef, appid_, 0, 12, (const jint*) device->app_id );

    (*envRef)->CallVoidMethod(envRef, obj, setMac, mac_);
    (*envRef)->CallVoidMethod(envRef, obj, setSerial, serial_);
    (*envRef)->CallVoidMethod(envRef, obj, setIsAuthenticated, device->is_authorised);
    (*envRef)->CallVoidMethod(envRef, obj, setAppId, appid_);

    jbyteArray data_ = (*envRef)->NewByteArray(envRef,10024);
    (*envRef)->SetByteArrayRegion(envRef, data_, 0, length, (const jbyte*) data );

    (*envRef)->CallVoidMethod(envRef, coreInterfaceRef, interfaceMethodHMApiCallbackCustomCommandResponse, obj,data_,length);

    if ((*envRef)->ExceptionCheck(envRef)) {
        (*envRef)->ExceptionClear(envRef);
      }
}

void hm_api_callback_error_command_incoming(uint64_t appContxtId, hm_device_t *device, uint8_t command, uint8_t errorType){

    jclass cls = (*envRef)->FindClass(envRef, "com/highmobility/btcore/HMDevice");
    jmethodID constructor = (*envRef)->GetMethodID(envRef,cls, "<init>", "()V");
    jmethodID setMac = (*envRef)->GetMethodID(envRef,cls, "setMac", "([B)V");
    jmethodID setSerial = (*envRef)->GetMethodID(envRef,cls, "setSerial", "([B)V");
    jmethodID setIsAuthenticated = (*envRef)->GetMethodID(envRef,cls, "setIsAuthenticated", "(I)V");
    jmethodID setAppId = (*envRef)->GetMethodID(envRef,cls, "setAppId", "([B)V");

    jobject obj = (*envRef)->NewObject(envRef,cls, constructor);

    jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
    (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jint*) device->mac );

    jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
    (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jint*) device->serial_number );

    jbyteArray appid_ = (*envRef)->NewByteArray(envRef,12);
    (*envRef)->SetByteArrayRegion(envRef, appid_, 0, 12, (const jint*) device->app_id );

    (*envRef)->CallVoidMethod(envRef, obj, setMac, mac_);
    (*envRef)->CallVoidMethod(envRef, obj, setSerial, serial_);
    (*envRef)->CallVoidMethod(envRef, obj, setIsAuthenticated, device->is_authorised);
    (*envRef)->CallVoidMethod(envRef, obj, setAppId, appid_);

    (*envRef)->CallVoidMethod(envRef, coreInterfaceRef, interfaceMethodHMApiCallbackErrorCommandIncoming, obj, command, errorType);

    if ((*envRef)->ExceptionCheck(envRef)) {
        (*envRef)->ExceptionClear(envRef);
      }
}

uint32_t hm_api_callback_get_device_certificate_failed(uint64_t appContxtId, hm_device_t *device, uint8_t *nonce)
{
    jclass cls = (*envRef)->FindClass(envRef, "com/highmobility/btcore/HMDevice");
    jmethodID constructor = (*envRef)->GetMethodID(envRef,cls, "<init>", "()V");
    jmethodID setMac = (*envRef)->GetMethodID(envRef,cls, "setMac", "([B)V");
    jmethodID setSerial = (*envRef)->GetMethodID(envRef,cls, "setSerial", "([B)V");
    jmethodID setIsAuthenticated = (*envRef)->GetMethodID(envRef,cls, "setIsAuthenticated", "(I)V");
    jmethodID setAppId = (*envRef)->GetMethodID(envRef,cls, "setAppId", "([B)V");

    jobject obj = (*envRef)->NewObject(envRef,cls, constructor);

    jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
    (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jint*) device->mac );

    jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
    (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jint*) device->serial_number );

    jbyteArray appid_ = (*envRef)->NewByteArray(envRef,12);
    (*envRef)->SetByteArrayRegion(envRef, appid_, 0, 12, (const jint*) device->app_id );

    (*envRef)->CallVoidMethod(envRef, obj, setMac, mac_);
    (*envRef)->CallVoidMethod(envRef, obj, setSerial, serial_);
    (*envRef)->CallVoidMethod(envRef, obj, setIsAuthenticated, device->is_authorised);
    (*envRef)->CallVoidMethod(envRef, obj, setAppId, appid_);

    jbyteArray nonce_ = (*envRef)->NewByteArray(envRef,9);
    (*envRef)->SetByteArrayRegion(envRef, nonce_, 0, 9, (const jbyte*) nonce );

    jint ret = (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMApiCallbackGetDeviceCertificateFailed, obj,nonce_);

    if ((*envRef)->ExceptionCheck(envRef)) {
        (*envRef)->ExceptionClear(envRef);
        return 0;
      }

    jbyte* nonce_array = (*envRef)->GetByteArrayElements(envRef, nonce_, NULL);
    memcpy(nonce,nonce_array,9);

    return ret;
}

void hm_api_callback_access_certificate_registered(uint64_t appContxtId, hm_device_t *device, uint8_t *public_key, uint8_t error)
{

}

uint32_t hm_api_callback_pairing_requested(uint64_t appContxtId, hm_device_t *device){

    jclass cls = (*envRef)->FindClass(envRef, "com/highmobility/btcore/HMDevice");
    jmethodID constructor = (*envRef)->GetMethodID(envRef,cls, "<init>", "()V");
    jmethodID setMac = (*envRef)->GetMethodID(envRef,cls, "setMac", "([B)V");
    jmethodID setSerial = (*envRef)->GetMethodID(envRef,cls, "setSerial", "([B)V");
    jmethodID setIsAuthenticated = (*envRef)->GetMethodID(envRef,cls, "setIsAuthenticated", "(I)V");
    jmethodID setAppId = (*envRef)->GetMethodID(envRef,cls, "setAppId", "([B)V");

    jobject obj = (*envRef)->NewObject(envRef,cls, constructor);

    jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
    (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jint*) device->mac );

    jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
    (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jint*) device->serial_number );

    jbyteArray appid_ = (*envRef)->NewByteArray(envRef,12);
    (*envRef)->SetByteArrayRegion(envRef, appid_, 0, 12, (const jint*) device->app_id );

    (*envRef)->CallVoidMethod(envRef, obj, setMac, mac_);
    (*envRef)->CallVoidMethod(envRef, obj, setSerial, serial_);
    (*envRef)->CallVoidMethod(envRef, obj, setIsAuthenticated, device->is_authorised);
    (*envRef)->CallVoidMethod(envRef, obj, setAppId, appid_);

    jint ret = (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMApiCallbackPairingRequested, obj);

    if ((*envRef)->ExceptionCheck(envRef)) {
        (*envRef)->ExceptionClear(envRef);
        return 0;
      }

    return ret;
}

void hm_api_callback_telematics_command_incoming(uint64_t appContxtId, hm_device_t *device, uint8_t id, uint32_t length, uint8_t *data, uint8_t *respID, uint16_t respID_size, uint8_t version){
    jclass cls = (*envRef)->FindClass(envRef, "com/highmobility/btcore/HMDevice");
    jmethodID constructor = (*envRef)->GetMethodID(envRef,cls, "<init>", "()V");
    jmethodID setMac = (*envRef)->GetMethodID(envRef,cls, "setMac", "([B)V");
    jmethodID setSerial = (*envRef)->GetMethodID(envRef,cls, "setSerial", "([B)V");
    jmethodID setIsAuthenticated = (*envRef)->GetMethodID(envRef,cls, "setIsAuthenticated", "(I)V");
    jmethodID setAppId = (*envRef)->GetMethodID(envRef,cls, "setAppId", "([B)V");

    jobject obj = (*envRef)->NewObject(envRef,cls, constructor);

    jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
    (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jint*) device->mac );

    jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
    (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jint*) device->serial_number );

    jbyteArray appid_ = (*envRef)->NewByteArray(envRef,12);
    (*envRef)->SetByteArrayRegion(envRef, appid_, 0, 12, (const jint*) device->app_id );

    (*envRef)->CallVoidMethod(envRef, obj, setMac, mac_);
    (*envRef)->CallVoidMethod(envRef, obj, setSerial, serial_);
    (*envRef)->CallVoidMethod(envRef, obj, setIsAuthenticated, device->is_authorised);
    (*envRef)->CallVoidMethod(envRef, obj, setAppId, appid_);

    jbyteArray data_ = (*envRef)->NewByteArray(envRef,length);
    (*envRef)->SetByteArrayRegion(envRef, data_, 0, length, (const jbyte*) data );

    (*envRef)->CallVoidMethod(envRef, coreInterfaceRef, interfaceMethodHMApiCallbackTelematicsCommandIncoming, obj, id, length, data_);

    if ((*envRef)->ExceptionCheck(envRef)) {
        (*envRef)->ExceptionClear(envRef);
      }
}

void hm_api_callback_revoke_response(uint64_t appContxtId, hm_device_t *device, uint8_t *data, uint16_t length, uint8_t status){
jclass cls = (*envRef)->FindClass(envRef, "com/highmobility/btcore/HMDevice");
    jmethodID constructor = (*envRef)->GetMethodID(envRef,cls, "<init>", "()V");
    jmethodID setMac = (*envRef)->GetMethodID(envRef,cls, "setMac", "([B)V");
    jmethodID setSerial = (*envRef)->GetMethodID(envRef,cls, "setSerial", "([B)V");
    jmethodID setIsAuthenticated = (*envRef)->GetMethodID(envRef,cls, "setIsAuthenticated", "(I)V");
    jmethodID setAppId = (*envRef)->GetMethodID(envRef,cls, "setAppId", "([B)V");

    jobject obj = (*envRef)->NewObject(envRef,cls, constructor);

    jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
    (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jint*) device->mac );

    jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
    (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jint*) device->serial_number );

    jbyteArray appid_ = (*envRef)->NewByteArray(envRef,12);
    (*envRef)->SetByteArrayRegion(envRef, appid_, 0, 12, (const jint*) device->app_id );

    (*envRef)->CallVoidMethod(envRef, obj, setMac, mac_);
    (*envRef)->CallVoidMethod(envRef, obj, setSerial, serial_);
    (*envRef)->CallVoidMethod(envRef, obj, setIsAuthenticated, device->is_authorised);
    (*envRef)->CallVoidMethod(envRef, obj, setAppId, appid_);

    jbyteArray data_ = (*envRef)->NewByteArray(envRef,length);
    (*envRef)->SetByteArrayRegion(envRef, data_, 0, length, (const jbyte*) data );

    (*envRef)->CallVoidMethod(envRef, coreInterfaceRef, interfaceMethodHMApiCallbackRevokeResponse, obj, data_, length, status);

    if ((*envRef)->ExceptionCheck(envRef)) {
        (*envRef)->ExceptionClear(envRef);
      }
}

void hm_api_callback_revoke_incoming(uint64_t appContxtId, hm_device_t *device, uint8_t *data, uint16_t *size){
    jclass cls = (*envRef)->FindClass(envRef, "com/highmobility/btcore/HMDevice");
    jmethodID constructor = (*envRef)->GetMethodID(envRef,cls, "<init>", "()V");
    jmethodID setMac = (*envRef)->GetMethodID(envRef,cls, "setMac", "([B)V");
    jmethodID setSerial = (*envRef)->GetMethodID(envRef,cls, "setSerial", "([B)V");
    jmethodID setIsAuthenticated = (*envRef)->GetMethodID(envRef,cls, "setIsAuthenticated", "(I)V");
    jmethodID setAppId = (*envRef)->GetMethodID(envRef,cls, "setAppId", "([B)V");

    jobject obj = (*envRef)->NewObject(envRef,cls, constructor);

    jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
    (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jint*) device->mac );

    jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
    (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jint*) device->serial_number );

    jbyteArray appid_ = (*envRef)->NewByteArray(envRef,12);
    (*envRef)->SetByteArrayRegion(envRef, appid_, 0, 12, (const jint*) device->app_id );

    (*envRef)->CallVoidMethod(envRef, obj, setMac, mac_);
    (*envRef)->CallVoidMethod(envRef, obj, setSerial, serial_);
    (*envRef)->CallVoidMethod(envRef, obj, setIsAuthenticated, device->is_authorised);
    (*envRef)->CallVoidMethod(envRef, obj, setAppId, appid_);

    jbyteArray data_ = (*envRef)->NewByteArray(envRef,200);
    (*envRef)->SetByteArrayRegion(envRef, data_, 0, 200, (const jbyte*) data );

    jintArray size_ = (*envRef)->NewIntArray(envRef,1);
      (*envRef)->SetIntArrayRegion(envRef, size_, 0, 1, (const jint*) size );

    (*envRef)->CallVoidMethod(envRef, coreInterfaceRef, interfaceMethodHMApiCallbackRevokeIncoming, obj, data_, size_);

    jint* size_array = (*envRef)->GetIntArrayElements(envRef, size_, NULL);
      *size = size_array[0];

    jbyte* content_array = (*envRef)->GetByteArrayElements(envRef, data_, NULL);
    memcpy(data,content_array,*size);

    if ((*envRef)->ExceptionCheck(envRef)) {
        (*envRef)->ExceptionClear(envRef);
    }
}