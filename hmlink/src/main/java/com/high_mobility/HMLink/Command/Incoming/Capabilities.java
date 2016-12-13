package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.Command.Capability.StateCapability;
import com.high_mobility.HMLink.Command.CommandParseException;

import java.util.Arrays;

/**
 * Created by ttiganik on 14/09/16.
 *
 * This command is sent when a Get Capabilities command is received by the car.
 */
public class Capabilities extends IncomingCommand {
    StateCapability[] capabilites;

    public Capabilities(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 3) throw new CommandParseException();

        int capabilitiesCount = bytes[2];
        if (capabilitiesCount == 0) return;

        capabilites = new StateCapability[capabilitiesCount];
        int knownCapabilitesCount = 0;
        int capabilityPosition = 3;

        for (int i = 0; i < capabilitiesCount; i++) {
            int capabilityLength = bytes[capabilityPosition];
            // use the capability bytes that are after the length byte
            byte[] capabilityBytes = Arrays.copyOfRange(bytes, capabilityPosition + 1,
                        capabilityPosition + 1 + capabilityLength);
            StateCapability capability = StateCapability.fromBytes(capabilityBytes);

            capabilites[i] = capability;
            capabilityPosition += capabilityLength + 1;
            if (capability != null) knownCapabilitesCount++;
        }

        if (capabilitiesCount != knownCapabilitesCount) {
            // resize the array if any of the capabilities is unknown(null)
            StateCapability[] trimmedCapabilites = new StateCapability[knownCapabilitesCount];
            int trimmedCapabilitesPosition = 0;
            for (int i = 0; i < capabilitiesCount; i++) {
                StateCapability capability = capabilites[i];
                if (capability != null) {
                    trimmedCapabilites[trimmedCapabilitesPosition] = capability;
                    trimmedCapabilitesPosition++;
                }
            }

            capabilites = trimmedCapabilites;
        }
    }

    public StateCapability[] getCapabilites() {
        return capabilites;
    }

    static boolean capabilityIs(byte[] bytes, int position, VehicleStatus.State type) {
        return bytes[position] == type.getIdentifier()[0]
                && bytes[position + 1] == type.getIdentifier()[1];
    }
}
