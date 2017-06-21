package com.highmobility.hmkit.Command.Capability;

import com.highmobility.hmkit.Command.Capability.*;
import com.highmobility.hmkit.Command.Command;
import com.highmobility.hmkit.Command.CommandParseException;

/**
 * Created by root on 6/20/17.
 */

public class FuelingCapability extends com.highmobility.hmkit.Command.Capability.FeatureCapability {
    AvailableCapability.Capability fuelCapCapability;

    public FuelingCapability(byte[] bytes) throws CommandParseException {
        super(Command.Identifier.FUELING);
        if (bytes.length != 4) throw new CommandParseException();
        fuelCapCapability = AvailableCapability.Capability.fromByte(bytes[3]);
    }

    public AvailableCapability.Capability getFuelCapCapability() {
        return fuelCapCapability;
    }
}