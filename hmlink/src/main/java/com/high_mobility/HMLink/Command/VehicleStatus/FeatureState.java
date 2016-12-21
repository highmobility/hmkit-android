package com.high_mobility.HMLink.Command.VehicleStatus;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;
import com.high_mobility.HMLink.Command.VehicleFeature;

import static com.high_mobility.HMLink.Command.VehicleFeature.CHARGING;
import static com.high_mobility.HMLink.Command.VehicleFeature.CLIMATE;
import static com.high_mobility.HMLink.Command.VehicleFeature.DOOR_LOCKS;
import static com.high_mobility.HMLink.Command.VehicleFeature.REMOTE_CONTROL;
import static com.high_mobility.HMLink.Command.VehicleFeature.ROOFTOP;
import static com.high_mobility.HMLink.Command.VehicleFeature.TRUNK_ACCESS;
import static com.high_mobility.HMLink.Command.VehicleFeature.VALET_MODE;
import static com.high_mobility.HMLink.Command.VehicleFeature.VEHICLE_LOCATION;

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
        VehicleFeature feature = VehicleFeature.fromIdentifier(bytes[0], bytes[1]);

        if (feature == DOOR_LOCKS) return new DoorLocks(bytes);
        if (feature == TRUNK_ACCESS) return new TrunkAccess(bytes);
        if (feature == REMOTE_CONTROL) return new RemoteControl(bytes);
        if (feature == CHARGING) return new Charging(bytes);
        if (feature == CLIMATE) return new Climate(bytes);
        if (feature == VEHICLE_LOCATION) return new VehicleLocation(bytes);
        if (feature == VALET_MODE) return new ValetMode(bytes);
        if (feature == ROOFTOP) return new RooftopState(bytes);

        return null;
    }

    public VehicleFeature getFeature() {
        return feature;
    }
}
