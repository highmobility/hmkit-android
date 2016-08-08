package com.high_mobility.digitalkey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.high_mobility.HMLink.DeviceCertificate;
import com.high_mobility.HMLink.Shared.ByteUtils;
import com.high_mobility.HMLink.Shared.Shared;
import com.high_mobility.digitalkey.broadcast.BroadcastActivity;
import com.high_mobility.digitalkey.scan.ScanActivity;

public class MainActivity extends AppCompatActivity {
    public static final byte[] CaAppId = ByteUtils.bytesFromHex("***REMOVED***");
    public static final byte[] DEVICE_PUBLIC_KEY = ByteUtils.bytesFromHex("***REMOVED***");
    public static final byte[] DEVICE_PRIVATE_KEY = ByteUtils.bytesFromHex("***REMOVED***");
    public static final byte[] DEVICE_SERIAL = ByteUtils.bytesFromHex("01231910D62CA571EF");
    public static final byte[] CA_PUBLIC_KEY = ByteUtils.bytesFromHex("***REMOVED***");

    Shared shared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDeviceCertificate();
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    void setDeviceCertificate() {
        // Create a demo certificate. In real life situation the certificate should be queried from the server
        // Reference: http://dc-9141.high-mobility.com/android-tutorial/#setting-device-certificate
        // Reference: http://dc-9141.high-mobility.com/android-reference-device-certificate/#convenience-init
        final byte[] APP_IDENTIFIER = ByteUtils.bytesFromHex("***REMOVED***");
        final byte[] ISSUER = ByteUtils.bytesFromHex("48494D4F");

        DeviceCertificate cert = new DeviceCertificate(ISSUER, APP_IDENTIFIER, DEVICE_SERIAL, DEVICE_PUBLIC_KEY);
        cert.setSignature(ByteUtils.bytesFromHex("***REMOVED***"));
        // set the device certificate.
        shared = Shared.getInstance();
        shared.initialize(cert, DEVICE_PRIVATE_KEY, CA_PUBLIC_KEY, getApplicationContext());
    }

    public void onBroadcastClicked(View v) {
        Intent intent = new Intent(this, BroadcastActivity.class);
        startActivity(intent);
    }

    public void onScanClicked(View v) {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

}
