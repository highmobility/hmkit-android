package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/10/2016.
 */

public class ChassisCapabilities extends CapabilityType {

    Capabilities.AvailableGetState windshieldHeatingCapability;

    Capabilities.AvailableGetState rooftopControlCapability;

    public Capabilities.AvailableGetState getWindshieldHeatingCapability() {
        return windshieldHeatingCapability;
    }

    public Capabilities.AvailableGetState getRooftopControlCapability() {
        return rooftopControlCapability;
    }

    public ChassisCapabilities(byte[] chassisBytes) throws CommandParseException {
        super(Type.CHASSIS);
        windshieldHeatingCapability = Capabilities.getStateCapability(chassisBytes[0]);
        rooftopControlCapability = Capabilities.getStateCapability(chassisBytes[1]);
    }
}
