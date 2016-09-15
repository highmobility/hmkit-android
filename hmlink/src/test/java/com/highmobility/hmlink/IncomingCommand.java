package com.highmobility.hmlink;

import com.high_mobility.HMLink.Command.Command;
import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.ControlMode;
import com.high_mobility.HMLink.Command.DeliveredParcels;
import com.high_mobility.HMLink.Command.LockState;
import com.high_mobility.HMLink.Command.RooftopState;
import com.high_mobility.HMLink.Command.VehicleStatus;
import com.high_mobility.HMLink.Command.WindshieldHeatingState;
import com.high_mobility.HMLink.Shared.ByteUtils;

import org.junit.Test;

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
        byte[] bytes = ByteUtils.bytesFromHex("002101");

        LockState command = null;

        try {
            command = new LockState(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getLockStatus() == Command.DigitalKey.LockStatus.LOCKED);
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
}
