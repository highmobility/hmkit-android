package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.Command.Capability.FeatureCapability;
import com.high_mobility.HMLink.Command.CommandParseException;

import java.util.Arrays;

/**
 * Created by ttiganik on 14/09/16.
 *
 * This command is sent when a Get Capabilities command is received by the car.
 */
public class Capability extends IncomingCommand {
    FeatureCapability capability;

    public FeatureCapability getCapability() {
        return capability;
    }

    public Capability(byte[] bytes) throws CommandParseException {
         super(bytes);
         if (bytes.length < 5) throw new CommandParseException();
         capability = FeatureCapability.fromBytes(Arrays.copyOfRange(bytes, 2, bytes.length));
    }
}
