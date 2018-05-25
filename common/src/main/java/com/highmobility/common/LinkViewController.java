package com.highmobility.common;

import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.highmobility.autoapi.Capabilities;
import com.highmobility.autoapi.Command;
import com.highmobility.autoapi.CommandResolver;
import com.highmobility.autoapi.DiagnosticsState;
import com.highmobility.autoapi.Failure;
import com.highmobility.autoapi.GetCapabilities;
import com.highmobility.autoapi.GetVehicleStatus;
import com.highmobility.autoapi.LockState;
import com.highmobility.autoapi.LockUnlockDoors;
import com.highmobility.autoapi.OpenCloseTrunk;
import com.highmobility.autoapi.TrunkState;
import com.highmobility.autoapi.VehicleStatus;
import com.highmobility.autoapi.property.TrunkLockState;
import com.highmobility.autoapi.property.TrunkPosition;
import com.highmobility.autoapi.property.diagnostics.TireStateProperty;
import com.highmobility.autoapi.property.doors.DoorLock;
import com.highmobility.hmkit.ConnectedLink;
import com.highmobility.hmkit.ConnectedLinkListener;
import com.highmobility.hmkit.Error.LinkError;
import com.highmobility.hmkit.Link;
import com.highmobility.hmkit.Manager;
import com.highmobility.value.Bytes;

import java.util.Arrays;
import java.util.List;

import static com.highmobility.autoapi.property.TrunkLockState.LOCKED;
import static com.highmobility.autoapi.property.TrunkLockState.UNLOCKED;

public class LinkViewController implements ILinkViewController, ConnectedLinkListener {
    static final String LINK_IDENTIFIER_MESSAGE = "com.highmobility.digitalkeydemo.LINKIDENTIFIER";
    private static final String TAG = "LinkViewController";

    ILinkView view;

    ConnectedLink link;

    boolean doorsLocked;
    TrunkLockState trunkLockState;

    CountDownTimer timeoutTimer;

    // LinkListener

    public LinkViewController(ILinkView view) {
        this.view = view;

        Intent intent = view.getActivity().getIntent();
        byte[] serial = intent.getByteArrayExtra(LINK_IDENTIFIER_MESSAGE);
        List<ConnectedLink> links = Manager.getInstance().getBroadcaster().getLinks();

        for (ConnectedLink link : links) {
            if (link.getSerial().equals(serial)) {
                this.link = link;
                this.link.setListener(this);
                break;
            }
        }

        // ask for initial state
        requestInitialState();
    }

    @Override
    public void onAuthorizationRequested(ConnectedLink link, AuthorizationCallback
            approvedCallback) {

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
        Command command = CommandResolver.resolve(bytes);

        if (command instanceof LockState) {
            onLockStateUpdate(((LockState) command).isLocked());
        } else if (command instanceof DiagnosticsState) {
            DiagnosticsState diagnostics = (DiagnosticsState) command;
            Log.d(TAG, "front left: " + diagnostics.getTireState(TireStateProperty.Location.FRONT_LEFT).getPressure());
            Log.d(TAG, "front right: " + diagnostics.getTireState(TireStateProperty.Location
                    .FRONT_RIGHT).getPressure());
            Log.d(TAG, "rear left: " + diagnostics.getTireState(TireStateProperty.Location
                    .REAR_LEFT).getPressure());
            Log.d(TAG, "rear right: " + diagnostics.getTireState(TireStateProperty.Location
                    .REAR_RIGHT).getPressure());
        } else if (command instanceof TrunkState) {
            onTrunkStateUpdate(((TrunkState) command).getLockState());
        } else if (command instanceof VehicleStatus) {
            onVehicleStatusUpdate((VehicleStatus) command);
        } else if (command instanceof Capabilities) {
            link.sendCommand(new GetVehicleStatus(), new Link
                    .CommandCallback() {
                @Override
                public void onCommandSent() {

                }

                @Override
                public void onCommandFailed(LinkError error) {
                    onInitializeFinished(error.getCode(), "Get vehicle status failed");
                }
            });
        } else if (command instanceof Failure) {
            Failure failure = (Failure) command;
            Log.d(TAG, "failure " + failure.getFailureReason().toString());

            onInitializeFinished(-13, failure.getFailedType() + " failed " + failure.getFailureReason());
        }
    }

    @Override
    public void onLockDoorsClicked() {
//        Manager.getInstance().getBroadcaster().disconnectAllLinks();

        view.showLoadingView(true);
        Bytes bytes = new LockUnlockDoors(doorsLocked ? DoorLock.UNLOCKED :
                DoorLock.LOCKED);
        link.sendCommand(bytes, new Link.CommandCallback() {
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

        boolean trunkLocked = trunkLockState == LOCKED;

        Bytes command = new OpenCloseTrunk(trunkLocked ? UNLOCKED : LOCKED, TrunkPosition
                .CLOSED);

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

        link.sendCommand(new GetCapabilities(), new Link.CommandCallback() {
            @Override
            public void onCommandSent() {

            }

            @Override
            public void onCommandFailed(LinkError error) {
                onInitializeFinished(error.getCode(), "Get capa failed");
            }
        });
    }

    void onLockStateUpdate(boolean locked) {
        Log.i(TAG, "Lock status changed " + locked);
        doorsLocked = locked;

        if (doorsLocked == true) {
            view.onDoorsLocked(true);
        } else {
            view.onDoorsLocked(false);
        }

        onCommandFinished(null);
    }

    void onTrunkStateUpdate(TrunkLockState lockState) {
        this.trunkLockState = lockState;
        Log.i(TAG, "trunk status changed " + trunkLockState);

        if (trunkLockState == LOCKED) {
            view.onTrunkLocked(true);

        } else {
            view.onTrunkLocked(false);
        }

        onCommandFinished(null);
    }

    void onVehicleStatusUpdate(VehicleStatus status) {
        LockState doorLocksState = (LockState) status.getState(LockState.TYPE);
        TrunkState trunkAccessState = (TrunkState) status.getState(TrunkState.TYPE);

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
        timeoutTimer = new CountDownTimer((long) (com.highmobility.hmkit.Constants.commandTimeout
                * 10000), 120000) {
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
            showToast("Initialization failed:" + (reason != null ? " " + reason + " " : " ") +
                    errorCode);
            view.getActivity().finish();
        } else {

        }
    }

    void startInitializeTimer() {
        // 30 s
        timeoutTimer = new CountDownTimer((long) (120000), 120000) {
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
