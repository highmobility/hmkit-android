#include "hm_bt_crypto_hal.h"
#include "hm_bt_persistence_hal.h"
#include "Crypto.h"
#include <string.h>
#include <time.h>
#include <stdlib.h>
#include "hmbtcore.h"
#include "hm_bt_log.h"
#include "hm_cert.h"

uint32_t hm_bt_crypto_hal_aes_ecb_block_encrypt(uint64_t appContxtId, uint8_t *key, uint8_t *cleartext, uint8_t *cipertext){
  return hm_crypto_openssl_aes_iv(key, cleartext, cipertext);
}

uint32_t hm_bt_crypto_hal_ecc_get_ecdh(uint64_t appContxtId, uint8_t *serial, uint8_t *ecdh){

  uint8_t cert[178];
  uint16_t size = 0;
  hm_certificate_t certificate;

  if(hm_bt_persistence_hal_get_access_certificate(appContxtId, serial, cert, &size) == 1){
    return 1;
  }

  hm_cert_get_as_struct(cert, &certificate);

  hm_bt_log_data(NULL,NULL,HM_BT_LOG_INFO,certificate.public_key,64,"[HMCrypto] PUB");

  uint8_t private[32];
  if(hm_bt_persistence_hal_get_local_private_key(appContxtId, private) == 1){
    return 1;
  }

  uint32_t retcode = hm_crypto_openssl_dh(private, certificate.public_key, ecdh);

  hm_bt_log_data(NULL,NULL,HM_BT_LOG_INFO,ecdh,32,"[HMCrypto] ECDH");

  return retcode;
}

uint32_t hm_bt_crypto_hal_ecc_add_signature(uint64_t appContxtId, uint8_t *data, uint8_t size, uint8_t *signature){
  uint8_t private[32];
  if(hm_bt_persistence_hal_get_local_private_key(appContxtId, private) == 1){
   return 1;
  }

  return hm_crypto_openssl_signature(data, size, private, signature);
}

uint32_t hm_bt_crypto_hal_ecc_validate_signature(uint64_t appContxtId, uint8_t *data, uint8_t size, uint8_t *signature, uint8_t *serial){

  uint8_t cert[178];
  uint16_t certsize = 0;
  hm_certificate_t certificate;

  if(hm_bt_persistence_hal_get_access_certificate(appContxtId, serial, cert, &certsize) == 1){
    return 1;
  }

  hm_cert_get_as_struct(cert, &certificate);

  return hm_crypto_openssl_verify(data, size, certificate.public_key, signature);
}

uint32_t hm_bt_crypto_hal_ecc_validate_all_signatures(uint64_t appContxtId, uint8_t *data, uint8_t size, uint8_t *signature){
  uint8_t count = 0;
  hm_bt_persistence_hal_get_access_certificate_count(appContxtId, &count);

  uint8_t serial[9];
  uint8_t usepublic[64];
  uint8_t start_date[5];
  uint8_t end_date[5];
  uint8_t command_size;
  uint8_t command[16];

  uint8_t i = 0;
  for( i=0 ; i < count ; i++ ){

    uint8_t cert[178];
    uint16_t certsize = 0;
    hm_certificate_t certificate;

    if(hm_bt_persistence_hal_get_access_certificate_by_index(appContxtId, i, cert, &certsize) == 1){
        return 1;
    }

    hm_cert_get_as_struct(cert, &certificate);

    if(hm_crypto_openssl_verify(data, size, certificate.public_key, signature) == 0 ){
      return 0;
    }

  }

  return 1;
}

uint32_t hm_bt_crypto_hal_ecc_validate_ca_signature(uint64_t appContxtId, uint8_t *data, uint8_t size, uint8_t *signature){
  uint8_t ca_pub[64];
  hm_bt_persistence_hal_get_ca_public_key(appContxtId, ca_pub);
  return hm_crypto_openssl_verify(data, size, ca_pub, signature);
}

uint32_t hm_bt_crypto_hal_ecc_validate_oem_ca_signature(uint64_t appContxtId, uint8_t *data, uint8_t size, uint8_t *signature){
  uint8_t ca_pub[64];
  hm_bt_persistence_hal_get_oem_ca_public_key(appContxtId, ca_pub);
  return hm_crypto_openssl_verify(data, size, ca_pub, signature);
}

uint32_t hm_bt_crypto_hal_hmac(uint64_t appContxtId, uint8_t *key, uint8_t *data, uint16_t size, uint8_t *hmac){

hm_bt_log_data(NULL,NULL,HM_BT_LOG_INFO,key,32,"[HMCrypto] HMAC KEY");
hm_bt_log_data(NULL,NULL,HM_BT_LOG_INFO,data,size,"[HMCrypto] HMAC DATA");

uint32_t ret = hm_crypto_openssl_hmac(data, size, key, hmac);

hm_bt_log_data(NULL,NULL,HM_BT_LOG_INFO,hmac,32,"[HMCrypto] HMAC RESULT");

  return ret;
}

uint32_t hm_bt_crypto_hal_generate_nonce(uint64_t appContxtId, uint8_t *nonce){
  jbyteArray nonce_ = (*envRef)->NewByteArray(envRef,9);
  (*envRef)->SetByteArrayRegion(envRef, nonce_, 0, 9, (const jbyte*) nonce );
  (*envRef)->CallVoidMethod(envRef, coreInterfaceRef, interfaceMethodHMCryptoHalGenerateNonce, nonce_);

  if ((*envRef)->ExceptionCheck(envRef)) {
      (*envRef)->ExceptionClear(envRef);
      return 0;
    }

  jbyte* content_array = (*envRef)->GetByteArrayElements(envRef, nonce_, NULL);
  memcpy(nonce,content_array,9);
  return 0;
}
