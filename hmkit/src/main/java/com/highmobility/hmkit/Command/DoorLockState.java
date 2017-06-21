package com.highmobility.hmkit.Command;

/**
 * Created by root on 6/21/17.
 */

public class DoorLockState {
    Constants.DoorPosition position;
    Constants.LockState lockState;

    public DoorLockState(byte position, byte lockState) throws CommandParseException {
        this.position = Constants.DoorPosition.fromByte(position);
        this.lockState = Constants.LockState.fromByte(lockState);
    }

    public Constants.DoorPosition getPosition() {
        return position;
    }

    public Constants.LockState getLockState() {
        return lockState;
    }
}