package com.high_mobility.digitalkey.scan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.high_mobility.HMLink.LinkException;
import com.high_mobility.HMLink.Scanning.ExternalDevice;
import com.high_mobility.HMLink.Scanning.ExternalDeviceManager;
import com.high_mobility.HMLink.Scanning.ExternalDeviceManagerListener;
import com.high_mobility.digitalkey.MainActivity;
import com.high_mobility.digitalkey.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ttiganik on 02/06/16.
 */
public class ScanActivity extends AppCompatActivity implements ExternalDeviceManagerListener {
    private static final String TAG = "Scan";

    @BindView(R.id.scan_list_view) ListView listView;

    ExternalDeviceManager deviceManager;
    ScanListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scan_view);

        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getBlePermission();
        }
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
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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
        deviceManager = ExternalDeviceManager.getInstance(getApplicationContext());
        deviceManager.setListener(this);
        onStateChanged(deviceManager.getState());

        adapter = new ScanListAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, deviceManager.getDevices());
        listView.setAdapter(adapter);
        byte[][] appIdentifiers = new byte[][] { MainActivity.CaAppId };
        try {
            deviceManager.startScanning(appIdentifiers);
        } catch (LinkException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStateChanged(ExternalDeviceManager.State oldState) {
        switch (deviceManager.getState()) {
            case BLUETOOTH_UNAVAILABLE:
                setTitle("BLE Unavailable");
                break;
            case IDLE:
                setTitle("Idle");
                break;
            case SCANNING:
                setTitle("Scanning");
                break;
        }
    }

    @Override
    public void onDeviceEnteredProximity(ExternalDevice device) {

    }

    @Override
    public void onDeviceExitedProximity(ExternalDevice device) {

    }
}
