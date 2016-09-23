package com.high_mobility.digitalkey.broadcast;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.high_mobility.HMLink.Command;
import com.high_mobility.HMLink.ControlMode;
import com.high_mobility.HMLink.IncomingCommand;
import com.high_mobility.HMLink.CommandParseException;
import com.high_mobility.HMLink.LockState;
import com.high_mobility.HMLink.BroadcasterListener;
import com.high_mobility.HMLink.ConnectedLink;
import com.high_mobility.HMLink.ConnectedLinkListener;
import com.high_mobility.HMLink.Constants;
import com.high_mobility.HMLink.Broadcaster;
import com.high_mobility.HMLink.Link;
import com.high_mobility.HMLink.Manager;
import com.high_mobility.digitalkey.R;

/**
 * Created by ttiganik on 02/06/16.
 */
public class BroadcastActivity extends AppCompatActivity implements BroadcasterListener, ConnectedLinkListener {
    static final String TAG = "BroadcastActivity";

    TextView statusTextView;
    Switch broadcastSwitch;

    LinearLayout pairingView;
    Button confirmPairButton;

    ViewPager pager;
    LinkPagerAdapter adapter;

    Broadcaster device;
    Constants.ApprovedCallback pairApproveCallback;

    void onBroadcastCheckedChanged() {
        if (broadcastSwitch.isChecked()) {
            if (device.getState() == Broadcaster.State.BROADCASTING) return;
            int errorCode = device.startBroadcasting();
            if (errorCode != 0) Log.e(TAG, "cant start broadcasting " + errorCode);
        }
        else {
            device.stopBroadcasting();
        }
    }

    void onPairConfirmClick() {
        pairingView.setVisibility(View.GONE);
        pairApproveCallback.approve();
        pairApproveCallback = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.broadcast_view);

        device = Manager.getInstance().getBroadcaster();

        // set the device listener
        device.setListener(this);

        createViews();

        broadcastSwitch.setChecked(true);
    }

    @Override
    protected void onDestroy() {
        for (ConnectedLink link : device.getLinks()) {
            link.setListener(null);
        }

        device.setListener(null);
        device.stopBroadcasting();

        super.onDestroy();
    }

    // BroadcasterListener

    @Override
    public void onStateChanged(Broadcaster.State state1) {
        broadcastSwitch.setEnabled(device.getState() != Broadcaster.State.BLUETOOTH_UNAVAILABLE);

        switch (device.getState()) {
            case IDLE:
                if (state1 == Broadcaster.State.BLUETOOTH_UNAVAILABLE && broadcastSwitch.isChecked()) {
                    int errorCode = device.startBroadcasting();
                    if (errorCode != 0) Log.e(TAG, "cant start broadcasting " + errorCode);
                }
                statusTextView.setText("idle");
                break;
            case BLUETOOTH_UNAVAILABLE:
                statusTextView.setText("N/A");
                break;

            case BROADCASTING:
                statusTextView.setText("broadcasting");
                setTitle(device.getName());
                break;
        }
    }

    @Override
    public void onLinkReceived(ConnectedLink link) {
        link.setListener(this);
        adapter.setLinks(device.getLinks());
    }

    @Override
    public void onLinkLost(ConnectedLink link) {
        link.setListener(null);
        adapter.setLinks(device.getLinks());
    }

    // LinkListener

    @Override
    public void onStateChanged(Link link, Link.State state) {
        if (link.getState() == ConnectedLink.State.AUTHENTICATED) {
//            certUtils.onCertificateReadForSerial(link.getSerial());
        }

        adapter.setLinks(device.getLinks());
    }

    @Override
    public void onCommandReceived(Link link, byte[] bytes) {
        try {
            IncomingCommand command = IncomingCommand.create(bytes);

            if (command.is(Command.DigitalKey.LOCK_STATE)) {
                LockState stateNotification = (LockState) command;
                Log.i(TAG, "Lock status changed " + stateNotification.getState());
            }
            else if (command.is(Command.RemoteControl.CONTROL_MODE)) {
                ControlMode controlModeNotification = (ControlMode) command;
                Log.i(TAG, "Control Mode angle " + controlModeNotification.getAngle());
            }
        }
        catch (CommandParseException e) {
            Log.d(TAG, "IncomingCommand parse exception ", e);
        }
    }

    @Override
    public void onPairingRequested(ConnectedLink link, Constants.ApprovedCallback approvedCallback) {
        this.pairApproveCallback = approvedCallback;
        pairingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPairingRequestTimeout(ConnectedLink link) {
        pairingView.setVisibility(View.GONE);
    }

    void createViews() {
        statusTextView = (TextView) findViewById(R.id.status_textview);
        broadcastSwitch = (Switch) findViewById(R.id.broadcast_switch);
        broadcastSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onBroadcastCheckedChanged();
            }
        });

        pairingView = (LinearLayout) findViewById(R.id.pairing_view);
        confirmPairButton = (Button) findViewById(R.id.confirm_pairing_button);
        confirmPairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPairConfirmClick();
            }
        });

        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new LinkPagerAdapter(this, getSupportFragmentManager());
        pager.setAdapter(adapter);
    }

    void onLockClicked(ConnectedLink link) {
        final LinkFragment fragment = adapter.getFragment(link);
        ViewUtils.enableView(fragment.authView, false);

        link.sendCommand(Command.DigitalKey.lockDoors(true), true, new Constants.ResponseCallback() {
            @Override
            public void response(int errorCode) {
            ViewUtils.enableView(fragment.authView, true);
            if (errorCode != 0) {
                Log.e(TAG, "command send exception " + errorCode);
                return;
            }
            // all went ok
            }
        });
    }

    void onUnlockClicked(ConnectedLink link) {
        final LinkFragment fragment = adapter.getFragment(link);

        ViewUtils.enableView(fragment.authView, false);
        link.sendCommand(Command.DigitalKey.lockDoors(false), true, new Constants.ResponseCallback() {
            @Override
            public void response(int errorCode) {
                ViewUtils.enableView(fragment.authView, true);
                if (errorCode != 0) {
                    Log.e(TAG, "command send exception " + errorCode);
                    return;
                }
                // all went ok
            }
        });
    }}
