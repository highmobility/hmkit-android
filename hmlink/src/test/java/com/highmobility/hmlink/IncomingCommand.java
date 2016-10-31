package com.highmobility.hmlink;

import com.high_mobility.HMLink.Capabilities;
import com.high_mobility.HMLink.Capability;
import com.high_mobility.HMLink.ChassisCapabilities;
import com.high_mobility.HMLink.CommandParseException;
import com.high_mobility.HMLink.ControlMode;
import com.high_mobility.HMLink.DeliveredParcels;
import com.high_mobility.HMLink.DigitalKeyCapabilities;
import com.high_mobility.HMLink.Failure;
import com.high_mobility.HMLink.HealthCapabilities;
import com.high_mobility.HMLink.LockState;
import com.high_mobility.HMLink.ParkingCapabilities;
import com.high_mobility.HMLink.RooftopState;
import com.high_mobility.HMLink.TrunkState;
import com.high_mobility.HMLink.VehicleStatus;
import com.high_mobility.HMLink.WindshieldHeatingState;
import com.high_mobility.HMLink.ByteUtils;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by ttiganik on 15/09/16.
 */
public class IncomingCommand {
    @Test
    public void capabilities_init() {
        byte[] bytes = ByteUtils.bytesFromHex("10110104000101000001");

        Capabilities capabilities= null;

        try {
            capabilities = new Capabilities(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(capabilities.getDigitalKeyCapabilities().getDoorLocksCapability() == Capability.AvailableGetState.AVAILABLE);
        assertTrue(capabilities.getDigitalKeyCapabilities().getTrunkAccessCapability() == DigitalKeyCapabilities.TrunkAccessCapability.GET_STATE_UNLOCK_AVAILABLE);
        assertTrue(capabilities.getChassisCapabilities().getWindshieldHeatingCapability() == Capability.AvailableGetState.UNAVAILABLE);
        assertTrue(capabilities.getChassisCapabilities().getRooftopControlCapability() == Capability.AvailableGetState.AVAILABLE);
        assertTrue(capabilities.getParkingCapabilities().getRemoteControlCapability() == Capability.Available.AVAILABLE);
        assertTrue(capabilities.getHealthCapabilities().getHeartRateCapability() == Capability.Available.UNAVAILABLE);
        assertTrue(capabilities.getPoiCapabilities().getSetDestinationCapability() == Capability.Available.UNAVAILABLE);
        assertTrue(capabilities.getParcelDeliveryCapabilities().getDeliveredParcelsCapability() == Capability.Available.AVAILABLE);
    }

    @Test
    public void capability_init_chassis() {
        byte[] bytes = ByteUtils.bytesFromHex("1013110100");

        Capability capability= null;

        try {
            capability = new Capability(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(capability.getCapabilityType().getClass() == ChassisCapabilities.class);
        assertTrue(((ChassisCapabilities)capability.getCapabilityType()).getWindshieldHeatingCapability()
                == Capability.AvailableGetState.AVAILABLE);

        assertTrue(((ChassisCapabilities)capability.getCapabilityType()).getRooftopControlCapability()
                == Capability.AvailableGetState.UNAVAILABLE);
    }

    @Test
    public void capability_init_digital_key() {
        byte[] bytes = ByteUtils.bytesFromHex("1013100006");

        Capability capability= null;

        try {
            capability = new Capability(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(capability.getCapabilityType().getClass() == DigitalKeyCapabilities.class);
        assertTrue(((DigitalKeyCapabilities)capability.getCapabilityType()).getDoorLocksCapability()
                == Capability.AvailableGetState.UNAVAILABLE);

        assertTrue(((DigitalKeyCapabilities)capability.getCapabilityType()).getTrunkAccessCapability()
                == DigitalKeyCapabilities.TrunkAccessCapability.GET_STATE_OPEN_AVAILABLE);
    }

    @Test
    public void capability_init_parking() {
        byte[] bytes = ByteUtils.bytesFromHex("10131201");

        Capability capability= null;

        try {
            capability = new Capability(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(capability.getCapabilityType().getClass() == ParkingCapabilities.class);
        assertTrue(((ParkingCapabilities)capability.getCapabilityType()).getRemoteControlCapability()
                == Capability.Available.AVAILABLE);
    }

    @Test
    public void capability_init_health() {
        byte[] bytes = ByteUtils.bytesFromHex("10131300");

        Capability capability= null;

        try {
            capability = new Capability(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(capability.getCapabilityType().getClass() == HealthCapabilities.class);
        assertTrue(((HealthCapabilities)capability.getCapabilityType()).getHeartRateCapability()
                == Capability.Available.UNAVAILABLE);
    }

    @Test
    public void deliveredParcels_init() {
        byte[] bytes = ByteUtils.bytesFromHex("***REMOVED***");

        DeliveredParcels parcels = null;

        try {
            parcels = new DeliveredParcels(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(parcels.getDeliveredParcels().length == 2);
        assertTrue(parcels.getDeliveredParcels()[0].equals("4B87EFA8B4A6EC08"));
        assertTrue(parcels.getDeliveredParcels()[1].equals("4B87EFA8B4A6EC09"));
    }

    @Test
    public void controlMode_init() {
        byte[] bytes = ByteUtils.bytesFromHex("0042020032");

        ControlMode controlMode = null;

        try {
            controlMode = new ControlMode(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(controlMode.getMode() == ControlMode.Mode.STARTED);
        assertTrue(controlMode.getAngle() == 50);
    }

    @Test
    public void lockstate_init() {
        byte[] bytes = ByteUtils.bytesFromHex("002100");

        LockState command = null;

        try {
            command = new LockState(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getState() == LockState.State.UNLOCKED);
    }

    @Test
    public void windshieldHeatingState_init_active() {
        byte[] bytes = ByteUtils.bytesFromHex("005BFF");

        WindshieldHeatingState command = null;

        try {
            command = new WindshieldHeatingState(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getState() == WindshieldHeatingState.State.UNSUPPORTED);
    }

    @Test
    public void windshieldHeatingState_init_inactive() {
        byte[] bytes = ByteUtils.bytesFromHex("005B00");

        WindshieldHeatingState command = null;

        try {
            command = new WindshieldHeatingState(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getState() == WindshieldHeatingState.State.INACTIVE);
    }

    @Test
    public void rooftopState_init_opaque() {
        byte[] bytes = ByteUtils.bytesFromHex("005E01");

        RooftopState command = null;

        try {
            command = new RooftopState(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getState() == RooftopState.State.OPAQUE);
    }

    @Test
    public void rooftopState_init_transparent() {
        byte[] bytes = ByteUtils.bytesFromHex("005E00");

        RooftopState command = null;

        try {
            command = new RooftopState(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getState() == RooftopState.State.TRANSPARENT);
    }

    @Test
    public void vehicleStatus_init() {
        byte[] bytes = ByteUtils.bytesFromHex("0015000100FF0002");

        VehicleStatus command = null;

        try {
            command = new VehicleStatus(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getDoorLockState() == LockState.State.UNLOCKED);
        assertTrue(command.getTrunkLockState() == TrunkState.LockState.LOCKED);
        assertTrue(command.getTrunkPosition() == TrunkState.Position.CLOSED);
        assertTrue(command.getWindshieldHeatingState() == WindshieldHeatingState.State.UNSUPPORTED);
        assertTrue(command.getRooftopState() == RooftopState.State.TRANSPARENT);
        assertTrue(command.getRemoteControlMode() == ControlMode.Mode.STARTED);
    }

    @Test
    public void trunkState_init() {
        byte[] bytes = ByteUtils.bytesFromHex("00240001");

        TrunkState command = null;

        try {
            command = new TrunkState(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getLockState() == TrunkState.LockState.UNLOCKED);
        assertTrue(command.getPosition() == TrunkState.Position.OPEN);
    }

    @Test
    public void failure_init() {
        byte[] bytes = ByteUtils.bytesFromHex("0002002302");

        Failure command = null;

        try {
            command = new Failure(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(Arrays.equals(command.getFailedCommandIdentifier(), new byte[] {0x00, 0x23}));
        assertTrue(command.getFailureReason() == Failure.Reason.INCORRECT_STATE);
    }

}
