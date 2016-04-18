package com.high_mobility.digitalkey.HMLink.Shared;

import com.high_mobility.digitalkey.Utils;

/**
 * Created by ttiganik on 13/04/16.
 *
 * Device Certificate is used to recognize a valid device.
 * Certificate binary format
 * Bytes[0 to 4]: Issuer (4 bytes)
 * Bytes[4 to 16]: App ID (12 bytes)
 * Bytes[16 to 25]: Device serial (9 bytes)
 * Bytes[25 to 89]: Device Public Key ( 64 bytes)
 * Bytes[89 to 153]: CA Signature ( 64 bytes)
 *
 */
public class DeviceCertificate extends Certificate {
    public byte[] getIssuer() {
        byte[] bytes = new byte[4];
        System.arraycopy(this.bytes, 0, bytes, 0, 4);
        return bytes;
    }

    public byte[] getAppIdentifier() {
        byte[] bytes = new byte[12];
        System.arraycopy(this.bytes, 4, bytes, 0, 12);
        return bytes;
    }

    public byte[] getSerial() {
        byte[] bytes = new byte[9];
        System.arraycopy(this.bytes, 16, bytes, 0, 9);
        return bytes;
    }

    public byte[] getPublicKey() {
        byte[] bytes = new byte[64];
        System.arraycopy(this.bytes, 25, bytes, 0, 64);
        return bytes;
    }

    @Override
    public byte[] getCertificateData() {
        if (bytes.length == 89) {
            return bytes;
        }

        byte[] bytes = new byte[89];
        System.arraycopy(this.bytes, 0, bytes, 0, 89);
        return bytes;
    }

    @Override
    public byte[] getSignature() {
        if (bytes.length == 153) {
            byte[] bytes = new byte[64];
            System.arraycopy(this.bytes, 89, bytes, 0, 64);
            return bytes;
        }

        return null;
    }

    public void setSignature(byte[] bytes) {
        if (bytes.length == 64) {
            this.bytes = Utils.concatBytes(getCertificateData(), bytes);
        }
    }

    @Override
    public String toString() {
        String description = "";

        description += "issuer: " + Utils.hexFromBytes(getIssuer());
        description += "appIdentifer: " + Utils.hexFromBytes(getAppIdentifier());
        description += "serial: " + Utils.hexFromBytes(getSerial());
        description += "public key: " + Utils.hexFromBytes(getPublicKey());
        description += "signature: " + Utils.hexFromBytes(getSignature());

        return description;
    }

    public DeviceCertificate(byte[] bytes) throws IllegalArgumentException {
        super(bytes);

        if (bytes.length < 89) {
            throw new IllegalArgumentException();
        }
    }

    public DeviceCertificate(byte[] issuer,
                             byte[] appIdentifier,
                             byte[] serial,
                             byte[] publicKey) throws IllegalArgumentException {
        super();

        if (issuer.length < 4 || appIdentifier.length < 12 || serial.length < 9 || publicKey.length < 64) {
            throw new IllegalArgumentException();
        }

        byte[] bytes = new byte[0];
        Utils.concatBytes(bytes, issuer);
        Utils.concatBytes(bytes, appIdentifier);
        Utils.concatBytes(bytes, serial);
        Utils.concatBytes(bytes, publicKey);

        this.bytes = bytes;
    }
}
