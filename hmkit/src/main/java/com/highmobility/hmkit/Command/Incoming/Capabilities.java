package com.highmobility.hmkit.Command.Incoming;

import android.util.Log;

import com.highmobility.hmkit.Command.Capability.FeatureCapability;
import com.highmobility.hmkit.Command.CommandParseException;

import java.util.Arrays;

import static com.highmobility.hmkit.Command.Incoming.DeliveredParcels.TAG;

/**
 * Created by ttiganik on 14/09/16.
 *
 * This command is sent when a Get Capabilities command is received by the car.
 */
public class Capabilities extends IncomingCommand {
    FeatureCapability[] capabilites;

    public Capabilities(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 4) throw new CommandParseException();

        int capabilitiesCount = bytes[3];
        if (capabilitiesCount == 0) return;

        capabilites = new FeatureCapability[capabilitiesCount];
        int knownCapabilitesCount = 0;
        int capabilityPosition = 4;

        for (int i = 0; i < capabilitiesCount; i++) {
            int capabilityLength = bytes[capabilityPosition + 2];
            byte[] capabilityBytes = Arrays.copyOfRange(bytes, capabilityPosition,
                        capabilityPosition + 3 + capabilityLength); // length = 2x identifier byte + length byte + bytes
            FeatureCapability featureCapability = FeatureCapability.fromBytes(capabilityBytes);

            capabilites[i] = featureCapability;
            capabilityPosition += capabilityLength + 3;
            if (featureCapability != null) {
                knownCapabilitesCount++;
            }
            else {
                knownCapabilitesCount ++;
                knownCapabilitesCount --;
            }
        }

        if (capabilitiesCount != knownCapabilitesCount) {
            // resize the array if any of the capabilities is unknown(null)
            FeatureCapability[] trimmedCapabilites = new FeatureCapability[knownCapabilitesCount];
            int trimmedCapabilitesPosition = 0;
            for (int i = 0; i < capabilitiesCount; i++) {
                FeatureCapability featureCapability = capabilites[i];
                if (featureCapability != null) {
                    trimmedCapabilites[trimmedCapabilitesPosition] = featureCapability;
                    trimmedCapabilitesPosition++;
                }
            }

            capabilites = trimmedCapabilites;
        }
    }

    /**
     *
     * @return All of the Capabilities that are available for the vehicle.
     */
    public FeatureCapability[] getCapabilites() {
        return capabilites;
    }
}
