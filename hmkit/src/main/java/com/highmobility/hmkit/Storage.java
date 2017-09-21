package com.highmobility.hmkit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.highmobility.byteutils.Bytes;
import com.highmobility.crypto.AccessCertificate;
import com.highmobility.crypto.Certificate;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.highmobility.hmkit.Broadcaster.TAG;

/**
 * Created by ttiganik on 14/04/16.
 *
 * Storage is used to access broadcaster's storage, where certificates are stored.
 *
 * Uses Android SharedPreferences.
 */
public class Storage {
    private static final String ACCESS_CERTIFICATE_STORAGE_KEY = "ACCESS_CERTIFICATE_STORAGE_KEY";

    public enum Result {
        SUCCESS(0), STORAGE_FULL(1), INTERNAL_ERROR(2);

        private final int value;
        Result(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    Storage(Context ctx) {
        settings = ctx.getSharedPreferences("com.hm.wearable.UserPrefs",
                Context.MODE_PRIVATE);
        editor = settings.edit();
    }

    AccessCertificate storeDownloadedCertificates(JSONObject response) throws Exception {
        if (response.has("device_access_certificate") == false) throw new Exception();

        String vehicleAccessCertificateBase64, deviceAccessCertificateBase64;
        AccessCertificate vehicleAccessCertificate, deviceAccessCertificate;

        // providing device, gaining vehicle
        deviceAccessCertificateBase64 = response.getString("device_access_certificate");
        deviceAccessCertificate = new AccessCertificate(deviceAccessCertificateBase64);
        Result result = storeCertificate(deviceAccessCertificate);
        if (result != Result.SUCCESS) {
            if (Manager.getInstance().loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue()) {
                Log.d(TAG, "storeDownloadedCertificates: storeCertificate failed " + result);
            }
            throw new Exception();
        }
        if (Manager.getInstance().loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "storeDownloadedCertificates: deviceCert " + deviceAccessCertificate.toString());

        if (response.has("vehicle_access_certificate") == true) {
            // stored cert. this does not has to exist in the response
            vehicleAccessCertificateBase64 = response.getString("vehicle_access_certificate");
            if (vehicleAccessCertificateBase64 != null && vehicleAccessCertificateBase64.equals("null") == false) {
                vehicleAccessCertificate = new AccessCertificate(vehicleAccessCertificateBase64);

                if (storeCertificate(vehicleAccessCertificate) != Result.SUCCESS) {
                    Log.d(TAG, "storeDownloadedCertificates: " + "cannot store vehicle access cert");
                    throw new Exception();
                }

                if (Manager.getInstance().loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                    Log.d(TAG, "storeDownloadedCertificates: vehicleCert " + vehicleAccessCertificate.toString());
            }
        }

        return deviceAccessCertificate;
    }

    AccessCertificate[] getCertificates() {
        Set<String> bytesStringSet = settings.getStringSet(ACCESS_CERTIFICATE_STORAGE_KEY, null);

        if (bytesStringSet != null && bytesStringSet.size() > 0) {
            AccessCertificate[] certificates = new AccessCertificate[bytesStringSet.size()];

            int counter = 0;
            for (String bytesString : bytesStringSet) {
                AccessCertificate cert = new AccessCertificate(Bytes.bytesFromHex(bytesString));
                certificates[counter] = cert;
                counter++;
            }

            return certificates;
        }

        return new AccessCertificate[0];
    }

    void setCertificates(AccessCertificate[] certificates) {
        HashSet<String> stringSet = new HashSet<>();

        for (Certificate cert : certificates) {
            stringSet.add(Bytes.hexFromBytes(cert.getBytes()));
        }

        editor.putStringSet(ACCESS_CERTIFICATE_STORAGE_KEY, stringSet);
        editor.commit();
    }

    AccessCertificate[] getCertificatesWithGainingSerial(byte[] serialNumber) {
        AccessCertificate[] certificates = getCertificates();
        ArrayList<AccessCertificate> storedCertificates = new ArrayList<>();

        for (AccessCertificate cert : certificates) {
            if (Arrays.equals(cert.getGainerSerial(), serialNumber)) {
                storedCertificates.add(cert);
            }
        }

        if (storedCertificates.size() > 0) {
            return storedCertificates.toArray(new AccessCertificate[storedCertificates.size()]);
        }

        return new AccessCertificate[0];
    }

    AccessCertificate[] getCertificatesWithProvidingSerial(byte[] serialNumber) {
        AccessCertificate[] certificates = getCertificates();
        ArrayList<AccessCertificate> storedCertificates = new ArrayList<>();

        for (AccessCertificate cert : certificates) {
            if (Arrays.equals(cert.getProviderSerial(), serialNumber)) {
                storedCertificates.add(cert);
            }
        }

        if (storedCertificates.size() > 0) {
            return storedCertificates.toArray(new AccessCertificate[storedCertificates.size()]);
        }

        return new AccessCertificate[0];
    }

    AccessCertificate[] getCertificatesWithoutProvidingSerial(byte[] serialNumber) {
        AccessCertificate[] certificates = getCertificates();
        ArrayList<AccessCertificate> storedCertificates = new ArrayList<>();

        for (AccessCertificate cert : certificates) {
            if (!Arrays.equals(cert.getProviderSerial(), serialNumber)) {
                storedCertificates.add(cert);
            }
        }

        if (storedCertificates.size() > 0) {
            return storedCertificates.toArray(new AccessCertificate[storedCertificates.size()]);
        }

        return new AccessCertificate[0];

    }

    void resetStorage() {
        editor.remove(ACCESS_CERTIFICATE_STORAGE_KEY);
        editor.commit();
    }

    boolean deleteCertificate(byte[] gainingSerial, byte[] providingSerial) {
        AccessCertificate[] certs = getCertificates();

        int removedIndex = -1;
        for (int i = 0; i < certs.length; i++) {
            AccessCertificate cert = certs[i];
            if (Arrays.equals(cert.getGainerSerial(), gainingSerial) &&
                Arrays.equals(cert.getProviderSerial(), providingSerial)) {
                removedIndex = i;

                if (Manager.getInstance().loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue()) {
                    Log.d(TAG, "deleteCertificate success: " + cert.toString());
                }

                break;
            }
        }

        if (removedIndex != -1) {
            AccessCertificate[] newCerts = removeAtIndex(removedIndex, certs);
            setCertificates(newCerts);
            return true;
        }
        else {
            if (Manager.getInstance().loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue()) {
                Log.d(TAG, "deleteCertificate: failed for gaining: " + Bytes.hexFromBytes(gainingSerial)
                        + " providing: " + Bytes.hexFromBytes(providingSerial));
            }
            return false;
        }
    }

    boolean deleteCertificateWithGainingSerial(byte[] serial) {
        AccessCertificate[] certs = getCertificates();

        int removedIndex = -1;
        for (int i=0; i<certs.length; i++) {
            AccessCertificate cert = certs[i];
            if (Arrays.equals(cert.getGainerSerial(), serial)) {
                removedIndex = i;
                if (Manager.getInstance().loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue()) {
                    Log.d(TAG, "deleteCertificateWithGainingSerial success:" + cert.toString());
                }
                break;
            }
        }

        if (removedIndex != -1) {
            AccessCertificate[] newCerts = removeAtIndex(removedIndex, certs);
            setCertificates(newCerts);
            return true;
        }
        else {
            if (Manager.getInstance().loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue()) {
                Log.d(TAG, "deleteCertificateWithGainingSerial failed: " + Bytes.hexFromBytes(serial));
            }
            return false;
        }
    }

    boolean deleteCertificateWithProvidingSerial(byte[] serial) {
        AccessCertificate[] certs = getCertificates();

        int removedIndex = -1;
        for (int i=0; i<certs.length; i++) {
            AccessCertificate cert = certs[i];
            if (Arrays.equals(cert.getProviderSerial(), serial)) {
                removedIndex = i;
                if (Manager.getInstance().loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue()) {
                    Log.d(TAG, "deleteCertificateWithProvidingSerial success: " + cert.toString());
                }
                break;
            }
        }

        if (removedIndex != -1) {
            AccessCertificate[] newCerts = removeAtIndex(removedIndex, certs);
            setCertificates(newCerts);
            return true;
        }
        else {
            return false;
        }
    }

    AccessCertificate certWithProvidingSerial(byte[] serial) {
        AccessCertificate[] certs = getCertificates();

        for (int i=0; i<certs.length; i++) {
            AccessCertificate cert = certs[i];

            if (Arrays.equals(cert.getProviderSerial(), serial)) {
                return cert;
            }
        }

        return null;
    }

    AccessCertificate certWithGainingSerial(byte[] serial) {
        AccessCertificate[] certs = getCertificates();

        for (int i = 0; i < certs.length; i++) {
            AccessCertificate cert = certs[i];
            if (Arrays.equals(cert.getGainerSerial(), serial)) {
                return cert;
            }
        }

        return null;
    }

    Result storeCertificate(AccessCertificate certificate) {
        if (certificate == null) return Result.INTERNAL_ERROR;

        AccessCertificate[] certs = getCertificates();

        if (certs.length >= Constants.certificateStorageCount) return Result.STORAGE_FULL;

        // delete existing cert with same serials
        for (int i = 0; i < certs.length; i++) {
            AccessCertificate cert = certs[i];
            if (Arrays.equals(cert.getGainerSerial(), certificate.getGainerSerial())
                && Arrays.equals(cert.getProviderSerial(), certificate.getProviderSerial())
                && Arrays.equals(cert.getGainerPublicKey(), certificate.getGainerPublicKey())) {

                if (!deleteCertificateWithGainingSerial(certificate.getGainerSerial())) {
                    Log.e(TAG, "failed to delete existing cert");
                }
            }
        }

        certs = getCertificates();
        AccessCertificate[] newCerts = new AccessCertificate[certs.length + 1];

        for (int i = 0; i < certs.length; i++) {
            newCerts[i] = certs[i];
        }

        newCerts[newCerts.length - 1] = certificate;

        setCertificates(newCerts);

        return Result.SUCCESS;
    }

    private AccessCertificate[] removeAtIndex(int removedIndex, AccessCertificate[] certs) {
        AccessCertificate[] newCerts = new AccessCertificate[certs.length - 1];

        for (int i = 0; i < certs.length; i++) {
            if (i < removedIndex) {
                newCerts[i] = certs[i];
            }
            else if (i > removedIndex){
                newCerts[i - 1] = certs[i];
            }
        }

        return newCerts;
    }

    private void _test_Storage() {
        editor.clear();
        editor.commit();
        AccessCertificate cert1 = new AccessCertificate(Bytes.bytesFromHex("***REMOVED***"));
        AccessCertificate cert2 = new AccessCertificate(Bytes.bytesFromHex("***REMOVED***"));

        AccessCertificate[] certs = new AccessCertificate[] {cert1, cert2};
        setCertificates(certs);

        AccessCertificate[] readCerts = getCertificates();
        boolean success = true;
        if (readCerts != null) {
            outerLoop:
            for (AccessCertificate storedCert : certs) {
                for (AccessCertificate readCert : readCerts) {
                    if (Arrays.equals(storedCert.getGainerPublicKey(), readCert.getGainerPublicKey())) {
                        continue outerLoop;
                    }
                }

                success = false;
                break;
            }
        }
        else {
            success = false;
        }

        Log.d("", "TEST: store certs " + success);
    }
}
