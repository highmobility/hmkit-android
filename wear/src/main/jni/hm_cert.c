#include "hm_cert.h"
#include "string.h"
#include "hm_bt_debug_hal.h"

uint8_t hm_cert_print(uint8_t *cert){

  hm_bt_debug_hal_log("Gaining serial");

  uint8_t i;
  for(i = 0 ; i < 9; i++){
    hm_bt_debug_hal_log("%02X ",cert[i]);
  }
  hm_bt_debug_hal_log("");

  hm_bt_debug_hal_log("Public key");

  for(i = 0 ; i < 64; i++){
    hm_bt_debug_hal_log("%02X ",cert[i + 9]);
  }
  hm_bt_debug_hal_log("");

  hm_bt_debug_hal_log("Provaiding serial");

  for(i = 0 ; i < 9; i++){
    hm_bt_debug_hal_log("%02X ",cert[i + 9 + 64]);
  }
  hm_bt_debug_hal_log("");

  hm_bt_debug_hal_log("Start date");

  for(i = 0 ; i < 5; i++){
    hm_bt_debug_hal_log("%02X ",cert[i + 9 + 64 + 9]);
  }
  hm_bt_debug_hal_log("");

  hm_bt_debug_hal_log("End date");

  for(i = 0 ; i < 5; i++){
    hm_bt_debug_hal_log("%02X ",cert[i + 9 + 64 + 9 + 5]);
  }
  hm_bt_debug_hal_log("");

  hm_bt_debug_hal_log("Permissions size");

  uint8_t per = cert[9 + 64 + 9 + 5 + 5];
  hm_bt_debug_hal_log("%d ",per);

  hm_bt_debug_hal_log("");

  hm_bt_debug_hal_log("Permissions");

  for(i = 0 ; i < per; i++){
    hm_bt_debug_hal_log("%02X ",cert[i + 9 + 64 + 9 + 5 + 5 + 1]);
  }
  hm_bt_debug_hal_log("");

  hm_bt_debug_hal_log("Signature");

  for(i = 0 ; i < 64; i++){
    hm_bt_debug_hal_log("%02X ",cert[i + 9 + 64 + 9 + 5 + 5 + 1 + per]);
  }
  hm_bt_debug_hal_log("");

  return 0;
}

uint16_t hm_cert_size(uint8_t *cert){
  uint16_t per = cert[9 + 64 + 9 + 5 + 5];
  return 9 + 64 + 9 + 5 + 5 + 1 + per;
}

uint8_t hm_cert_get_gaining_serial(uint8_t *cert, uint8_t *serial){
  memcpy(serial, cert, 9);
  return 0;
}

uint8_t hm_cert_get_gaining_public_key(uint8_t *cert, uint8_t *publicKey){
  memcpy(publicKey, cert + 9, 64);
  return 0;
}

uint8_t hm_cert_get_providing_serial(uint8_t *cert, uint8_t *serial){
  memcpy(serial, cert + 9 + 64, 9);
  return 0;
}

uint8_t hm_cert_get_start_date(uint8_t *cert, uint8_t *date){
  memcpy(date, cert + 9 + 64 + 9, 5);
  return 0;
}

uint8_t hm_cert_get_end_date(uint8_t *cert, uint8_t *date){
  memcpy(date, cert + 9 + 64 + 9 + 5, 5);
  return 0;
}

uint8_t hm_cert_get_permissions(uint8_t *cert, uint8_t *permissionsSize, uint8_t *permissions){
  *permissionsSize = cert[9 + 64 + 9 + 5 + 5];

  if(*permissionsSize > 16){
    *permissionsSize = 0;
  }

  if(*permissionsSize > 0 && *permissionsSize < 17){
    memcpy(permissions, cert + 9 + 64 + 9 + 5 + 5 + 1, *permissionsSize);
  }

  return 0;
}

uint8_t hm_cert_get_signature(uint8_t *cert, uint8_t *signature){

  uint8_t permissionsSize = 0;
  uint8_t permissions[16];

  hm_cert_get_permissions(cert, &permissionsSize, permissions);

  memcpy(signature, cert + 9 + 64 + 9 + 5 + 5 + 1 + permissionsSize, 64);
  return 0;
}

uint8_t hm_cert_get_as_bytes( hm_certificate_t *certificate, uint8_t *cert){

  memcpy(cert, certificate->gaining_serial,9);
  memcpy(cert + 9, certificate->public_key,64);
  memcpy(cert + 9 + 64, certificate->providing_serial, 9);
  memcpy(cert + 9 + 64 + 9, certificate->start_date,5);
  memcpy(cert + 9 + 64 + 9 + 5, certificate->end_date,5);
  memcpy(cert + 9 + 64 + 9 + 5 + 5, &certificate->permissions_size,1);
  memcpy(cert + 9 + 64 + 9 + 5 + 5 + 1, certificate->permissions,certificate->permissions_size);

  memcpy(cert + 9 + 64 + 9 + 5 + 5 + 1 + certificate->permissions_size, certificate->ca_signature,64);

  return 0;
}

uint8_t hm_cert_get_as_struct(uint8_t *cert, hm_certificate_t *certificate){

  hm_cert_get_gaining_serial(cert, certificate->gaining_serial);
  hm_cert_get_gaining_public_key(cert, certificate->public_key);
  hm_cert_get_providing_serial(cert,certificate->providing_serial);
  hm_cert_get_start_date(cert,certificate->start_date);
  hm_cert_get_end_date(cert,certificate->end_date);
  hm_cert_get_permissions(cert, &certificate->permissions_size, certificate->permissions);
  hm_cert_get_signature(cert,certificate->ca_signature);

  return 0;
}
