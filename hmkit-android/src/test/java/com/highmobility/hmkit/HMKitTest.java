package com.highmobility.hmkit;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.highmobility.crypto.DeviceCertificate;
import com.highmobility.crypto.value.PrivateKey;
import com.highmobility.crypto.value.PublicKey;
import com.highmobility.value.Bytes;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;


@RunWith(RobolectricTestRunner.class)
public class HMKitTest {
    Context context = ApplicationProvider.getApplicationContext();

    public void testConstructor() {
        // cannot run this right now because unit test doesn't have access to NDK

        DeviceCertificate cert = new DeviceCertificate(new Bytes("AAAAAAAABBBBBBBBBBBBBBBBBBBBBBBBCCCCCCCCCCCCCCCCCC865856E69C351F0F2C616FA4D101AA55D593896806C1EE3D3C1AF07BC0F999C2B2A700307F827E131DB4F63FDADB4F0A89BCE88E0E40521F08E1AC4D122BF896"));

        // these are random keys, not used anywhere
        PrivateKey privateKey = new PrivateKey("C4CEE2704DB595C7A628ACCCF4B6D09F175FEA98F372BE15DC2D324DEA237ECE");
        PublicKey publicKey = new PublicKey("865856E69C351F0F2C616FA4D101AA55D593896806C1EE3D3C1AF07BC0F999C2B2A700307F827E131DB4F63FDADB4F0A89BCE88E0E40521F08E1AC4D122BF896");

        HMKit.Configuration configuration = new HMKit.Configuration.Builder()
          .bleReturnFullOffset(true)
          .build();

        HMKit.getInstance().initialise(
          cert.getBase64(),
          privateKey.getBase64(),
          publicKey.getBase64(),
          context,
          configuration
        );
    }
}
