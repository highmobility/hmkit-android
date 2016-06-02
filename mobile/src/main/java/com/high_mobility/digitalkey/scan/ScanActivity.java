package com.high_mobility.digitalkey.scan;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.high_mobility.HMLink.Scanning.ExternalDevice;
import com.high_mobility.HMLink.Scanning.ExternalDeviceManager;
import com.high_mobility.HMLink.Scanning.ExternalDeviceManagerListener;
import com.high_mobility.digitalkey.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ttiganik on 02/06/16.
 */
public class ScanActivity extends AppCompatActivity implements ExternalDeviceManagerListener {
    @BindView(R.id.scan_list_view) ListView listView;
    ScanListAdapter adapter;
    ExternalDeviceManager deviceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_view);
        ButterKnife.bind(this);

        deviceManager = ExternalDeviceManager.getInstance(getApplicationContext());

        adapter = new ScanListAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, deviceManager.getDevices());
        listView.setAdapter(adapter);
    }

    @Override
    public void onStateChanged(ExternalDeviceManager.State oldState) {

    }

    @Override
    public void onDeviceEnteredProximity(ExternalDevice device) {

    }

    @Override
    public void onDeviceExitedroximity(ExternalDevice device) {

    }
}
