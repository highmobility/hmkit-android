package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/10/2016.
 */

public class POICapabilities extends CapabilityType {

    public Capability.Available getSetDestinationCapability() {
        return setDestinationCapability;
    }
    private final Capability.Available setDestinationCapability;

    public POICapabilities(byte aByte) throws CommandParseException {
        super(Type.POI);
        setDestinationCapability = Capability.availableCapability(aByte);
    }

}
