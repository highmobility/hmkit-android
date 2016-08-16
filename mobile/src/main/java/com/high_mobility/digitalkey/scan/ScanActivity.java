package com.high_mobility.digitalkey.scan;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.high_mobility.HMLink.LinkException;
import com.high_mobility.HMLink.Shared.ExternalDevice;
import com.high_mobility.HMLink.Shared.ExternalDeviceManager;
import com.high_mobility.HMLink.Shared.ExternalDeviceManagerListener;
import com.high_mobility.HMLink.Shared.Shared;
import com.high_mobility.digitalkey.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ttiganik on 02/06/16.
 */
public class ScanActivity extends AppCompatActivity implements ExternalDeviceManagerListener {
    private static final String TAG = "Scan";

    @BindView(R.id.scan_list_view) ListView listView;
    @BindView(R.id.scan_switch) Switch scanSwitch;
    @BindView(R.id.status_textview) TextView statusTextView;

    ExternalDeviceManager deviceManager;
    ScanListAdapter adapter;

    void onScanCheckedChanged() {
        if (scanSwitch.isChecked() && deviceManager.getState() != ExternalDeviceManager.State.SCANNING) {
            try {
                deviceManager.startScanning();
            } catch (LinkException e) {
                e.printStackTrace();
            }
        }
        else {
            deviceManager.stopScanning();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scan_view);

        ButterKnife.bind(this);
        scanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            onScanCheckedChanged();
            }
        });

        getBlePermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceManager.stopScanning();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void getBlePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        else {
            didReceiveBlePermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    didReceiveBlePermission();
                }
                return;
            }
        }
    }

    private void didReceiveBlePermission() {
        deviceManager = Shared.getInstance().getExternalDeviceManager();
        deviceManager.setListener(this);
        onStateChanged(deviceManager.getState());

        adapter = new ScanListAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, deviceManager.getDevices());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ScanActivity.this, DeviceActivity.class);
                intent.putExtra(DeviceActivity.DEVICE_POSITION, position);
                startActivity(intent);

            }
        });

        scanSwitch.setChecked(true);
    }

    @Override
    public void onStateChanged(ExternalDeviceManager.State oldState) {
        switch (deviceManager.getState()) {
            case BLUETOOTH_UNAVAILABLE:
                statusTextView.setText("BLE Unavailable");
                scanSwitch.setEnabled(false);
                break;
            case IDLE:
                statusTextView.setText("Idle");
                scanSwitch.setEnabled(true);
                break;
            case SCANNING:
                statusTextView.setText("Scanning");
                scanSwitch.setEnabled(true);
                break;
        }
    }

    @Override
    public void onDeviceEnteredProximity(ExternalDevice device) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceExitedProximity(ExternalDevice device) {
        adapter.notifyDataSetChanged();
    }
}
