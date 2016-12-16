package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Constants;

/**
 * This is an evented message that is sent from the car every time the trunk state changes. This
 * message is also sent when a Get Trunk State is received by the car. The new status is included
 * in the message payload and may be the result of user, device or car triggered action.
 */

public class TrunkState extends IncomingCommand {
    /**
     * @return the current lock status of the trunk
     */
    public Constants.TrunkLockState getLockState() {
        return state;
    }

    /**
     * @return the current position of the trunk
     */
    public Constants.TrunkPosition getPosition() {
        return position;
    }

    Constants.TrunkLockState state;
    Constants.TrunkPosition position;

    public TrunkState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 5) {
            throw new CommandParseException();
        }

        state = Constants.TrunkLockState.fromByte(bytes[3]);
        position = Constants.TrunkPosition.fromByte(bytes[4]);
    }
}