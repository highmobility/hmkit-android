package com.high_mobility.digitalkey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.high_mobility.HMLink.DeviceCertificate;
import com.high_mobility.HMLink.LinkException;
import com.high_mobility.HMLink.Shared.ByteUtils;
import com.high_mobility.HMLink.Shared.Manager;
import com.high_mobility.digitalkey.broadcast.BroadcastActivity;
import com.high_mobility.digitalkey.scan.ScanActivity;

import java.util.IllegalFormatCodePointException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";

    public static final byte[] DEVICE_PUBLIC_KEY = ByteUtils.bytesFromHex("***REMOVED***");
    public static final byte[] DEVICE_PRIVATE_KEY = ByteUtils.bytesFromHex("***REMOVED***");
    public static final byte[] DEVICE_SERIAL = ByteUtils.bytesFromHex("01231910D62CA571EF");
    public static final byte[] CA_PUBLIC_KEY = ByteUtils.bytesFromHex("***REMOVED***");

    Manager manager;
    CertUtils certUtils;

    @BindView(R.id.serial_textview) TextView serialTextView;
    @BindView(R.id.public_key_textview) TextView publicKeyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setDeviceCertificate();

//        Intent intent = new Intent(this, ScanActivity.class);
//        startActivity(intent);
    }

    void setDeviceCertificate() {
        // Create a demo certificate. In real life situation the certificate should be queried from the server
        // Reference: http://dc-9141.high-mobility.com/android-tutorial/#setting-device-certificate
        // Reference: http://dc-9141.high-mobility.com/android-reference-device-certificate/#convenience-init
        final byte[] APP_IDENTIFIER = ByteUtils.bytesFromHex("***REMOVED***");
        final byte[] ISSUER = ByteUtils.bytesFromHex("48494D4F");

        DeviceCertificate cert = new DeviceCertificate(ISSUER, APP_IDENTIFIER, DEVICE_SERIAL, DEVICE_PUBLIC_KEY);
        cert.setSignature(ByteUtils.bytesFromHex("***REMOVED***"));
        manager = Manager.getInstance();
        manager.initialize(cert, DEVICE_PRIVATE_KEY, CA_PUBLIC_KEY, getApplicationContext());

        serialTextView.setText(ByteUtils.hexFromBytes(manager.getCertificate().getSerial()));
        publicKeyTextView.setText(ByteUtils.hexFromBytes(manager.getCertificate().getPublicKey()));

        if (certUtils == null) {
            certUtils = new CertUtils(this, MainActivity.DEVICE_SERIAL, MainActivity.DEVICE_PUBLIC_KEY);
        }

        certUtils.registerAndStoreAllCertificates(manager.getBroadcaster());
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
