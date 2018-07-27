package com.highmobility.common;

import android.content.Intent;
import android.util.Log;

import com.highmobility.autoapi.LockUnlockDoors;
import com.highmobility.autoapi.property.doors.DoorLock;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.hmkit.BroadcastConfiguration;
import com.highmobility.hmkit.Broadcaster;
import com.highmobility.hmkit.BroadcasterListener;
import com.highmobility.hmkit.ConnectedLink;
import com.highmobility.hmkit.ConnectedLinkListener;
import com.highmobility.hmkit.Link;
import com.highmobility.hmkit.Manager;
import com.highmobility.hmkit.Telematics;
import com.highmobility.hmkit.error.BroadcastError;
import com.highmobility.hmkit.error.DownloadAccessCertificateError;
import com.highmobility.hmkit.error.TelematicsError;
import com.highmobility.value.Bytes;

public class BroadcastingViewController implements IBroadcastingViewController,
        BroadcasterListener, ConnectedLinkListener {
    private static final String TAG = "BroadcastingVC";
    public static final int LINK_ACTIVITY_RESULT = 1;
    IBroadcastingView view;

    AuthorizationCallback authorizationCallback;
    Broadcaster broadcaster;
    ConnectedLink link;

    public BroadcastingViewController(IBroadcastingView view) {
        this.view = view;

        downloadAccessCertificates(() -> startBroadcasting(), null);
//        sendTelematicsCommand();
    }

    @Override
    public void onLinkViewResult(int result) {
        if (link != null) onStateChanged(link, link.getState());
        startBroadcasting();
    }

    private void sendTelematicsCommand() {
        String token =
                "***REMOVED***";

        Manager.getInstance().downloadCertificate(token, new Manager.DownloadCallback() {
            @Override
            public void onDownloaded(DeviceSerial serial) {
                Bytes command = new LockUnlockDoors(DoorLock.LOCKED);
                Manager.getInstance().getTelematics().sendCommand(command, serial, new Telematics
                        .CommandCallback() {
                    @Override
                    public void onCommandResponse(Bytes bytes) {
                        Log.d(TAG, "onCommandResponse: " + bytes);
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
        // clear the references
        broadcaster.setListener(null);
        broadcaster = null;
        link.setListener(null);
        link = null;
    }
    
    @Override public void onPause(boolean pause) {
        Log.d(TAG, "onPause() called with: pause = [" + pause + "]");
        if (pause) {
            try {
                Manager.getInstance().terminate();
            }
            catch (Exception e) {
                Log.e(TAG, "onPause: ", e);
            }
        }
        else {
            downloadAccessCertificates(() -> startBroadcasting(), null);
        }
    }

    @Override
    public void onPairingApproved(boolean approved) {
        if (approved) {
            authorizationCallback.approve();
            view.showPairingView(false);
        } else {
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
        intent.putExtra(LinkViewController.LINK_IDENTIFIER_MESSAGE, link.getSerial().getByteArray
                ());
        view.getActivity().startActivityForResult(intent, LINK_ACTIVITY_RESULT);
    }

    @Override public void onDisconnectClicked() {
        broadcaster.stopBroadcasting();
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
            onStateChanged(broadcaster.getState());
        }
    }

    @Override
    public void onAuthorizationRequested(ConnectedLink connectedLink, AuthorizationCallback
            authorizationCallback) {
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
        if (link == this.link) {
            view.updateLink((ConnectedLink) link);

            if (link.getState() == Link.State.AUTHENTICATED) {
                onLinkClicked();
                view.setStatusText("authenticated");
            } else if (link.getState() == Link.State.CONNECTED) {
                view.setStatusText("connected");
            } else {
                this.link = null;
                onStateChanged(broadcaster.getState());
            }
        }
    }

    @Override
    public void onCommandReceived(Link link, Bytes bytes) {

    }

    void downloadAccessCertificates(Runnable success, Runnable failed) {
        // prod nexus 5

        Manager.getInstance().initialize(
                "dGVzdLnVeFXsIJTMMDWwwF7qX" +
                        "***REMOVED***",
                "***REMOVED***",
                "***REMOVED***" +
                        "+z2sxxdwWNaItdBUWg==",
                view.getActivity()
        );

        broadcaster = Manager.getInstance().getBroadcaster();
        // set the broadcaster listener
        broadcaster.setListener(this);

        // PASTE ACCESS TOKEN HERE
        String accessToken =
                "Rp1wTWvW79qKE6iwGpYBimM12y" +
                        "***REMOVED***";

        Manager.getInstance().downloadCertificate(accessToken, new Manager.DownloadCallback() {
            @Override
            public void onDownloaded(DeviceSerial serial) {
                if (success != null) success.run();
            }

            @Override
            public void onDownloadFailed(DownloadAccessCertificateError error) {
                Log.e(TAG, "Could not download a certificate with token: " + error
                        .getMessage());
                if (failed != null) failed.run();
            }
        });
    }

    void startBroadcasting() {
        BroadcastConfiguration conf = new BroadcastConfiguration.Builder()
                .setOverridesAdvertisementName(true).build(); // true is default anyway

        broadcaster.startBroadcasting(new Broadcaster.StartCallback() {
            @Override public void onBroadcastingStarted() {
                Log.d(TAG, "onBroadcastingStarted: ");
            }

            @Override public void onBroadcastingFailed(BroadcastError error) {
                Log.d(TAG, "onBroadcastingFailed: " + error.getMessage());
            }
        }, conf);
    }
}