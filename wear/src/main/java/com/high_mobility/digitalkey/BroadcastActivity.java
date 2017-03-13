package com.high_mobility.digitalkey;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;

import com.high_mobility.HMLink.Broadcaster;
import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.ConnectedLink;
import com.high_mobility.HMLink.ConnectedLinkListener;
import com.high_mobility.HMLink.Link;
import com.high_mobility.HMLink.BroadcasterListener;
import com.high_mobility.HMLink.Constants;
import com.high_mobility.HMLink.Crypto.DeviceCertificate;
import com.high_mobility.HMLink.Manager;

public class BroadcastActivity extends WearableActivity implements BroadcasterListener, ConnectedLinkListener {
    private static final byte[] CA_PUBLIC_KEY = ByteUtils.bytesFromHex("***REMOVED***");

    static final String TAG = "DigitalKey";

    Broadcaster broadcaster;

    private TextView textView;
    private GridViewPager gridViewPager;
    private DotsPageIndicator dotsPageIndicator;
    private LinkGridViewAdapter gridViewAdapter;
    private PairingView pairingView;
    private BoxInsetLayout container;

    public void didTapTitle(View view) {
        if (broadcaster.getState() == Broadcaster.State.IDLE) {
            int errorCode = broadcaster.startBroadcasting();
            if (errorCode != 0) Log.d(TAG, "Can't start broadcasting");
        }
        else if (broadcaster.getState() == Broadcaster.State.BROADCASTING) {
            broadcaster.stopBroadcasting();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        gridViewAdapter = new LinkGridViewAdapter(this, getFragmentManager());

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                textView = (TextView) findViewById(R.id.text);
                gridViewPager = (GridViewPager) findViewById(R.id.pager);

                gridViewPager.setAdapter(gridViewAdapter);

                dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
                dotsPageIndicator.setPager(gridViewPager);

                pairingView = (PairingView)findViewById(R.id.pairing_view);
                pairingView.setVisibility(View.GONE);

                container = (BoxInsetLayout) findViewById(R.id.container);

                stub.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        int chinHeight = insets.getSystemWindowInsetBottom();
                        container.setPadding(0, 0, 0, chinHeight);
                        return insets;
                    }
                });

                initializeDevice();

                try {
                    broadcaster.startBroadcasting();
                } catch (Exception e) {
                    Log.e(TAG, "cannot start broadcasting");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        for (ConnectedLink link : broadcaster.getLinks()) {
            link.setListener(null);
        }

        broadcaster.setListener(null);
        broadcaster.stopBroadcasting();
        super.onDestroy();
    }

    private void initializeDevice() {
        Manager.getInstance().initialize(
                "***REMOVED***",
                "***REMOVED***=",
                "***REMOVED***==",
                getApplicationContext()
        );
        broadcaster = Manager.getInstance().getBroadcaster();
        onStateChanged(broadcaster.getState());
        broadcaster.setListener(this);
    }

    @Override
    public void onStateChanged(Broadcaster.State oldState) {
        switch (broadcaster.getState()) {
            case BLUETOOTH_UNAVAILABLE:
                textView.setText(broadcaster.getName() + " / x");
                break;
            case IDLE:
                textView.setText(broadcaster.getName() + " / -");
                break;
            case BROADCASTING:
                textView.setText(broadcaster.getName() + " / +");
                break;
        }
    }

    @Override
    public void onLinkReceived(ConnectedLink link) {
        gridViewAdapter.setLinks(broadcaster.getLinks());
        link.setListener(this);
        Log.i(TAG, "onLinkReceived");
    }

    @Override
    public void onLinkLost(ConnectedLink link) {
        gridViewAdapter.setLinks(broadcaster.getLinks());
        link.setListener(null);
        Log.i(TAG, "onLinkLost");
    }

    @Override
    public void onStateChanged(Link link, ConnectedLink.State oldState) {
        gridViewAdapter.setLinks(broadcaster.getLinks());
    }

    @Override
    public void onCommandReceived(Link link, byte[] bytes) {
        Log.i(TAG, "onCommandReceived " + ByteUtils.hexFromBytes(bytes));
    }

    @Override
    public void onPairingRequested(ConnectedLink link,
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
    public void onPairingRequestTimeout(ConnectedLink link) {
        pairingView.declineButton.setOnClickListener(null);
        pairingView.confirmButton.setOnClickListener(null);
        pairingView.setVisibility(View.GONE);
    }

    public void didClickLock(ConnectedLink link) {
        byte[] cmd = new byte[] { 0x17, 0x01 };
        final LinkFragment fragment = gridViewAdapter.getFragment(link);

        ViewUtils.enableView(fragment.authView, false);
        link.sendCommand(cmd, true, new Constants.ResponseCallback() {
            @Override
            public void response(int errorCode) {
                if (errorCode != 0) {
                    Log.e(BroadcastActivity.TAG, "lock exception: " + errorCode);
                }
                ViewUtils.enableView(fragment.authView, true);
            }
        });
    }

    public void didClickUnlock(ConnectedLink link) {
        byte[] cmd = new byte[] { 0x17, 0x00 };
        final LinkFragment fragment = gridViewAdapter.getFragment(link);

        ViewUtils.enableView(fragment.authView, false);
        link.sendCommand(cmd, true, new Constants.ResponseCallback() {
            @Override
            public void response(int errorCode) {
                if (errorCode != 0) {
                    Log.e(BroadcastActivity.TAG, "unlock exception: " + errorCode);
                }
                ViewUtils.enableView(fragment.authView, true);
            }
        });
    }
}
