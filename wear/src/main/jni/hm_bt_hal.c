
#include "hm_bt_hal.h"
#include "hm_bt_core.h"

#include "hm_conf_access.h"

#define BLE_GATT_HVX_NOTIFICATION 0x01
#define MAX_CLIENTS 5

void hm_bt_hal_delay_ms(uint32_t number_of_ms){

}

uint32_t hm_bt_hal_scan_start(){
  return 0;
}

uint32_t hm_bt_hal_scan_stop(){
  return 0;
}

uint32_t hm_bt_hal_write_data(uint8_t *mac, uint16_t length, uint8_t *data){
  return 0;
}

uint32_t hm_bt_hal_read_data(uint8_t *mac, uint16_t offset){
  return 0;
}

uint32_t hm_bt_hal_service_discovery(uint8_t *mac){
  return 0;
}

uint32_t hm_bt_hal_init(){
  return 0;
}

uint32_t hm_bt_hal_connect(const uint8_t *mac){
  return 0;
}

uint32_t hm_bt_hal_disconnect(uint8_t *mac){
  return 0;
}
