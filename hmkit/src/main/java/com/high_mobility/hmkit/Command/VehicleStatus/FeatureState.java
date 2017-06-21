package com.high_mobility.hmkit.Command.VehicleStatus;

import com.high_mobility.hmkit.Command.CommandParseException;
import com.high_mobility.hmkit.Command.Command.Identifier;

import static com.high_mobility.hmkit.Command.Command.Identifier.CHARGING;
import static com.high_mobility.hmkit.Command.Command.Identifier.CLIMATE;
import static com.high_mobility.hmkit.Command.Command.Identifier.DIAGNOSTICS;
import static com.high_mobility.hmkit.Command.Command.Identifier.DOOR_LOCKS;
import static com.high_mobility.hmkit.Command.Command.Identifier.REMOTE_CONTROL;
import static com.high_mobility.hmkit.Command.Command.Identifier.ROOFTOP;
import static com.high_mobility.hmkit.Command.Command.Identifier.TRUNK_ACCESS;
import static com.high_mobility.hmkit.Command.Command.Identifier.VALET_MODE;
import static com.high_mobility.hmkit.Command.Command.Identifier.VEHICLE_LOCATION;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class FeatureState {
    Identifier feature;

    FeatureState(Identifier feature) {
        this.feature = feature;
    }

    public static FeatureState fromBytes(byte[] bytes) throws CommandParseException {
        if (bytes.length < 3) throw new CommandParseException();
        Identifier feature = Identifier.fromIdentifier(bytes[0], bytes[1]);

        if (feature == DOOR_LOCKS) return new DoorLocks(bytes);
        if (feature == TRUNK_ACCESS) return new TrunkAccess(bytes);
        if (feature == REMOTE_CONTROL) return new RemoteControl(bytes);
        if (feature == CHARGING) return new Charging(bytes);
        if (feature == CLIMATE) return new Climate(bytes);
        if (feature == VEHICLE_LOCATION) return new VehicleLocation(bytes);
        if (feature == VALET_MODE) return new ValetMode(bytes);
        if (feature == ROOFTOP) return new RooftopState(bytes);
        if (feature == DIAGNOSTICS) return new Diagnostics(bytes);

        return null;
    }

    public Identifier getFeature() {
        return feature;
    }
}
