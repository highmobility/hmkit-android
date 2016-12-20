package com.highmobility.hmlink;

import com.high_mobility.HMLink.Command.AutoHvacState;
import com.high_mobility.HMLink.Command.Command;
import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Constants;
import com.high_mobility.HMLink.Command.Incoming.ChargeState;
import com.high_mobility.HMLink.Command.Incoming.ClimateState;
import com.high_mobility.HMLink.Command.Incoming.ControlMode;
import com.high_mobility.HMLink.Command.Incoming.DeliveredParcels;
import com.high_mobility.HMLink.Command.Incoming.Failure;
import com.high_mobility.HMLink.Command.Incoming.LockState;
import com.high_mobility.HMLink.Command.Incoming.RooftopState;
import com.high_mobility.HMLink.Command.Incoming.TrunkState;
import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.VehicleFeature;
import com.high_mobility.HMLink.Command.VehicleStatus.Charging;
import com.high_mobility.HMLink.Command.VehicleStatus.Climate;

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

        assertTrue(command.getClass() == ControlMode.class);
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

        assertTrue(command.getClass() == LockState.class);
        assertTrue(((LockState)command).getState() == Constants.LockState.UNLOCKED);
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

        assertTrue(command.getClass() == RooftopState.class);
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

        assertTrue(command.getClass() == RooftopState.class);
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

        assertTrue(command.getClass() == TrunkState.class);
        assertTrue(((TrunkState)command).getLockState() == Constants.TrunkLockState.UNLOCKED);
        assertTrue(((TrunkState)command).getPosition() == Constants.TrunkPosition.OPEN);
    }


    @Test
    public void charging_init() {
        byte[] bytes = ByteUtils.bytesFromHex("0023010200FF32BF19999A01905A003C3F5EB85201");

        com.high_mobility.HMLink.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.high_mobility.HMLink.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == ChargeState.class);
        assertTrue(((ChargeState)command).getChargingState() == Constants.ChargingState.CHARGING);
        assertTrue(((ChargeState)command).getEstimatedRange() == 255f);
        assertTrue(((ChargeState)command).getBatteryLevel() == .5f);
        assertTrue(((ChargeState)command).getChargerVoltage() == 400f);
        assertTrue(((ChargeState)command).getChargeLimit() == .9f);
        assertTrue(((ChargeState)command).getTimeToCompleteCharge() == 60f);
        assertTrue(((ChargeState)command).getChargingRate() == .87f);
        assertTrue(((ChargeState)command).getBatteryCurrent() == -.6f);
        assertTrue(((ChargeState)command).getChargePortState() == Constants.ChargePortState.OPEN);
    }

    @Test
    public void climate() {
        byte[] bytes = ByteUtils.bytesFromHex("002401419800004140000041ac000041ac00000100006000000000000000000000071E071F");

        com.high_mobility.HMLink.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.high_mobility.HMLink.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == ClimateState.class);
        assertTrue(((ClimateState)command).getInsideTemperature() == 19f);
        assertTrue(((ClimateState)command).getOutsideTemperature() == 12f);
        assertTrue(((ClimateState)command).getDriverTemperatureSetting() == 21.5f);
        assertTrue(((ClimateState)command).getPassengerTemperatureSetting() == 21.5f);

        assertTrue(((ClimateState)command).isHvacActive() == true);
        assertTrue(((ClimateState)command).isDefoggingActive() == false);
        assertTrue(((ClimateState)command).isDefrostingActive() == false);

        assertTrue(((ClimateState)command).isAutoHvacConstant() == false);
        AutoHvacState[] autoHvacStates = ((ClimateState)command).getAutoHvacStates();
        assertTrue(autoHvacStates != null);
        assertTrue(autoHvacStates.length == 7);

        assertTrue(autoHvacStates[0].isActive() == false);

        assertTrue(autoHvacStates[5].isActive() == true);
        assertTrue(autoHvacStates[5].getDay() == 5);
        assertTrue(autoHvacStates[5].getStartHour() == 7);
        assertTrue(autoHvacStates[5].getStartMinute() == 30);

        assertTrue(autoHvacStates[6].isActive() == true);
        assertTrue(autoHvacStates[6].getDay() == 6);
        assertTrue(autoHvacStates[6].getStartHour() == 7);
        assertTrue(autoHvacStates[6].getStartMinute() == 31);
    }
}
