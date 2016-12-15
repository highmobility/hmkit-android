package com.highmobility.hmlink;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.ControlMode;
import com.high_mobility.HMLink.Command.Incoming.DeliveredParcels;
import com.high_mobility.HMLink.Command.Incoming.Failure;
import com.high_mobility.HMLink.Command.Incoming.LockState;
import com.high_mobility.HMLink.Command.Incoming.RooftopState;
import com.high_mobility.HMLink.Command.Incoming.TrunkState;
import com.high_mobility.HMLink.ByteUtils;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by ttiganik on 15/09/16.
 */
public class IncomingCommand {
    @Test
    public void deliveredParcels_init() {
        /*
        0x00, 0x32, # MSB, LSB Message Identifier for Delivered Parcels
        0x01,       # Message Type for Delivered Parcels
        0x02,       # Two parcels in the car
        0x4B87EFA8B4A6EC08, # Tracking number for first parcel
        0x4B87EFA8B4A6EC09  # Tracking number for second parcel
         */
        byte[] bytes = ByteUtils.bytesFromHex("003201024B87EFA8B4A6EC084B87EFA8B4A6EC09");

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
        byte[] bytes = ByteUtils.bytesFromHex("002701020032");

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
        byte[] bytes = ByteUtils.bytesFromHex("00200100");

        LockState command = null;

        try {
            command = new LockState(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getState() == LockState.State.UNLOCKED);
    }

    @Test
    public void rooftopState_init_random() {
        byte[] bytes = ByteUtils.bytesFromHex("0025010135");

        RooftopState command = null;

        try {
            command = new RooftopState(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getDimmingPercentage() == .01f);
        assertTrue(command.getOpenPercentage() == .53f);
    }

    @Test
    public void rooftopState_init_opaque() {
        byte[] bytes = ByteUtils.bytesFromHex("0025016400");

        RooftopState command = null;

        try {
            command = new RooftopState(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getDimmingPercentage() == 1f);
        assertTrue(command.getOpenPercentage() == 0f);
    }

    @Test
    public void trunkState_init() {
        byte[] bytes = ByteUtils.bytesFromHex("0021010001");

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
        byte[] bytes = ByteUtils.bytesFromHex("0002002304");

        Failure command = null;

        try {
            command = new Failure(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(Arrays.equals(command.getFailedCommandIdentifier(), new byte[] {0x00, 0x23}));
        assertTrue(command.getFailureReason() == Failure.Reason.VEHICLE_ASLEEP);
    }

}
