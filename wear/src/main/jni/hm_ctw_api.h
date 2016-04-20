/*
 * hm_ctw_api.h
 *
 *      Author: High-Mobility
 */

#ifndef HM_CTW_API_H_
#define HM_CTW_API_H_

#include <stdint.h>

typedef struct {
  uint8_t gaining_serial[9];
  uint8_t public_key[64];
  uint8_t providing_serial[9];
  uint8_t start_date[5];
  uint8_t end_date[5];
  uint8_t permissions_size;
  uint8_t permissions[16];
  uint8_t ca_signature[64];
} hm_certificate_t;

typedef struct {
  uint8_t   mac[6];
  uint8_t   serial_number[9];
  //hm_certificate_t certificate;
  uint8_t   is_authorised;
  uint16_t   major;
  uint16_t   minor;
} hm_device_t;

typedef struct {
  uint8_t  id;
  int8_t   rssi;
} hm_receiver_t;

uint8_t hm_ctw_retrieve_authorised_devices(uint8_t *device_size, hm_device_t *devices); //Loeb mälust cerdid ainult

//AUTH
uint8_t hm_ctw_read_device_certificate(uint8_t *mac, uint8_t *nonce, uint8_t *ca_signature);

//PAIR
uint8_t hm_ctw_register_access_certificate(hm_certificate_t *cert);

//PERSISTENCE
uint8_t hm_ctw_get_public_key(uint8_t *public_key);
uint8_t hm_ctw_get_serial_number(uint8_t *serial_number);
uint8_t hm_ctw_get_access_certificate(uint8_t *serial_number, uint8_t *cert);
uint8_t hm_ctw_store_access_certificate(hm_certificate_t *cert);
uint8_t hm_ctw_remove_access_certificate(uint8_t *serial_number);

//SECURE CONTAINER
uint8_t hw_ctw_send_command(uint8_t *serial_number, uint8_t *data, uint8_t size);

//BLE
uint8_t hw_ctw_ble_on(uint8_t action);

#endif /* HM_CTW_API_H_ */
