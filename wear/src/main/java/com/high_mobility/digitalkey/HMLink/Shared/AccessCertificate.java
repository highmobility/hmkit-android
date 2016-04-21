package com.high_mobility.digitalkey.HMLink.Shared;

import com.high_mobility.digitalkey.Utils;

import java.util.Date;

/**
 * Created by ttiganik on 13/04/16.
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
    public byte[] getGainerSerial() {
        byte[] bytes = new byte[9];
        System.arraycopy(this.bytes, 0, bytes, 0, 9);
        return bytes;
    }

    public byte[] getGainerPublicKey() {
        byte[] bytes = new byte[64];
        System.arraycopy(this.bytes, 9, bytes, 0, 64);
        return bytes;
    }

    public byte[] getProviderSerial() {
        byte[] bytes = new byte[9];
        System.arraycopy(this.bytes, 73, bytes, 0, 9);
        return bytes;
    }

    public Date getStartDate() {
        return dateFromBytes(getStartDateBytes());
    }

    public byte[] getStartDateBytes() {
        byte[] bytes = new byte[5];
        System.arraycopy(this.bytes, 82, bytes, 0, 5);
        return bytes;
    }

    public Date getEndDate() {
        return dateFromBytes(getEndDateBytes());
    }

    public byte[] getEndDateBytes() {
        byte[] bytes = new byte[5];
        System.arraycopy(this.bytes, 87, bytes, 0, 5);
        return bytes;
    }

    public byte[] getPermissions() {
        int length = bytes[92];
        if (length > 0) {
            byte[] bytes = new byte[length];
            System.arraycopy(this.bytes, 93, bytes, 0, length);
            return bytes;
        }
        else {
            return null;
        }
    }

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

    public void setSignature(byte[] bytes) {
        if (bytes.length == 64) {
            this.bytes = Utils.concatBytes(getCertificateData(), bytes);
        }
    }

    @Override
    public String toString() {
        String description = "";

        description += "gainingSerial: " + Utils.hexFromBytes(getGainerSerial());
        description += "gainingPublicKey: " + Utils.hexFromBytes(getGainerPublicKey());
        description += "providingSerial: " + Utils.hexFromBytes(getProviderSerial());
        description += "valid from: : " + getStartDate() + " to: " + getEndDate();
        description += "permissions: " + Utils.hexFromBytes(getPermissions());
        description += "signature: " + Utils.hexFromBytes(getSignature());

        return description;
    }

    public AccessCertificate(byte[] bytes) throws IllegalArgumentException {
        super(bytes);

        if (bytes.length < 93) {
            throw new IllegalArgumentException();
        }
    }

    public AccessCertificate(byte[] gainerSerial,
                             byte[] gainingPublicKey,
                             byte[] providingSerial,
                             byte[] startDate,
                             byte[] endDate,
                             byte[] permissions) throws IllegalArgumentException {
        super();

        byte[] bytes = new byte[0];

        bytes = Utils.concatBytes(bytes, gainerSerial);
        bytes = Utils.concatBytes(bytes, gainingPublicKey);
        bytes = Utils.concatBytes(bytes, providingSerial);
        bytes = Utils.concatBytes(bytes, startDate);
        bytes = Utils.concatBytes(bytes, endDate);

        if (permissions.length > 0) {
            bytes = Utils.concatBytes(bytes, new byte[] {(byte)permissions.length});
            bytes = Utils.concatBytes(bytes, permissions);
        }
        else {
            bytes = Utils.concatBytes(bytes, new byte[] {0x00});
        }

        if (bytes.length < 93) {
            throw new IllegalArgumentException();
        }

        this.bytes = bytes;
    }



    public AccessCertificate(byte[] gainerSerial,
                             byte[] gainingPublicKey,
                             byte[] providingSerial,
                             Date startDate,
                             Date endDate,
                             byte[] permissions) throws IllegalArgumentException {
        this(gainerSerial, gainingPublicKey, providingSerial, bytesFromDate(startDate), bytesFromDate(endDate), permissions);
    }
}
