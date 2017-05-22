package com.highmobility.common;

import android.content.Intent;
import android.util.Log;

import com.high_mobility.HMLink.Broadcaster;
import com.high_mobility.HMLink.BroadcasterListener;
import com.high_mobility.HMLink.ConnectedLink;
import com.high_mobility.HMLink.ConnectedLinkListener;
import com.high_mobility.HMLink.Constants;
import com.high_mobility.HMLink.Link;
import com.high_mobility.HMLink.Manager;

import static com.high_mobility.HMLink.Broadcaster.*;

public class BroadcastingViewController implements IBroadcastingViewController, BroadcasterListener, ConnectedLinkListener {
    private static final String TAG = "BroadcastingVC";
    IBroadcastingView view;

    Constants.ApprovedCallback pairApproveCallback;
    Broadcaster broadcaster;
    ConnectedLink link;
    Cloud cloud;

    public BroadcastingViewController(IBroadcastingView view) {
        this.view = view;
        cloud = new Cloud(view.getActivity());
        initializeManager();
        broadcaster = Manager.getInstance().getBroadcaster();
        // set the broadcaster listener
        broadcaster.setListener(this);
        startBroadcasting();
    }

    @Override
    public void onDestroy() {
        for (ConnectedLink link : broadcaster.getLinks()) {
            link.setListener(null);
        }

        broadcaster.setListener(null);
        broadcaster.stopBroadcasting();
        Manager.getInstance().terminate();
    }

    @Override
    public void onPairingApproved(boolean approved) {
        if (approved) {
            pairApproveCallback.approve();
            view.showPairingView(false);
        }
        else {
            pairApproveCallback.decline();
            link.setListener(null);
            view.getActivity().finish();
        }
    }

    @Override
    public void onLinkClicked() {
        if (link == null) {
            Log.d(TAG, "link is null");
            return;
        }

        Intent intent = new Intent(view.getActivity(), view.getLinkActivityClass());
        intent.putExtra(LinkViewController.LINK_IDENTIFIER_MESSAGE, link.getSerial());
        view.getActivity().startActivity(intent);
    }

    // Broadcasterlistener

    @Override
    public void onStateChanged(State state) {
        switch (broadcaster.getState()) {
            case IDLE:
                view.setStatusText("Idle");

                if (state == State.BLUETOOTH_UNAVAILABLE) {
                    startBroadcasting();
                }
                break;
            case BLUETOOTH_UNAVAILABLE:
                view.setStatusText("Bluetooth N/A");
                break;
            case BROADCASTING:
                if (link == null) {
                    view.setStatusText("Looking for links...");
                }
        }
    }

    @Override
    public void onLinkReceived(ConnectedLink link) {
        Log.d(TAG, "onLinkReceived");
        if (this.link == null) {
            this.link = link;
            view.updateLink(link);
            link.setListener(this);
            onStateChanged(link, Link.State.CONNECTED);
        }
    }

    @Override
    public void onLinkLost(ConnectedLink link) {
        Log.d(TAG, "onLinkLost");

        if (link == this.link) {
            link.setListener(null);
            this.link = null;
            view.updateLink(link);

            if (broadcaster.getState() == State.BROADCASTING)
                view.setStatusText("Looking for links...");
        }
    }

    @Override
    public void onPairingRequested(ConnectedLink connectedLink, Constants.ApprovedCallback approvedCallback) {
        this.pairApproveCallback = approvedCallback;
        view.showPairingView(true);
        Log.d(TAG, "show pairing view " + true);
    }

    @Override
    public void onPairingRequestTimeout(ConnectedLink connectedLink) {
        view.showPairingView(false);
    }

    @Override
    public void onStateChanged(Link link, Link.State state) {
        Log.d(TAG, "link state changed " + link.getState());
        if (link == this.link ) {
            if (link.getState() == Link.State.AUTHENTICATED) {
                view.updateLink((ConnectedLink) link);
                onLinkClicked();
                view.setStatusText("authenticated");
            }
            else if (link.getState() == Link.State.CONNECTED) {
                view.setStatusText("connected");
            }
            else {
                this.link = null;
                view.setStatusText("broadcasting");
            }
        }
    }

    @Override
    public void onCommandReceived(Link link, byte[] bytes) {

    }

    void startBroadcasting() {
        int statusCode = broadcaster.startBroadcasting();
        if (statusCode != 0) {
            Log.e(TAG, "cant start broadcasting " + statusCode);
            view.setStatusText("Status " + statusCode);
        }
    }

    void initializeManager() {
        Manager.getInstance().setContext(view.getActivity());
        Manager.getInstance().initialize(
                "***REMOVED***",
                "***REMOVED***=",
                "***REMOVED***=="
        );
    }
}
