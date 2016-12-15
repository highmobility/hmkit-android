package com.high_mobility.HMLink.Command.VehicleStatus;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;
import com.high_mobility.HMLink.Command.VehicleFeature;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class DoorLocks extends FeatureState {
    public enum State {
        LOCKED, UNLOCKED;

        static State fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNLOCKED;
                case 0x01: return LOCKED;
            }

            throw new CommandParseException();
        }
    }

    State state;

    DoorLocks(byte[] bytes) throws CommandParseException {
        super(VehicleFeature.DOOR_LOCKS);
        state = State.fromByte(bytes[3]);
    }

    public State getState() {
        return state;
    }
}
