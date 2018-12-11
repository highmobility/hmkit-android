package com.highmobility.hmkit

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.highmobility.crypto.AccessCertificate
import com.highmobility.crypto.value.DeviceSerial
import com.highmobility.value.Bytes
import org.junit.Assert.assertTrue


import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StorageTest {
    /*
        issuer: 746D6373
        providingSerial: 10342C3CBB9E845017
        gainingSerial: 04D982A5955382704A
        gainingPublicKey: E072E26D1776096D5FFF592A8B674E30DBD3AEC900F3E51C04D1D90CF33DFD38734A15C0A0F54FFF641EBA20C66564368CEB4CDF7574F91CC5D648ADBC44CB9F
        valid from: : 2018-03-20T14:13:00 to: 2027-01-01T17:17:00
        permissions: 1007FFFDFFEFFFFFFFFF1F0000000000
        signature: E38A1C015ADEEBAB8A0B12BC5E7E1B10C02CE55CC86E0E65B21883F057787C6740B8AFEE5BB996273032CA14829597328E09227725B1FA5104197A6E5E0E3B69
     */
    val cert1 = AccessCertificate(
            Bytes("01746D637310342C3CBB9E84501704D982A5955382704AE072E26D1776096D5FFF592A8B674E30DBD3AEC900F3E51C04D1D90CF33DFD38734A15C0A0F54FFF641EBA20C66564368CEB4CDF7574F91CC5D648ADBC44CB9F1203140C0D1B01010F11101007FFFDFFEFFFFFFFFF1F0000000000E38A1C015ADEEBAB8A0B12BC5E7E1B10C02CE55CC86E0E65B21883F057787C6740B8AFEE5BB996273032CA14829597328E09227725B1FA5104197A6E5E0E3B69"))

    /*
    issuer: 746D6373
    providingSerial: 10342C3CBB9E845017
    gainingSerial: CEDA19275FDDA3BCD7
    gainingPublicKey: 621D7CD37C94C3C716B3FE48848490F17FC2266922185D9EAB62F94700ABA5B713F9EB0AC6FA649128EE7F60101AB05E8E9C5A2DAEFDD94D9457641078037BA4
    valid from: : 2018-09-11T12:19:00 to: 2023-09-11T12:19:00
    permissions: 1007FFFDFFEFFFFFFFFF1F0000000000
    signature: 67F33E1E22C1181C194AE2FDC97D1C1921337F0A8125D1BC774FD8AEA386B6E7BC2B129B793B78DBEF611B24CC679DA613B985282099EE26615D2700CA2F1733
     */
    val cert2 = AccessCertificate(Bytes("01746D637310342C3CBB9E845017CEDA19275FDDA3BCD7621D7CD37C94C3C716B3FE48848490F17FC2266922185D9EAB62F94700ABA5B713F9EB0AC6FA649128EE7F60101AB05E8E9C5A2DAEFDD94D9457641078037BA412090B091317090B0913101007FFFDFFEFFFFFFFFF1F000000000067F33E1E22C1181C194AE2FDC97D1C1921337F0A8125D1BC774FD8AEA386B6E7BC2B129B793B78DBEF611B24CC679DA613B985282099EE26615D2700CA2F1733"))
    val storage = Storage(ApplicationProvider.getApplicationContext<Context>())

    @Test
    fun testCertAdded() {
        storage.storeCertificate(cert1)
        assertTrue(storage.getCertificate(cert1.gainerSerial) != null)
        assertTrue(storage.getCertificate(cert1.providerSerial) == null)
    }

    @Test
    fun testCertDeleted() {
        storage.storeCertificate(cert1)
        assertTrue(storage.getCertificate(cert1.gainerSerial) != null)
        assertTrue(storage.getCertificate(cert1.providerSerial) == null)
    }

    @Test
    fun testCertsNotDuplicated() {
        storage.storeCertificate(cert1)
        assertTrue(storage.getCertificate(cert1.gainerSerial) != null)
        storage.storeCertificate(cert1)
        assertTrue(storage.getCertificatesWithGainingSerial(cert1.gainerSerial.byteArray).size == 1)
    }

    @Test
    fun testStorageCleared() {
        storage.storeCertificate(cert1)
        storage.storeCertificate(cert2)
        assertTrue(storage.certificates.size == 2)
        storage.deleteCertificates()
        assertTrue(storage.certificates.isEmpty())
    }

    @Test
    fun testCertDeletedWithGaining() {
        storage.storeCertificate(cert1)
        assertTrue(storage.certificates.size == 1)
        storage.deleteCertificate(Bytes("04D982A5955382704A").byteArray, null)
        assertTrue(storage.certificates.count() == 0)
    }

    @Test
    fun testCertDeletedWithProviding() {
        storage.storeCertificate(cert1)
        assertTrue(storage.certificates.size == 1)
        storage.deleteCertificate(null, Bytes("10342C3CBB9E845017").byteArray)
        assertTrue(storage.certificates.count() == 0)
    }

    @Test
    fun testMultipleCertsWithSameSerialDeleted() {
        // This is to test that when in some cases 2 certs with the same gaining/providing are in
        // storage, then both are deleted.

        // these have the same gaining 10342C3CBB9E845017, but different providing
        val cert3 = AccessCertificate(
                Bytes("01746D637304D982A5955382704A10342C3CBB9E845017E072E26D1776096D5FFF592A8B674E30DBD3AEC900F3E51C04D1D90CF33DFD38734A15C0A0F54FFF641EBA20C66564368CEB4CDF7574F91CC5D648ADBC44CB9F1203140C0D1B01010F11101007FFFDFFEFFFFFFFFF1F0000000000E38A1C015ADEEBAB8A0B12BC5E7E1B10C02CE55CC86E0E65B21883F057787C6740B8AFEE5BB996273032CA14829597328E09227725B1FA5104197A6E5E0E3B69"))
        val cert4 = AccessCertificate(Bytes("01746D6373CEDA19275FDDA3BCD710342C3CBB9E845017621D7CD37C94C3C716B3FE48848490F17FC2266922185D9EAB62F94700ABA5B713F9EB0AC6FA649128EE7F60101AB05E8E9C5A2DAEFDD94D9457641078037BA412090B091317090B0913101007FFFDFFEFFFFFFFFF1F000000000067F33E1E22C1181C194AE2FDC97D1C1921337F0A8125D1BC774FD8AEA386B6E7BC2B129B793B78DBEF611B24CC679DA613B985282099EE26615D2700CA2F1733"))

        // field ones have the same providing 10342C3CBB9E845017, but different gaining
        storage.storeCertificate(cert1)
        storage.storeCertificate(cert2)
        storage.storeCertificate(cert3)
        storage.storeCertificate(cert4)
        assertTrue(storage.certificates.size == 4)

        //delete 2 duplicate certs
        storage.deleteCertificate(null, Bytes("10342C3CBB9E845017").byteArray)
        // now 2 left
        assertTrue(storage.certificates.count() == 2)
        assertTrue(storage.getCertificate(DeviceSerial("10342C3CBB9E845017")) != null) // there should only be 1 cert with same gaining/providing

        // delete rest of the duplicate certs
        storage.deleteCertificate(Bytes("10342C3CBB9E845017").byteArray, null)
        // 0 left
        assertTrue(storage.certificates.count() == 0)
    }
}