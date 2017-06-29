package com.highmobility.hmkit;

import android.graphics.Color;

import com.highmobility.hmkit.Command.AutoHvacState;
import com.highmobility.hmkit.Command.Command;
import com.highmobility.hmkit.Command.Constants;
import com.highmobility.hmkit.Command.Command.Identifier;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertTrue;

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
        String commandBytes = ByteUtils.hexFromBytes(Command.TrunkAccess.setTrunkState(Constants.TrunkLockState.UNLOCKED, Constants.TrunkPosition.OPEN));
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
}
