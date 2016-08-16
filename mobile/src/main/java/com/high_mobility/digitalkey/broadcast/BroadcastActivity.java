package com.high_mobility.digitalkey.broadcast;

import android.bluetooth.le.AdvertiseSettings;
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

import com.high_mobility.HMLink.Shared.Link;
import com.high_mobility.HMLink.Shared.LinkListener;
import com.high_mobility.HMLink.Shared.LocalDevice;
import com.high_mobility.HMLink.Shared.LocalDeviceListener;
import com.high_mobility.HMLink.Commands.AutoCommand;
import com.high_mobility.HMLink.Commands.AutoCommandNotification;
import com.high_mobility.HMLink.Commands.AutoCommandResponse;
import com.high_mobility.HMLink.Commands.CommandParseException;
import com.high_mobility.HMLink.Commands.LockStatusChangedNotification;
import com.high_mobility.HMLink.Constants;
import com.high_mobility.HMLink.Shared.Device;
import com.high_mobility.HMLink.LinkException;
import com.high_mobility.HMLink.Shared.Shared;
import com.high_mobility.digitalkey.CertUtils;
import com.high_mobility.digitalkey.MainActivity;
import com.high_mobility.digitalkey.R;

/**
 * Created by ttiganik on 02/06/16.
 */
public class BroadcastActivity extends AppCompatActivity implements LocalDeviceListener, LinkListener {
    static final String TAG = "BroadcastActivity";


    TextView statusTextView;
    Switch broadcastSwitch;

    LinearLayout pairingView;
    Button confirmPairButton;

    ViewPager pager;
    LinkPagerAdapter adapter;

    LocalDevice device;
    Constants.ApprovedCallback pairApproveCallback;

    void onBroadcastCheckedChanged() {
        if (broadcastSwitch.isChecked()) {
            if (device.getState() == LocalDevice.State.BROADCASTING) return;
            resetDevice();

            try {
                device.startBroadcasting();
            } catch (LinkException e) {
                e.printStackTrace();
            }
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
        LocalDevice.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        LocalDevice.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        device = Shared.getInstance().getLocalDevice();

        // set the device listener
        device.setListener(this);

        createViews();

        broadcastSwitch.setChecked(true);
    }

    @Override
    protected void onDestroy() {
        for (Link link : device.getLinks()) {
            link.setListener(null);
        }
        device.setListener(null);
        device.stopBroadcasting();
        super.onDestroy();
    }

    // LocalDeviceListener

    @Override
    public void onStateChanged(LocalDevice.State state, LocalDevice.State state1) {
        broadcastSwitch.setEnabled(state != LocalDevice.State.BLUETOOTH_UNAVAILABLE);

        switch (state) {
            case IDLE:
                if (state1 == LocalDevice.State.BLUETOOTH_UNAVAILABLE && broadcastSwitch.isChecked()) {
                    try {
                        device.startBroadcasting();
                    } catch (LinkException e) {
                        e.printStackTrace();
                    }
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
    public void onLinkReceived(Link link) {
        link.setListener(this);
        adapter.setLinks(device.getLinks());
    }

    @Override
    public void onLinkLost(Link link) {
        link.setListener(null);
        adapter.setLinks(device.getLinks());
    }

    // LinkListener

    @Override
    public void onStateChanged(Link link, Link.State state) {
        if (link.getState() == Link.State.AUTHENTICATED) {
//            certUtils.onCertificateReadForSerial(link.getSerial());
        }

        adapter.setLinks(device.getLinks());
    }

    @Override
    public byte[] onCommandReceived(Link link, byte[] bytes) {
        try {
            AutoCommandNotification notification = AutoCommandNotification.create(bytes);

            if (notification.getType() == AutoCommand.Type.LOCK_STATUS_CHANGED) {
                LockStatusChangedNotification changedNotification = (LockStatusChangedNotification)notification;
                Log.i(TAG, "LockStatusChanged " + changedNotification.getLockStatus());
            }
        }
        catch (CommandParseException e) {
            Log.e(TAG, "Notification parse exception ", e);
        }

        return null;
    }

    @Override
    public void onPairingRequested(Link link, Constants.ApprovedCallback approvedCallback) {
        this.pairApproveCallback = approvedCallback;
        pairingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPairingRequestTimeout(Link link) {
        pairingView.setVisibility(View.GONE);
    }

    void resetDevice() {
        // Delete the previous certificates from the device.
        // This is not needed in real scenario where certificates are not faked.
        device.reset();
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

    void onLockClicked(Link link) {
        final LinkFragment fragment = adapter.getFragment(link);
        ViewUtils.enableView(fragment.authView, false);

        link.sendCommand(AutoCommand.lockDoorsBytes(), true, new Constants.DataResponseCallback() {
            @Override
            public void response(byte[] bytes, LinkException exception) {
                ViewUtils.enableView(fragment.authView, true);
                if (exception != null) {
                    Log.e(TAG, "command exception", exception);
                    return;
                }

                try {
                    // generic ack/error response does not have a separate response class
                    AutoCommandResponse response = new AutoCommandResponse(bytes);

                    if (response.getErrorCode() == 0) {
                        Log.i(TAG, "successfully locked the vehicle");
                    }
                    else {
                        Log.i(TAG, "failed to lock the vehicle");
                    }
                } catch (CommandParseException e) {
                    Log.e(TAG, "CommandParseException ", e);
                }

            }
        });
    }

    void onUnlockClicked(Link link) {
        final LinkFragment fragment = adapter.getFragment(link);

        ViewUtils.enableView(fragment.authView, false);
        link.sendCommand(AutoCommand.unlockDoorsBytes(), true, new Constants.DataResponseCallback() {
            @Override
            public void response(byte[] bytes, LinkException exception) {
                ViewUtils.enableView(fragment.authView, true);
                try {
                    // generic ack/error response does not have a separate response class
                    AutoCommandResponse response = new AutoCommandResponse(bytes);

                    if (response.getErrorCode() == 0) {
                        Log.i(TAG, "successfully unlocked the vehicle");
                    }
                    else {
                        Log.i(TAG, "failed to unlock the vehicle");
                    }
                } catch (CommandParseException e) {
                    Log.e(TAG, "CommandParseException ", e);
                }
            }
        });
    }}
