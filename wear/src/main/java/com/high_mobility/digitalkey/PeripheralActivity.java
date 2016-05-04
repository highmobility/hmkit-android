package com.high_mobility.digitalkey;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
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


public class PeripheralActivity extends WearableActivity implements LocalDeviceCallback, LinkCallback {
    private static final byte[] CA_PUBLIC_KEY = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] CA_APP_IDENTIFIER = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] CA_ISSUER = Utils.bytesFromHex("48494D4C");

    private static final byte[] DEVICE_PUBLIC_KEY = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] DEVICE_PRIVATE_KEY = Utils.bytesFromHex("***REMOVED***");

    static final String TAG = "DigitalKey";

    LocalDevice device = LocalDevice.getInstance();

    private TextView textView;
    private GridViewPager gridViewPager;
    private DotsPageIndicator dotsPageIndicator;
    private LinkGridViewAdapter gridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i(TAG, "create");

        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text);
        gridViewPager = (GridViewPager) findViewById(R.id.pager);

        gridViewAdapter = new LinkGridViewAdapter(this, getFragmentManager());
        gridViewPager.setAdapter(gridViewAdapter);

        dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(gridViewPager);

        DeviceCertificate cert = new DeviceCertificate(CA_ISSUER, CA_APP_IDENTIFIER, getSerial(), DEVICE_PUBLIC_KEY);
        cert.setSignature(Utils.bytesFromHex("***REMOVED***"));
        device.setDeviceCertificate(cert, DEVICE_PRIVATE_KEY, CA_PUBLIC_KEY, getApplicationContext());

        device.registerCallback(this);

        try {
            device.startBroadcasting();
        } catch (Exception e) {
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
        return new byte [] {0x01, 0x23, 0x19, 0x10, (byte)0xD6, 0x2C, (byte)0xA5, 0x71, (byte)0xEE};
        // TODO: use random serial number when device certificate is dynamic in core
      /*  SharedPreferences settings;
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
        }*/
    }

    @Override
    public void localDeviceStateChanged(LocalDevice.State state, LocalDevice.State oldState) {
        switch (state) {
            case BLUETOOTH_UNAVAILABLE:
                textView.setText(device.getName() + " / x");
                break;
            case IDLE:
                textView.setText(device.getName() + " / -");
                break;
            case BROADCASTING:
                textView.setText(device.getName() + " / +");
                break;
        }
    }

    @Override
    public void localDeviceDidReceiveLink(Link link) {
        gridViewAdapter.setLinks(device.getLinks());
        link.registerCallback(this);
        Log.i(TAG, "localDeviceDidReceiveLink");
    }

    @Override
    public void localDeviceDidLoseLink(Link link) {
        gridViewAdapter.setLinks(device.getLinks());
        link.registerCallback(null);
        Log.i(TAG, "localDeviceDidLoseLink");
    }

    @Override
    public void linkStateDidChange(Link link, Link.State oldState) {
        gridViewAdapter.setLinks(device.getLinks());
    }

    @Override
    public void linkDidExecuteCommand(Link link, Constants.Command command, LinkException exception) {

    }

    @Override
    public byte[] linkDidReceiveCustomCommand(Link link, byte[] bytes) {
        Log.i(TAG, "linkDidReceiveCustomCommand " + Utils.hexFromBytes(bytes));
        return new byte[0];
    }

    @Override
    public void linkDidReceivePairingRequest(Link link, byte[] serialNumber, Constants.ApprovedCallback approvedCallback, float timeout) {
        // TODO: show in UI
    }

    void didClickSendCmdButton(Link link) {
        byte[] cmd = new byte[] { (byte) 0x8d };
        Log.v(TAG, "send cmd " + Utils.hexFromBytes(cmd));

        link.sendCustomCommand(cmd, true, new Constants.DataResponseCallback() {
            @Override
            public void response(byte[] bytes, LinkException exception) {
                Log.v(TAG, "did receive response " + Utils.hexFromBytes(bytes) + " " + exception.code);
            }
        });
    }
}
