package com.highmobility.hmkit;

import android.content.Context;
import android.content.SharedPreferences;

import com.highmobility.crypto.AccessCertificate;
import com.highmobility.crypto.Certificate;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.utils.ByteUtils;
import com.highmobility.value.Bytes;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Access for stored Access Certificates.
 * <p>
 * Uses Android SharedPreferences.
 */
public class Storage {
    private static final String ACCESS_CERTIFICATE_STORAGE_KEY = "ACCESS_CERTIFICATE_STORAGE_KEY";
    private static final String device_certificate_json_object = "device_access_certificate";

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

    private final SharedPreferences settings;
    private final SharedPreferences.Editor editor;

    Storage(Context ctx) {
        settings = ctx.getSharedPreferences("com.hm.wearable.UserPrefs",
                Context.MODE_PRIVATE);
        editor = settings.edit();
    }

    /**
     * @param serial The serial of the device that is providing access (eg this device).
     * @return All stored Access Certificates where the device with the given serial is providing
     * access.
     */
    public AccessCertificate[] getCertificates(DeviceSerial serial) {
        return getCertificatesWithProvidingSerial(serial.getByteArray());
    }

    /**
     * Find an Access Certificate with the given serial number.
     *
     * @param serial The serial number of the device that is gaining access.
     * @return An Access Certificate for the given serial if one exists, otherwise null.
     */
    @Nullable public AccessCertificate getCertificate(DeviceSerial serial) {
        AccessCertificate[] certificates = getCertificatesWithGainingSerial(serial
                .getByteArray());

        if (certificates != null && certificates.length > 0) {
            return certificates[0];
        }

        return null;
    }

    /**
     * Deletes all of the stored Access Certificates.
     */
    public void deleteCertificates() {
        editor.remove(ACCESS_CERTIFICATE_STORAGE_KEY);
        editor.commit();
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

        HMLog.d("storeDownloadedCertificates: deviceCert %s",
                deviceAccessCertificate.toString());

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

                HMLog.d("storeDownloadedCertificates: vehicleCert %s",
                        vehicleAccessCertificate.toString());
            }
        }

        return deviceAccessCertificate;
    }

    protected AccessCertificate[] getCertificates() {
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

    private boolean writeCertificates(AccessCertificate[] certificates) {
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
            return storedCertificates.toArray(new AccessCertificate[0]);
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
            return storedCertificates.toArray(new AccessCertificate[0]);
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
            return storedCertificates.toArray(new AccessCertificate[0]);
        }

        return new AccessCertificate[0];

    }

    /**
     * Delete all certs that have the given gaining serial or providing serial or both.
     *
     * @param gainingSerial   The gaining serial.
     * @param providingSerial The providing serial.
     * @return true if one or more certificates were deleted.
     */
    boolean deleteCertificate(@Nullable byte[] gainingSerial, @Nullable byte[] providingSerial) {
        HMLog.d("deleteCertificate for gaining: %s providing: %s",
                gainingSerial != null ? ByteUtils.hexFromBytes(gainingSerial) : "any",
                providingSerial != null ? ByteUtils.hexFromBytes(providingSerial) : "any");

        if (gainingSerial == null && providingSerial == null) return false;
        AccessCertificate[] certs = getCertificates();

        ArrayList<AccessCertificate> newCertificates = new ArrayList<>(certs.length);

        boolean foundCertToDelete = false;

        for (int i = 0; i < certs.length; i++) {
            AccessCertificate cert = certs[i];

            boolean certIsToBeDeleted = (gainingSerial == null || cert.getGainerSerial().equals(gainingSerial)) &&
                    (providingSerial == null || cert.getProviderSerial().equals(providingSerial));

            if (certIsToBeDeleted) {
                HMLog.d("will delete cert: %s", cert.toString());
                foundCertToDelete = true;
            } else {
                newCertificates.add(cert);
            }
        }

        if (foundCertToDelete) {
            boolean result = writeCertificates(newCertificates.toArray(new AccessCertificate[0]));
            if (result != true) HMLog.d("deleteCertificate: failed to write");
            return result;
        } else {
            HMLog.d("deleteCertificate: did not find a cert to delete");
            return false;
        }
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

        // replace existing cert with same serials if exists
        for (int i = 0; i < certs.length; i++) {
            AccessCertificate cert = certs[i];
            if (cert.getGainerSerial().equals(certificate.getGainerSerial())
                    && cert.getProviderSerial().equals(certificate.getProviderSerial())
                    && cert.getGainerPublicKey().equals(certificate.getGainerPublicKey())) {
                certs[i] = certificate;
                if (writeCertificates(certs) == true) return Result.SUCCESS;
                return Result.STORAGE_FULL;
            }
        }

        // otherwise add new cert
        AccessCertificate[] newCerts = new AccessCertificate[certs.length + 1];
        System.arraycopy(certs, 0, newCerts, 0, certs.length);
        newCerts[newCerts.length - 1] = certificate;

        if (writeCertificates(newCerts) == true) return Result.SUCCESS;

        return Result.STORAGE_FULL;
    }
}
