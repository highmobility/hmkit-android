package com.high_mobility.hmkit.Crypto;

import android.util.Base64;

/**
 * Created by ttiganik on 26/05/16.
 */
public class KeyPair {
    byte[] privateKey;
    byte[] publicKey;

    public KeyPair(byte[] privateKey, byte[] publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public String getPublicKeyBase64() {
        return new String(Base64.encode(publicKey, Base64.NO_WRAP));
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public String getPrivateKeyBase64() {
        return new String(Base64.encode(privateKey, Base64.NO_WRAP));
    }
}
