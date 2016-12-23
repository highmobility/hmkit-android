package com.high_mobility.HMLink.Command.Capability;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Command.Identifier;

/**
 * Created by ttiganik on 13/12/2016.
 */

public class RooftopCapability extends FeatureCapability {
    public enum DimmingCapability {
        UNAVAILABLE, AVAILABLE, GET_STATE_AVAILABLE, ONLY_OPAQUE_OR_TRANSPARENT;

        public static DimmingCapability fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNAVAILABLE;
                case 0x01: return AVAILABLE;
                case 0x02: return GET_STATE_AVAILABLE;
                case 0x03: return ONLY_OPAQUE_OR_TRANSPARENT;
            }

            throw new CommandParseException();
        }
    }

    public enum OpenCloseCapability {
        UNAVAILABLE, AVAILABLE, GET_STATE_AVAILABLE, ONLY_FULLY_OPEN_OR_CLOSED;

        public static OpenCloseCapability fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNAVAILABLE;
                case 0x01: return AVAILABLE;
                case 0x02: return GET_STATE_AVAILABLE;
                case 0x03: return ONLY_FULLY_OPEN_OR_CLOSED;
            }

            throw new CommandParseException();
        }
    }

    OpenCloseCapability openCloseCapability;
    DimmingCapability dimmingCapability;

    public DimmingCapability getDimmingCapability() {
        return dimmingCapability;
    }

    public OpenCloseCapability getOpenCloseCapability() {
        return openCloseCapability;
    }

    public RooftopCapability(byte[] bytes) throws CommandParseException {
        super(Identifier.ROOFTOP);

        if (bytes.length != 5) throw new CommandParseException();
        dimmingCapability = DimmingCapability.fromByte(bytes[3]);
        openCloseCapability = OpenCloseCapability.fromByte(bytes[4]);
    }

}
