package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.Command.CommandParseException;

import java.util.Arrays;

/**
 * Created by ttiganik on 14/09/16.
 *
 * This command is sent when a Get Capabilities command is received by the car.
 */
public class Capability extends IncomingCommand {
    com.high_mobility.HMLink.Command.Capability.Capability capability;

    public com.high_mobility.HMLink.Command.Capability.Capability getCapability() {
        return capability;
    }

    public Capability(byte[] bytes) throws CommandParseException {
         super(bytes);
         if (bytes.length < 5) throw new CommandParseException();
         capability = com.high_mobility.HMLink.Command.Capability.Capability.fromBytes(Arrays.copyOfRange(bytes, 2, bytes.length));
    }
}
