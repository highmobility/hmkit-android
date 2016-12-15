package com.high_mobility.HMLink.Command.Capability;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.VehicleFeature;

/**
 * Created by ttiganik on 14/10/2016.
 */
public class FeatureCapability {
    VehicleFeature feature;

    FeatureCapability(VehicleFeature feature) {
        this.feature = feature;
    }

    public static FeatureCapability fromBytes(byte[] capabilityBytes) throws CommandParseException {
        if (capabilityBytes.length < 4) throw new CommandParseException();

        FeatureCapability featureCapability = null;
        VehicleFeature feature = VehicleFeature.fromIdentifier(capabilityBytes[0], capabilityBytes[1]);

        if (feature == VehicleFeature.DOOR_LOCKS ||
                feature == VehicleFeature.CHARGING ||
                feature == VehicleFeature.VALET_MODE) {
            featureCapability = new AvailableGetStateCapability(feature, capabilityBytes);
        }
        else if (feature == VehicleFeature.TRUNK_ACCESS) {
            featureCapability = new TrunkAccessCapability(capabilityBytes);
        }
        else if (feature == VehicleFeature.WAKE_UP
                || feature == VehicleFeature.REMOTE_CONTROL
                || feature == VehicleFeature.HEART_RATE
                || feature == VehicleFeature.VEHICLE_LOCATION
                || feature == VehicleFeature.NAVI_DESTINATION
                || feature == VehicleFeature.DELIVERED_PARCELS) {
            featureCapability =  new AvailableCapability(feature, capabilityBytes);
        }
        else if (feature == VehicleFeature.CLIMATE) {
            featureCapability = new ClimateCapability(capabilityBytes);
        }
        else if (feature == VehicleFeature.ROOFTOP) {
            featureCapability = new RooftopCapability(capabilityBytes);
        }
        else if (feature == VehicleFeature.HONK_FLASH) {
            featureCapability = new HonkFlashCapability(capabilityBytes);
        }

        return featureCapability;
    }

    public VehicleFeature getFeature() {
        return feature;
    }
}
