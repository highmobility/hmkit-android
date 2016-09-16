package com.high_mobility.HMLink.Crypto;

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

    public byte[] getPrivateKey() {
        return privateKey;
    }
}
