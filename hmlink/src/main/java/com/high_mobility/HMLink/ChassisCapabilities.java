package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/10/2016.
 */

public class ChassisCapabilities extends CapabilityType {

    Capability.AvailableGetState windshieldHeatingCapability;

    Capability.AvailableGetState rooftopControlCapability;

    public Capability.AvailableGetState getWindshieldHeatingCapability() {
        return windshieldHeatingCapability;
    }

    public Capability.AvailableGetState getRooftopControlCapability() {
        return rooftopControlCapability;
    }

    public ChassisCapabilities(byte[] chassisBytes) throws CommandParseException {
        super(Type.CHASSIS);
        windshieldHeatingCapability = Capability.getStateCapability(chassisBytes[0]);
        rooftopControlCapability = Capability.getStateCapability(chassisBytes[1]);
    }
}
