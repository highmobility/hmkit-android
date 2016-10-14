package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/10/2016.
 */

public class POICapabilities extends CapabilityType {

    public Capabilities.Available getSetDestinationCapability() {
        return setDestinationCapability;
    }
    private final Capabilities.Available setDestinationCapability;

    public POICapabilities(byte aByte) throws CommandParseException {
        super(Type.POI);
        setDestinationCapability = Capabilities.availableCapability(aByte);
    }

}
