package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/10/2016.
 */

public class ParkingCapabilities extends CapabilityType {
    private final Capabilities.Available remoteControlCapability;

    public Capabilities.Available getRemoteControlCapability() {
        return remoteControlCapability;
    }

    public ParkingCapabilities(byte aByte) throws CommandParseException {
        super(Type.PARKING);
        remoteControlCapability = Capabilities.availableCapability(aByte);
    }
}
