#include "hm_bt_crypto_hal.h"
#include "hm_conf_access.h"
#include "hm_bt_persistence_hal.h"
#include "Crypto.h"
#include <string.h>
#include <time.h>
#include <stdlib.h>

uint32_t hm_bt_crypto_hal_aes_ecb_block_encrypt(uint8_t *key, uint8_t *cleartext, uint8_t *cipertext){
  return hm_crypto_openssl_aes_iv(key, cleartext, cipertext);
}

uint32_t hm_bt_crypto_hal_ecc_get_ecdh(uint8_t *serial, uint8_t *ecdh){

  uint8_t usepublic[64];
  uint8_t start_date[5];
  uint8_t end_date[5];
  uint8_t command_size;
  uint8_t command[16];

  if(hm_bt_persistence_hal_get_access_certificate(serial, usepublic, start_date, end_date, &command_size, command) == 1){
    return 1;
  }

  uint8_t private[32];
  if(hm_bt_persistence_hal_get_local_private_key(private) == 1){
    return 1;
  }

  return hm_crypto_openssl_dh(private, usepublic, ecdh);
}

uint32_t hm_bt_crypto_hal_ecc_add_signature(uint8_t *data, uint8_t size, uint8_t *signature){
  uint8_t private[32];
  if(hm_bt_persistence_hal_get_local_private_key(private) == 1){
   return 1;
  }

  return hm_crypto_openssl_signature(data, size, private, signature);
}

uint32_t hm_bt_crypto_hal_ecc_validate_signature(uint8_t *data, uint8_t size, uint8_t *signature, uint8_t *serial){

  uint8_t usepublic[64];
  uint8_t start_date[5];
  uint8_t end_date[5];
  uint8_t command_size;
  uint8_t command[16];

  if(hm_bt_persistence_hal_get_access_certificate(serial, usepublic, start_date, end_date, &command_size, command) == 1){
    return 1;
  }

  return hm_crypto_openssl_verify(data, size, usepublic, signature);
}

uint32_t hm_bt_crypto_hal_ecc_validate_all_signatures(uint8_t *data, uint8_t size, uint8_t *signature){
  uint8_t count = 0;
  hm_bt_persistence_hal_get_access_certificate_count(&count);

  uint8_t serial[9];
  uint8_t usepublic[64];
  uint8_t start_date[5];
  uint8_t end_date[5];
  uint8_t command_size;
  uint8_t command[16];

  uint8_t i = 0;
  for( i=0 ; i < count ; i++ ){

    if(hm_bt_persistence_hal_get_access_certificate_by_index(i, serial, usepublic, start_date, end_date, &command_size, command) == 1){
      return 1;
    }

    if(hm_crypto_openssl_verify(data, size, usepublic, signature) == 0 ){
      return 0;
    }

  }

  return 1;
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

  srand(time(NULL));
  nonce[0] = rand();
  nonce[1] = rand();
  nonce[2] = rand();
  nonce[3] = rand();
  nonce[4] = rand();
  nonce[5] = rand();
  nonce[6] = rand();
  nonce[7] = rand();
  nonce[8] = rand();

  return 0;
}
