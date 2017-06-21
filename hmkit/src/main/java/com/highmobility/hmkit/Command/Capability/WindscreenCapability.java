package com.highmobility.hmkit.Command.Capability;

import com.highmobility.hmkit.Command.Capability.*;
import com.highmobility.hmkit.Command.Command;
import com.highmobility.hmkit.Command.CommandParseException;

/**
 * Created by root on 6/20/17.
 */

public class WindscreenCapability extends com.highmobility.hmkit.Command.Capability.FeatureCapability {
    AvailableCapability.Capability wiperCapability;
    AvailableGetStateCapability.Capability windscreenDamageCapability;


    public WindscreenCapability(byte[] bytes) throws CommandParseException {
        super(Command.Identifier.WINDSCREEN);
        if (bytes.length != 5) throw new CommandParseException();
        wiperCapability = AvailableCapability.Capability.fromByte(bytes[3]);
        windscreenDamageCapability = AvailableGetStateCapability.Capability.fromByte(bytes[4]);
    }

}
