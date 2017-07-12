package com.highmobility.hmkit.Command.VehicleStatus;

import com.highmobility.hmkit.Command.CommandParseException;

import com.highmobility.hmkit.Command.Command;
import com.highmobility.hmkit.Command.Incoming.TrunkState;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class TrunkAccess extends FeatureState {
    TrunkState.LockState lockState;
    TrunkState.Position position;

    TrunkAccess(byte[] bytes) throws CommandParseException {
        super(Command.Identifier.TRUNK_ACCESS);

        lockState = TrunkState.LockState.fromByte(bytes[3]);
        position = TrunkState.Position.fromByte(bytes[4]);
    }

    public TrunkState.LockState getLockState() {
        return lockState;
    }

    public TrunkState.Position getPosition() {
        return position;
    }
}
