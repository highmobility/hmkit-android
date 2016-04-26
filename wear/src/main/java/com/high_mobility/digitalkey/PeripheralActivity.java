package com.high_mobility.digitalkey;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.high_mobility.digitalkey.HMLink.Broadcasting.Link;
import com.high_mobility.digitalkey.HMLink.Broadcasting.LinkCallback;
import com.high_mobility.digitalkey.HMLink.Broadcasting.LocalDevice;
import com.high_mobility.digitalkey.HMLink.Broadcasting.LocalDeviceCallback;
import com.high_mobility.digitalkey.HMLink.Constants;
import com.high_mobility.digitalkey.HMLink.LinkException;
import com.high_mobility.digitalkey.HMLink.Shared.DeviceCertificate;

import java.util.Random;

public class PeripheralActivity extends WearableActivity implements LocalDeviceCallback, LinkCallback {
    private static final byte[] CA_PUBLIC_KEY = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] CA_APP_IDENTIFIER = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] CA_ISSUER = Utils.bytesFromHex("48494D4C");

    private static final byte[] DEVICE_PUBLIC_KEY = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] DEVICE_PRIVATE_KEY = Utils.bytesFromHex("***REMOVED***");

    private static final String TAG = "PeripheralActivity";

    LocalDevice device = LocalDevice.getInstance();

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i(TAG, "create");

        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text);

        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        textView.setBackgroundColor(color);

        DeviceCertificate cert = new DeviceCertificate(CA_ISSUER, CA_APP_IDENTIFIER, getSerial(), DEVICE_PUBLIC_KEY);
        cert.setSignature(Utils.bytesFromHex("***REMOVED***"));
        device.setDeviceCertificate(cert, DEVICE_PRIVATE_KEY, CA_PUBLIC_KEY, getApplicationContext());

        device.registerCallback(this);

        try {
            device.startBroadcasting();
            textView.setText(device.getName());
        } catch (Exception e) {
            textView.setText("failed to start broadcast");
            Log.e(TAG, "cannot start broadcasting");
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        device.stopBroadcasting();
        device.closeGATTServer();
        Log.i(TAG, "onDestroy");

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
