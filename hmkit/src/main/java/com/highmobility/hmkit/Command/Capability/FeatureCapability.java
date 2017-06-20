package com.highmobility.hmkit.Command.Capability;

import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Command;

/**
 * Created by ttiganik on 14/10/2016.
 */
public class FeatureCapability {
    Command.Identifier identifier;

    FeatureCapability(Command.Identifier identifier) {
        this.identifier = identifier;
    }

    public static FeatureCapability fromBytes(byte[] capabilityBytes) throws CommandParseException {
        if (capabilityBytes.length < 4) throw new CommandParseException();

        FeatureCapability featureCapability = null;
        Command.Identifier feature = Command.Identifier.fromIdentifier(capabilityBytes[0], capabilityBytes[1]);

        if (feature == Command.Identifier.DOOR_LOCKS ||
                feature == Command.Identifier.CHARGING ||
                feature == Command.Identifier.VALET_MODE) {
            featureCapability = new AvailableGetStateCapability(feature, capabilityBytes);
        }
        else if (feature == Command.Identifier.TRUNK_ACCESS) {
            featureCapability = new TrunkAccessCapability(capabilityBytes);
        }
        else if (feature == Command.Identifier.WAKE_UP
                || feature == Command.Identifier.REMOTE_CONTROL
                || feature == Command.Identifier.HEART_RATE
                || feature == Command.Identifier.VEHICLE_LOCATION
                || feature == Command.Identifier.NAVI_DESTINATION
                || feature == Command.Identifier.DELIVERED_PARCELS) {
            featureCapability =  new AvailableCapability(feature, capabilityBytes);
        }
        else if (feature == Command.Identifier.CLIMATE) {
            featureCapability = new ClimateCapability(capabilityBytes);
        }
        else if (feature == Command.Identifier.ROOFTOP) {
            featureCapability = new RooftopCapability(capabilityBytes);
        }
        else if (feature == Command.Identifier.HONK_FLASH) {
            featureCapability = new HonkFlashCapability(capabilityBytes);
        }

        return featureCapability;
    }

    public Command.Identifier getIdentifier() {
        return identifier;
    }
}
