package com.high_mobility.HMLink.Command.Capability;
import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;

/**
 * Created by ttiganik on 12/12/2016.
 */

public class AvailableCapability extends Capability {
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

    public AvailableCapability(VehicleStatus.Feature type, byte[] bytes) throws CommandParseException {
        super(type);
        if (bytes.length != 4) throw new CommandParseException();
        capability = Capability.fromByte(bytes[3]);
    }
}