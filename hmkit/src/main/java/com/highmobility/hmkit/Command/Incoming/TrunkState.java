package com.highmobility.hmkit.Command.Incoming;

import com.highmobility.hmkit.Command.CommandParseException;

/**
 * This is an evented message that is sent from the car every time the trunk state changes. This
 * message is also sent when a Get Trunk State is received by the car. The new status is included
 * in the message payload and may be the result of user, device or car triggered action.
 */

public class TrunkState extends IncomingCommand {
    /**
     * The possible trunk lock states
     */
    public enum LockState {
        LOCKED, UNLOCKED, UNSUPPORTED;

        public static LockState fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNLOCKED;
                case 0x01: return LOCKED;
                case (byte)0xFF: return UNSUPPORTED;
            }


            throw new CommandParseException();
        }
    }

    /**
     * The possible trunk positions
     */
    public enum Position {
        OPEN, CLOSED, UNSUPPORTED;

        public static Position fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return CLOSED;
                case 0x01: return OPEN;
                case (byte)0xFF: return UNSUPPORTED;
            }

            throw new CommandParseException();
        }
    }

    /**
     * @return the current lock status of the trunk
     */
    public LockState getLockState() {
        return state;
    }

    /**
     * @return the current position of the trunk
     */
    public Position getPosition() {
        return position;
    }

    LockState state;
    Position position;

    public TrunkState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 5) {
            throw new CommandParseException();
        }

        state = LockState.fromByte(bytes[3]);
        position = Position.fromByte(bytes[4]);
    }
}