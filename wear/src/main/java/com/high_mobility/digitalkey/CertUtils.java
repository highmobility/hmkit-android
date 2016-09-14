package com.high_mobility.digitalkey;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.high_mobility.HMLink.AccessCertificate;
import com.high_mobility.HMLink.Shared.Broadcaster;
import com.high_mobility.HMLink.Shared.ByteUtils;
import com.high_mobility.HMLink.Crypto;
import com.high_mobility.HMLink.LinkException;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ttiganik on 27/05/16.
 */
public class CertUtils {
    static final String TAG = "CertUtils";
    enum BoxType {
        Red, NoBox, Yellow, Raspberry, Iphone
    }
    static final String CERT_UTILS_STORAGE_KEY = "CERT_UTILS_STORAGE_KEY";
    static final byte[] CA_PRIVATE_KEY = ByteUtils.bytesFromHex("***REMOVED***");

    static final byte[] CAR_SERIAL_RED = ByteUtils.bytesFromHex("01230924D72CA571EE");
    static final byte[] CAR_PUBLIC_RED = ByteUtils.bytesFromHex("***REMOVED***");

    static final byte[] CAR_SERIAL_NO_BOX = ByteUtils.bytesFromHex("01231910D62CA571EE");
    static final byte[] CAR_PUBLIC_NO_BOX = ByteUtils.bytesFromHex("***REMOVED***");

    static final byte[] CAR_SERIAL_YELLOW = ByteUtils.bytesFromHex("01234268D62CA571EE");
    static final byte[] CAR_PUBLIC_YELLOW = ByteUtils.bytesFromHex("***REMOVED***");

    static final byte[] CAR_SERIAL_RASPBERRY = ByteUtils.bytesFromHex("010203030303030202");
    static final byte[] CAR_PUBLIC_RASPBERRY = ByteUtils.bytesFromHex("***REMOVED***");

    static final byte[] CAR_SERIAL_IPHONE = ByteUtils.bytesFromHex("42463136393843362D");
    static final byte[] CAR_PUBLIC_IPHONE = ByteUtils.bytesFromHex("***REMOVED***");

    byte[] deviceSerial;
    byte[] devicePublic;

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    CertUtils(Context context, byte[] deviceSerial, byte[] devicePublic) {
        settings = context.getSharedPreferences("com.hm.wearable.CertUtilsUserPrefs", Context.MODE_PRIVATE);
        editor = settings.edit();
        this.deviceSerial = deviceSerial;
        this.devicePublic = devicePublic;
    }

    public boolean isCertificateReadForType(BoxType type) {
        Set<String> certificatesReadStringSet = settings.getStringSet(CERT_UTILS_STORAGE_KEY, null);
        if (certificatesReadStringSet == null) return false;
        byte[] serial;

        if (type == BoxType.Red) {
            serial = CAR_SERIAL_RED;
        }
        else if (type == BoxType.NoBox) {
            serial = CAR_SERIAL_NO_BOX;
        }
        else if (type == BoxType.Yellow) {
            serial = CAR_SERIAL_YELLOW;
        }
        else if (type == BoxType.Raspberry) {
            serial = CAR_SERIAL_RASPBERRY;
        }
        else {
            return false;
        }

        for (String storedSerial : certificatesReadStringSet) {
            if (storedSerial.equals(ByteUtils.hexFromBytes(serial)))
                return true;
        }

        return false;
    }

    public void onCertificateReadForSerial(byte[] serial) {
        Set<String> certificatesReadStringSet = settings.getStringSet(CERT_UTILS_STORAGE_KEY, null);
        if (certificatesReadStringSet == null) certificatesReadStringSet = new HashSet<>();
        String serialString = ByteUtils.hexFromBytes(serial);

        // verify that this serial is not stored yet
        for (String storedSerial : certificatesReadStringSet) {
            if (storedSerial.equals(serialString)) {
                return;
            }
        }

        certificatesReadStringSet.add(serialString);

        editor.putStringSet(CERT_UTILS_STORAGE_KEY, certificatesReadStringSet);
        editor.commit();
    }

    public void reset() {
        editor.remove(CERT_UTILS_STORAGE_KEY);
        editor.commit();
    }

    public AccessCertificate registerCertificateForBoxType(BoxType type) {
        Date startDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.YEAR, 1);
        Date endDate = c.getTime();

        AccessCertificate certificate;

        if (type == BoxType.Red) {
            certificate = new AccessCertificate(CAR_SERIAL_RED, CAR_PUBLIC_RED, deviceSerial, startDate, endDate, null);
        }
        else if (type == BoxType.NoBox) {
            certificate = new AccessCertificate(CAR_SERIAL_NO_BOX, CAR_PUBLIC_NO_BOX, deviceSerial, startDate, endDate, null);
        }
        else if (type == BoxType.Yellow) {
            certificate = new AccessCertificate(CAR_SERIAL_YELLOW, CAR_PUBLIC_YELLOW, deviceSerial, startDate, endDate, null);
        }
        else if (type == BoxType.Raspberry) {
            certificate = new AccessCertificate(CAR_SERIAL_RASPBERRY, CAR_PUBLIC_RASPBERRY, deviceSerial, startDate, endDate, null);
        }
        else if (type == BoxType.Iphone) {
            certificate = new AccessCertificate(CAR_SERIAL_IPHONE, CAR_PUBLIC_IPHONE, deviceSerial, startDate, endDate, null);
        }
        else {
            return null;
        }


        byte[] signature = Crypto.sign(certificate.getBytes(), CA_PRIVATE_KEY);
        certificate.setSignature(signature);
        return certificate;
    }

    public AccessCertificate storedCertificateForBoxType(BoxType type) {
        Date startDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.YEAR, 1);
        Date endDate = c.getTime();

        AccessCertificate certificate;
        if (type == BoxType.Red) {
            certificate = new AccessCertificate(deviceSerial, devicePublic, CAR_SERIAL_RED, startDate, endDate, null);
        }
        else if (type == BoxType.NoBox) {
            certificate = new AccessCertificate(deviceSerial, devicePublic, CAR_SERIAL_NO_BOX, startDate, endDate, null);
        }
        else if (type == BoxType.Yellow) {
            certificate = new AccessCertificate(deviceSerial, devicePublic, CAR_SERIAL_YELLOW, startDate, endDate, null);
        }
        else if (type == BoxType.Raspberry) {
            certificate = new AccessCertificate(deviceSerial, devicePublic, CAR_SERIAL_RASPBERRY, startDate, endDate, null);
        }
        else if (type == BoxType.Iphone) {
            certificate = new AccessCertificate(deviceSerial, devicePublic, CAR_SERIAL_IPHONE, startDate, endDate, null);
        }
        else {
            return null;
        }

        byte[] signature = Crypto.sign(certificate.getBytes(), CA_PRIVATE_KEY);
        certificate.setSignature(signature);
        return certificate;
    }

    void registerAndStoreCertificateForType(BoxType type, Broadcaster device) {
        AccessCertificate registeredCertificate = registerCertificateForBoxType(type);
        int errorCode = device.registerCertificate(registeredCertificate);
        if (errorCode != 0) {
            Log.e(TAG, "Cannot register cert " + registeredCertificate.getGainerSerial() + " " + errorCode);
            return;
        }

        if (!isCertificateReadForType(type)) {
            AccessCertificate storedCertificate = storedCertificateForBoxType(type);

            errorCode = device.storeCertificate(storedCertificate);
            if (errorCode != 0) Log.e(TAG, "Cannot store cert " + registeredCertificate.getGainerSerial() + " " + errorCode);
        }
    }

    public void registerAndStoreAllCertificates(Broadcaster device) {
        // create the AccessCertificates for the car to read(stored certificate)
        // and register ourselves with the car already(registeredCertificate)
        for (BoxType type : BoxType.values()) {
            registerAndStoreCertificateForType(type, device);
        }
    }
}
