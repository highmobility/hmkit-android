package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.Command.Capability.Capability;
import com.high_mobility.HMLink.Command.CommandParseException;

import java.util.Arrays;

/**
 * Created by ttiganik on 14/09/16.
 *
 * This command is sent when a Get Capabilities command is received by the car.
 */
public class Capabilities extends IncomingCommand {
    Capability[] capabilites;

    public Capabilities(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 3) throw new CommandParseException();

        int capabilitiesCount = bytes[2];
        if (capabilitiesCount == 0) return;

        capabilites = new Capability[capabilitiesCount];
        int knownCapabilitesCount = 0;
        int capabilityPosition = 3;

        for (int i = 0; i < capabilitiesCount; i++) {
            int capabilityLength = bytes[capabilityPosition + 2];
            byte[] capabilityBytes = Arrays.copyOfRange(bytes, capabilityPosition,
                        capabilityPosition + 3 + capabilityLength); // length = 2x identifier byte + length byte + bytes
            Capability capability = Capability.fromBytes(capabilityBytes);

            capabilites[i] = capability;
            capabilityPosition += capabilityLength + 3;
            if (capability != null) knownCapabilitesCount++;
        }

        if (capabilitiesCount != knownCapabilitesCount) {
            // resize the array if any of the capabilities is unknown(null)
            Capability[] trimmedCapabilites = new Capability[knownCapabilitesCount];
            int trimmedCapabilitesPosition = 0;
            for (int i = 0; i < capabilitiesCount; i++) {
                Capability capability = capabilites[i];
                if (capability != null) {
                    trimmedCapabilites[trimmedCapabilitesPosition] = capability;
                    trimmedCapabilitesPosition++;
                }
            }

            capabilites = trimmedCapabilites;
        }
    }

    public Capability[] getCapabilites() {
        return capabilites;
    }
}
