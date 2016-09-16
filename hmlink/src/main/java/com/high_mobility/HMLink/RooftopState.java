package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 13/09/16.
 */
public class RooftopState extends Incoming {
    private boolean opaque;

    public boolean isOpaque() {
        return opaque;
    }

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

    public State getState() {
        return state;
    }
}
