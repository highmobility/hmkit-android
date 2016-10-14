package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/10/2016.
 */

public class DigitalKeyCapabilities extends CapabilityType {
    public enum TrunkAccessCapability {
        UNAVAILABLE, AVAILABLE, GET_STATE_AVAILABLE, GET_STATE_LOCK_AVAILABLE, GET_STATE_POSITION_AVAILABLE
    }

    Capabilities.AvailableGetState doorLocksCapability;
    TrunkAccessCapability trunkAccessCapability;

    public Capabilities.AvailableGetState getDoorLocksCapability() {
        return doorLocksCapability;
    }

    public TrunkAccessCapability getTrunkAccessCapability() {
        return trunkAccessCapability;
    }

    public DigitalKeyCapabilities(byte[] digitalKeyBytes) throws CommandParseException{
        super(Type.DIGITAL_KEY);
        doorLocksCapability = Capabilities.getStateCapability(digitalKeyBytes[0]);

        switch (digitalKeyBytes[1]) {
            case 0x00: trunkAccessCapability = TrunkAccessCapability.UNAVAILABLE; break;
            case 0x01: trunkAccessCapability = TrunkAccessCapability.AVAILABLE; break;
            case 0x02: trunkAccessCapability = TrunkAccessCapability.GET_STATE_AVAILABLE; break;
            case 0x03: trunkAccessCapability = TrunkAccessCapability.GET_STATE_LOCK_AVAILABLE; break;
            case 0x04: trunkAccessCapability = TrunkAccessCapability.GET_STATE_POSITION_AVAILABLE; break;
        }
    }
}
