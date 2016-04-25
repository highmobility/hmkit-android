/*
 * hm_bt_core.h
 *
 *  Created on: Oct 21, 2014
 *      Author: cannedapps
 */

#ifndef HM_BT_CORE_H_
#define HM_BT_CORE_H_

#include <stdint.h>
#include <stdbool.h>
#include <string.h>

void hm_bt_core_init(void);
void hm_bt_core_clock(void);

//CORE SENSING

void hm_bt_core_sensing_read_notification(uint8_t *mac);
void hm_bt_core_sensing_read_response(uint8_t *data, uint16_t size, uint16_t offset, uint8_t *mac);

void hm_bt_core_sensing_write_response(uint8_t *mac);

void hm_bt_core_sensing_ping_notification(uint8_t *mac);

void hm_bt_core_sensing_process_advertisement(const uint8_t *mac, uint8_t *data, uint8_t size);
void hm_bt_core_sensing_discovery_event(uint8_t *mac);
void hm_bt_core_sensing_scan_start(void);

void hm_bt_core_sensing_connect(uint8_t *mac);
void hm_bt_core_sensing_disconnect(uint8_t *mac);

void hm_bt_core_explode(uint16_t source, uint8_t *dest);
uint16_t hm_bt_core_implode(uint8_t *msb);

//CORE LINK

void hm_bt_core_link_connect(uint8_t *mac);
void hm_bt_core_link_disconnect(uint8_t *mac);

void hm_bt_core_link_incoming_data(uint8_t *data, uint16_t size, uint8_t *mac);

//CORE INTERNAL API FOR CTW API
void hm_bt_ble_on(uint8_t action);
void sendAuthenticate(uint8_t *serial);
void sendGetDeviceCertificateRequest(uint8_t isctw, uint8_t *requestData, uint8_t *mac);
void sendRegisterCertificate(uint8_t isctw, uint8_t *certData, uint8_t size, uint8_t *serial);
void sendRevoke(uint8_t *serial);
void sendSecureContainer(uint8_t *serial, uint8_t *dataBuffer, uint8_t size);

#endif /* hm_bt_core_H_ */
