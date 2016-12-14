package com.high_mobility.HMLink.Command.VehicleStatus;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class TrunkAccess extends FeatureState {
    // TODO:
    public enum LockState {
        LOCKED, UNLOCKED, UNSUPPORTED;

        static LockState fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNLOCKED;
                case 0x01: return LOCKED;
                case (byte)0xFF: return UNSUPPORTED;
            }

            throw new CommandParseException();
        }
    }

    public enum Position {
        CLOSED, OPEN, UNSUPPORTED;

        static Position fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return CLOSED;
                case 0x01: return OPEN;
                case (byte)0xFF: return UNSUPPORTED;
            }

            throw new CommandParseException();
        }
    }

    LockState lockState;
    Position position;

    TrunkAccess(byte[] bytes) throws CommandParseException {
        super(VehicleStatus.Feature.TRUNK_ACCESS);

        lockState = LockState.fromByte(bytes[3]);
        position = Position.fromByte(bytes[4]);
    }

    public LockState getLockState() {
        return lockState;
    }

    public Position getPosition() {
        return position;
    }
}
