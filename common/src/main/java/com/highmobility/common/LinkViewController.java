package com.highmobility.common;

import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.highmobility.autoapi.Command;
import com.highmobility.autoapi.CommandParseException;

import com.highmobility.autoapi.incoming.*;
import com.highmobility.autoapi.vehiclestatus.DoorLocks;
import com.highmobility.autoapi.vehiclestatus.FeatureState;
import com.highmobility.autoapi.vehiclestatus.TrunkAccess;
import com.highmobility.hmkit.ConnectedLink;
import com.highmobility.hmkit.ConnectedLinkListener;
import com.highmobility.hmkit.Error.LinkError;
import com.highmobility.hmkit.Link;
import com.highmobility.hmkit.Manager;

import java.util.Arrays;
import java.util.List;


public class LinkViewController implements ILinkViewController, ConnectedLinkListener {
    static final String LINK_IDENTIFIER_MESSAGE = "com.highmobility.digitalkeydemo.LINKIDENTIFIER";
    private static final String TAG = "LinkViewController";

    ILinkView view;

    ConnectedLink link;

    boolean doorsLocked;
    TrunkState.LockState trunkLockState;

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
//        requestInitialState();
    }

    @Override
    public void onAuthorizationRequested(ConnectedLink link, AuthorizationCallback approvedCallback) {

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
                onLockStateUpdate(((LockState)command).isLocked());
            }
            else if (command.is(Command.Diagnostics.DIAGNOSTICS_STATE)) {
                DiagnosticsState diagnostics = (DiagnosticsState)command;
                Log.d(TAG, "front left: " + diagnostics.getFrontLeftTirePressure());
                Log.d(TAG, "front right: " + diagnostics.getFrontRightTirePressure());
                Log.d(TAG, "rear left: " + diagnostics.getRearLeftTirePressure());
                Log.d(TAG, "rear right: " + diagnostics.getRearRightTirePressure());
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
                // TODO: fix me. Catch initialization error somehow. add initializing boolean
                /*if (doorsLocked == false) {
                    onInitializeFinished(-13, failure.getFailureReason().toString());
                }
                else {

                }*/
            }
        }
        catch (CommandParseException e) {
            Log.d(TAG, "IncomingCommand parse exception ", e);
        }
    }

    @Override
    public void onLockDoorsClicked() {
        view.showLoadingView(true);

        link.sendCommand(Command.DoorLocks.lockDoors(doorsLocked ? false : true), new Link.CommandCallback() {
            @Override
            public void onCommandSent() {
                // else wait for command
                startCommandTimeout();
            }

            @Override
            public void onCommandFailed(LinkError error) {
                onCommandFinished("lock command send exception " + error.getType());
            }
        });
    }

    @Override
    public void onLockTrunkClicked() {
        view.showLoadingView(true);

        boolean trunkLocked = trunkLockState == TrunkState.LockState.LOCKED;

        byte[] command =
                Command.TrunkAccess.setTrunkState(trunkLocked ?
                        TrunkState.LockState.UNLOCKED :
                        TrunkState.LockState.LOCKED,
                        TrunkState.Position.OPEN);

        link.sendCommand(command, new Link.CommandCallback() {
            @Override
            public void onCommandSent() {
                startCommandTimeout();
            }

            @Override
            public void onCommandFailed(LinkError error) {
                onCommandFinished("trunk command send exception " + error.getType());
            }
        });
    }

    void requestInitialState() {
        view.showLoadingView(true);
        startInitializeTimer();

        link.sendCommand(Command.VehicleStatus.getVehicleStatus(), new Link.CommandCallback() {
            @Override
            public void onCommandSent() {

            }

            @Override
            public void onCommandFailed(LinkError error) {
                onInitializeFinished(error.getCode(), "Get vehicle status failed");
            }
        });
    }

    void onLockStateUpdate(boolean locked) {
        Log.i(TAG, "Lock status changed " + locked);

        if (doorsLocked == true) {
            view.onDoorsLocked(true);
        }
        else {
            view.onDoorsLocked(false);
        }

        onCommandFinished(null);
    }

    void onTrunkStateUpdate(TrunkState.LockState lockState) {
        this.trunkLockState = lockState;
        Log.i(TAG, "trunk status changed " + trunkLockState);

        if (trunkLockState == TrunkState.LockState.LOCKED) {
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
            if (featureState.getIdentifier() == Command.Identifier.DOOR_LOCKS) {
                doorLocksState = (DoorLocks)featureState;
            }
            else if (featureState.getIdentifier() == Command.Identifier.TRUNK_ACCESS) {
                trunkAccessState = (TrunkAccess) featureState;
            }
        }

        view.enableLockButton(doorLocksState != null);
        view.enableTrunkButton(trunkAccessState != null);

        if (doorLocksState == null && trunkAccessState == null) {
            onInitializeFinished(-13, "Unsupported");
            return;
        }

        if (doorLocksState != null) onLockStateUpdate(doorLocksState.isLocked());
        if (trunkAccessState != null) onTrunkStateUpdate(trunkAccessState.getLockState());
    }

    void onCommandFinished(String error) {
        view.showLoadingView(false);

        if (error != null) showToast(error);
        timeoutTimer.cancel();
    }

    void startCommandTimeout() {
        timeoutTimer = new CountDownTimer((long)(com.highmobility.hmkit.Constants.commandTimeout * 1000), 15000) {
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
        timeoutTimer = new CountDownTimer((long)(com.highmobility.hmkit.Constants.commandTimeout * 1000 + 10000), 15000) {
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
