package com.highmobility.digitalkey.scan;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by ttiganik on 02/06/16.
 */
public class ScanActivity extends AppCompatActivity /*implements ScannerListener */{
    /*
    private static final String TAG = "Scan";

    @BindView(R.id.scan_list_view) ListView listView;
    @BindView(R.id.scan_switch) Switch scanSwitch;
    @BindView(R.id.status_textview) TextView statusTextView;

    ScanListAdapter adapter;

    void onScanCheckedChanged() {

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

        scanSwitch.setEnabled(false);
        getBlePermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

        //adapter = new ScanListAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, scanner.getLinks());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ScanActivity.this, ScannedLinkActivity.class);
                intent.putExtra(ScannedLinkActivity.DEVICE_POSITION, position);
                startActivity(intent);

            }
        });

        scanSwitch.setEnabled(true);
        scanSwitch.setChecked(true);
    }

    @Override
    public void onStateChanged(Scanner.State oldState) {
        switch (scanner.getState()) {
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
    public void onDeviceEnteredProximity(ScannedLink device) {
        Log.i(TAG, "Entered proximity");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceExitedProximity(ScannedLink device) {
        adapter.notifyDataSetChanged();
    }
    */
}
