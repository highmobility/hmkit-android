package com.high_mobility.HMLink;

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
        LOCKED, UNLOCKED
    }

    /**
     * The possible trunk positions
     */
    public enum Position {
        OPEN, CLOSED
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

        state = bytes[2] == 0x00 ? LockState.UNLOCKED : LockState.LOCKED;
        position = bytes[3] == 0x00 ? Position.CLOSED : Position.OPEN;
    }
}