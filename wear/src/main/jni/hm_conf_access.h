#ifndef HM_CONF_ACCESS_H_
#define HM_CONF_ACCESS_H_

#include <stdint.h>

void hm_conf_access_get_ca_public_key(uint8_t *publicKey);
void hm_conf_access_get_ibeacon_uuid(uint8_t *uuid);
void hm_conf_access_get_appid_issure_uuid(uint8_t *uuid);
void hm_conf_access_get_cu_uuid(uint8_t *uuid);
void hm_conf_access_get_txrx_uuid(uint8_t *uuid);
uint16_t hm_conf_access_get_txrx_service();
uint16_t hm_conf_access_get_txrx_tx_char();
uint16_t hm_conf_access_get_txrx_rx_char();
uint16_t hm_conf_access_get_txrx_ping_char();
void hm_conf_access_get_issuer(uint8_t *issuer);
void hm_conf_access_get_app_id(uint8_t *appId);

#endif /* HM_CONF_ACCESS_H_ */
