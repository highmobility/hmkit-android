
#include "hm_connectivity_hal.h"
#include "hm_bt_core.h"

#include "hm_conf_access.h"

#include "hmbtcore.h"
#include <unistd.h>

#define BLE_GATT_HVX_NOTIFICATION 0x01
#define MAX_CLIENTS 5

void hm_bt_hal_delay_ms(uint32_t number_of_ms){
  usleep(1000*number_of_ms);
}

uint32_t hm_bt_hal_scan_start(){
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalScanStart);
}

uint32_t hm_bt_hal_scan_stop(){
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalScanStop);
}

uint32_t hm_bt_hal_advertisement_start(uint8_t *issuerId, uint8_t *appId){
  jbyteArray issuer_ = (*envRef)->NewByteArray(envRef,4);
  (*envRef)->SetByteArrayRegion(envRef, issuer_, 0, 4, (const jbyte*) issuerId );

  jbyteArray appId_ = (*envRef)->NewByteArray(envRef,12);
  (*envRef)->SetByteArrayRegion(envRef, appId_, 0, 12, (const jbyte*) appId );
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalAdvertisementStart,issuer_,appId_);
}

uint32_t hm_bt_hal_advertisement_stop(){
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalAdvertisementStop);
}

uint32_t hm_bt_hal_write_data(uint8_t *mac, uint16_t length, uint8_t *data, hm_characteristic characteristic){

  jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
  (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jbyte*) mac );

  jbyteArray data_ = (*envRef)->NewByteArray(envRef,length);
  (*envRef)->SetByteArrayRegion(envRef, data_, 0, length, (const jbyte*) data );

  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalWriteData, mac_, length, data_, characteristic);
}

uint32_t hm_bt_hal_read_data(uint8_t *mac, uint16_t offset, hm_characteristic characteristic){
  jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
  (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jbyte*) mac );

  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalReadData, mac_, offset, characteristic);
}

uint32_t hm_bt_hal_service_discovery(uint8_t *mac){
  jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
  (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jbyte*) mac );
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalServiceDiscovery, mac_);
}

uint32_t hm_bt_hal_init(){
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalInit);
}

uint32_t hm_bt_hal_connect(const uint8_t *mac, uint8_t macType){
  jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
  (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jbyte*) mac );
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalConnect, mac_);
}

uint32_t hm_bt_hal_disconnect(uint8_t *mac){
  jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
  (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jbyte*) mac );
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalDisconnect, mac_);
}

uint32_t hm_bt_hal_read_info(uint8_t *mac, uint16_t offset, hm_characteristic characteristic){
  return 0; //TODO for sensing
}

uint32_t hm_bt_hal_get_current_date_time(uint8_t *day, uint8_t *month, uint8_t *year, uint8_t *minute, uint8_t *hour){
  return 0;
}

uint32_t hm_bt_hal_telematics_send_data(uint8_t *issuer, uint8_t *serial, uint16_t length, uint8_t *data){

  jbyteArray issuer_ = (*envRef)->NewByteArray(envRef,4);
  (*envRef)->SetByteArrayRegion(envRef, issuer_, 0, 4, (const jbyte*) issuer );
  jbyteArray serial_ = (*envRef)->NewByteArray(envRef,9);
  (*envRef)->SetByteArrayRegion(envRef, serial_, 0, 9, (const jbyte*) serial );

  jbyteArray data_ = (*envRef)->NewByteArray(envRef,length);
  (*envRef)->SetByteArrayRegion(envRef, data_, 0, length, (const jbyte*) data );

  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalTelematicsSendData, issuer_, serial_, length, data_);
}
