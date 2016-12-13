package com.high_mobility.HMLink.Command.Capability;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;

/**
 * Created by ttiganik on 12/12/2016.
 */

public class AvailableGetStateCapability extends StateCapability {
    public enum Capability {
        UNAVAILABLE, AVAILABLE, GET_STATE_AVAILABLE;

        public static Capability fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNAVAILABLE;
                case 0x01: return AVAILABLE;
                case 0x02: return GET_STATE_AVAILABLE;
            }

            throw new CommandParseException();
        }
    }

    Capability capability;

    public Capability getCapability() {
        return capability;
    }

    public AvailableGetStateCapability(VehicleStatus.State state, byte[] bytes) throws CommandParseException {
        super(state);
        if (bytes.length != 3) throw new CommandParseException();
        capability = Capability.fromByte(bytes[2]);
    }
}
