package com.highmobility.hmkit.Command.VehicleStatus;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Constants;
import com.highmobility.hmkit.Command.Command;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class DoorLocks extends FeatureState {

    Constants.LockState state;

    DoorLocks(byte[] bytes) throws CommandParseException {
        super(Command.Identifier.DOOR_LOCKS);
        state = Constants.LockState.fromByte(bytes[3]);
    }

    public Constants.LockState getState() {
        return state;
    }
}
