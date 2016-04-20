#include "hm_ctw_api.h"
#include "string.h"
#include "hm_cert.h"

#include "hm_bt_core.h"
#include "hm_bt_persistence_hal.h"

uint8_t hm_ctw_retrieve_authorised_devices(uint8_t *device_size, hm_device_t *devices){
  //Return all authorised devices
  return 0;
}

uint8_t hm_ctw_read_device_certificate(uint8_t *mac, uint8_t *nonce, uint8_t *ca_signature){

  uint8_t data[73];
  memcpy(data,nonce,9);
  memcpy(data + 9,ca_signature,64);

  sendGetDeviceCertificateRequest(1, data, mac);
  return 0;
}

uint8_t hm_ctw_register_access_certificate(hm_certificate_t *cert){

  uint8_t cert_data[180];

  hm_cert_get_as_bytes( cert, cert_data);

  sendRegisterCertificate(1, cert_data, 92 + 1 + cert->permissions_size + 64, cert->providing_serial);

  return 0;
}

uint8_t hm_ctw_get_public_key(uint8_t *public_key){
  hm_bt_persistence_hal_get_local_public_key(public_key);
  return 0;
}

uint8_t hm_ctw_get_serial_number(uint8_t *serial_number){
  return hm_bt_persistence_hal_get_serial(serial_number);
}

uint8_t hm_ctw_get_access_certificate(uint8_t *serial_number, uint8_t *cert){

  hm_certificate_t cert_get;
  memcpy(cert_get.gaining_serial,serial_number,9);

  if(hm_bt_persistence_hal_get_public_key(serial_number, cert_get.public_key, cert_get.start_date, cert_get.end_date, &cert_get.permissions_size, cert_get.permissions) == 0){
    hm_cert_get_as_bytes( &cert_get, cert);
    return hm_cert_size(cert);
  }

  return 0;
}

uint8_t hm_ctw_store_access_certificate(hm_certificate_t *cert){
    hm_bt_persistence_hal_add_public_key(cert->gaining_serial, cert->public_key, cert->start_date, cert->end_date, cert->permissions_size, cert->permissions);
    sendAuthenticate(cert->gaining_serial);
  return 0;
}

uint8_t hm_ctw_remove_access_certificate(uint8_t *serial_number){
  sendRevoke(serial_number);
  return 0;
}

uint8_t hw_ctw_ble_on(uint8_t action){
  hm_bt_ble_on(action);
  return 0;
}

uint8_t hw_ctw_send_command(uint8_t *serial_number, uint8_t *data, uint8_t size){
  sendSecureContainer(serial_number, data, size);

  return 0;
}
