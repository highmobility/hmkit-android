package com.highmobility.hmlink;

import com.high_mobility.HMLink.CommandParseException;
import com.high_mobility.HMLink.ControlMode;
import com.high_mobility.HMLink.DeliveredParcels;
import com.high_mobility.HMLink.Failure;
import com.high_mobility.HMLink.LockState;
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
        assertTrue(true);
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
        byte[] bytes = ByteUtils.bytesFromHex("005B01");

        WindshieldHeatingState command = null;

        try {
            command = new WindshieldHeatingState(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.isActive() == true);
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

        assertTrue(command.isActive() == false);
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
        byte[] bytes = ByteUtils.bytesFromHex("***REMOVED***");

        VehicleStatus command = null;

        try {
            command = new VehicleStatus(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }
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
