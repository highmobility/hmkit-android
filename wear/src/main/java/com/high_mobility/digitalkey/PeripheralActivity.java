package com.high_mobility.digitalkey;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.high_mobility.digitalkey.HMLink.Broadcasting.Link;
import com.high_mobility.digitalkey.HMLink.Broadcasting.LinkCallback;
import com.high_mobility.digitalkey.HMLink.Broadcasting.LocalDevice;
import com.high_mobility.digitalkey.HMLink.Broadcasting.LocalDeviceCallback;
import com.high_mobility.digitalkey.HMLink.Constants;
import com.high_mobility.digitalkey.HMLink.LinkException;
import com.high_mobility.digitalkey.HMLink.Shared.DeviceCertificate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class PeripheralActivity extends WearableActivity implements LocalDeviceCallback, LinkCallback {
    private static final byte[] CA_PUBLIC_KEY = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] CA_APP_IDENTIFIER = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] CA_ISSUER = Utils.bytesFromHex("47494D4F");

    private static final byte[] DEVICE_PUBLIC_KEY = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] DEVICE_PRIVATE_KEY = Utils.bytesFromHex("***REMOVED***");

    private static final String TAG = "PeripheralActivity";

    LocalDevice device = LocalDevice.getInstance();

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i(TAG, "create");

        setContentView(R.layout.activity_main);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);

        DeviceCertificate cert = new DeviceCertificate(CA_ISSUER, CA_APP_IDENTIFIER, getSerial(), DEVICE_PUBLIC_KEY);
        cert.setSignature(Utils.bytesFromHex("***REMOVED***"));
        device.setDeviceCertificate(cert, DEVICE_PRIVATE_KEY, CA_PUBLIC_KEY, getApplicationContext());

        device.registerCallback(this);

        try {
            device.startBroadcasting();
            mTextView.setText(device.name);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "cannot start broadcasting");
        }
    }

    @Override
    protected void onDestroy() {
        device.stopBroadcasting();
        device.closeGATTServer();

        super.onDestroy();
    }

    private byte[] getSerial() {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = getApplicationContext().getSharedPreferences("com.hm.wearable.UserPrefs",
                Context.MODE_PRIVATE );
        editor = settings.edit();


        String serialKey = "serialUserDefaultsKey";

        if (settings.contains(serialKey)) {
            return Utils.bytesFromHex(settings.getString(serialKey, ""));
        }
        else {
            byte[] serialBytes = new byte[9];
            new Random().nextBytes(serialBytes);
            editor.putString(serialKey, Utils.hexFromBytes(serialBytes));
            return serialBytes;
        }
    }

    @Override
    public void localDeviceStateChanged(LocalDevice.State state, LocalDevice.State oldState) {

    }

    @Override
    public void localDeviceDidReceiveLink(Link link) {
        Log.i(TAG, "localDeviceDidReceiveLink");
    }

    @Override
    public void localDeviceDidLoseLink(Link link) {
        Log.i(TAG, "localDeviceDidLoseLink");
    }

    @Override
    public void linkStateDidChange(Link link, Link.State oldState) {

    }

    @Override
    public void linkDidExecuteCommand(Link link, Constants.Command command, LinkException exception) {

    }

    @Override
    public byte[] linkDidReceiveCustomCommand(Link link, byte[] bytes) {
        return new byte[0];
    }

    @Override
    public void linkDidReceivePairingRequest(Link link, byte[] serialNumber, Constants.ApprovedCallback approvedCallback, float timeout) {

    }

}
