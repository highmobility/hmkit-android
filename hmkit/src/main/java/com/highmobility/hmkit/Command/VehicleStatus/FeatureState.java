package com.highmobility.hmkit.Command.VehicleStatus;

import com.highmobility.hmkit.Command.Command;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Command.Identifier;

import static com.highmobility.hmkit.Command.Command.Identifier.CHARGING;
import static com.highmobility.hmkit.Command.Command.Identifier.CLIMATE;
import static com.highmobility.hmkit.Command.Command.Identifier.DIAGNOSTICS;
import static com.highmobility.hmkit.Command.Command.Identifier.DOOR_LOCKS;
import static com.highmobility.hmkit.Command.Command.Identifier.ENGINE;
import static com.highmobility.hmkit.Command.Command.Identifier.LIGHTS;
import static com.highmobility.hmkit.Command.Command.Identifier.MAINTENANCE;
import static com.highmobility.hmkit.Command.Command.Identifier.REMOTE_CONTROL;
import static com.highmobility.hmkit.Command.Command.Identifier.ROOFTOP;
import static com.highmobility.hmkit.Command.Command.Identifier.THEFT_ALARM;
import static com.highmobility.hmkit.Command.Command.Identifier.TRUNK_ACCESS;
import static com.highmobility.hmkit.Command.Command.Identifier.VALET_MODE;
import static com.highmobility.hmkit.Command.Command.Identifier.VEHICLE_LOCATION;

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
        else if (feature == TRUNK_ACCESS) return new TrunkAccess(bytes);
        else if (feature == REMOTE_CONTROL) return new RemoteControl(bytes);
        else if (feature == CHARGING) return new Charging(bytes);
        else if (feature == CLIMATE) return new Climate(bytes);
        else if (feature == VEHICLE_LOCATION) return new VehicleLocation(bytes);
        else if (feature == VALET_MODE) return new ValetMode(bytes);
        else if (feature == ROOFTOP) return new RooftopState(bytes);
        else if (feature == DIAGNOSTICS) return new Diagnostics(bytes);
        else if (feature == MAINTENANCE) return new Maintenance(bytes);
        else if (feature == ENGINE) return new Engine(bytes);
        else if (feature == LIGHTS) return new Lights(bytes);
        else if (feature == THEFT_ALARM) return new TheftAlarm(bytes);

        return null;
    }

    public Identifier getFeature() {
        return feature;
    }
}
