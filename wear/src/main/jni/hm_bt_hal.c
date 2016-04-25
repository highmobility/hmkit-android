
#include "hm_bt_hal.h"
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

uint32_t hm_bt_hal_advertisement_start(){
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalAdvertisementStart);
}

uint32_t hm_bt_hal_advertisement_stop(){
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalAdvertisementStop);
}

uint32_t hm_bt_hal_write_data(uint8_t *mac, uint16_t length, uint8_t *data){

  jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
  (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jbyte*) mac );

  jbyteArray data_ = (*envRef)->NewByteArray(envRef,length);
  (*envRef)->SetByteArrayRegion(envRef, data_, 0, length, (const jbyte*) data );

  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalWriteData, mac_, length, data_);
}

uint32_t hm_bt_hal_read_data(uint8_t *mac, uint16_t offset){
  jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
  (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jbyte*) mac );

  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalReadData, mac_, offset);
}

uint32_t hm_bt_hal_service_discovery(uint8_t *mac){
  jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
  (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jbyte*) mac );
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalServiceDiscovery, mac_);
}

uint32_t hm_bt_hal_init(){
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalInit);
}

uint32_t hm_bt_hal_connect(const uint8_t *mac){
  jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
  (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jbyte*) mac );
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalConnect, mac_);
}

uint32_t hm_bt_hal_disconnect(uint8_t *mac){
  jbyteArray mac_ = (*envRef)->NewByteArray(envRef,6);
  (*envRef)->SetByteArrayRegion(envRef, mac_, 0, 6, (const jbyte*) mac );
  return (*envRef)->CallIntMethod(envRef, coreInterfaceRef, interfaceMethodHMBTHalDisconnect, mac_);
}