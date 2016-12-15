package com.high_mobility.HMLink.Command.VehicleStatus;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;
import com.high_mobility.HMLink.Command.VehicleFeature;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class FeatureState {
    VehicleFeature feature;

    FeatureState(VehicleFeature feature) {
        this.feature = feature;
    }

    public static FeatureState fromBytes(byte[] bytes) throws CommandParseException {
        if (bytes.length < 3) throw new CommandParseException();
        FeatureState state = null;

        VehicleFeature feature = VehicleFeature.fromIdentifier(bytes[0], bytes[1]);

        if (feature == VehicleFeature.DOOR_LOCKS) {
            state = new DoorLocks(bytes);
        }
        else if (feature == VehicleFeature.TRUNK_ACCESS) {
            state = new TrunkAccess(bytes);
        }
        else if (feature == VehicleFeature.REMOTE_CONTROL) {
            state = new RemoteControl(bytes);
        }

        return state;
    }

    public VehicleFeature getFeature() {
        return feature;
    }
}
