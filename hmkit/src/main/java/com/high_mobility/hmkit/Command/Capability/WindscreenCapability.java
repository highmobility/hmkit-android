package com.high_mobility.hmkit.Command.Capability;

import com.high_mobility.hmkit.Command.Command;
import com.high_mobility.hmkit.Command.CommandParseException;

/**
 * Created by root on 6/20/17.
 */

public class WindscreenCapability extends FeatureCapability {
    AvailableCapability.Capability wiperCapability;
    AvailableGetStateCapability.Capability windscreenDamageCapability;


    public WindscreenCapability(byte[] bytes) throws CommandParseException {
        super(Command.Identifier.WINDSCREEN);
        if (bytes.length != 5) throw new CommandParseException();
        wiperCapability = AvailableCapability.Capability.fromByte(bytes[3]);
        windscreenDamageCapability = AvailableGetStateCapability.Capability.fromByte(bytes[4]);
    }

}
