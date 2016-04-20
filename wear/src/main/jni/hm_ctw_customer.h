/*
 * hm_ctw_customer.h
 *
 *      Author: High-Mobility
 */

#ifndef HM_CTW_CUSTOMER_H_
#define HM_CTW_CUSTOMER_H_

#include <stdbool.h>

#include "hm_ctw_api.h"

void hm_ctw_init();
void hm_ctw_ping();

//AUTH
void hm_ctw_authorised_device_added(hm_device_t *device, uint8_t error);

//PERSISTENCE
void hm_ctw_authorised_device_updated(hm_device_t *device, uint8_t error); //cert update

//Proximity
void hm_ctw_entered_proximity(hm_device_t *device);
void hm_ctw_proximity_measured(hm_device_t *device, uint8_t receiver_count, hm_receiver_t *receivers);
void hm_ctw_exited_proximity(hm_device_t *device);

//Callback
void hm_ctw_command_received(hm_device_t *device, uint8_t *data, uint8_t *length, uint8_t *error);
uint32_t hm_ctw_get_device_certificate_failed(hm_device_t *device, uint8_t *nonce); //ret false on, et ei jätka
void hm_ctw_device_certificate_registered(hm_device_t *device, uint8_t *public_key, uint8_t error);

#endif /* HM_CTW_CUSTOMER_H_ */
