package com.highmobility.hmlink;

import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.VehicleStatus.DoorLocks;
import com.high_mobility.HMLink.Command.VehicleStatus.FeatureState;
import com.high_mobility.HMLink.Command.VehicleStatus.RemoteControl;
import com.high_mobility.HMLink.Command.VehicleStatus.TrunkAccess;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by ttiganik on 13/12/2016.
 */

public class VehicleStatus {
    // TODO:
    com.high_mobility.HMLink.Command.Incoming.VehicleStatus vehicleStatus;
    @Before
    public void setup() {
        byte[] bytes = ByteUtils.bytesFromHex("00150300200101002102000100270102"); // TODO: add new states to test
        try {
            vehicleStatus = new com.high_mobility.HMLink.Command.Incoming.VehicleStatus(bytes);
        } catch (CommandParseException e) {
            e.printStackTrace();
            fail("init failed");
        }
    }

    @Test
    public void states_size() {
        assertTrue(vehicleStatus.getFeatureStates().length == 3);
    }

    @Test
    public void unknown_state() {
        byte[] bytes = ByteUtils.bytesFromHex("00150300570101002102000100270102");
        try {
            vehicleStatus = new com.high_mobility.HMLink.Command.Incoming.VehicleStatus(bytes);
        } catch (CommandParseException e) {
            e.printStackTrace();
            fail("init failed");
        }

        assertTrue(vehicleStatus.getFeatureStates().length == 2);

        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            assertTrue(vehicleStatus.getFeatureStates()[i] != null);
        }

    }

    @Test
    public void door_locks() {
        FeatureState state = null;
        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            FeatureState iteratingState = vehicleStatus.getFeatureStates()[i];
            if (iteratingState.getFeature() == com.high_mobility.HMLink.Command.Incoming.VehicleStatus.Feature.DOOR_LOCKS) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == DoorLocks.class);

        if (state.getClass() == DoorLocks.class) {
            assertTrue(((DoorLocks)state).getState() == DoorLocks.State.LOCKED);
        }
    }

    @Test
    public void trunk_access() {
        FeatureState state = null;
        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            FeatureState iteratingState = vehicleStatus.getFeatureStates()[i];
            if (iteratingState.getFeature() == com.high_mobility.HMLink.Command.Incoming.VehicleStatus.Feature.TRUNK_ACCESS) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == TrunkAccess.class);

        if (state.getClass() == TrunkAccess.class) {
            assertTrue(((TrunkAccess)state).getLockState() == TrunkAccess.LockState.UNLOCKED);
            assertTrue(((TrunkAccess)state).getPosition() == TrunkAccess.Position.OPEN);
        }
    }

    @Test
    public void remote_control() {
        FeatureState state = null;
        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            FeatureState iteratingState = vehicleStatus.getFeatureStates()[i];
            if (iteratingState.getFeature() == com.high_mobility.HMLink.Command.Incoming.VehicleStatus.Feature.REMOTE_CONTROL) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == RemoteControl.class);

        if (state.getClass() == RemoteControl.class) {
            assertTrue(((RemoteControl)state).getState() == RemoteControl.State.STARTED);
        }
    }
}
