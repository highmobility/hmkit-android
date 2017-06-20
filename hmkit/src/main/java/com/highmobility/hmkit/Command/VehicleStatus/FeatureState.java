package com.highmobility.hmkit.Command.VehicleStatus;

import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Command;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class FeatureState {
    Command.Identifier feature;

    FeatureState(Command.Identifier feature) {
        this.feature = feature;
    }

    public static FeatureState fromBytes(byte[] bytes) throws CommandParseException {
        if (bytes.length < 3) throw new CommandParseException();
        Command.Identifier feature = Command.Identifier.fromIdentifier(bytes[0], bytes[1]);

        if (feature == Command.Identifier.DOOR_LOCKS) return new DoorLocks(bytes);
        if (feature == Command.Identifier.TRUNK_ACCESS) return new TrunkAccess(bytes);
        if (feature == Command.Identifier.REMOTE_CONTROL) return new RemoteControl(bytes);
        if (feature == Command.Identifier.CHARGING) return new Charging(bytes);
        if (feature == Command.Identifier.CLIMATE) return new Climate(bytes);
        if (feature == Command.Identifier.VEHICLE_LOCATION) return new VehicleLocation(bytes);
        if (feature == Command.Identifier.VALET_MODE) return new ValetMode(bytes);
        if (feature == Command.Identifier.ROOFTOP) return new RooftopState(bytes);

        return null;
    }

    public Command.Identifier getFeature() {
        return feature;
    }
}
