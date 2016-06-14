package com.high_mobility.digitalkey;

import com.high_mobility.HMLink.AccessCertificate;
import com.high_mobility.HMLink.Broadcasting.ByteUtils;
import com.high_mobility.HMLink.Crypto;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ttiganik on 27/05/16.
 */
public class CertUtils {
    static final byte[] CA_PRIVATE_KEY = ByteUtils.bytesFromHex("***REMOVED***");

//    static final byte[] CAR_SERIAL = ByteUtils.bytesFromHex("01231910D62CA571EE");
//    static final byte[] CAR_PUBLIC = ByteUtils.bytesFromHex("***REMOVED***");

    static final byte[] CAR_SERIAL = ByteUtils.bytesFromHex("01234268D62CA571EE");
    static final byte[] CAR_PUBLIC = ByteUtils.bytesFromHex("7FBE0002FFBF0002FFFF0820FFFF00415200FF7B00407FF700107D3F0000DFFFDFFF0250FFF60412FFFB0003EFFF88010009DFEE50660200143D002058660200");


    public static AccessCertificate demoRegisteredCertificate(byte[] device_serial) {
        Date startDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.YEAR, 1);
        Date endDate = c.getTime();

        AccessCertificate registeredCertificate = new AccessCertificate(CAR_SERIAL, CAR_PUBLIC, device_serial, startDate, endDate, null);

        byte[] signature = Crypto.sign(registeredCertificate.getBytes(), CA_PRIVATE_KEY);
        registeredCertificate.setSignature(signature);
        return registeredCertificate;
    }

    public static AccessCertificate demoStoredCertificate(byte[] device_serial, byte[] device_public_key) {
        Date startDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.YEAR, 1);
        Date endDate = c.getTime();

        AccessCertificate storedCertificate = new AccessCertificate(device_serial, device_public_key, CAR_SERIAL, startDate, endDate, null);
        byte[] signature = Crypto.sign(storedCertificate.getBytes(), CA_PRIVATE_KEY);
        storedCertificate.setSignature(signature);
        return storedCertificate;
    }
}
