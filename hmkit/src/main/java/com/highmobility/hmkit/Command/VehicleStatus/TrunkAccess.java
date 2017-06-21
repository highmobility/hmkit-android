package com.highmobility.hmkit.Command.VehicleStatus;

import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Constants;
import com.highmobility.hmkit.Command.Command;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class TrunkAccess extends FeatureState {
    Constants.TrunkLockState lockState;
    Constants.TrunkPosition position;

    TrunkAccess(byte[] bytes) throws CommandParseException {
        super(Command.Identifier.TRUNK_ACCESS);

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
