// TODO: 24/07/2018 enable
/*
import com.highmobility.crypto.Crypto;
import com.highmobility.value.Bytes;
import com.highmobility.value.PrivateKey;
import com.highmobility.value.PublicKey;
import com.highmobility.value.Signature;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestSignatures {
    // only work on linux
    @Test public void testSignatures() {
        PublicKey publicKey = new PublicKey
                ("***REMOVED***");
        PrivateKey privateKey = new PrivateKey
                ("***REMOVED***");

        assertTrue(true);

        Bytes data = new Bytes(new byte[]{0x02, 0x03});
        Signature sig = Crypto.sign(data, privateKey);

        assertTrue(Crypto.verify(data, sig, publicKey));

        publicKey = new PublicKey("***REMOVED***");
        assertTrue(Crypto.verify(data, sig, publicKey) == false);
    }
}
*/
