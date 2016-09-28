package com.highmobility.hmlink;

import com.high_mobility.HMLink.Command;
import com.high_mobility.HMLink.RooftopState;
import com.high_mobility.HMLink.ByteUtils;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by ttiganik on 15/09/16.
 */
public class OutgoingCommand {

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


}
