package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.Command.CommandParseException;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This is an evented command that is sent from the car every time the rooftop state changes.
 * It is also sent when a Get Rooftop State is received by the car.
 */
public class RooftopState extends IncomingCommand {
    /**
     * The possible states of the rooftop.
     */
    public enum State {
        TRANSPARENT, OPAQUE, UNSUPPORTED;

        static State stateForByte(byte value) throws CommandParseException {
            switch (value) {
                case 0: return State.TRANSPARENT;
                case 1: return State.OPAQUE;
                case (byte)0xFF: return State.UNSUPPORTED;
                default: throw new CommandParseException();
            }
        }
    }

    State state;

    public RooftopState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 3) throw new CommandParseException();

        state = State.stateForByte(bytes[2]);
    }

    /**
     *
     * @return the state of the rooftop
     */
    public State getState() {
        return state;
    }
}
