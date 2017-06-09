package com.highmobility.common;

import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.high_mobility.HMLink.Command.Command;
import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.Failure;
import com.high_mobility.HMLink.Command.Incoming.IncomingCommand;
import com.high_mobility.HMLink.Command.Incoming.LockState;
import com.high_mobility.HMLink.Command.Incoming.TrunkState;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;
import com.high_mobility.HMLink.Command.VehicleStatus.DoorLocks;
import com.high_mobility.HMLink.Command.VehicleStatus.FeatureState;
import com.high_mobility.HMLink.Command.VehicleStatus.TrunkAccess;
import com.high_mobility.HMLink.ConnectedLink;
import com.high_mobility.HMLink.ConnectedLinkListener;
import com.high_mobility.HMLink.Constants;
import com.high_mobility.HMLink.Link;
import com.high_mobility.HMLink.Manager;

import java.util.Arrays;
import java.util.List;

import static com.high_mobility.HMLink.Command.Constants.LockState.LOCKED;

public class LinkViewController implements ILinkViewController, ConnectedLinkListener {
    static final String LINK_IDENTIFIER_MESSAGE = "com.highmobility.digitalkeydemo.LINKIDENTIFIER";
    private static final String TAG = "LinkViewController";

    ILinkView view;

    ConnectedLink link;

    com.high_mobility.HMLink.Command.Constants.LockState doorLockState;
    com.high_mobility.HMLink.Command.Constants.TrunkLockState trunkLockState;

    CountDownTimer timeoutTimer;

    // LinkListener

    public LinkViewController(ILinkView view) {
        this.view = view;
        Intent intent = view.getActivity().getIntent();
        byte[] serial = intent.getByteArrayExtra(LINK_IDENTIFIER_MESSAGE);
        List<ConnectedLink> links = Manager.getInstance().getBroadcaster().getLinks();

        for (ConnectedLink link : links) {
            if (Arrays.equals(link.getSerial(), serial)) {
                this.link = link;
                this.link.setListener(this);
                break;
            }
        }

        // ask for initial state
        requestInitialState();
    }

    @Override
    public void onAuthorizationRequested(ConnectedLink link, Constants.ApprovedCallback approvedCallback) {

    }

    @Override
    public void onAuthorizationTimeout(ConnectedLink link) {

    }

    @Override
    public void onStateChanged(Link link, Link.State state) {
        Log.d(TAG, "state changed " + link.getState());
        if (link.getState() != Link.State.AUTHENTICATED) {
            view.getActivity().finish();
        }
    }

    @Override
    public void onCommandReceived(Link link, byte[] bytes) {
        try {
            IncomingCommand command = IncomingCommand.create(bytes);

            if (command.is(Command.DoorLocks.LOCK_STATE)) {
                onLockStateUpdate(((LockState) command).getState());
            }
            else if (command.is(Command.TrunkAccess.TRUNK_STATE)) {
                onTrunkStateUpdate(((TrunkState) command).getLockState());
            }
            else if (command.is(Command.VehicleStatus.VEHICLE_STATUS)) {
                onVehicleStatusUpdate((VehicleStatus)command);
            }
            else if (command.is(Command.FailureMessage.FAILURE_MESSAGE)) {
                Failure failure = (Failure)command;
                Log.d(TAG, "failure " + failure.getFailureReason().toString());
                if (doorLockState == null) {
                    onInitializeFinished(-13, failure.getFailureReason().toString());
                }
                else {

                }
            }
        }
        catch (CommandParseException e) {
            Log.d(TAG, "IncomingCommand parse exception ", e);
        }
    }

    @Override
    public void onLockDoorsClicked() {
        view.showLoadingView(true);

        boolean doorsLocked = doorLockState == LOCKED;

        link.sendCommand(Command.DoorLocks.lockDoors(doorsLocked ? false : true), true, new Constants.ResponseCallback() {
                @Override
                public void response(int i) {
                    if (i != 0) {
                        onCommandFinished("lock command send exception " + i);
                    }
                    else {
                        // else wait for command
                        startCommandTimeout();
                    }
                }
            }
        );
    }

    @Override
    public void onLockTrunkClicked() {
        view.showLoadingView(true);

        boolean trunkLocked = trunkLockState == com.high_mobility.HMLink.Command.Constants.TrunkLockState.LOCKED;

        byte[] command =
                Command.TrunkAccess.setTrunkState(trunkLocked ?
                        com.high_mobility.HMLink.Command.Constants.TrunkLockState.UNLOCKED :
                        com.high_mobility.HMLink.Command.Constants.TrunkLockState.LOCKED,
                        com.high_mobility.HMLink.Command.Constants.TrunkPosition.OPEN);

        link.sendCommand(command, true, new Constants.ResponseCallback() {
                    @Override
                    public void response(int i) {
                if (i != 0) {
                    onCommandFinished("trunk command send exception " + i);
                }
                else {
                    // else wait for command
                    startCommandTimeout();
                }
                }
            }
        );
    }

    void requestInitialState() {
        view.showLoadingView(true);
        startInitializeTimer();

        link.sendCommand(Command.VehicleStatus.getVehicleStatus(), true, new Constants.ResponseCallback() {
            @Override
            public void response(int i) {
                if (i != 0) {
                    onInitializeFinished(i, "Get vehicle status failed");
                }
            }
        });
    }

    void onLockStateUpdate(com.high_mobility.HMLink.Command.Constants.LockState lockState) {
        doorLockState = lockState;
        Log.i(TAG, "Lock status changed " + lockState);

        if (doorLockState == LOCKED) {
            view.onDoorsLocked(true);
        }
        else {
            view.onDoorsLocked(false);
        }

        onCommandFinished(null);
    }

    void onTrunkStateUpdate(com.high_mobility.HMLink.Command.Constants.TrunkLockState lockState) {
        this.trunkLockState = lockState;
        Log.i(TAG, "trunk status changed " + trunkLockState);

        if (trunkLockState == com.high_mobility.HMLink.Command.Constants.TrunkLockState.LOCKED) {
            view.onTrunkLocked(true);

        }
        else {
            view.onTrunkLocked(false);
        }

        onCommandFinished(null);
    }

    void onVehicleStatusUpdate(VehicleStatus status) {
        FeatureState[] states = status.getFeatureStates();

        DoorLocks doorLocksState = null;
        TrunkAccess trunkAccessState = null;

        for (int i = 0; i < states.length; i++) {
            FeatureState featureState = states[i];
            if (featureState.getFeature() == Command.Identifier.DOOR_LOCKS) {
                doorLocksState = (DoorLocks)featureState;
            }
            else if (featureState.getFeature() == Command.Identifier.TRUNK_ACCESS) {
                trunkAccessState = (TrunkAccess) featureState;
            }
        }

        view.enableLockButton(doorLocksState != null);
        view.enableTrunkButton(trunkAccessState != null);

        if (doorLocksState == null && trunkAccessState == null) {
            onInitializeFinished(-13, "Unsupported");
            return;
        }

        if (doorLocksState != null) onLockStateUpdate(doorLocksState.getState());
        if (trunkAccessState != null) onTrunkStateUpdate(trunkAccessState.getLockState());
    }

    void onCommandFinished(String error) {
        view.showLoadingView(false);

        if (error != null) showToast(error);
        timeoutTimer.cancel();
    }

    void startCommandTimeout() {
        timeoutTimer = new CountDownTimer((long)(Constants.commandTimeout * 1000), 15000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                onCommandFinished("Command response timeout");
            }
        }.start();
    }

    void onInitializeFinished(int errorCode, String reason) {
        timeoutTimer.cancel();

        if (errorCode != 0) {
            link.setListener(null);
            showToast("Initialization failed:" + (reason != null ? " " + reason + " " : " ") + errorCode);
            view.getActivity().finish();
        }
        else {

        }
    }

    void startInitializeTimer() {
        // 30 s
        timeoutTimer = new CountDownTimer((long)(Constants.commandTimeout * 1000 + 10000), 15000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                onInitializeFinished(-12, "Timeout");
            }
        }.start();
    }

    void showToast(String text) {
        Toast.makeText(view.getActivity(), text, Toast.LENGTH_LONG).show();
        Log.d(TAG, text);
    }
}
