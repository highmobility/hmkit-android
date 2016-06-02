package com.high_mobility.digitalkey.scan;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.high_mobility.digitalkey.R;

/**
 * Created by ttiganik on 02/06/16.
 */
public class DeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.external_device_view);
    }
}
