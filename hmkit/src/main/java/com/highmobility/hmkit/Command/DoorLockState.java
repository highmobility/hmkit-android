package com.highmobility.hmkit.Command;

/**
 * Created by root on 6/21/17.
 */

public class DoorLockState {
    /**
     * The possible positions of a car door
     */
    public enum DoorPosition {
        OPEN, CLOSED;

        public static DoorPosition fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return CLOSED;
                case 0x01: return OPEN;
            }

            throw new CommandParseException();
        }
    }

    /**
     * The possible states of the car lock.
     */
    public enum LockState {
        LOCKED, UNLOCKED;

        public static LockState fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNLOCKED;
                case 0x01: return LOCKED;
            }

            throw new CommandParseException();
        }
    }

    DoorPosition position;
    LockState lockState;

    public DoorLockState(byte position, byte lockState) throws CommandParseException {
        this.position = DoorPosition.fromByte(position);
        this.lockState = LockState.fromByte(lockState);
    }

    public DoorPosition getPosition() {
        return position;
    }

    public LockState getLockState() {
        return lockState;
    }
}