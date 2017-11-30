package com.highmobility.common;

import android.content.Intent;
import android.util.Log;

import com.highmobility.hmkit.Broadcaster;
import com.highmobility.hmkit.BroadcasterListener;
import com.highmobility.autoapi.Command;
import com.highmobility.hmkit.ConnectedLink;
import com.highmobility.hmkit.ConnectedLinkListener;
import com.highmobility.hmkit.Error.BroadcastError;
import com.highmobility.hmkit.Error.DownloadAccessCertificateError;
import com.highmobility.hmkit.Error.TelematicsError;
import com.highmobility.hmkit.Link;
import com.highmobility.hmkit.Manager;
import com.highmobility.hmkit.Telematics;

public class BroadcastingViewController implements IBroadcastingViewController, BroadcasterListener, ConnectedLinkListener {
    private static final String TAG = "BroadcastingVC";
    public static final int LINK_ACTIVITY_RESULT = 1;
    IBroadcastingView view;

    AuthorizationCallback authorizationCallback;
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
//        sendTelematicsCommand();
    }

    private void sendTelematicsCommand() {
        String token = "***REMOVED***";

        Manager.getInstance().downloadCertificate(token, new Manager.DownloadCallback() {
            @Override
            public void onDownloaded(byte[] vehicleSerial) {
                byte[] command = Command.DoorLocks.lockDoors(true);
                Manager.getInstance().getTelematics().sendCommand(command, vehicleSerial, new Telematics.CommandCallback() {
                    @Override
                    public void onCommandResponse(byte[] bytes) {
                        Log.d(TAG, "onCommandResponse: ");
                    }

                    @Override
                    public void onCommandFailed(TelematicsError error) {
                        Log.d(TAG, "onCommandFailed: " + error.getType());
                    }
                });
            }

            @Override
            public void onDownloadFailed(DownloadAccessCertificateError error) {

            }
        });
    }

    @Override
    public void onDestroy() {
        Manager.getInstance().terminate();
        broadcaster = null;
        link = null;
    }

    @Override
    public void onPairingApproved(boolean approved) {
        if (approved) {
            authorizationCallback.approve();
            view.showPairingView(false);
        }
        else {
            authorizationCallback.decline();
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
        view.getActivity().startActivityForResult(intent, LINK_ACTIVITY_RESULT);
    }

    @Override
    public void onLinkViewResult(int result) {
        Manager.getInstance().terminate();
        broadcaster = null;
        initializeManager();
        broadcaster = Manager.getInstance().getBroadcaster();
        // set the broadcaster listener
        broadcaster.setListener(this);
        startBroadcasting();

    }

    // Broadcasterlistener

    @Override
    public void onStateChanged(Broadcaster.State state) {
        switch (broadcaster.getState()) {
            case IDLE:
                view.setStatusText("Idle");

                if (state == Broadcaster.State.BLUETOOTH_UNAVAILABLE) {
                    startBroadcasting();
                }
                break;
            case BLUETOOTH_UNAVAILABLE:
                view.setStatusText("Bluetooth N/A");
                break;
            case BROADCASTING:
                if (link == null) {
                    view.setStatusText("Looking for links: " + broadcaster.getName());
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

            if (broadcaster.getState() == Broadcaster.State.BROADCASTING)
                view.setStatusText("Looking for links...");
        }
    }

    @Override
    public void onAuthorizationRequested(ConnectedLink connectedLink, AuthorizationCallback authorizationCallback) {
        this.authorizationCallback = authorizationCallback;
        view.showPairingView(true);
        Log.d(TAG, "show pairing view " + true);
    }

    @Override
    public void onAuthorizationTimeout(ConnectedLink connectedLink) {
        view.showPairingView(false);
    }

    @Override
    public void onStateChanged(Link link, Link.State state) {
        Log.d(TAG, "link state changed " + link.getState());
        if (link == this.link ) {
            view.updateLink((ConnectedLink) link);

            if (link.getState() == Link.State.AUTHENTICATED) {
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
        broadcaster.startBroadcasting(new Broadcaster.StartCallback() {
            @Override
            public void onBroadcastingStarted() {

            }

            @Override
            public void onBroadcastingFailed(BroadcastError error) {
                Log.e(TAG, "cant start broadcasting " + error.getType());
                view.setStatusText("Start broadcasting error " + error.getType());
            }
        });
    }

    void initializeManager() {
        // prod
        Manager.getInstance().initialize(
                "***REMOVED******REMOVED******REMOVED***",
                "***REMOVED***=",
                "***REMOVED***+6pXmtkYxynMQm0rfcBU0XFF5A==",
                view.getActivity()
        );

    }
}