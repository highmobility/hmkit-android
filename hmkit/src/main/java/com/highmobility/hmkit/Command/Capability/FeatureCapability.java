package com.highmobility.hmkit.Command.Capability;

import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.CommandParseException;

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

        if (feature == Identifier.DOOR_LOCKS
                || feature == Identifier.CHARGING
                || feature == Identifier.VALET_MODE
                || feature == Identifier.ENGINE) {
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
                || feature == Identifier.DELIVERED_PARCELS
                || feature == Identifier.DIAGNOSTICS
                || feature == Identifier.MAINTENANCE
                || feature == Identifier.DRIVER_FATIGUE
                || feature == Identifier.VIDEO_HANDOVER
                || feature == Identifier.TEXT_INPUT
                || feature == Identifier.WINDOWS) {
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
        else if (feature == Identifier.LIGHTS) {
            featureCapability = new LightsCapability(capabilityBytes);
        }
        else if (feature == Identifier.MESSAGING) {
            featureCapability = new MessagingCapability(capabilityBytes);
        }
        else if (feature == Identifier.NOTIFICATIONS) {
            featureCapability = new NotificationsCapability(capabilityBytes);
        }
        else if (feature == Identifier.FUELING) {
            featureCapability = new FuelingCapability(capabilityBytes);
        }
        else if (feature == Identifier.WINDSCREEN) {
            featureCapability = new WindscreenCapability(capabilityBytes);
        }

        return featureCapability;
    }

    public Identifier getIdentifier() {
        return identifier;
    }
}
