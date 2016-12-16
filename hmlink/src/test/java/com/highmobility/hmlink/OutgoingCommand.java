package com.highmobility.hmlink;

import com.high_mobility.HMLink.Command.Command;
import com.high_mobility.HMLink.Command.Incoming.*;
import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;
import com.high_mobility.HMLink.Command.VehicleFeature;

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
        String commandBytes = ByteUtils.hexFromBytes(Command.Capabilities.getCapability(VehicleFeature.TRUNK_ACCESS));
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
}
