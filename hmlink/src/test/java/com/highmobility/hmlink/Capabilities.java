package com.highmobility.hmlink;

import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.Capability.AvailableCapability;
import com.high_mobility.HMLink.Command.Capability.AvailableGetStateCapability;
import com.high_mobility.HMLink.Command.Capability.Capability;
import com.high_mobility.HMLink.Command.Capability.ClimateCapability;
import com.high_mobility.HMLink.Command.Capability.HonkFlashCapability;
import com.high_mobility.HMLink.Command.Capability.RooftopCapability;
import com.high_mobility.HMLink.Command.Capability.TrunkAccessCapability;
import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by ttiganik on 13/12/2016.
 */

public class Capabilities {
    byte[] knownCapabilitiesBytes = ByteUtils.bytesFromHex("00110D002001010021020300002201000023010000240201030025020101002603010101002701010028010100290100003001010031010100320101");
    com.high_mobility.HMLink.Command.Incoming.Capabilities capabilites = null;

    @Before
    public void setUp() {
        try {
            capabilites = new com.high_mobility.HMLink.Command.Incoming.Capabilities(knownCapabilitiesBytes);
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
        byte[] unknownCapabilitiesBytes = ByteUtils.bytesFromHex("00110D005901010021020300002201000023010000240201030025020101002603010101002701010028010100290100003001010031010100320101");
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
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.DOOR_LOCKS) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == AvailableGetStateCapability.class);
        if (capability.getClass() == AvailableGetStateCapability.class) {
            assertTrue(((AvailableGetStateCapability)capability).getCapability() == AvailableGetStateCapability.Capability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_trunk_access() {
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.TRUNK_ACCESS) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == TrunkAccessCapability.class);
        if (capability.getClass() == TrunkAccessCapability.class) {
            assertTrue(((TrunkAccessCapability)capability).getLockCapability() == TrunkAccessCapability.LockCapability.GET_STATE_UNLOCK_AVAILABLE);
            assertTrue(((TrunkAccessCapability)capability).getPositionCapability() == TrunkAccessCapability.PositionCapability.UNAVAILABLE);
        }
    }

    @Test
    public void capabilites_init_wake_up() {
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.WAKE_UP) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == AvailableCapability.class);
        if (capability.getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability)capability).getCapability() == AvailableCapability.Capability.UNAVAILABLE);
        }
    }

    @Test
    public void capabilites_init_charging() {
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.CHARGING) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == AvailableGetStateCapability.class);
        if (capability.getClass() == AvailableGetStateCapability.class) {
            assertTrue(((AvailableGetStateCapability)capability).getCapability() == AvailableGetStateCapability.Capability.UNAVAILABLE);
        }
    }

    @Test
    public void capabilites_init_climate() {
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.CLIMATE) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == ClimateCapability.class);
        if (capability.getClass() == ClimateCapability.class) {
            assertTrue(((ClimateCapability)capability).getClimateCapability() == AvailableGetStateCapability.Capability.AVAILABLE);
            assertTrue(((ClimateCapability)capability).getProfileCapability() == ClimateCapability.ProfileCapability.NO_SCHEDULING);
        }
    }

    @Test
    public void capabilites_init_rooftop() {
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.ROOFTOP) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == RooftopCapability.class);
        if (capability.getClass() == RooftopCapability.class) {
            assertTrue(((RooftopCapability)capability).getDimmingCapability() == RooftopCapability.DimmingCapability.AVAILABLE);
            assertTrue(((RooftopCapability)capability).getOpenCloseCapability() == RooftopCapability.OpenCloseCapability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_honkflash() {
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.HONK_FLASH) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == HonkFlashCapability.class);
        if (capability.getClass() == HonkFlashCapability.class) {
            assertTrue(((HonkFlashCapability)capability).getHonkHornCapability() == AvailableCapability.Capability.AVAILABLE);
            assertTrue(((HonkFlashCapability)capability).getFlashLightsCapability() == AvailableCapability.Capability.AVAILABLE);
            assertTrue(((HonkFlashCapability)capability).getEmergencyFlasherCapability() == AvailableCapability.Capability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_remote_control() {
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.REMOTE_CONTROL) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == AvailableCapability.class);
        if (capability.getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability)capability).getCapability() == AvailableCapability.Capability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_valet_mode() {
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.VALET_MODE) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == AvailableGetStateCapability.class);
        if (capability.getClass() == AvailableGetStateCapability.class) {
            assertTrue(((AvailableGetStateCapability)capability).getCapability() == AvailableGetStateCapability.Capability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_heart_rate() {
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.HEART_RATE) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == AvailableCapability.class);
        if (capability.getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability)capability).getCapability() == AvailableCapability.Capability.UNAVAILABLE);
        }
    }

    @Test
    public void capabilites_init_vehicle_location() {
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.VEHICLE_LOCATION) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == AvailableCapability.class);
        if (capability.getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability)capability).getCapability() == AvailableCapability.Capability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_navi_destination() {
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.NAVI_DESTINATION) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == AvailableCapability.class);
        if (capability.getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability)capability).getCapability() == AvailableCapability.Capability.AVAILABLE);
        }
    }

    @Test
    public void capabilites_init_delivered_parcels() {
        Capability capability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            Capability iteratingCapability = capabilites.getCapabilites()[i];
            if (iteratingCapability.getFeature() == VehicleStatus.Feature.DELIVERED_PARCELS) {
                capability = iteratingCapability;
                break;
            }
        }

        assertTrue(capability != null);
        assertTrue(capability.getClass() == AvailableCapability.class);
        if (capability.getClass() == AvailableCapability.class) {
            assertTrue(((AvailableCapability)capability).getCapability() == AvailableCapability.Capability.AVAILABLE);
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
