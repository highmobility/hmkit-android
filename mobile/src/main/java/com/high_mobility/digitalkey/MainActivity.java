package com.high_mobility.digitalkey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.high_mobility.HMLink.Crypto.DeviceCertificate;
import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Manager;
import com.high_mobility.digitalkey.broadcast.BroadcastActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";

    public static final byte[] DEVICE_PUBLIC_KEY = ByteUtils.bytesFromHex("***REMOVED***");
    public static final byte[] DEVICE_PRIVATE_KEY = ByteUtils.bytesFromHex("***REMOVED***");
    public static final byte[] DEVICE_SERIAL = ByteUtils.bytesFromHex("01231910D62CA571EF");
    public static final byte[] CA_PUBLIC_KEY = ByteUtils.bytesFromHex("***REMOVED***");

    @BindView(R.id.serial_textview) TextView serialTextView;
    @BindView(R.id.public_key_textview) TextView publicKeyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Manager.getInstance().initialize(
                "***REMOVED***",
                "***REMOVED***=",
                "***REMOVED***==",
                getApplicationContext()
        );

        Intent intent = new Intent(this, BroadcastActivity.class);
        startActivity(intent);
    }

    public void onBroadcastClicked(View v) {
        Intent intent = new Intent(this, BroadcastActivity.class);
        startActivity(intent);
    }
/*
    public void onScanClicked(View v) {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }
*/
}
