package com.high_mobility.HMLink.Command.Capability;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;

/**
 * Created by ttiganik on 14/10/2016.
 */
public class Capability {
    VehicleStatus.Feature feature;

    Capability(VehicleStatus.Feature feature) {
        this.feature = feature;
    }

    public static Capability fromBytes(byte[] capabilityBytes) throws CommandParseException {
        if (capabilityBytes.length < 4) throw new CommandParseException();

        Capability capability = null;
        VehicleStatus.Feature feature = VehicleStatus.Feature.fromIdentifier(capabilityBytes[0], capabilityBytes[1]);

        if (feature == VehicleStatus.Feature.DOOR_LOCKS ||
                feature == VehicleStatus.Feature.CHARGING ||
                feature == VehicleStatus.Feature.VALET_MODE) {
            capability = new AvailableGetStateCapability(feature, capabilityBytes);
        }
        else if (feature == VehicleStatus.Feature.TRUNK_ACCESS) {
            capability = new TrunkAccessCapability(capabilityBytes);
        }
        else if (feature == VehicleStatus.Feature.WAKE_UP
                || feature == VehicleStatus.Feature.REMOTE_CONTROL
                || feature == VehicleStatus.Feature.HEART_RATE
                || feature == VehicleStatus.Feature.VEHICLE_LOCATION
                || feature == VehicleStatus.Feature.NAVI_DESTINATION
                || feature == VehicleStatus.Feature.DELIVERED_PARCELS) {
            capability =  new AvailableCapability(feature, capabilityBytes);
        }
        else if (feature == VehicleStatus.Feature.CLIMATE) {
            capability = new ClimateCapability(capabilityBytes);
        }
        else if (feature == VehicleStatus.Feature.ROOFTOP) {
            capability = new RooftopCapability(capabilityBytes);
        }
        else if (feature == VehicleStatus.Feature.HONK_FLASH) {
            capability = new HonkFlashCapability(capabilityBytes);
        }

        return capability;
    }

    public VehicleStatus.Feature getFeature() {
        return feature;
    }
}
