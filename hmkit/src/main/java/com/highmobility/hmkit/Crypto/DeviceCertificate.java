package com.highmobility.hmkit.Crypto;


import com.highmobility.byteutils.Bytes;

/**
 * Created by ttiganik on 13/04/16.
 *
 * Device Certificate is used to recognize a valid device.
 *
 *  Certificate binary format
 * Bytes[0 to 4]: Issuer (4 bytes)
 * Bytes[4 to 16]: App ID (12 bytes)
 * Bytes[16 to 25]: Device serial (9 bytes)
 * Bytes[25 to 89]: Device Public Key ( 64 bytes)
 * Bytes[89 to 153]: CA Signature ( 64 bytes)
 *
 */
public class DeviceCertificate extends Certificate {
    /**
     * @return The certificate issuer's identifier.
     */
    public byte[] getIssuer() {
        byte[] bytes = new byte[4];
        System.arraycopy(this.bytes, 0, bytes, 0, 4);
        return bytes;
    }

    /**
     * @return The certificate's app identifier.
     */
    public byte[] getAppIdentifier() {
        byte[] bytes = new byte[12];
        System.arraycopy(this.bytes, 4, bytes, 0, 12);
        return bytes;
    }

    /**
     * @return The serial number of the device.
     */
    public byte[] getSerial() {
        byte[] bytes = new byte[9];
        System.arraycopy(this.bytes, 16, bytes, 0, 9);
        return bytes;
    }

    /**
     * @return The public key of the device.
     */
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

    /**
     * @return The Certificate Authority's signature for the certificate, 64 bytes.
     */
    @Override
    public byte[] getSignature() {
        if (bytes.length == 153) {
            byte[] bytes = new byte[64];
            System.arraycopy(this.bytes, 89, bytes, 0, 64);
            return bytes;
        }

        return null;
    }

    /**
     * @param bytes The Certificate Authority's signature for the certificate, 64 bytes.
     */
    public void setSignature(byte[] bytes) {
        if (bytes.length == 64) {
            this.bytes = Bytes.concatBytes(getCertificateData(), bytes);
        }
    }

    @Override
    public String toString() {
        String description = "";

        description += "\nissuer: " + Bytes.hexFromBytes(getIssuer());
        description += "\nappIdentifer: " + Bytes.hexFromBytes(getAppIdentifier());
        description += "\nserial: " + Bytes.hexFromBytes(getSerial());
        description += "\npublic key: " + Bytes.hexFromBytes(getPublicKey());
        description += "\nsignature: " + Bytes.hexFromBytes(getSignature());

        return description;
    }

    /**
     * Initialise the device certificate with raw bytes.
     *
     * @param bytes The bytes making up the certificate (89 bytes are expected).
     * @throws IllegalArgumentException When bytes length is not correct.
     */
    public DeviceCertificate(byte[] bytes) throws IllegalArgumentException {
        super(bytes);

        if (bytes.length < 89) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Initialise the device certificate with all its attributes except Certificate Authority
     * signature.
     *
     * @param issuer        The issuer's identifying 4 bytes.
     * @param appIdentifier The specific app's identifying 12 bytes (one issuer might have many
     *                      apps / uses).
     * @param serial        The serial of the device with the certificate that's 9 bytes.
     * @param publicKey     The public key of the device with the certificate that's 64 bytes.
     * @throws IllegalArgumentException When the parameters sizes are wrong.
     */
    public DeviceCertificate(byte[] issuer,
                             byte[] appIdentifier,
                             byte[] serial,
                             byte[] publicKey) throws IllegalArgumentException {
        super();

        if (issuer.length < 4 || appIdentifier.length < 12 || serial.length < 9 || publicKey.length < 64) {
            throw new IllegalArgumentException();
        }

        byte[] bytes = new byte[0];
        bytes = Bytes.concatBytes(bytes, issuer);
        bytes = Bytes.concatBytes(bytes, appIdentifier);
        bytes = Bytes.concatBytes(bytes, serial);
        bytes = Bytes.concatBytes(bytes, publicKey);

        this.bytes = bytes;
    }


    /*
        /// Initialise the LocalDevice with essential values before using any other functionality.
    ///
    /// - parameter deviceCertificate: The device's certificate in base64
    /// - parameter devicePrivateKey:  The device's private key in base64, 32 bytes, using elliptic curve p256
    /// - parameter issuerPublicKey:   The issuer's public key in base64 , 64 bytes
    ///
    /// - throws: *LinkError.internalError* when the device cert couldn't be created from the input, or the keys are not the correct length
    public func initialise(deviceCertificate: Base64, devicePrivateKey: Base64, issuerPublicKey: Base64) throws {
     */

}
