package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/10/2016.
 */

public class HealthCapabilities extends CapabilityType {

    private final Capabilities.Available heartRateCapability;

    public HealthCapabilities(byte aByte) throws CommandParseException {
        super(Type.HEALTH);
        heartRateCapability = Capabilities.availableCapability(aByte);
    }

    public Capabilities.Available getHeartRateCapability() {
        return heartRateCapability;
    }
}
