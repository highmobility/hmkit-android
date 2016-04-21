package com.high_mobility.digitalkey.HMLink.Shared;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ttiganik on 13/04/16.
 */
public class Certificate {
    byte[] bytes;

    public Certificate(byte[] bytes) {
        this.bytes = bytes;
    }

    public Certificate(){}

    /// The certificate's data in binary format, without the signature
    public byte [] getCertificateData() {
        return null;
    }

    /// The certificate's signature
    public byte[] getSignature() {
        return null;
    }

    /// The full certificate data in binary format.
    public byte[] getBytes() {
        return bytes;
    }

    protected static Date dateFromBytes(byte[] bytes) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(2000 + bytes[0], bytes[1], bytes[2], bytes[3], bytes[4]);
        return cal.getTime(); // get back a Date object
    }

    protected static byte[] bytesFromDate(Date date) {
        byte [] bytes = new byte[5];
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        bytes[0] = (byte)(calendar.get(Calendar.YEAR) - 2000);
        bytes[1] = (byte)(calendar.get(Calendar.MONTH));
        bytes[2] = (byte)(calendar.get(Calendar.DAY_OF_MONTH));
        bytes[3] = (byte)(calendar.get(Calendar.HOUR));
        bytes[4] = (byte)(calendar.get(Calendar.MINUTE));

        return bytes;
    }
}
