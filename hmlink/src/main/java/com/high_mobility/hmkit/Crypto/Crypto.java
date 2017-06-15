package com.high_mobility.hmkit.Crypto;

import com.high_mobility.btcore.HMBTCore;

import java.util.Random;

/**
 * Created by ttiganik on 26/05/16.
 */
public class Crypto {
    public static KeyPair createKeypair() {
        HMBTCore core = new HMBTCore();

        byte[] privateKey = new byte[32];
        byte[] publicKey = new byte[64];

        core.HMBTCoreCryptoCreateKeys(privateKey, publicKey);
        return new KeyPair(privateKey, publicKey);
    }

    public static byte[] createSerialNumber() {
        byte[] serialBytes = new byte[9];
        new Random().nextBytes(serialBytes);
        return serialBytes;
    }

    public static byte[] sign(byte[] bytes, byte[] privateKey) {
        HMBTCore core = new HMBTCore();
        byte[] signature = new byte[64];
        core.HMBTCoreCryptoAddSignature(bytes, bytes.length, privateKey, signature);
        return signature;
    }
}
