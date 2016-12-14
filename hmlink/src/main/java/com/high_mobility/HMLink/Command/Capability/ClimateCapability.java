package com.high_mobility.HMLink.Command.Capability;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;

/**
 * Created by ttiganik on 12/12/2016.
 */

public class ClimateCapability extends Capability {
    public enum ProfileCapability {
        UNAVAILABLE, AVAILABLE, GET_STATE_AVAILABLE, NO_SCHEDULING;

        public static ProfileCapability fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNAVAILABLE;
                case 0x01: return AVAILABLE;
                case 0x02: return GET_STATE_AVAILABLE;
                case 0x03: return NO_SCHEDULING;
            }

            throw new CommandParseException();
        }
    }

    AvailableGetStateCapability.Capability climateCapability;
    ProfileCapability profileCapability;

    public AvailableGetStateCapability.Capability getClimateCapability() {
        return climateCapability;
    }

    public ProfileCapability getProfileCapability() {
        return profileCapability;
    }

    public ClimateCapability(byte[] bytes) throws CommandParseException {
        super(VehicleStatus.Feature.CLIMATE);

        if (bytes.length != 5) throw new CommandParseException();

        climateCapability = AvailableGetStateCapability.Capability.fromByte(bytes[3]);
        profileCapability = ProfileCapability.fromByte(bytes[4]);
    }
}
