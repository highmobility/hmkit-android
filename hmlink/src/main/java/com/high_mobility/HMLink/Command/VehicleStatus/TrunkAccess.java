package com.high_mobility.HMLink.Command.VehicleStatus;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Constants;
import com.high_mobility.HMLink.Command.VehicleFeature;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class TrunkAccess extends FeatureState {
    Constants.TrunkLockState lockState;
    Constants.TrunkPosition position;

    TrunkAccess(byte[] bytes) throws CommandParseException {
        super(VehicleFeature.TRUNK_ACCESS);

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
