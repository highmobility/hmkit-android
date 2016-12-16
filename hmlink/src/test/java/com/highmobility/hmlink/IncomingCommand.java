package com.highmobility.hmlink;

import com.high_mobility.HMLink.Command.Command;
import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.ControlMode;
import com.high_mobility.HMLink.Command.Incoming.DeliveredParcels;
import com.high_mobility.HMLink.Command.Incoming.Failure;
import com.high_mobility.HMLink.Command.Incoming.LockState;
import com.high_mobility.HMLink.Command.Incoming.RooftopState;
import com.high_mobility.HMLink.Command.Incoming.TrunkState;
import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.VehicleFeature;

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

        com.high_mobility.HMLink.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.high_mobility.HMLink.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == DeliveredParcels.class);

        assertTrue(((DeliveredParcels)command).getDeliveredParcels().length == 2);
        assertTrue(((DeliveredParcels)command).getDeliveredParcels()[0].equals("4B87EFA8B4A6EC08"));
        assertTrue(((DeliveredParcels)command).getDeliveredParcels()[1].equals("4B87EFA8B4A6EC09"));
    }

    @Test
    public void failure_wakeup() {
        byte[] bytes = ByteUtils.bytesFromHex("00020100210001");

        com.high_mobility.HMLink.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.high_mobility.HMLink.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == Failure.class);
        assertTrue(Arrays.equals(((Failure)command).getFailedCommandIdentifier(), Command.TrunkAccess.GET_TRUNK_STATE.getMessageIdentifier()));
        assertTrue(((Failure)command).getFailedType() == Command.TrunkAccess.GET_TRUNK_STATE.getMessageType());
        assertTrue(((Failure)command).getFailureReason() == Failure.Reason.UNAUTHORIZED);
    }

    @Test
    public void controlMode_init() {
        byte[] bytes = ByteUtils.bytesFromHex("002701020032");

        com.high_mobility.HMLink.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.high_mobility.HMLink.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(((ControlMode)command).getMode() == ControlMode.Mode.STARTED);
        assertTrue(((ControlMode)command).getAngle() == 50);
    }

    @Test
    public void lockstate_init() {
        byte[] bytes = ByteUtils.bytesFromHex("00200100");

        com.high_mobility.HMLink.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.high_mobility.HMLink.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(((LockState)command).getState() == LockState.State.UNLOCKED);
    }

    @Test
    public void rooftopState_init_random() {
        byte[] bytes = ByteUtils.bytesFromHex("0025010135");

        com.high_mobility.HMLink.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.high_mobility.HMLink.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(((RooftopState)command).getDimmingPercentage() == .01f);
        assertTrue(((RooftopState)command).getOpenPercentage() == .53f);
    }

    @Test
    public void rooftopState_init_opaque() {
        byte[] bytes = ByteUtils.bytesFromHex("0025016400");

        com.high_mobility.HMLink.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.high_mobility.HMLink.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(((RooftopState)command).getDimmingPercentage() == 1f);
        assertTrue(((RooftopState)command).getOpenPercentage() == 0f);
    }

    @Test
    public void trunkState_init() {
        byte[] bytes = ByteUtils.bytesFromHex("0021010001");

        com.high_mobility.HMLink.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.high_mobility.HMLink.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(((TrunkState)command).getLockState() == TrunkState.LockState.UNLOCKED);
        assertTrue(((TrunkState)command).getPosition() == TrunkState.Position.OPEN);
    }
}
