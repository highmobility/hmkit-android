package com.high_mobility.digitalkey.broadcast;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.high_mobility.HMLink.AccessCertificate;
import com.high_mobility.HMLink.Broadcasting.ByteUtils;
import com.high_mobility.HMLink.Broadcasting.LocalDevice;
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
    enum BoxType {
        Red, NoBox, Yellow, Raspberry
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

        AccessCertificate registeredCertificate;

        if (type == BoxType.Red) {
            registeredCertificate = new AccessCertificate(CAR_SERIAL_RED, CAR_PUBLIC_RED, deviceSerial, startDate, endDate, null);
        }
        else if (type == BoxType.NoBox) {
            registeredCertificate = new AccessCertificate(CAR_SERIAL_NO_BOX, CAR_PUBLIC_NO_BOX, deviceSerial, startDate, endDate, null);
        }
        else if (type == BoxType.Yellow) {
            registeredCertificate = new AccessCertificate(CAR_SERIAL_YELLOW, CAR_PUBLIC_YELLOW, deviceSerial, startDate, endDate, null);
        }
        else if (type == BoxType.Raspberry) {
            registeredCertificate = new AccessCertificate(CAR_SERIAL_RASPBERRY, CAR_PUBLIC_RASPBERRY, deviceSerial, startDate, endDate, null);
        }
        else {
            return null;
        }


        byte[] signature = Crypto.sign(registeredCertificate.getBytes(), CA_PRIVATE_KEY);
        registeredCertificate.setSignature(signature);
        return registeredCertificate;
    }

    public AccessCertificate storedCertificateForBoxType(BoxType type) {
        Date startDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.YEAR, 1);
        Date endDate = c.getTime();

        AccessCertificate storedCertificate;
        if (type == BoxType.Red) {
            storedCertificate = new AccessCertificate(deviceSerial, devicePublic, CAR_SERIAL_RED, startDate, endDate, null);
        }
        else if (type == BoxType.NoBox) {
            storedCertificate = new AccessCertificate(deviceSerial, devicePublic, CAR_SERIAL_NO_BOX, startDate, endDate, null);
        }
        else if (type == BoxType.Yellow) {
            storedCertificate = new AccessCertificate(deviceSerial, devicePublic, CAR_SERIAL_YELLOW, startDate, endDate, null);
        }
        else if (type == BoxType.Raspberry) {
            storedCertificate = new AccessCertificate(deviceSerial, devicePublic, CAR_SERIAL_RASPBERRY, startDate, endDate, null);
        }
        else {
            return null;
        }

        byte[] signature = Crypto.sign(storedCertificate.getBytes(), CA_PRIVATE_KEY);
        storedCertificate.setSignature(signature);
        return storedCertificate;
    }

    public void registerAndStoreAllCertificates(LocalDevice device) {
        // create the AccessCertificates for the car to read(stored certificate)
        // and register ourselves with the car already(registeredCertificate)

        AccessCertificate redRegisteredCertificate = registerCertificateForBoxType(CertUtils.BoxType.Red);
        try {
            device.registerCertificate(redRegisteredCertificate);
        } catch (LinkException e) {
            Log.e(BroadcastActivity.TAG, "Cannot register cert " + redRegisteredCertificate.getGainerSerial(), e);
        }

        if (!isCertificateReadForType(CertUtils.BoxType.Red)) {
            AccessCertificate storedCertificate = storedCertificateForBoxType(CertUtils.BoxType.Red);
            try {
                device.storeCertificate(storedCertificate);
            } catch (LinkException e) {
                Log.e(BroadcastActivity.TAG, "Cannot store cert " + storedCertificate.getProviderSerial(), e);
            }
        }

        AccessCertificate noBoxRegisteredCertificate = registerCertificateForBoxType(CertUtils.BoxType.NoBox);
        try {
            device.registerCertificate(noBoxRegisteredCertificate );
        } catch (LinkException e) {
            Log.e(BroadcastActivity.TAG, "Cannot register cert " + noBoxRegisteredCertificate .getGainerSerial(), e);
        }

        if (!isCertificateReadForType(CertUtils.BoxType.NoBox)) {
            AccessCertificate storedCertificate = storedCertificateForBoxType(CertUtils.BoxType.NoBox);
            try {
                device.storeCertificate(storedCertificate);
            } catch (LinkException e) {
                Log.e(BroadcastActivity.TAG, "Cannot store cert " + storedCertificate.getProviderSerial(), e);
            }
        }

        AccessCertificate yellowRegisteredCertificate = registerCertificateForBoxType(CertUtils.BoxType.Yellow);
        try {
            device.registerCertificate(yellowRegisteredCertificate );
        } catch (LinkException e) {
            Log.e(BroadcastActivity.TAG, "Cannot register cert " + yellowRegisteredCertificate .getGainerSerial(), e);
        }

        if (!isCertificateReadForType(CertUtils.BoxType.Yellow)) {
            AccessCertificate storedCertificate = storedCertificateForBoxType(CertUtils.BoxType.Yellow);
            try {
                device.storeCertificate(storedCertificate);
                Log.i(BroadcastActivity.TAG, "stored cert");
            } catch (LinkException e) {
                Log.e(BroadcastActivity.TAG, "Cannot store cert " + storedCertificate.getProviderSerial(), e);
            }
        }

        AccessCertificate raspberryRegisteredCertificate = registerCertificateForBoxType(BoxType.Raspberry);
        try {
            device.registerCertificate(raspberryRegisteredCertificate );
        } catch (LinkException e) {
            Log.e(BroadcastActivity.TAG, "Cannot register cert " + raspberryRegisteredCertificate .getGainerSerial(), e);
        }

        if (!isCertificateReadForType(BoxType.Raspberry)) {
            AccessCertificate storedCertificate = storedCertificateForBoxType(BoxType.Raspberry);
            try {
                device.storeCertificate(storedCertificate);
                Log.i(BroadcastActivity.TAG, "stored cert");
            } catch (LinkException e) {
                Log.e(BroadcastActivity.TAG, "Cannot store cert " + storedCertificate.getProviderSerial(), e);
            }
        }
    }
}
