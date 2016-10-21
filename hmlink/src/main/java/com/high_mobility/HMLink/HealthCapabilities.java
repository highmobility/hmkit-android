package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/10/2016.
 */

public class HealthCapabilities extends CapabilityType {

    private final Capability.Available heartRateCapability;

    public HealthCapabilities(byte aByte) throws CommandParseException {
        super(Type.HEALTH);
        heartRateCapability = Capability.availableCapability(aByte);
    }

    public Capability.Available getHeartRateCapability() {
        return heartRateCapability;
    }
}
