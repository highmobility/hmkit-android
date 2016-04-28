//
//  Crypto.h
//  Crypto
//
//  Created by Mikk Rätsep on 20.01.16.
//  Copyright © 2016 High-Mobility. All rights reserved.
//

#ifndef Crypto_h
#define Crypto_h

#include <stdbool.h>
#include <stdint.h>

// TODO: Add the inputs/outputs bytes sizes

/** @brief Creates cryptographical keys for the p256 elliptic curve.
 *
 *  Can also be used to generate the public key from the private, if the supplied private_key is filled.
 *
 *  @param private_key      The output or input for the private key.
 *  @param public_key       The output for the public key.
 *  @param create_both      If this is 'true', both keys are created. Otherwise the public key is created from the private.
 *
 *  @return                 0 for success
 */
uint32_t hm_crypto_openssl_create_keys(uint8_t *private_key, uint8_t *public_key, bool create_both);

/** @brief Creates a cryptographical signature for a message.
 *
 *  @param message          The message that's going to be signed.
 *  @param size             The size of the message.
 *  @param private_key      The private key to be used for signing.
 *  @param signature        The generated signature for the message.
 *
 *  @return                 0 for success
 */
uint32_t hm_crypto_openssl_signature(uint8_t *message, uint8_t size, uint8_t *private_key, uint8_t *signature);

/** @brief Verifies a cryptographical signature of a message.
 *
 *  @param message          The message that's signature is verified.
 *  @param size             The size of the message.
 *  @param public_key       The public key of the signature.
 *  @param signature        The signature of the message.
 *
 *  @return                 0 for success
 */
uint32_t hm_crypto_openssl_verify(uint8_t *message, uint8_t size, uint8_t *public_key, uint8_t *signature);

/** @brief Creates an HMAC for a message.
 *
 *  @param message          The message that's used for the HMAC.
 *  @param size             The size of the message.
 *  @param key              The key to be used for HMAC.
 *  @param hmac             The HMAC for the message.
 *
 *  @return                 0 for success
 */
uint32_t hm_crypto_openssl_hmac(uint8_t *message, uint16_t size, uint8_t *key, uint8_t *hmac);

/** @brief Encrypt the injection vector for use in block cipher.
 *
 *  @param key              The key for the BCE.
 *  @param iv               The inital IV.
 *  @param iv_out           The encrypted IV.
 *
 *  @return                 0 for success
 */
uint32_t hm_crypto_openssl_aes_iv(uint8_t *key, uint8_t *iv, uint8_t *iv_out);

/** @brief Creates cryptographical shared key with Diffie-Hellman
 *
 *  @param private_key      The private key for process.
 *  @param public_key       The public key for the process.
 *  @param shared_key       The output of DH.
 *
 *  @return                 0 for success
 */
uint32_t hm_crypto_openssl_dh(uint8_t *private_key, uint8_t *public_key, uint8_t *shared_key);

#endif /* Crypto_h */
