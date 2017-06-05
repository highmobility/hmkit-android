package com.high_mobility.HMLink.Command.Capability;

import com.high_mobility.HMLink.Command.Command.Identifier;
import com.high_mobility.HMLink.Command.CommandParseException;

/**
 * Created by ttiganik on 14/10/2016.
 */
public class FeatureCapability {
    Identifier identifier;

    FeatureCapability(Identifier identifier) {
        this.identifier = identifier;
    }

    public static FeatureCapability fromBytes(byte[] capabilityBytes) throws CommandParseException {
        if (capabilityBytes.length < 4) throw new CommandParseException();

        FeatureCapability featureCapability = null;
        Identifier feature = Identifier.fromIdentifier(capabilityBytes[0], capabilityBytes[1]);

        if (feature == Identifier.DOOR_LOCKS ||
                feature == Identifier.CHARGING ||
                feature == Identifier.VALET_MODE) {
            featureCapability = new AvailableGetStateCapability(feature, capabilityBytes);
        }
        else if (feature == Identifier.TRUNK_ACCESS) {
            featureCapability = new TrunkAccessCapability(capabilityBytes);
        }
        else if (feature == Identifier.WAKE_UP
                || feature == Identifier.REMOTE_CONTROL
                || feature == Identifier.HEART_RATE
                || feature == Identifier.VEHICLE_LOCATION
                || feature == Identifier.NAVI_DESTINATION
                || feature == Identifier.DELIVERED_PARCELS) {
            featureCapability =  new AvailableCapability(feature, capabilityBytes);
        }
        else if (feature == Identifier.CLIMATE) {
            featureCapability = new ClimateCapability(capabilityBytes);
        }
        else if (feature == Identifier.ROOFTOP) {
            featureCapability = new RooftopCapability(capabilityBytes);
        }
        else if (feature == Identifier.HONK_FLASH) {
            featureCapability = new HonkFlashCapability(capabilityBytes);
        }

        return featureCapability;
    }

    public Identifier getIdentifier() {
        return identifier;
    }
}
