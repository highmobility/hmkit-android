package com.highmobility.sdkapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.highmobility.common.BroadcastingViewController;
import com.highmobility.common.IBroadcastingView;
import com.highmobility.common.IBroadcastingViewController;
import com.highmobility.hmkit.ConnectedLink;
import com.highmobility.hmkit.Link;

/**
 * Created by ttiganik on 02/06/16.
 */
public class BroadcastActivity extends WearableActivity implements IBroadcastingView {
    static final String TAG = "BroadcastActivity";
    IBroadcastingViewController controller;

    @BindView(R.id.status_textview) TextView statusTextView;
    @BindView(R.id.pairing_view) LinearLayout pairingView;
    @BindView(R.id.confirm_pairing_button) Button confirmPairButton;
    @BindView(R.id.show_button) Button showButton;
    @BindView(R.id.disconnect_button) Button disconnectButton;


    void onPairConfirmClick() {
        controller.onPairingApproved(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.broadcast_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);

        controller = new BroadcastingViewController(this);
        confirmPairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPairConfirmClick();
            }
        });
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onLinkClicked();
            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                controller.onDisconnectClicked();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        confirmPairButton.setOnClickListener(null);
        controller.onDestroy();
    }

    @Override
    public void setStatusText(String text) {
        statusTextView.setText(text);
    }

    @Override
    public void showPairingView(boolean show) {
        pairingView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public Class getLinkActivityClass() {
        return LinkView.class;
    }

    @Override
    public void updateLink(ConnectedLink link) {
        showButton.setVisibility(link.getState() == Link.State.AUTHENTICATED ? View.VISIBLE : View.GONE);
    }

    @Override
    public Activity getActivity() {
        return this;
    }
}