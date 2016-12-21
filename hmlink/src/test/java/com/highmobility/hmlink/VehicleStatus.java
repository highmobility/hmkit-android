package com.highmobility.hmlink;

import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Constants;
import com.high_mobility.HMLink.Command.VehicleFeature;
import com.high_mobility.HMLink.Command.VehicleStatus.Charging;
import com.high_mobility.HMLink.Command.VehicleStatus.Climate;
import com.high_mobility.HMLink.Command.VehicleStatus.DoorLocks;
import com.high_mobility.HMLink.Command.VehicleStatus.FeatureState;
import com.high_mobility.HMLink.Command.VehicleStatus.RemoteControl;
import com.high_mobility.HMLink.Command.VehicleStatus.RooftopState;
import com.high_mobility.HMLink.Command.VehicleStatus.TrunkAccess;
import com.high_mobility.HMLink.Command.VehicleStatus.ValetMode;
import com.high_mobility.HMLink.Command.VehicleStatus.VehicleLocation;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by ttiganik on 13/12/2016.
 */

public class VehicleStatus {
    com.high_mobility.HMLink.Command.Incoming.VehicleStatus vehicleStatus;
    @Before
    public void setup() {
        String vehicleStatusHexString = "00110108" +
                "00200101" +
                "0021020001" +
                "0023080200FF32bf19999a" +
                "00240C419800004140000001000060" + // climate
                "0025020135" + // rooftop state
                "00270102" +
                "00280101" + // valet mode
                "00300842561eb941567ab1"; // location 53.530003 13.404954

        byte[] bytes = ByteUtils.bytesFromHex(vehicleStatusHexString);

        try {
            com.high_mobility.HMLink.Command.Incoming.IncomingCommand command = com.high_mobility.HMLink.Command.Incoming.IncomingCommand.create(bytes);
            assertTrue(command.getClass() == com.high_mobility.HMLink.Command.Incoming.VehicleStatus.class);
            vehicleStatus = (com.high_mobility.HMLink.Command.Incoming.VehicleStatus)command;
        } catch (CommandParseException e) {
            e.printStackTrace();
            fail("init failed");
        }
    }

    @Test
    public void states_size() {
        assertTrue(vehicleStatus.getFeatureStates().length == 8);
    }

    @Test
    public void unknown_state() {
        byte[] bytes = ByteUtils.bytesFromHex("0011010300590101002102000100270102");
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
            if (iteratingState.getFeature() == VehicleFeature.DOOR_LOCKS) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == DoorLocks.class);

        if (state.getClass() == DoorLocks.class) {
            assertTrue(((DoorLocks)state).getState() == Constants.LockState.LOCKED);
        }
    }

    @Test
    public void trunk_access() {
        FeatureState state = null;
        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            FeatureState iteratingState = vehicleStatus.getFeatureStates()[i];
            if (iteratingState.getFeature() == VehicleFeature.TRUNK_ACCESS) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == TrunkAccess.class);

        if (state.getClass() == TrunkAccess.class) {
            assertTrue(((TrunkAccess)state).getLockState() == Constants.TrunkLockState.UNLOCKED);
            assertTrue(((TrunkAccess)state).getPosition() == Constants.TrunkPosition.OPEN);
        }
    }

    @Test
    public void remote_control() {
        FeatureState state = null;
        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            FeatureState iteratingState = vehicleStatus.getFeatureStates()[i];
            if (iteratingState.getFeature() == VehicleFeature.REMOTE_CONTROL) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == RemoteControl.class);
        assertTrue(((RemoteControl)state).getState() == RemoteControl.State.STARTED);
    }

    @Test
    public void charging() {
        FeatureState state = null;
        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            FeatureState iteratingState = vehicleStatus.getFeatureStates()[i];
            if (iteratingState.getFeature() == VehicleFeature.CHARGING) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == Charging.class);
        assertTrue(((Charging)state).getChargingState() == Constants.ChargingState.CHARGING);
        assertTrue(((Charging)state).getEstimatedRange() == 255f);
        assertTrue(((Charging)state).getBatteryLevel() == .5f);
        assertTrue(((Charging)state).getBatteryCurrent() == -.6f);
    }

    @Test
    public void climate() {
        FeatureState state = null;
        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            FeatureState iteratingState = vehicleStatus.getFeatureStates()[i];
            if (iteratingState.getFeature() == VehicleFeature.CLIMATE) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == Climate.class);

        assertTrue(((Climate)state).getInsideTemperature() == 19f);
        assertTrue(((Climate)state).getOutsideTemperature() == 12f);

        assertTrue(((Climate)state).isHvacActive() == true);
        assertTrue(((Climate)state).isDefoggingActive() == false);
        assertTrue(((Climate)state).isDefrostingActive() == false);
        assertTrue(((Climate)state).isAutoHvacConstant() == false);

        boolean[] autoHvacStates = ((Climate)state).getHvacActiveOnDays();
        assertTrue(autoHvacStates != null);
        assertTrue(autoHvacStates.length == 7);

        assertTrue(autoHvacStates[0] == false);
        assertTrue(autoHvacStates[5] == true);
        assertTrue(autoHvacStates[6] == true);
    }

    @Test
    public void valetMode() {
        FeatureState state = null;
        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            FeatureState iteratingState = vehicleStatus.getFeatureStates()[i];
            if (iteratingState.getFeature() == VehicleFeature.VALET_MODE) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == ValetMode.class);
        assertTrue(((ValetMode)state).isActive() == true);
    }

    @Test
    public void vehicleLocation() {
        FeatureState state = null;
        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            FeatureState iteratingState = vehicleStatus.getFeatureStates()[i];
            if (iteratingState.getFeature() == VehicleFeature.VEHICLE_LOCATION) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == VehicleLocation.class);
        assertTrue(((VehicleLocation)state).getLatitude() == 53.530003f);
        assertTrue(((VehicleLocation)state).getLongitude() == 13.404954f);
    }

    @Test
    public void rooftopState() {
        FeatureState state = null;
        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            FeatureState iteratingState = vehicleStatus.getFeatureStates()[i];
            if (iteratingState.getFeature() == VehicleFeature.ROOFTOP) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == RooftopState.class);
        assertTrue(((RooftopState)state).getDimmingPercentage() == .01f);
        assertTrue(((RooftopState)state).getOpenPercentage() == .53f);
    }
}
