package com.high_mobility.HMLink.Command.Capability;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;

/**
 * Created by ttiganik on 12/12/2016.
 */

public class TrunkAccessCapability extends Capability {
    /**
     * The possible lock positions
     */
    public enum LockCapability {
        UNAVAILABLE, AVAILABLE, GET_STATE_AVAILABLE, GET_STATE_UNLOCK_AVAILABLE;

        static LockCapability fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNAVAILABLE;
                case 0x01: return AVAILABLE;
                case 0x02: return GET_STATE_AVAILABLE;
                case 0x03: return GET_STATE_UNLOCK_AVAILABLE;
            }

            throw new CommandParseException();
        }
    }

    /**
     * The possible trunk positions
     */
    public enum PositionCapability {
        UNAVAILABLE, AVAILABLE, GET_STATE_AVAILABLE, GET_STATE_OPEN_AVAILABLE;

        static PositionCapability fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNAVAILABLE;
                case 0x01: return AVAILABLE;
                case 0x02: return GET_STATE_AVAILABLE;
                case 0x03: return GET_STATE_OPEN_AVAILABLE;
            }

            throw new CommandParseException();
        }
    }

    LockCapability lockCapability;
    PositionCapability position;

    /**
     * @return the current lock status of the trunk
     */
    public LockCapability getLockCapability() {
        return lockCapability;
    }

    /**
     *
     * @return the current position of the trunk
     */
    public PositionCapability getPositionCapability() {
        return position;
    }

    public TrunkAccessCapability(byte[] bytes) throws CommandParseException {
        super(VehicleStatus.Feature.TRUNK_ACCESS);
        if (bytes.length != 5) throw new CommandParseException();
        lockCapability = LockCapability.fromByte(bytes[3]);
        position = PositionCapability.fromByte(bytes[4]);
    }
}
