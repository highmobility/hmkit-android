package com.highmobility.hmkit;

import com.highmobility.hmkit.Command.AutoHvacState;
import com.highmobility.hmkit.Command.Command;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Constants;
import com.highmobility.hmkit.Command.DoorLockState;
import com.highmobility.hmkit.Command.Incoming.ChargeState;
import com.highmobility.hmkit.Command.Incoming.ClimateState;
import com.highmobility.hmkit.Command.Incoming.ControlMode;
import com.highmobility.hmkit.Command.Incoming.DeliveredParcels;
import com.highmobility.hmkit.Command.Incoming.Diagnostics;
import com.highmobility.hmkit.Command.Incoming.DriverFatigue;
import com.highmobility.hmkit.Command.Incoming.IgnitionState;
import com.highmobility.hmkit.Command.Incoming.Failure;
import com.highmobility.hmkit.Command.Incoming.Lights;
import com.highmobility.hmkit.Command.Incoming.LockState;
import com.highmobility.hmkit.Command.Incoming.Maintenance;
import com.highmobility.hmkit.Command.Incoming.Notification;
import com.highmobility.hmkit.Command.Incoming.NotificationAction;
import com.highmobility.hmkit.Command.Incoming.RooftopState;
import com.highmobility.hmkit.Command.Incoming.SendMessage;
import com.highmobility.hmkit.Command.Incoming.TrunkState;
import com.highmobility.hmkit.Command.Incoming.ValetMode;
import com.highmobility.hmkit.Command.Incoming.VehicleLocation;
import com.highmobility.hmkit.Command.Incoming.WindscreenState;
import com.highmobility.hmkit.Command.WindscreenDamagePosition;


import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
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

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == Failure.class);
        assertTrue(((Failure)command).getFailedType() == Command.TrunkAccess.GET_TRUNK_STATE);
        assertTrue(((Failure)command).getFailureReason() == Failure.Reason.UNAUTHORIZED);
    }


    @Test
    public void failure_vs() {
        byte[] bytes = ByteUtils.bytesFromHex("00020100100101");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == Failure.class);
        assertTrue(command.is(Command.FailureMessage.FAILURE_MESSAGE));
        assertTrue(((Failure)command).getFailedType() == Command.Capabilities.CAPABILITIES);
        assertTrue(((Failure)command).getFailureReason() == Failure.Reason.UNAUTHORIZED);
    }

    @Test
    public void controlMode_init() {
        byte[] bytes = ByteUtils.bytesFromHex("002701020032");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == ControlMode.class);
        assertTrue(((ControlMode)command).getMode() == ControlMode.Mode.STARTED);
        assertTrue(((ControlMode)command).getAngle() == 50);
    }

    @Test
    public void lockstate_init() {
        byte[] bytes = ByteUtils.bytesFromHex("00200104000100010000020001030001");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == LockState.class);

        assertTrue(((LockState)command).getFrontLeft() != null);
        assertTrue(((LockState)command).getFrontRight() != null);
        assertTrue(((LockState)command).getRearLeft() != null);
        assertTrue(((LockState)command).getRearRight() != null);

        assertTrue(((LockState)command).getFrontLeft().getPosition() == DoorLockState.DoorPosition.OPEN);
        assertTrue(((LockState)command).getFrontLeft().getLockState() == DoorLockState.LockState.UNLOCKED);

        assertTrue(((LockState)command).getFrontRight().getPosition() == DoorLockState.DoorPosition.CLOSED);
        assertTrue(((LockState)command).getFrontRight().getLockState() == DoorLockState.LockState.UNLOCKED);

        assertTrue(((LockState)command).getRearLeft().getPosition() == DoorLockState.DoorPosition.CLOSED);
        assertTrue(((LockState)command).getRearLeft().getLockState() == DoorLockState.LockState.LOCKED);

        assertTrue(((LockState)command).getRearRight().getPosition() == DoorLockState.DoorPosition.CLOSED);
        assertTrue(((LockState)command).getRearRight().getLockState() == DoorLockState.LockState.LOCKED);
    }

    @Test
    public void lockstate_two_front_doors() {
        byte[] bytes = ByteUtils.bytesFromHex("00200102000100010000");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == LockState.class);

        assertTrue(((LockState)command).getFrontLeft() != null);
        assertTrue(((LockState)command).getFrontRight() != null);
        assertTrue(((LockState)command).getRearLeft() == null);
        assertTrue(((LockState)command).getRearRight() == null);

        assertTrue(((LockState)command).getFrontLeft().getPosition() == DoorLockState.DoorPosition.OPEN);
        assertTrue(((LockState)command).getFrontLeft().getLockState() == DoorLockState.LockState.UNLOCKED);

        assertTrue(((LockState)command).getFrontRight().getPosition() == DoorLockState.DoorPosition.CLOSED);
        assertTrue(((LockState)command).getFrontRight().getLockState() == DoorLockState.LockState.UNLOCKED);
    }

    @Test
    public void rooftopState_init_random() {
        byte[] bytes = ByteUtils.bytesFromHex("0025010135");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
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

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
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

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
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

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
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
        byte[] bytes = ByteUtils.bytesFromHex("002401419800004140000041ac000041ac000001000041ac00006000000000000000000000071E071F");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
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
        assertTrue(((ClimateState)command).getDefrostingTemperature() == 21.5f);

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

    @Test
    public void valetMode() {
        byte[] bytes = ByteUtils.bytesFromHex("00280101");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == ValetMode.class);
        assertTrue(((ValetMode)command).isActive() == true);
    }

    @Test
    public void location() {
        byte[] bytes = ByteUtils.bytesFromHex("0030014252147d41567ab1");
        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == VehicleLocation.class);
        assertTrue(((VehicleLocation)command).getLatitude() == 52.520008f);
        assertTrue(((VehicleLocation)command).getLongitude() == 13.404954f);
    }

    @Test
    public void diagnostics() {
        byte[] bytes = ByteUtils.bytesFromHex("0033010249F00063003C09C45A0104004013d70a014013d70a02401666660340166666");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == Diagnostics.class);
        assertTrue(((Diagnostics)command).getMileage() == 150000);
        assertTrue(((Diagnostics)command).getOilTemperature() == 99);
        assertTrue(((Diagnostics)command).getSpeed() == 60);
        assertTrue(((Diagnostics)command).getRpm() == 2500);
        assertTrue(((Diagnostics)command).getFuelLevel() == .9f);
        assertTrue(((Diagnostics)command).getWasherFluidLevel() == Constants.WasherFluidLevel.FULL);
        assertTrue(((Diagnostics)command).getFrontLeftTirePressure() == 2.31f);
        assertTrue(((Diagnostics)command).getFrontRightTirePressure() == 2.31f);
        assertTrue(((Diagnostics)command).getRearLeftTirePressure() == 2.35f);
        assertTrue(((Diagnostics)command).getRearRightTirePressure() == 2.35f);
    }

    @Test
    public void maintenance() {
        byte[] bytes = ByteUtils.bytesFromHex("00340101F5000E61");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == Maintenance.class);
        assertTrue(((Maintenance)command).getDaysToNextService() == 501);
        assertTrue(((Maintenance)command).getKilometersToNextService() == 3681);
    }

    @Test
    public void engine() {
        byte[] bytes = ByteUtils.bytesFromHex("00350101");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == IgnitionState.class);
        assertTrue(((IgnitionState)command).isOn() == true);
    }

    @Test
    public void lights() {
        byte[] bytes = ByteUtils.bytesFromHex("003601020100FF0000");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == Lights.class);
        assertTrue(((Lights)command).getFrontExteriorLightState() == Constants.FrontExteriorLightState.ACTIVE_WITH_FULL_BEAM);
        assertTrue(((Lights)command).isRearExteriorLightActive() == true);
        assertTrue(((Lights)command).isInteriorLightActive() == false);
//        assertTrue(((Lights)command).getAmbientColor() == Color.RED); // Color is not mocked
    }

    @Test
    public void sendMessage() {
        byte[] bytes = ByteUtils.bytesFromHex("0037010e2b31203535352d3535352d3535350d48656c6c6f20796f7520746f6f");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == SendMessage.class);
        assertTrue(((SendMessage)command).getRecipientHandle().equals("+1 555-555-555"));
        assertTrue(((SendMessage)command).getText().equals("Hello you too"));
    }

    @Test
    public void notificationAction() {
        byte[] bytes = ByteUtils.bytesFromHex("003801FE");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == NotificationAction.class);
        assertTrue(((NotificationAction)command).getActionIdentifier() == -2);
    }


    @Test
    public void notification() {
        byte[] bytes = ByteUtils.bytesFromHex("003800115374617274206e617669676174696f6e3f0200024e6f0103596573");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == Notification.class);
        assertTrue(((Notification)command).getText().equals("Start navigation?"));
        assertTrue(((Notification)command).getNotificationActions().length == 2);
        assertTrue(((Notification)command).getNotificationActions()[0].getIdentifier() == 0);
        assertTrue(((Notification)command).getNotificationActions()[0].getText().equals("No"));
        assertTrue(((Notification)command).getNotificationActions()[1].getIdentifier() == 1);
        assertTrue(((Notification)command).getNotificationActions()[1].getText().equals("Yes"));
    }

    @Test
    public void windscreenState() {
        byte[] bytes = ByteUtils.bytesFromHex("0042010203024312025f11010a102005");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == WindscreenState.class);
        assertTrue(((WindscreenState)command).getWiperState() == WindscreenState.WiperState.AUTOMATIC);
        assertTrue(((WindscreenState)command).getWiperIntensity() == WindscreenState.WiperIntensity.LEVEL_3);
        assertTrue(((WindscreenState)command).getWindscreenDamage() == WindscreenState.WindscreenDamage.DAMAGE_SMALLER_THAN_1);
        assertTrue(((WindscreenState)command).getWindscreenReplacementState() == WindscreenState.WindscreenReplacementState.REPLACEMENT_NEEDED);
        WindscreenDamagePosition position = ((WindscreenState)command).getWindscreenDamagePosition();

        assertTrue(position.getWindscreenSizeHorizontal() == 4);
        assertTrue(position.getWindscreenSizeVertical() == 3);

        assertTrue(position.getDamagePositionX() == 1);
        assertTrue(position.getDamagePositionY() == 2);

        assertTrue(((WindscreenState)command).getDamageConfidence() == .95f);
        //2017-07-29T14:09:31+00:00
        String string = "2017-01-10T16:32:05";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date = format.parse(string);
            Date commandDate = ((WindscreenState)command).getDamageDetectionTime();
            assertTrue((format.format(commandDate).equals(format.format(date))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void fatigueLevel() {
        byte[] bytes = ByteUtils.bytesFromHex("00410101");

        com.highmobility.hmkit.Command.Incoming.IncomingCommand command = null;

        try {
            command = com.highmobility.hmkit.Command.Incoming.IncomingCommand.create(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getClass() == DriverFatigue.class);
        assertTrue(((DriverFatigue)command).getFatigueLevel() == DriverFatigue.FatigueLevel.PAUSE_RECOMMENDED);

    }
}
