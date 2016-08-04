package com.high_mobility.digitalkey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.high_mobility.HMLink.Shared.ByteUtils;
import com.high_mobility.digitalkey.broadcast.BroadcastActivity;
import com.high_mobility.digitalkey.scan.ScanActivity;

public class MainActivity extends AppCompatActivity {
    public static final byte[] CaAppId = ByteUtils.bytesFromHex("***REMOVED***");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Intent intent = new Intent(this, BroadcastActivity.class);
//        startActivity(intent);
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
