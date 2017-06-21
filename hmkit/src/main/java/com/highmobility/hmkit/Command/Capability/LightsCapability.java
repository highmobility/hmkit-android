package com.highmobility.hmkit.Command.Capability;

import com.highmobility.hmkit.Command.Capability.*;
import com.highmobility.hmkit.Command.Command;
import com.highmobility.hmkit.Command.CommandParseException;

/**
 * Created by root on 6/20/17.
 */

public class LightsCapability extends com.highmobility.hmkit.Command.Capability.FeatureCapability {
    AvailableGetStateCapability.Capability exteriorLightsCapability;
    AvailableGetStateCapability.Capability interiorLightsCapability;
    AvailableCapability.Capability ambientLightsCapability;

    public LightsCapability(byte[] bytes) throws CommandParseException {
        super(Command.Identifier.LIGHTS);
        if (bytes.length != 6) throw new CommandParseException();
        exteriorLightsCapability = AvailableGetStateCapability.Capability.fromByte(bytes[3]);
        interiorLightsCapability = AvailableGetStateCapability.Capability.fromByte(bytes[4]);
        ambientLightsCapability = AvailableCapability.Capability.fromByte(bytes[5]);
    }

    public AvailableGetStateCapability.Capability getExteriorLightsCapability() {
        return exteriorLightsCapability;
    }

    public AvailableGetStateCapability.Capability getInteriorLightsCapability() {
        return interiorLightsCapability;
    }

    public AvailableCapability.Capability getAmbientLightsCapability() {
        return ambientLightsCapability;
    }
}
