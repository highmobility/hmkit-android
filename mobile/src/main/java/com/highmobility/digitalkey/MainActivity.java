package com.highmobility.digitalkey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.highmobility.digitalkey.broadcast.BroadcastActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";

    @BindView(R.id.serial_textview) TextView serialTextView;
    @BindView(R.id.public_key_textview) TextView publicKeyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // this activity will be used if scanning is possible with the sdk as well.
        // atm broadcasting activity is just launched
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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
