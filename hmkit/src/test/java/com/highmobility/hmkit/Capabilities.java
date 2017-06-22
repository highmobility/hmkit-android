package com.highmobility.hmkit;


import com.highmobility.hmkit.Command.Capability.AvailableCapability;
import com.highmobility.hmkit.Command.Capability.AvailableGetStateCapability;
import com.highmobility.hmkit.Command.Capability.FeatureCapability;
import com.highmobility.hmkit.Command.Capability.ClimateCapability;
import com.highmobility.hmkit.Command.Capability.FuelingCapability;
import com.highmobility.hmkit.Command.Capability.HonkFlashCapability;
import com.highmobility.hmkit.Command.Capability.LightsCapability;
import com.highmobility.hmkit.Command.Capability.MessagingCapability;
import com.highmobility.hmkit.Command.Capability.NotificationsCapability;
import com.highmobility.hmkit.Command.Capability.RooftopCapability;
import com.highmobility.hmkit.Command.Capability.TrunkAccessCapability;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.Incoming.*;
import com.highmobility.hmkit.Command.Incoming.IncomingCommand;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by ttiganik on 13/12/2016.
 */

public class Capabilities {
    byte[] knownCapabilitiesBytes = ByteUtils.bytesFromHex("001001" +
            "19" + // length
            "002001010021020300002201000023010000240201000025020101002603010101002701010028010100290100003001010031010100320101" +
            "00330100" +
            "00340101" +
            "003603020201" +
            "0037020101" +
            "0038020000" +
            "00400101" +
            "00410101" +
            "0042020102" + // 22(0x16)
            "00430100" +
            "00440100" + // 24(0x18)
            "00450101" +
            "00350102");
    
    com.highmobility.hmkit.Command.Incoming.Capabilities capabilites = null;
    @Before
    public void setUp() {
        try {
            com.highmobility.hmkit.Command.Incoming.IncomingCommand command = IncomingCommand.create(knownCapabilitiesBytes);
            assertTrue(command.getClass() == com.highmobility.hmkit.Command.Incoming.Capabilities.class);
            capabilites = (com.highmobility.hmkit.Command.Incoming.Capabilities)command;
        } catch (CommandParseException e) {
            fail("capabilities init failed");
        }
    }

    @Test
    public void capabilities_init() {
        assertTrue(capabilites.getCapabilites() != null);
        assertTrue(capabilites.getCapabilites().length == 25);
    }

    @Test
    public void unknownCapabilities_init() {
        // 00 59 unknown
        byte[] unknownCapabilitiesBytes = ByteUtils.bytesFromHex("0010010D005901010021020300002201000023010000240201030025020101002603010101002701010028010100290100003001010031010100320101");
        com.highmobility.hmkit.Command.Incoming.Capabilities unknownCapabilities= null;

        try {
            unknownCapabilities = new com.highmobility.hmkit.Command.Incoming.Capabilities(unknownCapabilitiesBytes);
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.DOOR_LOCKS) {
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.TRUNK_ACCESS) {
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.WAKE_UP) {
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.CHARGING) {
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.CLIMATE) {
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.ROOFTOP) {
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.HONK_FLASH) {
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.REMOTE_CONTROL) {
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.VALET_MODE) {
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.HEART_RATE) {
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.VEHICLE_LOCATION) {
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.NAVI_DESTINATION) {
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
            if (iteratingFeatureCapability.getIdentifier() == Identifier.DELIVERED_PARCELS) {
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
    public void capabilites_init_diagnostics() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getIdentifier() == Identifier.DIAGNOSTICS) {
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
    public void capabilites_init_maintenance() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getIdentifier() == Identifier.MAINTENANCE) {
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
    public void capabilites_init_engine() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getIdentifier() == Identifier.ENGINE) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableGetStateCapability.class);
        assertTrue(((AvailableGetStateCapability) featureCapability).getCapability()
                    == AvailableGetStateCapability.Capability.GET_STATE_AVAILABLE);
    }

    @Test
    public void capabilites_init_lights() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getIdentifier() == Identifier.LIGHTS) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == LightsCapability.class);

        assertTrue(((LightsCapability) featureCapability).getExteriorLightsCapability()
                == AvailableGetStateCapability.Capability.GET_STATE_AVAILABLE);
        assertTrue(((LightsCapability) featureCapability).getInteriorLightsCapability()
                == AvailableGetStateCapability.Capability.GET_STATE_AVAILABLE);
        assertTrue(((LightsCapability) featureCapability).getAmbientLightsCapability()
                == AvailableCapability.Capability.AVAILABLE);

    }

    @Test
    public void capabilites_init_messaging() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getIdentifier() == Identifier.MESSAGING) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == MessagingCapability.class);

        assertTrue(((MessagingCapability) featureCapability).getMessageReceived()
                == AvailableCapability.Capability.AVAILABLE);
        assertTrue(((MessagingCapability) featureCapability).getSendMessage()
                == AvailableCapability.Capability.AVAILABLE);
    }

    @Test
    public void capabilites_init_notifications() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getIdentifier() == Identifier.NOTIFICATIONS) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == NotificationsCapability.class);

        assertTrue(((NotificationsCapability) featureCapability).getNotification()
                == AvailableCapability.Capability.UNAVAILABLE);
        assertTrue(((NotificationsCapability) featureCapability).getNotificationAction()
                == AvailableCapability.Capability.UNAVAILABLE);
    }

    @Test
    public void capabilites_init_fueling() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getIdentifier() == Identifier.FUELING) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == FuelingCapability.class);
        assertTrue(((FuelingCapability) featureCapability).getFuelCapCapability()
                == AvailableCapability.Capability.AVAILABLE);
    }

    @Test
    public void capabilites_init_driver_fatigue() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getIdentifier() == Identifier.DRIVER_FATIGUE) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableCapability.class);
        assertTrue(((AvailableCapability) featureCapability).getCapability()
                == AvailableCapability.Capability.AVAILABLE);
    }

    @Test
    public void capabilites_init_video_handover() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getIdentifier() == Identifier.VIDEO_HANDOVER) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableCapability.class);
        assertTrue(((AvailableCapability) featureCapability).getCapability()
                == AvailableCapability.Capability.UNAVAILABLE);
    }

    @Test
    public void capabilites_init_text_input() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getIdentifier() == Identifier.TEXT_INPUT) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableCapability.class);
        assertTrue(((AvailableCapability) featureCapability).getCapability()
                == AvailableCapability.Capability.UNAVAILABLE);
    }

    @Test
    public void capabilites_init_windows() {
        FeatureCapability featureCapability = null;
        for (int i = 0; i < capabilites.getCapabilites().length; i++) {
            FeatureCapability iteratingFeatureCapability = capabilites.getCapabilites()[i];
            if (iteratingFeatureCapability.getIdentifier() == Identifier.WINDOWS) {
                featureCapability = iteratingFeatureCapability;
                break;
            }
        }

        assertTrue(featureCapability != null);
        assertTrue(featureCapability.getClass() == AvailableCapability.class);
        assertTrue(((AvailableCapability) featureCapability).getCapability()
                == AvailableCapability.Capability.AVAILABLE);
    }

    // single capabilities

    @Test
    public void capability_init_climate() {
        byte[] message = ByteUtils.bytesFromHex("00130024020002");
        Capability capability = null;
        try {
            capability = new Capability(message);
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
        Capability capability = null;
        try {
            capability = new Capability(message);
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
