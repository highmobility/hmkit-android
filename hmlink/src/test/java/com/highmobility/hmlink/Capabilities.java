package com.highmobility.hmlink;

import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.Capability.AvailableCapability;
import com.high_mobility.HMLink.Command.Capability.AvailableGetStateCapability;
import com.high_mobility.HMLink.Command.Capability.FeatureCapability;
import com.high_mobility.HMLink.Command.Capability.ClimateCapability;
import com.high_mobility.HMLink.Command.Capability.HonkFlashCapability;
import com.high_mobility.HMLink.Command.Capability.RooftopCapability;
import com.high_mobility.HMLink.Command.Capability.TrunkAccessCapability;
import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Command.Identifier;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by ttiganik on 13/12/2016.
 */

public class Capabilities {
    byte[] knownCapabilitiesBytes = ByteUtils.bytesFromHex("0010010D002001010021020300002201000023010000240201000025020101002603010101002701010028010100290100003001010031010100320101");
    com.high_mobility.HMLink.Command.Incoming.Capabilities capabilites = null;

    @Before
    public void setUp() {
        try {
            com.high_mobility.HMLink.Command.Incoming.IncomingCommand command = com.high_mobility.HMLink.Command.Incoming.IncomingCommand.create(knownCapabilitiesBytes);
            assertTrue(command.getClass() == com.high_mobility.HMLink.Command.Incoming.Capabilities.class);
            capabilites = (com.high_mobility.HMLink.Command.Incoming.Capabilities)command;
        } catch (CommandParseException e) {
            fail("capabilities init failed");
        }
    }

    @Test
    public void capabilities_init() {
        assertTrue(capabilites.getCapabilites() != null);
        assertTrue(capabilites.getCapabilites().length == 13);
    }

    @Test
    public void unknownCapabilities_init() {
        // 00 59 unknown
        byte[] unknownCapabilitiesBytes = ByteUtils.bytesFromHex("0010010D005901010021020300002201000023010000240201030025020101002603010101002701010028010100290100003001010031010100320101");
        com.high_mobility.HMLink.Command.Incoming.Capabilities unknownCapabilities= null;

        try {
            unknownCapabilities = new com.high_mobility.HMLink.Command.Incoming.Capabilities(unknownCapabilitiesBytes);
        } catch (CommandParseException e) {
            fail("unknowncapabilities init failed");
        }

        assertTrue(unknownCapabilities.getCapabilites().length == 12);
        for (int i = 0; i < unknownCapabilities.getCapabilites().length; i++) {
            assertTrue(unknownCapabilities.getCapabilites()[i] != null);
        }
    }

    @Test
    public void capabilites_init_door_locks() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.DOOR_LOCKS) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableGetStateCapability.class);
        if (featureCapability.getClass() == AvailableGetStateCapability.class) {
            assertTrue(((AvailableGetStateCapability) featureCapability).getCapability() == AvailableGetStateCapability.Capability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_trunk_access() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.TRUNK_ACCESS) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == TrunkAccessCapability.class);
        if (featureCapability.getClass() == TrunkAccessCapability.class) {
            assertTrue(((TrunkAccessCapability) featureCapability).getLockCapability() == TrunkAccessCapability.LockCapability.GET_STATE_UNLOCK_AVAILABLE);
            assertTrue(((TrunkAccessCapability) featureCapability).getPositionCapability() == TrunkAccessCapability.PositionCapability.UNAVAILABLE);
        }
    }

    @Test
    public void capabilites_init_wake_up() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.WAKE_UP) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableCapability.class);
        if (featureCapability.getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability) featureCapability).getCapability() == AvailableCapability.Capability.UNAVAILABLE);
        }
    }

    @Test
    public void capabilites_init_charging() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.CHARGING) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableGetStateCapability.class);
        if (featureCapability.getClass() == AvailableGetStateCapability.class) {
            assertTrue(((AvailableGetStateCapability) featureCapability).getCapability() == AvailableGetStateCapability.Capability.UNAVAILABLE);
        }
    }

    @Test
    public void capabilites_init_climate() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.CLIMATE) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == ClimateCapability.class);
        if (featureCapability.getClass() == ClimateCapability.class) {
            assertTrue(((ClimateCapability) featureCapability).getClimateCapability() == AvailableGetStateCapability.Capability.AVAILABLE);
            assertTrue(((ClimateCapability) featureCapability).getProfileCapability() == ClimateCapability.ProfileCapability.UNAVAILABLE);
        }
    }

    @Test
    public void capabilites_init_rooftop() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.ROOFTOP) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == RooftopCapability.class);
        if (featureCapability.getClass() == RooftopCapability.class) {
            assertTrue(((RooftopCapability) featureCapability).getDimmingCapability() == RooftopCapability.DimmingCapability.AVAILABLE);
            assertTrue(((RooftopCapability) featureCapability).getOpenCloseCapability() == RooftopCapability.OpenCloseCapability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_honkflash() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.HONK_FLASH) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == HonkFlashCapability.class);
        if (featureCapability.getClass() == HonkFlashCapability.class) {
            assertTrue(((HonkFlashCapability) featureCapability).getHonkHornCapability() == AvailableCapability.Capability.AVAILABLE);
            assertTrue(((HonkFlashCapability) featureCapability).getFlashLightsCapability() == AvailableCapability.Capability.AVAILABLE);
            assertTrue(((HonkFlashCapability) featureCapability).getEmergencyFlasherCapability() == AvailableCapability.Capability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_remote_control() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.REMOTE_CONTROL) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableCapability.class);
        if (featureCapability.getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability) featureCapability).getCapability() == AvailableCapability.Capability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_valet_mode() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.VALET_MODE) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableGetStateCapability.class);
        if (featureCapability.getClass() == AvailableGetStateCapability.class) {
            assertTrue(((AvailableGetStateCapability) featureCapability).getCapability() == AvailableGetStateCapability.Capability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_heart_rate() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.HEART_RATE) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableCapability.class);
        if (featureCapability.getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability) featureCapability).getCapability() == AvailableCapability.Capability.UNAVAILABLE);
        }
    }

    @Test
    public void capabilites_init_vehicle_location() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.VEHICLE_LOCATION) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableCapability.class);
        if (featureCapability.getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability) featureCapability).getCapability() == AvailableCapability.Capability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_navi_destination() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.NAVI_DESTINATION) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableCapability.class);
        if (featureCapability.getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability) featureCapability).getCapability() == AvailableCapability.Capability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_delivered_parcels() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getFeature() == Identifier.DELIVERED_PARCELS) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableCapability.class);
        if (featureCapability.getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability) featureCapability).getCapability() == AvailableCapability.Capability.AVAILABLE);
        }
    }

    // single capabilities

    @Test
    public void capability_init_climate() {
        byte[] message = ByteUtils.bytesFromHex("00130024020002");
        com.high_mobility.HMLink.Command.Incoming.Capability capability = null;
        try {
            capability = new com.high_mobility.HMLink.Command.Incoming.Capability(message);
        } catch (CommandParseException e) {
            fail("climate capability init failed");
            e.printStackTrace();
        }

        assertTrue(capability.getCapability().getClass() == ClimateCapability.class);

        if (capability.getCapability().getClass() == ClimateCapability.class) {
            assertTrue(((ClimateCapability)capability.getCapability()).getClimateCapability() ==
                    AvailableGetStateCapability.Capability.UNAVAILABLE);
            assertTrue(((ClimateCapability)capability.getCapability()).getProfileCapability() ==
                    ClimateCapability.ProfileCapability.GET_STATE_AVAILABLE);
        }
    }

    @Test
    public void capability_init_heartrate() {
        byte[] message = ByteUtils.bytesFromHex("001300290101");
        com.high_mobility.HMLink.Command.Incoming.Capability capability = null;
        try {
            capability = new com.high_mobility.HMLink.Command.Incoming.Capability(message);
        } catch (CommandParseException e) {
            fail("climate capability init failed");
            e.printStackTrace();
        }

        assertTrue(capability.getCapability().getClass() == AvailableCapability.class);

        if (capability.getCapability().getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability)capability.getCapability()).getCapability() ==
                    AvailableCapability.Capability.AVAILABLE);
        }
    }
}
