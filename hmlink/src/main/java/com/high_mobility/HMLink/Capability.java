package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/09/16.
 *
 * This command is sent when a Get Capabilities command is received by the car.
 */
public class Capability extends IncomingCommand {
    public enum Available {
        UNAVAILABLE, AVAILABLE, UNSUPPORTED
    }

    public enum AvailableGetState {
        UNAVAILABLE, AVAILABLE, GET_STATE_AVAILABLE, UNSUPPORTED
    }


    public CapabilityType getCapabilityType() {
        return capabilityType;
    }

    CapabilityType capabilityType;

     public Capability(byte[] bytes) throws CommandParseException {
        super(bytes);
        capabilityType = CapabilityType.capabilityFromIncomingCapability(bytes);
    }

    static AvailableGetState getStateCapability(byte value) throws CommandParseException {
        switch (value) {
            case 0x00: return AvailableGetState.UNAVAILABLE;
            case 0x01: return AvailableGetState.AVAILABLE;
            case 0x02: return AvailableGetState.GET_STATE_AVAILABLE;
            case (byte)0xFF: return AvailableGetState.UNSUPPORTED;
            default: throw new CommandParseException();
        }
    }

    static Available availableCapability(byte value) throws CommandParseException {
        switch (value) {
            case 0x00: return Available.UNAVAILABLE;
            case 0x01: return Available.AVAILABLE;
            case (byte) 0xFF: return Available.UNSUPPORTED;
            default: throw new CommandParseException();
        }
    }
}
