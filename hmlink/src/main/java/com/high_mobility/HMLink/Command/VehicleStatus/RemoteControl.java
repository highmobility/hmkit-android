package com.high_mobility.HMLink.Command.VehicleStatus;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;
import com.high_mobility.HMLink.Command.VehicleFeature;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class RemoteControl extends FeatureState {
    public enum State {
        UNAVAILABLE, AVAILABLE, STARTED;

        static State fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return UNAVAILABLE;
                case 0x01: return AVAILABLE;
                case 0x02: return STARTED;
            }

            throw new CommandParseException();
        }
    }

    State state;

    public State getState() {
        return state;
    }

    RemoteControl(byte[] bytes) throws CommandParseException {
        super(VehicleFeature.REMOTE_CONTROL);

        if (bytes.length != 4) throw new CommandParseException();
        state = State.fromByte(bytes[3]);
    }
}
