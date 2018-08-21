package com.highmobility.hmkit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.highmobility.crypto.AccessCertificate;
import com.highmobility.crypto.Certificate;
import com.highmobility.utils.ByteUtils;
import com.highmobility.value.Bytes;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.highmobility.hmkit.Broadcaster.TAG;

/**
 * Created by ttiganik on 14/04/16.
 * <p>
 * Storage is used to access broadcaster's storage, where certificates are stored.
 * <p>
 * Uses Android SharedPreferences.
 */
class Storage {
    private static final String ACCESS_CERTIFICATE_STORAGE_KEY = "ACCESS_CERTIFICATE_STORAGE_KEY";
    static final String device_certificate_json_object = "device_access_certificate";

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
        if (response.has(device_certificate_json_object) == false) {
            throw new Exception("response does not have a " + device_certificate_json_object + " " +
                    "object");
        }

        String vehicleAccessCertificateBase64, deviceAccessCertificateBase64;
        AccessCertificate vehicleAccessCertificate, deviceAccessCertificate;

        // providing device, gaining vehicle
        deviceAccessCertificateBase64 = response.getString(device_certificate_json_object);

        try {
            Bytes bytes = new Bytes(deviceAccessCertificateBase64);
            deviceAccessCertificate = new AccessCertificate(bytes);
        } catch (IllegalArgumentException e) {
            throw new Exception("response's " + deviceAccessCertificateBase64 + " bytes could " +
                    "not be parsed to an Access Certificate. " + e.getMessage());
        }

        Result result = storeCertificate(deviceAccessCertificate);
        if (result != Result.SUCCESS) {
            throw new Exception("certificate storage failed " + result);
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "storeDownloadedCertificates: deviceCert " + deviceAccessCertificate
                    .toString());

        if (response.has("vehicle_access_certificate") == true) {
            // stored cert. this does not has to exist in the response
            vehicleAccessCertificateBase64 = response.getString("vehicle_access_certificate");
            if (vehicleAccessCertificateBase64 != null && vehicleAccessCertificateBase64.equals
                    ("null") == false) {
                vehicleAccessCertificate = new AccessCertificate(new Bytes
                        (vehicleAccessCertificateBase64));

                if (storeCertificate(vehicleAccessCertificate) != Result.SUCCESS) {
                    throw new Exception("cannot store vehicle access cert");
                }

                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG
                        .getValue())
                    Log.d(TAG, "storeDownloadedCertificates: vehicleCert " +
                            vehicleAccessCertificate.toString());
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
                AccessCertificate cert = new AccessCertificate(new Bytes(bytesString));
                certificates[counter] = cert;
                counter++;
            }

            return certificates;
        }

        return new AccessCertificate[0];
    }

    boolean writeCertificates(AccessCertificate[] certificates) {
        HashSet<String> stringSet = new HashSet<>();

        for (Certificate cert : certificates) {
            stringSet.add(cert.getBytes().getHex());
        }

        editor.putStringSet(ACCESS_CERTIFICATE_STORAGE_KEY, stringSet);
        return editor.commit();
    }

    AccessCertificate[] getCertificatesWithGainingSerial(byte[] serialNumber) {
        AccessCertificate[] certificates = getCertificates();
        ArrayList<AccessCertificate> storedCertificates = new ArrayList<>();

        for (AccessCertificate cert : certificates) {
            if (cert.getGainerSerial().equals(serialNumber)) {
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
            if (cert.getProviderSerial().equals(serialNumber)) {
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
            if (cert.getProviderSerial().equals(serialNumber) == false) {
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
            if (cert.getGainerSerial().equals(gainingSerial) &&
                    cert.getProviderSerial().equals(providingSerial)) {
                removedIndex = i;

                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG
                        .getValue()) {
                    Log.d(TAG, "deleteCertificate success: " + cert.toString());
                }

                break;
            }
        }

        if (removedIndex != -1) {
            AccessCertificate[] newCerts = removeAtIndex(removedIndex, certs);
            if (writeCertificates(newCerts) == true) return true;
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue
                ()) {
            Log.d(TAG, "deleteCertificate: failed for gaining: " + ByteUtils.hexFromBytes
                    (gainingSerial)
                    + " providing: " + ByteUtils.hexFromBytes(providingSerial));
        }

        return false;
    }

    boolean deleteCertificateWithGainingSerial(byte[] serial) {
        AccessCertificate[] certs = getCertificates();

        int removedIndex = -1;
        for (int i = 0; i < certs.length; i++) {
            AccessCertificate cert = certs[i];
            if (cert.getGainerSerial().equals(serial)) {
                removedIndex = i;
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG
                        .getValue()) {
                    Log.d(TAG, "deleteCertificateWithGainingSerial success:" + cert.toString());
                }
                break;
            }
        }

        if (removedIndex != -1) {
            AccessCertificate[] newCerts = removeAtIndex(removedIndex, certs);
            if (writeCertificates(newCerts) == true) return true;
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue
                ()) {
            Log.d(TAG, "deleteCertificateWithGainingSerial failed: " + ByteUtils.hexFromBytes
                    (serial));
        }

        return false;
    }

    boolean deleteCertificateWithProvidingSerial(byte[] serial) {
        AccessCertificate[] certs = getCertificates();

        int removedIndex = -1;
        for (int i = 0; i < certs.length; i++) {
            AccessCertificate cert = certs[i];
            if (cert.getProviderSerial().equals(serial)) {
                removedIndex = i;
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG
                        .getValue()) {
                    Log.d(TAG, "deleteCertificateWithProvidingSerial success: " + cert.toString());
                }
                break;
            }
        }

        if (removedIndex != -1) {
            AccessCertificate[] newCerts = removeAtIndex(removedIndex, certs);
            return writeCertificates(newCerts) == true;
        }

        return false;
    }

    AccessCertificate certWithProvidingSerial(byte[] serial) {
        AccessCertificate[] certs = getCertificates();

        for (int i = 0; i < certs.length; i++) {
            AccessCertificate cert = certs[i];

            if (cert.getProviderSerial().equals(serial)) {
                return cert;
            }
        }

        return null;
    }

    AccessCertificate certWithGainingSerial(byte[] serial) {
        AccessCertificate[] certs = getCertificates();

        for (int i = 0; i < certs.length; i++) {
            AccessCertificate cert = certs[i];
            if (cert.getGainerSerial().equals(serial)) {
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
            if (cert.getGainerSerial().equals(certificate.getGainerSerial())
                    && cert.getProviderSerial().equals(certificate.getProviderSerial())
                    && cert.getGainerPublicKey().equals(certificate.getGainerPublicKey())) {

                if (!deleteCertificateWithGainingSerial(certificate.getGainerSerial()
                        .getByteArray())) {
                    Log.e(TAG, "failed to delete existing cert");
                }
            }
        }

        certs = getCertificates();
        AccessCertificate[] newCerts = new AccessCertificate[certs.length + 1];
        System.arraycopy(certs, 0, newCerts, 0, certs.length);
        newCerts[newCerts.length - 1] = certificate;

        if (writeCertificates(newCerts) == true) return Result.SUCCESS;

        return Result.STORAGE_FULL;
    }

    private AccessCertificate[] removeAtIndex(int removedIndex, AccessCertificate[] certs) {
        AccessCertificate[] newCerts = new AccessCertificate[certs.length - 1];

        for (int i = 0; i < certs.length; i++) {
            if (i < removedIndex) {
                newCerts[i] = certs[i];
            } else if (i > removedIndex) {
                newCerts[i - 1] = certs[i];
            }
        }

        return newCerts;
    }
}
