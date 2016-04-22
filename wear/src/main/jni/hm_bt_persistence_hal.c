
#include "hm_bt_persistence_hal.h"
#include "hmbtcore.h"
#include "hm_bt_debug_hal.h"
#include <string.h>

uint32_t hm_bt_persistence_hal_get_serial(uint8_t *serial){

  jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
  (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jbyte*) serial );
  jint ret = (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMPersistenceHalgetSerial, serial_);

  jbyte* content_array = (*envRef)->GetByteArrayElements(envRef, serial_, NULL);
  memcpy(serial,content_array,9);

  return ret;
}

uint32_t hm_bt_persistence_hal_get_local_public_key(uint8_t *public){

  jbyteArray public_ = (*envRef)->NewByteArray(envRef,64);
  (*envRef)->SetByteArrayRegion(envRef, public_, 0, 64, (const jbyte*) public );
  jint ret = (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMPersistenceHalgetLocalPublicKey, public_);

  jbyte* content_array = (*envRef)->GetByteArrayElements(envRef, public_, NULL);
  memcpy(public,content_array,64);

  return ret;
}

uint32_t hm_bt_persistence_hal_add_public_key(uint8_t *serial, uint8_t *public, uint8_t *startDate, uint8_t *endDate, uint8_t commandSize, uint8_t *command){

  jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
  (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jbyte*) serial );

  jbyteArray public_ = (*envRef)->NewByteArray(envRef,64);
  (*envRef)->SetByteArrayRegion(envRef, public_, 0, 64, (const jbyte*) public );

  jbyteArray startDate_ = (*envRef)->NewByteArray(envRef,5);
  (*envRef)->SetByteArrayRegion(envRef, startDate_, 0, 5, (const jbyte*) startDate );

  jbyteArray endDate_ = (*envRef)->NewByteArray(envRef,5);
  (*envRef)->SetByteArrayRegion(envRef, endDate_, 0, 5, (const jbyte*) endDate );

  jbyteArray command_ = (*envRef)->NewByteArray(envRef,commandSize);
  (*envRef)->SetByteArrayRegion(envRef, command_, 0, commandSize, (const jbyte*) command );

  jint ret = (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMPersistenceHaladdPublicKey, serial_, public_, startDate_, endDate_, commandSize, command_);

  return ret;
}

uint32_t hm_bt_persistence_hal_get_public_key(uint8_t *serial, uint8_t *public, uint8_t *startDate, uint8_t *endDate, uint8_t *commandSize, uint8_t *command){

  jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
  (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jbyte*) serial );

  jbyteArray public_ = (*envRef)->NewByteArray(envRef,64);
  (*envRef)->SetByteArrayRegion(envRef, public_, 0, 64, (const jbyte*) public );

  jbyteArray startDate_ = (*envRef)->NewByteArray(envRef,5);
  (*envRef)->SetByteArrayRegion(envRef, startDate_, 0, 5, (const jbyte*) startDate );

  jbyteArray endDate_ = (*envRef)->NewByteArray(envRef,5);
  (*envRef)->SetByteArrayRegion(envRef, endDate_, 0, 5, (const jbyte*) endDate );

  jbyteArray command_ = (*envRef)->NewByteArray(envRef,commandSize);
  (*envRef)->SetByteArrayRegion(envRef, command_, 0, commandSize, (const jbyte*) command );

  jint ret = (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMPersistenceHalgetPublicKey, serial_, public_, startDate_, endDate_, *commandSize, command_);

  jbyte* serial_array = (*envRef)->GetByteArrayElements(envRef, serial_, NULL);
  memcpy(serial,serial_array,9);

  jbyte* public_array = (*envRef)->GetByteArrayElements(envRef, public_, NULL);
  memcpy(public,public_array,64);

  jbyte* startDate_array = (*envRef)->GetByteArrayElements(envRef, startDate_, NULL);
  memcpy(startDate,startDate_array,5);

  jbyte* endDate_array = (*envRef)->GetByteArrayElements(envRef, endDate_, NULL);
  memcpy(endDate,endDate_array,5);

  jbyte* command_array = (*envRef)->GetByteArrayElements(envRef, command_, NULL);
  memcpy(command,command_array,*commandSize);

  //TODO parse data

  return ret;
}

uint32_t hm_bt_persistence_hal_remove_public_key(uint8_t *serial){

  jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
  (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jbyte*) serial );
  jint ret = (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMPersistenceHalremovePublicKey, serial_);

  return ret;
}

uint32_t hm_bt_persistence_hal_add_stored_certificate(uint8_t *cert, uint16_t size){

  jbyteArray cert_ = (*envRef)->NewByteArray(envRef,9);
  (*envRef)->SetByteArrayRegion(envRef, cert_, 0, 9, (const jbyte*) cert );
  jint ret = (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMPersistenceHaladdStoredCertificate, cert_, size);

  return ret;
}

uint32_t hm_bt_persistence_hal_get_stored_certificate(uint8_t *cert, uint16_t *size){

  jbyteArray cert_ = (*envRef)->NewByteArray(envRef,*size);
  (*envRef)->SetByteArrayRegion(envRef, cert_, 0, *size, (const jbyte*) cert );
  jint ret = (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMPersistenceHalgetStoredCertificate, cert_, *size);

  jbyte* content_array = (*envRef)->GetByteArrayElements(envRef, cert_, NULL);
  memcpy(cert,content_array,*size);

  return ret;
}

uint32_t hm_bt_persistence_hal_erase_stored_certificate(){

  jint ret = (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMPersistenceHaleraseStoredCertificate);

  return ret;
}
