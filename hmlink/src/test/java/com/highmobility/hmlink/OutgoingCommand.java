package com.highmobility.hmlink;

import com.high_mobility.HMLink.CapabilityType;
import com.high_mobility.HMLink.Command;
import com.high_mobility.HMLink.LockState;
import com.high_mobility.HMLink.RooftopState;
import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.TrunkState;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertTrue;

/**
 * Created by ttiganik on 15/09/16.
 */
public class OutgoingCommand {
    @Test
    public void getCapability() {
        String waitingForBytes = "001211";
        String commandBytes = ByteUtils.hexFromBytes(Command.General.getCapability(CapabilityType.Type.CHASSIS));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void lockUnlockDoors_lock() {
        String waitingForBytes = "002201";
        String commandBytes = ByteUtils.hexFromBytes(Command.DigitalKey.lockDoors(true));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void setWindshieldHeatingState_active() {
        String waitingForBytes = "005C01";
        String commandBytes = ByteUtils.hexFromBytes(Command.Chassis.setWindshieldHeating(true));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void setRooftopTransparency_opaque() {
        String waitingForBytes = "005F01";
        String commandBytes = ByteUtils.hexFromBytes(Command.Chassis.controlRooftop(RooftopState.State.OPAQUE));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void controlCommand() {
        String waitingForBytes = "0045030032";
        String commandBytes = ByteUtils.hexFromBytes(Command.RemoteControl.controlCommand(3, 50));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void getTrunkState() {
        String waitingForBytes = "0023";
        String commandBytes = ByteUtils.hexFromBytes(Command.DigitalKey.getTrunkState());
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void setTrunkState() {
        String waitingForBytes = "00250001";
        String commandBytes = ByteUtils.hexFromBytes(Command.DigitalKey.setTrunkState(TrunkState.LockState.UNLOCKED, TrunkState.Position.OPEN));
        assertTrue(waitingForBytes.equals(commandBytes));
    }

    @Test
    public void setDestination() {
        String waitingForBytes = "00704252147D41567AB1064265726C696E";

        String commandBytes = null;
        try {
            commandBytes = ByteUtils.hexFromBytes(Command.PointOfInterest.setDestination(52.520008f, 13.404954f, "Berlin"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assertTrue(waitingForBytes.equals(commandBytes));
    }
}
