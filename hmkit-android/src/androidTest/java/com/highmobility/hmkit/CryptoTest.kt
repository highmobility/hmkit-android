package com.highmobility.hmkit;

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.highmobility.crypto.value.PrivateKey
import com.highmobility.crypto.value.PublicKey
import com.highmobility.value.Bytes
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test crypto methods
 */
@RunWith(AndroidJUnit4::class)
class CryptoTest {

    private lateinit var kit:HMKit

    @Test fun testSignatures() {
        val crypto = kit.crypto
        var publicKey =
                PublicKey("***REMOVED***")
        val privateKey =
                PrivateKey("***REMOVED***")

        val data = Bytes("0203")
        val sig = crypto.sign(data, privateKey)

        assertTrue(crypto.verify(data, sig, publicKey))

        publicKey =
                PublicKey("***REMOVED***")
        assertTrue(crypto.verify(data, sig, publicKey) == false)
    }

    @Before
    fun setup() {
        kit = HMKit.getInstance()
        kit.initialise(ApplicationProvider.getApplicationContext())
    }
}
