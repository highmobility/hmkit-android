package com.high_mobility.HMLink.Broadcasting;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.high_mobility.HMLink.LinkException;
import com.high_mobility.HMLink.Shared.AccessCertificate;
import com.high_mobility.HMLink.Shared.Certificate;
import com.high_mobility.digitalkey.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ttiganik on 14/04/16.
 *
 * Storage is used to access device's storage, where certificates are stored.
 *
 * Uses Android SharedPreferences.
 */
class Storage {
    private static final String ACCESS_CERTIFICATE_STORAGE_KEY = "ACCESS_CERTIFICATE_STORAGE_KEY";

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    Storage(Context ctx) {
        settings = ctx.getSharedPreferences("com.hm.wearable.UserPrefs",
                Context.MODE_PRIVATE);
        editor = settings.edit();
    }

    AccessCertificate[] getCertificates() {
        Set<String> bytesStringSet = settings.getStringSet(ACCESS_CERTIFICATE_STORAGE_KEY, null);

        if (bytesStringSet != null && bytesStringSet.size() > 0) {
            AccessCertificate[] certificates = new AccessCertificate[bytesStringSet.size()];

            int counter = 0;
            for (String bytesString : bytesStringSet) {
                AccessCertificate cert = new AccessCertificate(Utils.bytesFromHex(bytesString));
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
            stringSet.add(Utils.hexFromBytes(cert.getBytes()));
        }

        editor.putStringSet(ACCESS_CERTIFICATE_STORAGE_KEY, stringSet);
        editor.commit();
    }

    AccessCertificate[] getRegisteredCertificates(byte[] serialNumber) {
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

    AccessCertificate[] getStoredCertificates(byte[] serialNumber) {
        AccessCertificate[] certificates = getCertificates();
        ArrayList<AccessCertificate> storedCertificates = new ArrayList<>();

        for (AccessCertificate cert : certificates) {
            if (!Arrays.equals(cert.getGainerSerial(), serialNumber)) {
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

    boolean deleteCertificateWithGainingSerial(byte[] serial) {
        AccessCertificate[] certs = getCertificates();

        int removedIndex = -1;
        for (int i=0; i<certs.length; i++) {
            AccessCertificate cert = certs[i];
            if (Arrays.equals(cert.getGainerSerial(), serial)) {
                removedIndex = i;
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

    boolean deleteCertificateWithProvidingSerial(byte[] serial) {
        AccessCertificate[] certs = getCertificates();

        int removedIndex = -1;
        for (int i=0; i<certs.length; i++) {
            AccessCertificate cert = certs[i];
            if (Arrays.equals(cert.getProviderSerial(), serial)) {
                removedIndex = i;
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

    void storeCertificate(AccessCertificate certificate) throws LinkException {
        if (certificate == null) throw new LinkException(LinkException.LinkExceptionCode.INTERNAL_ERROR);

        AccessCertificate[] certs = getCertificates();

        if (certs.length >= 5) throw new LinkException(LinkException.LinkExceptionCode.STORAGE_FULL);

        for (int i = 0; i < certs.length; i++) {
            AccessCertificate cert = certs[i];
            if (Arrays.equals(cert.getGainerSerial(), certificate.getGainerSerial())
                && Arrays.equals(cert.getProviderSerial(), certificate.getProviderSerial())
                && Arrays.equals(cert.getGainerPublicKey(), certificate.getGainerPublicKey())) {

                if (!deleteCertificateWithGainingSerial(certificate.getGainerSerial())) {
                    Log.e(LocalDevice.TAG, "failed to delete cert");
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
        AccessCertificate cert1 = new AccessCertificate(Utils.bytesFromHex("***REMOVED***"));
        AccessCertificate cert2 = new AccessCertificate(Utils.bytesFromHex("***REMOVED***"));

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

        Log.i("", "TEST: store certs " + success);
    }
}
