//
//  Crypto.c
//  Crypto
//
//  Created by Mikk Rätsep on 20.01.16.
//  Copyright © 2016 High-Mobility. All rights reserved.
//

#include "Crypto.h"
#include "openssl/sha.h"
#include "openssl/ec.h"
#include "openssl/ossl_typ.h"
#include "openssl/bn.h"
#include "openssl/obj_mac.h"
#include "openssl/ecdsa.h"
#include "openssl/hmac.h"
#include "openssl/evp.h"
#include "openssl/aes.h"
#include "openssl/ecdh.h"

// C-includes
#include <stdio.h>
#include <string.h>

void p_fill_to_256b(uint8_t *data, uint16_t size, uint8_t *buffer) {
    memset(buffer, 0x00, 256);
    memcpy(buffer, data, size);
}

uint32_t p_create_sha256(uint8_t *message, uint8_t size, uint8_t *hash_out) {
    uint8_t data[256];
    uint8_t hash[SHA256_DIGEST_LENGTH];
    SHA256_CTX context;

    p_fill_to_256b(message, size, data);

    if (1 != SHA256_Init(&context))                          return 1;
    if (1 != SHA256_Update(&context, &data, 256))            return 1;
    if (1 != SHA256_Final((unsigned char *)&hash, &context)) return 1;

    // Output
    memcpy(hash_out, hash, SHA256_DIGEST_LENGTH);

    return 0;
}


// Keys
uint32_t p_free_keys_variables(EC_KEY *a, BIGNUM *b, EC_GROUP *c, EC_POINT *d, BN_CTX *e) {
    EC_KEY_free(a);
    BN_clear_free(b);

    return 1;
}

void p_extract_private_key(EC_KEY *key, uint8_t *private_key) {
    uint8_t private[32];

    BN_bn2bin(EC_KEY_get0_private_key(key), private);

    // Output
    memcpy(private_key, private, 32);
}

void p_extract_public_key(EC_KEY *key, uint8_t *public_key) {
    const EC_GROUP *group   = EC_KEY_get0_group(key);
    const EC_POINT *point   = EC_KEY_get0_public_key(key);

    BN_CTX *ctx = BN_CTX_new();
    BIGNUM *pub = BN_new();
    uint8_t public[64];

    EC_POINT_point2bn(group, point, POINT_CONVERSION_UNCOMPRESSED, pub, ctx);
    BN_bn2bin(pub, public);

    // Output
    memcpy(public_key, public + 1, 64);

    // Cleanup
    BN_CTX_free(ctx);
}

uint32_t hm_crypto_openssl_create_keys(uint8_t *private_key, uint8_t *public_key, bool create_both) {
    if (create_both) {
        EC_KEY *key;
        uint8_t private[32];
        uint8_t public[64];

        // This is like the 'guard' statements in swift
        if (NULL == (key = EC_KEY_new_by_curve_name(NID_X9_62_prime256v1))) return p_free_keys_variables(key, NULL, NULL, NULL, NULL);
        if (1 != EC_KEY_generate_key(key))                                  return p_free_keys_variables(key, NULL, NULL, NULL, NULL);
        if (1 != EC_KEY_check_key(key))                                     return p_free_keys_variables(key, NULL, NULL, NULL, NULL);

        p_extract_private_key(key, private);
        //p_extract_public_key(key, public);

        // Output
        memcpy(private_key, private, 32);
        memcpy(public_key, public, 64);

        // Cleanup
        p_free_keys_variables(key, NULL, NULL, NULL, NULL);
    }
    else {
        EC_KEY *key;
        BIGNUM *bn;
        EC_GROUP *group;
        EC_POINT *point;
        BN_CTX *ctx;
        uint8_t public[64];

        if (NULL == (group = EC_GROUP_new_by_curve_name(NID_X9_62_prime256v1))) return p_free_keys_variables(key, bn, group, point, ctx);
        if (NULL == (bn = BN_new()))                                            return p_free_keys_variables(key, bn, group, point, ctx);

        BN_bin2bn(private_key, 32, bn);

        if (NULL == (key = EC_KEY_new_by_curve_name(NID_X9_62_prime256v1)))     return p_free_keys_variables(key, bn, group, point, ctx);
        if (NULL == (point = EC_POINT_new(group)))                              return p_free_keys_variables(key, bn, group, point, ctx);
        if (NULL == (ctx = BN_CTX_new()))                                       return p_free_keys_variables(key, bn, group, point, ctx);

        if (1 != EC_KEY_set_private_key(key, bn))                               return p_free_keys_variables(key, bn, group, point, ctx);
        if (1 != EC_KEY_generate_key(key))                                      return p_free_keys_variables(key, bn, group, point, ctx);
        if (1 != EC_KEY_check_key(key))                                         return p_free_keys_variables(key, bn, group, point, ctx);
        if (1 != EC_POINT_mul(group, point, bn, NULL, NULL, NULL))              return p_free_keys_variables(key, bn, group, point, ctx);

        EC_POINT_point2bn(group, point, POINT_CONVERSION_UNCOMPRESSED, bn, ctx);
        BN_bn2bin(bn, public);

        // Output
        memcpy(public_key, public + 1, 64);

        // Cleanup
        p_free_keys_variables(key, bn, group, point, ctx);
    }

    return 0;
}


// Signature
uint32_t p_free_signature_variables(EC_KEY *a, BIGNUM *b, ECDSA_SIG *c) {
    EC_KEY_free(a);
    BN_clear_free(b);
    ECDSA_SIG_free(c);

    return 1;
}

uint32_t hm_crypto_openssl_signature(uint8_t *message, uint8_t size, uint8_t *private_key, uint8_t *signature) {
    EC_KEY *key;
    BIGNUM *bn;
    ECDSA_SIG *sig;
    uint8_t hash[SHA256_DIGEST_LENGTH];
    uint8_t rBin[32];
    uint8_t sBin[32];

    // Variable creation
    if (NULL == (key = EC_KEY_new_by_curve_name(NID_X9_62_prime256v1))) return p_free_signature_variables(key, bn, sig);
    if (NULL == (bn = BN_new()))                                        return p_free_signature_variables(key, bn, sig);
    if (NULL == (sig = ECDSA_SIG_new()))                                return p_free_signature_variables(key, bn, sig);

    // Key calculations
    if (NULL == BN_bin2bn(private_key, 32, bn))                         return p_free_signature_variables(key, bn, sig);
    if (1 != EC_KEY_set_private_key(key, bn))                           return p_free_signature_variables(key, bn, sig);

    // Signature calculations
    if (0 != p_create_sha256(message, size, hash))                      return p_free_signature_variables(key, bn, sig);
    if (NULL == (sig = ECDSA_do_sign(hash, SHA256_DIGEST_LENGTH, key))) return p_free_signature_variables(key, bn, sig);

    // Big number calculations
    if (0 == BN_bn2bin(sig->r, rBin))                                   return p_free_signature_variables(key, bn, sig);
    if (0 == BN_bn2bin(sig->s, sBin))                                   return p_free_signature_variables(key, bn, sig);

    // Output
    memcpy(signature,       rBin, 32);
    memcpy(signature + 32,  sBin, 32);

    // Cleanup
    p_free_signature_variables(key, bn, sig);

    return 0;
}


// Verify signature
uint32_t p_free_verify_variables(EC_KEY *a, BIGNUM *b, BIGNUM *c, ECDSA_SIG *d, EC_GROUP *g, EC_POINT *h) {
    EC_KEY_free(a);
    BN_clear_free(b);
    BN_clear_free(c);
    ECDSA_SIG_free(d);
    EC_GROUP_clear_free(g);
    EC_POINT_clear_free(h);

    return 1;
}

uint32_t hm_crypto_openssl_verify(uint8_t *message, uint8_t size, uint8_t *public_key, uint8_t *signature) {
    EC_KEY *key;
    EC_GROUP *group;
    EC_POINT *point;
    BIGNUM *pub_x;
    BIGNUM *pub_y;
    ECDSA_SIG *sig;
    BIGNUM *sig_r;
    BIGNUM *sig_s;
    uint8_t verified = 0;
    uint8_t hash[SHA256_DIGEST_LENGTH];

    // Variable creation
    if (NULL == (key = EC_KEY_new_by_curve_name(NID_X9_62_prime256v1)))     return 1;
    if (NULL == (pub_x = BN_new()))                                         return p_free_verify_variables(key, pub_x, pub_y, sig, group, point);
    if (NULL == (pub_y = BN_new()))                                         return p_free_verify_variables(key, pub_x, pub_y, sig, group, point);
    if (NULL == (group = EC_GROUP_new_by_curve_name(NID_X9_62_prime256v1))) return p_free_verify_variables(key, pub_x, pub_y, sig, group, point);
    if (NULL == (point = EC_POINT_new(group)))                              return p_free_verify_variables(key, pub_x, pub_y, sig, group, point);

    // Big number conversion
    BN_bin2bn(public_key,       32, pub_x);
    BN_bin2bn(public_key + 32,  32, pub_y);

    // Crypto calculations
    if (1 != EC_POINT_set_affine_coordinates_GFp(group, point, pub_x, pub_y, NULL))
                                                                            return p_free_verify_variables(key, pub_x, pub_y, sig, group, point);
    if (1 != EC_KEY_set_public_key(key, point))                             return p_free_verify_variables(key, pub_x, pub_y, sig, group, point);
    if (1 != EC_KEY_check_key(key))                                         return p_free_verify_variables(key, pub_x, pub_y, sig, group, point);

    // Variable creation
    if (NULL == (sig = ECDSA_SIG_new()))                                    return p_free_verify_variables(key, pub_x, pub_y, sig, group, point);
    if (NULL == (sig_r = BN_new()))                                         return p_free_verify_variables(key, pub_x, pub_y, sig, group, point);
    if (NULL == (sig_s = BN_new()))                                         return p_free_verify_variables(key, pub_x, pub_y, sig, group, point);
    if (0 != p_create_sha256(message, size, hash))                          return p_free_verify_variables(key, pub_x, pub_y, sig, group, point);

    // Big number conversion
    BN_bin2bn(signature,        32, sig_r);
    BN_bin2bn(signature + 32,   32, sig_s);

    sig->r      = sig_r;
    sig->s      = sig_s;
    verified    = ECDSA_do_verify(hash, SHA256_DIGEST_LENGTH, sig, key);

    // Cleanup (sig_r and sig_s are freed by the ECDSA_SIG)
    p_free_verify_variables(key, pub_x, pub_y, sig, group, point);

    // Output
    return (verified == 1) ? 0 : 1;
}


// HMAC
uint32_t p_free_hmac_variables(HMAC_CTX a) {
    HMAC_CTX_cleanup(&a);

    return 1;
}

uint32_t hm_crypto_openssl_hmac(uint8_t *message, uint16_t size, uint8_t *key, uint8_t *hmac) {
    HMAC_CTX ctx;
    uint8_t data[256];
    uint8_t output[32];

    // Initial
    HMAC_CTX_init(&ctx);
    p_fill_to_256b(message, size, data);
    HMAC_Init_ex(&ctx, key, 32, EVP_sha256(), NULL);

    // HMAC calculations
    if (1 != HMAC_Update(&ctx, data, 256))      return p_free_hmac_variables(ctx);
    if (1 != HMAC_Final(&ctx, output, NULL))    return p_free_hmac_variables(ctx);

    // Output
    memcpy(hmac, output, 32);

    // Cleanup
    p_free_hmac_variables(ctx);

    return 0;
}


// AES
uint32_t hm_crypto_openssl_aes_iv(uint8_t *key, uint8_t *iv, uint8_t *iv_out) {
    AES_KEY aes;
    uint8_t iv2[16];

    // Sets the key for AES
    if (0 != AES_set_encrypt_key(key, 128, &aes))    return 1;

    // Creates the Block Cipher (IV2)
    AES_ecb_encrypt(iv, iv2, &aes, AES_ENCRYPT);

    // Output
    memcpy(iv_out, iv2, 16);

    return 0;
}


// DH
uint32_t p_free_dh_variables(EC_KEY *a, BIGNUM *b, BIGNUM *c, BIGNUM *d, EC_POINT *e, BN_CTX *f) {
    EC_KEY_free(a);
    BN_clear_free(b);
    BN_clear_free(c);
    BN_clear_free(d);
    EC_POINT_clear_free(e);
    BN_CTX_free(f);

    return 1;
}

uint32_t hm_crypto_openssl_dh(uint8_t *private_key, uint8_t *public_key, uint8_t *shared_key) {
    EC_KEY *key;
    BIGNUM *pri_bn;
    BIGNUM *pub_x;
    BIGNUM *pub_y;
    EC_POINT *point;
    BN_CTX *context;
    uint8_t output[32];

    // Variable creation
    if (NULL == (key = EC_KEY_new_by_curve_name(NID_X9_62_prime256v1))) return 1;
    if (NULL == (pri_bn = BN_new()))                            return p_free_dh_variables(key, pri_bn, pub_x, pub_y, point, context);

    // Crypto calculations
    if (NULL == BN_bin2bn(private_key, 32, pri_bn))             return p_free_dh_variables(key, pri_bn, pub_x, pub_y, point, context);
    if (1 != EC_KEY_set_private_key(key, pri_bn))               return p_free_dh_variables(key, pri_bn, pub_x, pub_y, point, context);

    // Variable creation
    if (NULL == (pub_x = BN_new()))                             return p_free_dh_variables(key, pri_bn, pub_x, pub_y, point, context);
    if (NULL == (pub_y = BN_new()))                             return p_free_dh_variables(key, pri_bn, pub_x, pub_y, point, context);

    // Big number conversion
    BN_bin2bn(public_key,       32, pub_x);
    BN_bin2bn(public_key + 32,  32, pub_y);

    // Variable creation
    if (NULL == (point = EC_POINT_new(EC_KEY_get0_group(key)))) return p_free_dh_variables(key, pri_bn, pub_x, pub_y, point, context);
    if (NULL == (context = BN_CTX_new()))                       return p_free_dh_variables(key, pri_bn, pub_x, pub_y, point, context);

    // Crypto calculations
    if (1 != EC_POINT_set_affine_coordinates_GFp(EC_KEY_get0_group(key), point, pub_x, pub_y, context))
                                                                return p_free_dh_variables(key, pri_bn, pub_x, pub_y, point, context);
    if (-1 == ECDH_compute_key(&output, 32, point, key, 0))     return p_free_dh_variables(key, pri_bn, pub_x, pub_y, point, context);

    // Output
    memcpy(shared_key, output, 32);

    // Cleanup
    p_free_dh_variables(key, pri_bn, pub_x, pub_y, point, context);

    return 0;
}
