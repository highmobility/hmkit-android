package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.Command.CommandParseException;

/**
 * Created by ttiganik on 28/09/2016.
 *
 * This is an evented message that is sent from the car every time the trunk state changes. This
 * message is also sent when a Get Trunk State is received by the car. The new status is included
 * in the message payload and may be the result of user, device or car triggered action.
 */

public class TrunkState extends IncomingCommand {
    /**
     * The possible lock positions
     */
    public enum LockState {
        LOCKED, UNLOCKED, UNSUPPORTED;

        static LockState lockStateFromByte(byte value) throws CommandParseException {
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

        static Position positionFromByte(byte value) throws CommandParseException {
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
     *
     * @return the current position of the trunk
     */
    public Position getPosition() {
        return position;
    }

    LockState state;
    Position position;

    public TrunkState(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length != 4) {
            throw new CommandParseException();
        }

        state = LockState.lockStateFromByte(bytes[2]);
        position = Position.positionFromByte(bytes[3]);
    }
}