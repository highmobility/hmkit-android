package com.highmobility.hmkit;

import android.graphics.Color;

import com.highmobility.hmkit.Command.AutoHvacState;
import com.highmobility.hmkit.Command.Command;
import com.highmobility.hmkit.Command.Constants;
import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.Incoming.TrunkState;
import com.highmobility.hmkit.Command.Incoming.WindscreenState;
import com.highmobility.hmkit.Command.NotificationAction;
import com.highmobility.hmkit.Command.WindowState;
import com.highmobility.hmkit.Command.WindscreenDamagePosition;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by ttiganik on 15/09/16.
 */
public class OutgoingCommand {
    @Test
    public void getCapability() {
        String waitingForBytes = "0010020021";
        String commandBytes = ByteUtils.hexFromBytes(Command.Capabilities.getCapability(Identifier.TRUNK_ACCESS));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void getVehicleStatus() {
        String waitingForBytes = "001100";
        String commandBytes = ByteUtils.hexFromBytes(Command.VehicleStatus.getVehicleStatus());
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void lockUnlockDoors_lock() {
        String waitingForBytes = "00200201";
        String commandBytes = ByteUtils.hexFromBytes(Command.DoorLocks.lockDoors(true));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void setRooftopTransparency_opaque() {
        String waitingForBytes = "0025020064";
        String commandBytes = ByteUtils.hexFromBytes(Command.RooftopControl.controlRooftop(0f, 1f));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void setRooftopTransparency_random() {
        String waitingForBytes = "002502340B";
        String commandBytes = ByteUtils.hexFromBytes(Command.RooftopControl.controlRooftop(.52f, .11f));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void controlCommand() {
        String waitingForBytes = "002704030032";
        String commandBytes = ByteUtils.hexFromBytes(Command.RemoteControl.controlCommand(3, 50));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void getTrunkState() {
        String waitingForBytes = "002100";
        String commandBytes = ByteUtils.hexFromBytes(Command.TrunkAccess.getTrunkState());
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void setTrunkState() {
        String waitingForBytes = "0021020001";
        String commandBytes = ByteUtils.hexFromBytes(Command.TrunkAccess.setTrunkState(TrunkState.LockState.UNLOCKED, TrunkState.Position.OPEN));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void getDeliveredParcels() {
        String waitingForBytes = "003200";
        String commandBytes = ByteUtils.hexFromBytes(Command.DeliveredParcels.getDeliveredParcels());
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void setDestination() {
        String waitingForBytes = "0031004252147D41567AB1064265726C696E";

        String commandBytes = null;
        try {
            commandBytes = ByteUtils.hexFromBytes(Command.NaviDestination.setDestination(52.520008f, 13.404954f, "Berlin"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void wakeUp() {
        String waitingForBytes = "002202";
        String commandBytes = ByteUtils.hexFromBytes(Command.WakeUp.wakeUp());
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void getChargeState() {
        String waitingForBytes = "002300";
        String commandBytes = ByteUtils.hexFromBytes(Command.Charging.getChargeState());
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void startCharging() {
        String waitingForBytes = "00230201";
        String commandBytes = ByteUtils.hexFromBytes(Command.Charging.startCharging(true));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void setChargeLimit() {
        String waitingForBytes = "0023035A";
        String commandBytes = ByteUtils.hexFromBytes(Command.Charging.setChargeLimit(.9f));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void honkFlash() {
        String waitingForBytes = "0026000103";
        String commandBytes = ByteUtils.hexFromBytes(Command.HonkFlash.honkFlash(1, 3));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test(expected=IllegalArgumentException.class)
    public void honkFlashInvalidParameter() {
        ByteUtils.hexFromBytes(Command.HonkFlash.honkFlash(6, 11));
    }

    @Test(expected=IllegalArgumentException.class)
    public void honkFlashInvalidParameterTwo() {
        ByteUtils.hexFromBytes(Command.HonkFlash.honkFlash(-1, -1));
    }

    @Test
    public void emergencyFlasher() {
        String waitingForBytes = "00260101";
        String commandBytes = ByteUtils.hexFromBytes(Command.HonkFlash.startEmergencyFlasher(true));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void getClimateState() {
        String waitingForBytes = "002400";
        String commandBytes = ByteUtils.hexFromBytes(Command.Climate.getClimateState());
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void getDiagnosticsState() {
        String waitingForBytes = "003300";
        String commandBytes = ByteUtils.hexFromBytes(Command.Diagnostics.getDiagnosticsState());
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void setClimateProfile() {
        String waitingForBytes = "0024026000000000000000000000071E071E41ac000041ac0000";
        AutoHvacState[] states = new AutoHvacState[7];
        for (int i = 0; i < 7; i++) {
            AutoHvacState state;
            if (i < 5)
                state = new AutoHvacState(false, i, 0, 0);
            else
                state = new AutoHvacState(true, i, 7, 30);

            states[i] = state;
        }

        boolean autoHvacConstant = false;
        float driverTemp = 21.5f;
        float passengerTemp = 21.5f;
        String commandBytes = ByteUtils.hexFromBytes(Command.Climate.setClimateProfile(states, autoHvacConstant, driverTemp, passengerTemp));
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void startStopHVAC() {
        String waitingForBytes = "00240301";
        String commandBytes = ByteUtils.hexFromBytes(Command.Climate.startHvac(true));
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void startStopDefog() {
        String waitingForBytes = "00240401";
        String commandBytes = ByteUtils.hexFromBytes(Command.Climate.startDefog(true));
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void startStopDefrost() {
        String waitingForBytes = "00240501";
        String commandBytes = ByteUtils.hexFromBytes(Command.Climate.startDefrost(true));
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void getValetMode() {
        String waitingForBytes = "002800";
        String commandBytes = ByteUtils.hexFromBytes(Command.ValetMode.getValetMode());
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void activateValetMode() {
        String waitingForBytes = "00280201";
        String commandBytes = ByteUtils.hexFromBytes(Command.ValetMode.activateValetMode(true));
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void getLocation() {
        String waitingForBytes = "003000";
        String commandBytes = ByteUtils.hexFromBytes(Command.VehicleLocation.getLocation());
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void getMaintenance() {
        String waitingForBytes = "003400";
        String commandBytes = ByteUtils.hexFromBytes(Command.Maintenance.getMaintenanceState());
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void getIgnitionState() {
        String waitingForBytes = "003500";
        String commandBytes = ByteUtils.hexFromBytes(Command.Engine.getIgnitionState());
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void turnEngineOnOff() {
        String waitingForBytes = "00350200";
        String commandBytes = ByteUtils.hexFromBytes(Command.Engine.turnEngineOn(false));
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void getLightsState() {
        String waitingForBytes = "003600";
        String commandBytes = ByteUtils.hexFromBytes(Command.Lights.getLightsState());
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void controlLights() {
        String waitingForBytes = "003602020100ff0000";
        String commandBytes = ByteUtils.hexFromBytes(Command.Lights.controlLights(Constants.FrontExteriorLightState.ACTIVE_WITH_FULL_BEAM
        , true, false, Color.RED));
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void messageReceived() {
        String waitingForBytes = "0037000e2b31203535352d3535352d3535350548656c6c6f";
        String commandBytes = null;
        try {
            commandBytes = ByteUtils.hexFromBytes(Command.Messaging.messageReceived("+1 555-555-555", "Hello"));
            assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
        } catch (UnsupportedEncodingException e) {
            fail();
        }
    }

    @Test
    public void notification() {
        /*
        assertTrue(((Notification)command).getText().equals("Start navigation?"));
        assertTrue(((Notification)command).getNotificationActions().length == 2);
        assertTrue(((Notification)command).getNotificationActions()[0].getIdentifier() == 0);
        assertTrue(((Notification)command).getNotificationActions()[0].getText().equals("No"));
        assertTrue(((Notification)command).getNotificationActions()[1].getIdentifier() == 1);
        assertTrue(((Notification)command).getNotificationActions()[1].getText().equals("Yes"));
         */
        String waitingForBytes = "003800115374617274206e617669676174696f6e3f0200024e6f0103596573";
        NotificationAction[] notificationActions = new NotificationAction[2];
        NotificationAction action1 = new NotificationAction(0, "No");
        NotificationAction action2 = new NotificationAction(1, "Yes");
        notificationActions[0] = action1;
        notificationActions[1] = action2;

        String commandBytes = null;
        try {
            commandBytes = ByteUtils.hexFromBytes(Command.Notifications.notification("Start navigation?", notificationActions));
            assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
        } catch (UnsupportedEncodingException e) {
            fail();
        }
    }

    @Test
    public void notificationAction() {
        String waitingForBytes = "003801FE";
        String commandBytes = ByteUtils.hexFromBytes(Command.Notifications.notificationAction(-2));
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void windows() {
        String waitingForBytes = "0045020200010101";

        WindowState[] positions = new WindowState[2];
        WindowState action1 = new WindowState(WindowState.Location.FRONT_LEFT, WindowState.Position.OPEN);
        WindowState action2 = new WindowState(WindowState.Location.FRONT_RIGHT, WindowState.Position.OPEN);
        positions[0] = action1;
        positions[1] = action2;

        String commandBytes = ByteUtils.hexFromBytes(Command.Windows.openCloseWindows(positions));
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void getWindscreenState() {
        String waitingForBytes = "004200";
        String commandBytes = ByteUtils.hexFromBytes(Command.Windscreen.getWindscreenState());
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void setWindscreenDamage() {
        String waitingForBytes = "004202023301";

        WindscreenDamagePosition position = new WindscreenDamagePosition(4, 3, 3, 3);
        String commandBytes = ByteUtils.hexFromBytes(Command.Windscreen.setWindscreenDamage(
                WindscreenState.WindscreenDamage.DAMAGE_SMALLER_THAN_1,
                position,
                WindscreenState.WindscreenReplacementState.REPLACEMENT_NOT_NEEDED
                ));
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void videoHandover() throws UnsupportedEncodingException {
        String waitingForBytes = "0043002b68747470733a2f2f7777772e796f75747562652e636f6d2f77617463683f763d795756423755366d5832595a00";

        String commandBytes = ByteUtils.hexFromBytes(Command.VideoHandover.videoHandover(
                "https://www.youtube.com/watch?v=yWVB7U6mX2Y", 90, Constants.ScreenLocation.FRONT
        ));
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void textInput() throws UnsupportedEncodingException {
        String waitingForBytes = "00440003796573";
        String commandBytes = ByteUtils.hexFromBytes(Command.TextInput.textInput("yes"));
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void openGasFlap() {
        String waitingForBytes = "004002";
        String commandBytes = ByteUtils.hexFromBytes(Command.Fueling.openGasFlap());
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void getTheftAlarmState()  {
        String waitingForBytes = "004600";
        String commandBytes = ByteUtils.hexFromBytes(Command.TheftAlarm.getTheftAlarmState());
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void getParkingTicket() {
        String waitingForBytes = "004700";
        String commandBytes = ByteUtils.hexFromBytes(Command.ParkingTicket.getParkingTicket());
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void startParking() throws UnsupportedEncodingException {
        String waitingForBytes = "0047020e4265726c696e205061726b696e670363054F11010a11220A000000000000";
        String string = "2017-01-10T17:34:10";

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date startDate = format.parse(string);

            String commandBytes = ByteUtils.hexFromBytes(Command.ParkingTicket.startParking("Berlin Parking", 6489423, startDate, null));
            assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
        } catch (ParseException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void endParking() {
        String waitingForBytes = "004703";
        String commandBytes = ByteUtils.hexFromBytes(Command.ParkingTicket.endParking());
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }

    @Test
    public void getKeyfobPosition() {
        String waitingForBytes = "004800";
        String commandBytes = ByteUtils.hexFromBytes(Command.KeyfobPosition.getKeyfobPosition());
        assertTrue(waitingForBytes.equalsIgnoreCase(commandBytes));
    }
}
