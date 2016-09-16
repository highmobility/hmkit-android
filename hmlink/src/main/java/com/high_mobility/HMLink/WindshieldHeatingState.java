package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 13/09/16.
 */
public class WindshieldHeatingState extends Incoming {
    // TODO:
    boolean active;

    public WindshieldHeatingState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 3) throw new CommandParseException();

        active = bytes[2] != 0x00;
    }

    public boolean isActive() {
        return active;
    }
}
