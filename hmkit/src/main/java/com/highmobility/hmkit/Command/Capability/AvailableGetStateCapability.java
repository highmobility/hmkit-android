package com.highmobility.hmkit.Command.Capability;

import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Command;

/**
 * Created by ttiganik on 12/12/2016.
 */

public class AvailableGetStateCapability extends FeatureCapability {
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

    public AvailableGetStateCapability(Command.Identifier identifier, byte[] bytes) throws CommandParseException {
        super(identifier);
        if (bytes.length != 4) throw new CommandParseException();
        capability = Capability.fromByte(bytes[3]);
    }
}
