package com.high_mobility.HMLink.Command.Incoming;
import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.VehicleStatus.FeatureState;

import java.util.Arrays;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This command is sent when a Get Vehicle Status command is received by the car.
 *
 */
public class VehicleStatus extends IncomingCommand {
    FeatureState[] featureStates;

    public VehicleStatus(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length < 3) throw new CommandParseException();

        int stateCount = bytes[2];
        if (stateCount == 0) return;

        featureStates = new FeatureState[stateCount];
        int knownStatesCount = 0;
        int statePosition = 3;

        for (int i = 0; i < stateCount; i++) {
            int stateLength = bytes[statePosition + 2];
            byte[] stateBytes = Arrays.copyOfRange(bytes, statePosition,
                    statePosition + 3 + stateLength); // length = 2x identifier byte + length byte + bytes
            FeatureState state = FeatureState.fromBytes(stateBytes);

            featureStates[i] = state;
            statePosition += stateLength + 3;
            if (state != null) knownStatesCount++;
        }

        if (stateCount != knownStatesCount) {
            // resize the array if any of the capabilities is unknown(null)
            FeatureState[] trimmedStates = new FeatureState[knownStatesCount];
            int trimmedStatesPosition = 0;
            for (int i = 0; i < stateCount; i++) {
                FeatureState state = featureStates[i];
                if (state != null) {
                    trimmedStates[trimmedStatesPosition] = state;
                    trimmedStatesPosition++;
                }
            }

            featureStates = trimmedStates;
        }
    }

    public FeatureState[] getFeatureStates() {
        return featureStates;
    }
}
