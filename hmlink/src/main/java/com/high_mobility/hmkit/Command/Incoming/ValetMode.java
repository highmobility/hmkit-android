package com.high_mobility.hmkit.Command.Incoming;

import com.high_mobility.hmkit.Command.CommandParseException;

/**
 * Created by ttiganik on 16/12/2016.
 *
 * This is an evented message that is sent from the car every time the valet mode changes.
 * This message is also sent when a Get Valet Mode message is received by the car.
 */
public class ValetMode extends IncomingCommand {
    boolean isActive;

    ValetMode(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length != 4) throw new CommandParseException();

        isActive = bytes[3] == 0x01;
    }

    /**
     *
     * @return Whether Valet Mode is active
     */
    public boolean isActive() {
        return isActive;
    }
}
