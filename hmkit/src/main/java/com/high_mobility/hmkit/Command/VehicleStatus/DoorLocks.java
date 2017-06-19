package com.high_mobility.hmkit.Command.VehicleStatus;
import com.high_mobility.hmkit.Command.CommandParseException;
import com.high_mobility.hmkit.Command.Constants;
import com.high_mobility.hmkit.Command.Command.Identifier;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class DoorLocks extends FeatureState {

    Constants.LockState state;

    DoorLocks(byte[] bytes) throws CommandParseException {
        super(Identifier.DOOR_LOCKS);
        state = Constants.LockState.fromByte(bytes[3]);
    }

    public Constants.LockState getState() {
        return state;
    }
}
