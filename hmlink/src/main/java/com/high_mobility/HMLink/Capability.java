package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/09/16.
 *
 * This command is sent when a Get Capabilities command is received by the car.
 */
public class Capability extends IncomingCommand {

    public CapabilityType getCapabilityType() {
        return capabilityType;
    }
    CapabilityType capabilityType;


     public Capability(byte[] bytes) throws CommandParseException {
        super(bytes);
        capabilityType = CapabilityType.capabilityFromIncomingCapability(bytes);
    }
}
