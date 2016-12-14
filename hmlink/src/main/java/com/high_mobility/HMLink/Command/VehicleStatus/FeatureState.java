package com.high_mobility.HMLink.Command.VehicleStatus;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class FeatureState {
    VehicleStatus.Feature feature;

    FeatureState(VehicleStatus.Feature feature) {
        this.feature = feature;
    }

    public static FeatureState fromBytes(byte[] bytes) throws CommandParseException {
        if (bytes.length < 3) throw new CommandParseException();
        FeatureState state = null;

        VehicleStatus.Feature feature = VehicleStatus.Feature.fromIdentifier(bytes[0], bytes[1]);

        if (feature == VehicleStatus.Feature.DOOR_LOCKS) {
            state = new DoorLocks(bytes);
        }
        else if (feature == VehicleStatus.Feature.TRUNK_ACCESS) {
            state = new TrunkAccess(bytes);
        }
        else if (feature == VehicleStatus.Feature.REMOTE_CONTROL) {
            state = new RemoteControl(bytes);
        }

        return state;
    }

    public VehicleStatus.Feature getFeature() {
        return feature;
    }
}
