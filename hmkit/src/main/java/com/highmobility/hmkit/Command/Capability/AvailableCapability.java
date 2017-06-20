package com.highmobility.hmkit.Command.Capability;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Command;

/**
 * Created by ttiganik on 12/12/2016.
 */

public class AvailableCapability extends FeatureCapability {
    public enum Capability {
        UNAVAILABLE, AVAILABLE;

        static Capability fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return Capability.UNAVAILABLE;
                case 0x01: return Capability.AVAILABLE;
            }

            throw new CommandParseException();
        }
    }

    Capability capability;

    public Capability getCapability() {
        return capability;
    }

    public AvailableCapability(Command.Identifier feature, byte[] bytes) throws CommandParseException {
        super(feature);
        if (bytes.length != 4) throw new CommandParseException();
        capability = Capability.fromByte(bytes[3]);
    }
}
