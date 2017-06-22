package com.highmobility.digitalkey.broadcast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.highmobility.digitalkey.R;
import com.highmobility.hmkit.ConnectedLink;
import com.highmobility.hmkit.Constants;
import com.highmobility.hmkit.Link;

import com.highmobility.common.BroadcastingViewController;
import com.highmobility.common.IBroadcastingView;
import com.highmobility.common.IBroadcastingViewController;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ttiganik on 02/06/16.
 */
public class BroadcastActivity extends AppCompatActivity implements IBroadcastingView {
    static final String TAG = "BroadcastActivity";
    IBroadcastingViewController controller;

    @BindView(R.id.status_textview) TextView statusTextView;
    @BindView(R.id.pairing_view) LinearLayout pairingView;
    @BindView(R.id.confirm_pairing_button) Button confirmPairButton;
    @BindView(R.id.show_button) Button showButton;

    ConnectedLink.AuthorizationCallback pairApproveCallback;

    void onPairConfirmClick() {
        controller.onPairingApproved(true);
        pairApproveCallback = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.broadcast_view);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BroadcastingViewController.LINK_ACTIVITY_RESULT) {
            controller.onLinkViewResult(requestCode);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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