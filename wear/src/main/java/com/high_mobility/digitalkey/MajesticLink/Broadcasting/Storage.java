package com.high_mobility.digitalkey.MajesticLink.Broadcasting;

import com.high_mobility.digitalkey.MajesticLink.Shared.AccessCertificate;
import com.high_mobility.digitalkey.MajesticLink.Shared.DeviceCertificate;

import java.util.ArrayList;

/**
 * Created by ttiganik on 14/04/16.
 *
 * Storage is used to access device's storage, where certificates are securely stored.
 *
 * Uses Android Keystore.
 */
class Storage {
    static Storage instance;

    byte[] serialNumber;

    ArrayList<AccessCertificate> certificates = new ArrayList<>();
    DeviceCertificate deviceCertificate;

    static Storage getInstance() {
        if (instance == null) {
           instance = new Storage();
        }

        return instance;
    }

    // TODO:

    AccessCertificate[] getRegisteredCertificates() {
        return null;
    }

    AccessCertificate[] getStoredCertificates() {
        return null;
    }

    void resetStorage() {

    }



    boolean deleteCertificateWithGainingSerial(byte[] serial) {

        return true;
    }

    boolean deleteCertificateWithProvidingSerial(byte[] serial) {

        return true;
    }

    AccessCertificate certWithProvidingSerial(byte[] serial) {
        return null;
    }

    AccessCertificate certWithGainingSerial(byte[] serial) {

        return null;
    }

    void storeCertificate(AccessCertificate certificate, byte[] CAPublicKey) {


        certificates.add(certificate);
    }
}
