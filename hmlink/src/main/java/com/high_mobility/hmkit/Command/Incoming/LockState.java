package com.high_mobility.hmkit.Command.Incoming;

import com.high_mobility.hmkit.Command.CommandParseException;
import com.high_mobility.hmkit.Command.Constants;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This is an evented notification that is sent from the car every time the lock state changes. This
 * notificaiton is also sent when a Get Lock State is received by the car. The new status is included
 * and may be the result of user, device or car triggered action.
 */
public class LockState extends IncomingCommand {
    /**
     *
     * @return the current lock status of the car
     */
    public Constants.LockState getState() {
        return state;
    }

    Constants.LockState state;

    public LockState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 4) {
            throw new CommandParseException();
        }

        state = Constants.LockState.fromByte(bytes[3]);
    }
}
