package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This is an evented command that is sent from the car every time the windshield heating state
 * changes. This command is also sent when a Get Windshield Heating State is received by the car.
 * The state is Active when the heating is turned on.
 */
public class WindshieldHeatingState extends IncomingCommand {
    State state;
    public enum State {
        ACTIVE, INACTIVE, UNSUPPORTED
    }

    public WindshieldHeatingState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 3) throw new CommandParseException();

        state = windshieldState(bytes[2]);
    }

    static State windshieldState(byte value) throws CommandParseException {

        switch (value) {
            case 0x00: return State.INACTIVE;
            case 0x01: return State.ACTIVE;
            case (byte)0xFF: return State.UNSUPPORTED;
        }

       throw new CommandParseException();
    }

    /**
     *
     * @return whether the windshield heating state is state or not
     */
    public State getState() {
        return state;
    }
}