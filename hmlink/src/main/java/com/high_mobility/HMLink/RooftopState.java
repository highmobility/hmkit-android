package com.high_mobility.HMLink;

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
        TRANSPARENT, OPAQUE
    }

    State state;

    public RooftopState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 3) throw new CommandParseException();

        int stateByte = bytes[2];
        switch (stateByte) {
            case 0: state = State.TRANSPARENT; break;
            case 1: state = State.OPAQUE; break;
            default: throw new CommandParseException();
        }
    }

    /**
     *
     * @return the state of the rooftop
     */
    public State getState() {
        return state;
    }
}
