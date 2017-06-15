package com.high_mobility.hmkit.Command.VehicleStatus;

import com.high_mobility.hmkit.Command.CommandParseException;
import com.high_mobility.hmkit.Command.Constants;
import com.high_mobility.hmkit.Command.Command.Identifier;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class TrunkAccess extends FeatureState {
    Constants.TrunkLockState lockState;
    Constants.TrunkPosition position;

    TrunkAccess(byte[] bytes) throws CommandParseException {
        super(Identifier.TRUNK_ACCESS);

        lockState = Constants.TrunkLockState.fromByte(bytes[3]);
        position = Constants.TrunkPosition.fromByte(bytes[4]);
    }

    public Constants.TrunkLockState getLockState() {
        return lockState;
    }

    public Constants.TrunkPosition getPosition() {
        return position;
    }
}
