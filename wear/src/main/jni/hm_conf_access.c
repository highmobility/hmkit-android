#include "hm_conf_access.h"
#include "hm_conf.h"
#include <string.h>

void hm_conf_access_get_ca_public_key(uint8_t *publicKey){
  uint8_t key[64] = {HM_CONFIG_CA_PUBLIC_KEY};
  memcpy(publicKey,key,64);
}

void hm_conf_access_get_ibeacon_uuid(uint8_t *uuid){
  uint8_t uuidNew[16] = {HM_CONFIG_IBEACON_UUID};
  memcpy(uuid,uuidNew,16);
}

void hm_conf_access_get_appid_issure_uuid(uint8_t *uuid){
  uint8_t uuidNew[16] = {HM_CONFIG_APP_ID, HM_CONFIG_ISSUER};
  memcpy(uuid,uuidNew,16);
}

void hm_conf_access_get_cu_uuid(uint8_t *uuid){
  uint8_t uuidNew[16] = {HM_CONFIG_CU_UUID};
  memcpy(uuid,uuidNew,16);
}

void hm_conf_access_get_txrx_uuid(uint8_t *uuid){
  uint8_t uuidNew[16] = {HM_CONFIG_TXRX_UUID};
  memcpy(uuid,uuidNew,16);
}

uint16_t hm_conf_access_get_txrx_service(){
  return HM_CONFIG_TXRX_SERVICE;
}

uint16_t hm_conf_access_get_txrx_tx_char(){
  return HM_CONFIG_TXRX_TX_CHAR;
}

uint16_t hm_conf_access_get_txrx_rx_char(){
  return HM_CONFIG_TXRX_RX_CHAR;
}

uint16_t hm_conf_access_get_txrx_ping_char(){
  return HM_CONFIG_TXRX_PING_CHAR;
}

void hm_conf_access_get_issuer(uint8_t *issuer){
  uint8_t issuerNew[4] = {HM_CONFIG_ISSUER};
  memcpy(issuer,issuerNew,4);
}

void hm_conf_access_get_app_id(uint8_t *appId){
  uint8_t appIdNew[12] = {HM_CONFIG_APP_ID};
  memcpy(appId,appIdNew,12);
}
