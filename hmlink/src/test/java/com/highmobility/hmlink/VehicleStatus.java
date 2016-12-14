package com.highmobility.hmlink;

import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.CommandParseException;

import org.junit.Before;

import static org.junit.Assert.fail;

/**
 * Created by ttiganik on 13/12/2016.
 */

public class VehicleStatus {
    // TODO:
    com.high_mobility.HMLink.Command.Incoming.VehicleStatus vehicleStatus;
    @Before
    public void setup() {
        byte[] bytes = ByteUtils.bytesFromHex("001500200101002102000100270102");
        try {
            vehicleStatus = new com.high_mobility.HMLink.Command.Incoming.VehicleStatus(bytes);
        } catch (CommandParseException e) {
            e.printStackTrace();
            fail("init failed");
        }
    }
    /*
    @Test
    public void vehicleStatus_init() {
        byte[] bytes = ByteUtils.bytesFromHex("0015000100FF0002");

        VehicleStatus command = null;

        try {
            command = new VehicleStatus(bytes);
        } catch (CommandParseException e) {
            fail("init failed");
        }

        assertTrue(command.getDoorLockState() == LockState.Feature.UNLOCKED);
        assertTrue(command.getTrunkLockState() == TrunkState.LockState.LOCKED);
        assertTrue(command.getTrunkPosition() == TrunkState.Position.CLOSED);
        assertTrue(command.getWindshieldHeatingState() == WindshieldHeatingState.Feature.UNSUPPORTED);
        assertTrue(command.getRooftopState() == RooftopState.Feature.TRANSPARENT);
        assertTrue(command.getRemoteControlMode() == ControlMode.Mode.STARTED);
    }
    */
}
