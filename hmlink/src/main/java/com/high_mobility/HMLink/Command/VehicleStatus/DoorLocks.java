package com.high_mobility.HMLink.Command.VehicleStatus;
import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Constants;
import com.high_mobility.HMLink.Command.VehicleFeature;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class DoorLocks extends FeatureState {

    Constants.LockState state;

    DoorLocks(byte[] bytes) throws CommandParseException {
        super(VehicleFeature.DOOR_LOCKS);
        state = Constants.LockState.fromByte(bytes[3]);
    }

    public Constants.LockState getState() {
        return state;
    }
}
