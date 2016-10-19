package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This command is sent when a Get Vehicle Status command is received by the car.
 *
 */
public class VehicleStatus extends IncomingCommand {
    private LockState.State doorLockStatus;
    private TrunkState.LockState trunkLockStatus;
    private TrunkState.Position trunkPosition;
    private boolean windshieldHeatingActive;
    private RooftopState.State rooftopState;
    private ControlMode.Mode remoteControlMode;

    public VehicleStatus(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length != 8) throw new CommandParseException();

        doorLockStatus = LockState.State.lockStateFromByte(bytes[2]);
        trunkLockStatus = TrunkState.LockState.lockStateFromByte(bytes[3]);
        trunkPosition = TrunkState.Position.positionFromByte(bytes[4]);
        windshieldHeatingActive = WindshieldHeatingState.isWindshieldActiveForByte(bytes[5]);
        rooftopState = RooftopState.State.stateForByte(bytes[6]);
        remoteControlMode = ControlMode.Mode.controlModeFromByte(bytes[7]);
    }

    public LockState.State getDoorLockState() {
        return doorLockStatus;
    }

    public TrunkState.LockState getTrunkLockState() {
        return trunkLockStatus;
    }

    public TrunkState.Position getTrunkPosition() {
        return trunkPosition;
    }

    public boolean isWindshieldHeatingActive() {
        return windshieldHeatingActive;
    }

    public RooftopState.State getRooftopState() {
        return rooftopState;
    }

    public ControlMode.Mode getRemoteControlMode() {
        return remoteControlMode;
    }
}
