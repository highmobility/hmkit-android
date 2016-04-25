#ifndef HM_CERT_H_
#define HM_CERT_H_

#include <stdint.h>
#include "hm_ctw_api.h"

uint8_t hm_cert_print(uint8_t *cert);
uint16_t hm_cert_size(uint8_t *cert);

uint8_t hm_cert_get_gaining_serial(uint8_t *cert, uint8_t *serial);
uint8_t hm_cert_get_gaining_public_key(uint8_t *cert, uint8_t *publicKey);
uint8_t hm_cert_get_providing_serial(uint8_t *cert, uint8_t *serial);
uint8_t hm_cert_get_start_date(uint8_t *cert, uint8_t *date);
uint8_t hm_cert_get_end_date(uint8_t *cert, uint8_t *date);
uint8_t hm_cert_get_permissions(uint8_t *cert, uint8_t *permissionsSize, uint8_t *permissions);
uint8_t hm_cert_get_signature(uint8_t *cert, uint8_t *signature);
uint8_t hm_cert_get_as_bytes( hm_certificate_t *certificate, uint8_t *cert);
uint8_t hm_cert_get_as_struct(uint8_t *cert, hm_certificate_t *certificate);

#endif /* HM_CERT_H_ */
