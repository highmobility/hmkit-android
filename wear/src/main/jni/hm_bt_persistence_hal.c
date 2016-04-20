
#include "hm_bt_persistence_hal.h"

uint32_t hm_bt_persistence_hal_get_serial(uint8_t *serial){
  return 0;
}

uint32_t hm_bt_persistence_hal_get_local_public_key(uint8_t *public){
  return 0;
}

uint32_t hm_bt_persistence_hal_add_public_key(uint8_t *serial, uint8_t *public, uint8_t *startDate, uint8_t *endDate, uint8_t commandSize, uint8_t *command){
  return 0;
}

uint32_t hm_bt_persistence_hal_get_public_key(uint8_t *serial, uint8_t *public, uint8_t *startDate, uint8_t *endDate, uint8_t *commandSize, uint8_t *command){
  return 0;
}

uint32_t hm_bt_persistence_hal_remove_public_key(uint8_t *serial){
  return 0;
}

uint32_t hm_bt_persistence_hal_add_stored_certificate(uint8_t *cert, uint16_t size){
  return 0;
}

uint32_t hm_bt_persistence_hal_get_stored_certificate(uint8_t *cert, uint16_t *size){
  return 0;
}
uint32_t hm_bt_persistence_hal_erase_stored_certificate(){
  return 0;
}
