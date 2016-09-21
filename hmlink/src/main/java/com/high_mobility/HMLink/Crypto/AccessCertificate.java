package com.high_mobility.HMLink.Crypto;

import com.high_mobility.HMLink.ByteUtils;

import java.util.Date;

/**
 * Created by ttiganik on 13/04/16.
 *
 * Access Certificate is used to recognise and authorise two HM SDK-enabled devices.
 *
 * Certificate binary format
 * Cert Data[0 to 9]: Access Gaining Serial number ( 9 bytes )
 * Cert Data[9 to 73]: Access Gaining Public Key ( 64 bytes )
 * Cert Data[73 to 82]: Access Providing Serial number (9 bytes)
 * Cert Data[82 to 87]: Start date ( 5 bytes)
 * Cert Data[87 to 92]: End date ( 5 bytes)
 * Cert Data[92]: Permissions Size ( 1 byte )
 * Cert Data[93 to A]: Permissions ( 0 - 16 bytes )
 * Signature Data[A to B]: Certificate Authority Signature ( 64 bytes Only for Certificate data )
 * Date binary format
 * Data[0]: Year ( 00 to 99, means year from 2000 to 2099)
 * Data[1]: month ( 1 to 12 )
 * Data[2]: day ( 1 to 31)
 * Data[4]: Hours ( 0 to 23 )
 * Data[5]: Minutes ( 0 to 59 )
 *
 */
public class AccessCertificate extends Certificate {
    /**
     * @return The serial number of the device that's gaining access.
     */
    public byte[] getGainerSerial() {
        byte[] bytes = new byte[9];
        System.arraycopy(this.bytes, 0, bytes, 0, 9);
        return bytes;
    }

    /**
     * @return The public key of the device that's gaining access.
     */
    public byte[] getGainerPublicKey() {
        byte[] bytes = new byte[64];
        System.arraycopy(this.bytes, 9, bytes, 0, 64);
        return bytes;
    }

    /**
     * @return The serial number of the device that's providing access.
     */
    public byte[] getProviderSerial() {
        byte[] bytes = new byte[9];
        System.arraycopy(this.bytes, 73, bytes, 0, 9);
        return bytes;
    }

    /**
     * @return The certificate validity start date.
     */
    public Date getStartDate() {
        return dateFromBytes(getStartDateBytes());
    }

    /**
     * @return The certificate validity start date in byte format.
     */
    public byte[] getStartDateBytes() {
        byte[] bytes = new byte[5];
        System.arraycopy(this.bytes, 82, bytes, 0, 5);
        return bytes;
    }

    /**
     * @return The certificate validity end date.
     */
    public Date getEndDate() {
        return dateFromBytes(getEndDateBytes());
    }

    /**
     * @return The certificate validity end date in byte format.
     */
    public byte[] getEndDateBytes() {
        byte[] bytes = new byte[5];
        System.arraycopy(this.bytes, 87, bytes, 0, 5);
        return bytes;
    }

    /**
     * @return  The permissions given for the certificate, up to 16 bytes and can both contain
     *          arbitrary data as well as permission bytes that correspond to the General API.
     */
    public byte[] getPermissions() {
        int length = bytes[92];
        if (length > 0) {
            byte[] bytes = new byte[length];
            System.arraycopy(this.bytes, 93, bytes, 0, length);
            return bytes;
        }
        else {
            return new byte[0];
        }
    }

    /**
     * Set the certificate's new permissions. After this the signature is invalidated
     * @param permissions The new permissions, up to 16 bytes.
     */
    public void setPermissions(byte[] permissions) {
        byte length = 0x00;
        byte[] newBytes;
        if (permissions != null && permissions.length > 0) {
            length = (byte)permissions.length;
        }

        newBytes = new byte[93 + length];
        System.arraycopy(this.bytes, 0, newBytes, 0, 91);
        System.arraycopy(this.bytes, 92, new byte[] {length}, 0, 1);

        if (length > 0) {
            System.arraycopy(this.bytes, 93, permissions, 0, length);
        }
    }

    /**
     * @return A boolean value indicating if the certificate has expired.
     */
    public boolean isExpired() {
        return getEndDate().after(new Date());
    }

    @Override
    public byte[] getCertificateData() {
        int dataLength = 93;

        if (bytes[92] > 0) {
            dataLength += bytes[92];
        }
        byte[] bytes = new byte[dataLength];
        System.arraycopy(this.bytes, 0, bytes, 0, dataLength);
        return bytes;
    }

    @Override
    public byte[] getSignature() {
        int permissionsSize = bytes[92];

        if (bytes.length == 93 + permissionsSize) {
            return null; // no sig
        }
        else {
            byte[] bytes = new byte[64];
            System.arraycopy(this.bytes, 93 + permissionsSize, bytes, 0, 64);
            return bytes;
        }
    }

    /**
     * @param bytes The Certificate Authority's signature for the certificate, 64 bytes
     */
    public void setSignature(byte[] bytes) {
        if (bytes.length == 64) {
            this.bytes = ByteUtils.concatBytes(getCertificateData(), bytes);
        }
    }

    @Override
    public String toString() {
        String description = "";

        description += "\ngainingSerial: " + ByteUtils.hexFromBytes(getGainerSerial());
        description += "\ngainingPublicKey: " + ByteUtils.hexFromBytes(getGainerPublicKey());
        description += "\nprovidingSerial: " + ByteUtils.hexFromBytes(getProviderSerial());
        description += "\nvalid from: : " + getStartDate() + " to: " + getEndDate();
        description += "\npermissions: " + ByteUtils.hexFromBytes(getPermissions());
        description += "\nsignature: " + ByteUtils.hexFromBytes(getSignature()) + "\n";

        return description;
    }

    /**
     * Initialise the access certificate with raw bytes.
     *
     * This method requires at least 93 bytes to succeed, but is not recommended
     * for manual initialisation (instead use the alternative AccessCertificate()).
     *
     * @param bytes The bytes making up the certificate (at least 93 bytes are expected).
     * @throws IllegalArgumentException When bytes length is not correct.
     */
    public AccessCertificate(byte[] bytes) throws IllegalArgumentException {
        super(bytes);

        if (bytes.length < 93) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Initialise the access certificate with all its attributes except Certificate Authority signature.
     *
     * @param gainerSerial      9-byte serial number of the device that's gaining access.
     * @param gainingPublicKey  64-byte public key of the device gaining access.
     * @param providingSerial   9-byte serial number of the device providing access to itself.
     * @param startDate         The start time (and date) of the certificate.
     * @param endDate           The expiration time of the certificate.
     * @param permissions       Permissions supplied with the certificate (up to 16 bytes).
     * @throws IllegalArgumentException When parameters are not in correct size according to the table on top.
     */
    public AccessCertificate(byte[] gainerSerial,
                             byte[] gainingPublicKey,
                             byte[] providingSerial,
                             byte[] startDate,
                             byte[] endDate,
                             byte[] permissions) throws IllegalArgumentException {
        super();

        byte[] bytes = new byte[0];

        bytes = ByteUtils.concatBytes(bytes, gainerSerial);
        bytes = ByteUtils.concatBytes(bytes, gainingPublicKey);
        bytes = ByteUtils.concatBytes(bytes, providingSerial);
        bytes = ByteUtils.concatBytes(bytes, startDate);
        bytes = ByteUtils.concatBytes(bytes, endDate);

        if (permissions != null && permissions.length > 0) {
            bytes = ByteUtils.concatBytes(bytes, new byte[] {(byte)permissions.length});
            bytes = ByteUtils.concatBytes(bytes, permissions);
        }
        else {
            bytes = ByteUtils.concatBytes(bytes, new byte[] {0x00});
        }

        if (bytes.length < 93) {
            throw new IllegalArgumentException();
        }

        this.bytes = bytes;
    }


    /**
     * Initialise the access certificate with all its attributes except Certificate Authority signature.
     *
     * @param gainerSerial      9-byte serial number of the device that's gaining access.
     * @param gainingPublicKey  64-byte public key of the device gaining access.
     * @param providingSerial   9-byte serial number of the device providing access to itself.
     * @param startDate         The start time (and date) of the certificate.
     * @param endDate           The expiration date of the certificate.
     * @param permissions       Permissions supplied with the certificate (up to 16 bytes).
     * @throws IllegalArgumentException When parameters are not in correct size according to the table on top.
     */
    public AccessCertificate(byte[] gainerSerial,
                             byte[] gainingPublicKey,
                             byte[] providingSerial,
                             Date startDate,
                             Date endDate,
                             byte[] permissions) throws IllegalArgumentException {
        this(gainerSerial, gainingPublicKey, providingSerial, bytesFromDate(startDate), bytesFromDate(endDate), permissions);
    }
}
