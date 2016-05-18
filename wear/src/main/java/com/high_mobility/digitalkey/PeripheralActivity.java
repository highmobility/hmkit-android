package com.high_mobility.digitalkey;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;

import com.high_mobility.HMLink.Broadcasting.Link;
import com.high_mobility.HMLink.Broadcasting.LinkCallback;
import com.high_mobility.HMLink.Broadcasting.LocalDevice;
import com.high_mobility.HMLink.Broadcasting.LocalDeviceCallback;
import com.high_mobility.HMLink.Constants;
import com.high_mobility.HMLink.LinkException;
import com.high_mobility.HMLink.Shared.DeviceCertificate;

public class PeripheralActivity extends WearableActivity implements LocalDeviceCallback, LinkCallback {
    private static final byte[] CA_PUBLIC_KEY = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] CA_APP_IDENTIFIER = Utils.bytesFromHex("***REMOVED***");
//    private static final byte[] CA_ISSUER = Utils.bytesFromHex("48494D4C");
    private static final byte[] CA_ISSUER = Utils.bytesFromHex("48494D4F");
    private static final byte[] DEVICE_PUBLIC_KEY = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] DEVICE_PRIVATE_KEY = Utils.bytesFromHex("***REMOVED***");

    static final String TAG = "DigitalKey";

    LocalDevice device = LocalDevice.getInstance();

    private TextView textView;
    private GridViewPager gridViewPager;
    private DotsPageIndicator dotsPageIndicator;
    private LinkGridViewAdapter gridViewAdapter;
    private PairingView pairingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        gridViewAdapter = new LinkGridViewAdapter(this, getFragmentManager());

        DeviceCertificate cert = new DeviceCertificate(CA_ISSUER, CA_APP_IDENTIFIER, getSerial(), DEVICE_PUBLIC_KEY);
//        cert.setSignature(Utils.bytesFromHex("***REMOVED***")); // 48494D4C sig
        cert.setSignature(Utils.bytesFromHex("***REMOVED***")); // original

        device.setDeviceCertificate(cert, DEVICE_PRIVATE_KEY, CA_PUBLIC_KEY, getApplicationContext());
        device.registerCallback(this);

        // TODO: set bottom bar inset for moto360
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                stub.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        int chinHeight = insets.getSystemWindowInsetBottom();
                        // chinHeight = 30;
                        return insets;
                    }
                });

                textView = (TextView) findViewById(R.id.text);
                gridViewPager = (GridViewPager) findViewById(R.id.pager);

                gridViewPager.setAdapter(gridViewAdapter);

                dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
                dotsPageIndicator.setPager(gridViewPager);

                pairingView = (PairingView)findViewById(R.id.pairing_view);
                pairingView.setVisibility(View.GONE);

                localDeviceStateChanged(device.state, device.state);

                try {
                    device.startBroadcasting();
                } catch (Exception e) {
                    Log.e(TAG, "cannot start broadcasting");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        device.stopBroadcasting();
        device.closeGATTServer();
        super.onDestroy();
    }

    private byte[] getSerial() {
        return new byte [] {0x01, 0x23, 0x19, 0x10, (byte)0xD6, 0x2C, (byte)0xA5, 0x71, (byte)0xEE};
        // TODO: use random serial number when you can get CA sig from web
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
    public byte[] linkDidReceiveCustomCommand(Link link, byte[] bytes) {
        Log.i(TAG, "linkDidReceiveCustomCommand " + Utils.hexFromBytes(bytes));
        return new byte[] { 0x01, bytes[0] };
    }

    @Override
    public void linkDidReceivePairingRequest(Link link,
                                             final Constants.ApprovedCallback approvedCallback) {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] vibrationPattern = {0, 300};
        final int indexInPatternToRepeat = -1;
        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);

        pairingView.setVisibility(View.VISIBLE);

        pairingView.confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairingView.declineButton.setOnClickListener(null);
                pairingView.confirmButton.setOnClickListener(null);
                pairingView.setVisibility(View.GONE);
                approvedCallback.approve();
            }
        });

        pairingView.declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairingView.declineButton.setOnClickListener(null);
                pairingView.confirmButton.setOnClickListener(null);
                pairingView.setVisibility(View.GONE);
                approvedCallback.decline();
            }
        });
    }

    @Override
    public void linkPairingDidTimeout(Link link) {
        pairingView.declineButton.setOnClickListener(null);
        pairingView.confirmButton.setOnClickListener(null);
        pairingView.setVisibility(View.GONE);
    }

    public void didClickLock(Link link) {
        byte[] cmd = new byte[] { 0x17, 0x01 };
        final LinkFragment fragment = gridViewAdapter.getFragment(link);

        Utils.enableView(fragment.authView, false);
        link.sendCustomCommand(cmd, true, new Constants.DataResponseCallback() {
            @Override
            public void response(byte[] bytes, LinkException exception) {
                Utils.enableView(fragment.authView, true);
            }
        });
    }

    public void didClickUnlock(Link link) {
        byte[] cmd = new byte[] { 0x17, 0x00 };
        final LinkFragment fragment = gridViewAdapter.getFragment(link);

        Utils.enableView(fragment.authView, false);
        link.sendCustomCommand(cmd, true, new Constants.DataResponseCallback() {
            @Override
            public void response(byte[] bytes, LinkException exception) {
                Utils.enableView(fragment.authView, true);
            }
        });
    }
}
