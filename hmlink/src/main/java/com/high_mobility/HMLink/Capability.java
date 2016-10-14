package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/09/16.
 *
 * This command is sent when a Get Capabilities command is received by the car.
 */
public class Capability extends IncomingCommand {
    public Capability(byte[] bytes) throws CommandParseException {
        super(bytes); // TODO:
    }
}
