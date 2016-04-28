#include "hm_bt_crypto_hal.h"
#include "hm_conf_access.h"
#include "hm_bt_persistence_hal.h"
#include "Crypto.h"
#include <string.h>

uint8_t priv[32] = {0x92, 0x5d, 0x6b, 0xdd, 0xa5, 0x72, 0x0c, 0xbd, 0x76, 0xde, 0x15, 0x50, 0x32, 0x0b, 0x74, 0x2d, 0x25, 0x3e, 0x47, 0x5f, 0xbe, 0xd7, 0x1d, 0x95, 0x23, 0x10, 0xfa, 0xee, 0x73, 0x62, 0xc0, 0x8e };

uint32_t hm_bt_crypto_hal_aes_ecb_block_encrypt(uint8_t *key, uint8_t *cleartext, uint8_t *cipertext){
  return hm_crypto_openssl_aes_iv(key, cleartext, cipertext);
}

uint32_t hm_bt_crypto_hal_ecc_get_ecdh(uint8_t *serial, uint8_t *ecdh){

  uint8_t usepublic[64];
  uint8_t start_date[5];
  uint8_t end_date[5];
  uint8_t command_size;
  uint8_t command[16];

  if(hm_bt_persistence_hal_get_public_key(serial, usepublic, start_date, end_date, &command_size, command) == 1){
    return 1;
  }

  return hm_crypto_openssl_dh(priv, usepublic, ecdh);
}

uint32_t hm_bt_crypto_hal_ecc_add_signature(uint8_t *data, uint8_t size, uint8_t *signature){
  return hm_crypto_openssl_signature(data, size, priv, signature);
}

uint32_t hm_bt_crypto_hal_ecc_validate_signature(uint8_t *data, uint8_t size, uint8_t *signature, uint8_t *serial){

  uint8_t usepublic[64];
  uint8_t start_date[5];
  uint8_t end_date[5];
  uint8_t command_size;
  uint8_t command[16];

  if(hm_bt_persistence_hal_get_public_key(serial, usepublic, start_date, end_date, &command_size, command) == 1){
    return 1;
  }

  return hm_crypto_openssl_verify(data, size, usepublic, signature);
}

uint32_t hm_bt_crypto_hal_ecc_validate_all_signatures(uint8_t *data, uint8_t size, uint8_t *signature){
  return 0;
}

uint32_t hm_bt_crypto_hal_ecc_validate_ca_signature(uint8_t *data, uint8_t size, uint8_t *signature){
  uint8_t ca_pub[64];
  hm_conf_access_get_ca_public_key(ca_pub);
  return hm_crypto_openssl_verify(data, size, ca_pub, signature);
}

uint32_t hm_bt_crypto_hal_hmac(uint8_t *key, uint8_t *data, uint8_t *hmac){
  return hm_crypto_openssl_hmac(data, 256, key, hmac);
}

uint32_t hm_bt_crypto_hal_generate_nonce(uint8_t *nonce){
  return 0;
}
