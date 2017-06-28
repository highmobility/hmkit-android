package com.highmobility.hmkit;

import com.highmobility.hmkit.Command.Command;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Constants;
import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.DoorLockState;
import com.highmobility.hmkit.Command.Incoming.IncomingCommand;
import com.highmobility.hmkit.Command.VehicleStatus.Charging;
import com.highmobility.hmkit.Command.VehicleStatus.Climate;

import com.highmobility.hmkit.Command.VehicleStatus.Diagnostics;
import com.highmobility.hmkit.Command.VehicleStatus.DoorLocks;
import com.highmobility.hmkit.Command.VehicleStatus.FeatureState;
import com.highmobility.hmkit.Command.VehicleStatus.RemoteControl;
import com.highmobility.hmkit.Command.VehicleStatus.RooftopState;
import com.highmobility.hmkit.Command.VehicleStatus.TrunkAccess;
import com.highmobility.hmkit.Command.VehicleStatus.ValetMode;
import com.highmobility.hmkit.Command.VehicleStatus.VehicleLocation;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by ttiganik on 13/12/2016.
 */

public class VehicleStatus {
    com.highmobility.hmkit.Command.Incoming.VehicleStatus vehicleStatus;

    @Before
    public void setup() {
        String vehicleStatusHexString =
                "0011" + // MSB, LSB Message Identifier for Vehicle Status
                "01"       + // Message Type for Vehicle Status
                "4a46325348424443374348343531383639" + // VIN
                "01"           + // All-electric powertrain
                "06"           + // Model name is 6 bytes
                "547970652058" + // "Type X"
                "06"           + // Car name is 6 bytes
                "4d7920436172" + // "My Car"
                "06"           + // License plate is 6 bytes
                "414243313233" + // "ABC123"
                "09" +              // 8 feature states
                "00200D04000100010000020001030001" + // door locks
                "0021020001" +
                "0023080200FF32bf19999a" +
                "002410419800004140000001000041ac000060" + // climate
                "0025020135" + // rooftop state
                "00270102" +
                "00280101" + // valet mode
                "00300842561eb941567ab1" + // location 53.530003 13.404954; // 8 feature states
                "00330B0249F00063003C09C45A01" +
                "";
        byte[] bytes = ByteUtils.bytesFromHex(vehicleStatusHexString);

        try {
            com.highmobility.hmkit.Command.Incoming.IncomingCommand command = IncomingCommand.create(bytes);
            assertTrue(command.getClass() == com.highmobility.hmkit.Command.Incoming.VehicleStatus.class);
            vehicleStatus = (com.highmobility.hmkit.Command.Incoming.VehicleStatus)command;
        } catch (CommandParseException e) {
            e.printStackTrace();
            fail("init failed");
        }
    }

    @Test
    public void states_size() {
        assertTrue(vehicleStatus.getFeatureStates().length == 9);
    }

    @Test
    public void vin() {
        assertTrue(vehicleStatus.getVin().equals("JF2SHBDC7CH451869"));
    }

    @Test
    public void power_train() {
        assertTrue(vehicleStatus.getPowerTrain() == com.highmobility.hmkit.Command.Incoming.VehicleStatus.PowerTrain.ALLELECTRIC);
    }

    @Test
    public void model_name() {
        assertTrue(vehicleStatus.getModelName().equals("Type X"));
    }

    @Test
    public void car_name() {
        assertTrue(vehicleStatus.getName().equals("My Car"));
    }

    @Test
    public void license_plate() {
        assertTrue(vehicleStatus.getLicensePlate().equals("ABC123"));
    }

    @Test
    public void unknown_state() {
        byte[] bytes = ByteUtils.bytesFromHex("0011014a463253484244433743483435313836390106547970652058064d7920436172064142433132330300590101002102000100270102");
        try {
            vehicleStatus = new com.highmobility.hmkit.Command.Incoming.VehicleStatus(bytes);
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
            if (iteratingState.getFeature() == Identifier.DOOR_LOCKS) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == DoorLocks.class);

        assertTrue(((DoorLocks)state).getFrontLeft() != null);
        assertTrue(((DoorLocks)state).getFrontRight() != null);
        assertTrue(((DoorLocks)state).getRearLeft() != null);
        assertTrue(((DoorLocks)state).getRearRight() != null);

        assertTrue(((DoorLocks)state).getFrontLeft().getPosition() == DoorLockState.DoorPosition.OPEN);
        assertTrue(((DoorLocks)state).getFrontLeft().getLockState() == DoorLockState.LockState.UNLOCKED);

        assertTrue(((DoorLocks)state).getFrontRight().getPosition() == DoorLockState.DoorPosition.CLOSED);
        assertTrue(((DoorLocks)state).getFrontRight().getLockState() == DoorLockState.LockState.UNLOCKED);

        assertTrue(((DoorLocks)state).getRearLeft().getPosition() == DoorLockState.DoorPosition.CLOSED);
        assertTrue(((DoorLocks)state).getRearLeft().getLockState() == DoorLockState.LockState.LOCKED);

        assertTrue(((DoorLocks)state).getRearRight().getPosition() == DoorLockState.DoorPosition.CLOSED);
        assertTrue(((DoorLocks)state).getRearRight().getLockState() == DoorLockState.LockState.LOCKED);
    }

    @Test
    public void trunk_access() {
        FeatureState state = null;
        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            FeatureState iteratingState = vehicleStatus.getFeatureStates()[i];
            if (iteratingState.getFeature() == Identifier.TRUNK_ACCESS) {
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
            if (iteratingState.getFeature() == Identifier.REMOTE_CONTROL) {
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
            if (iteratingState.getFeature() == Identifier.CHARGING) {
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
            if (iteratingState.getFeature() == Identifier.CLIMATE) {
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
        assertTrue(((Climate)state).getDefrostingTemperature() == 21.5f);
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
            if (iteratingState.getFeature() == Identifier.VALET_MODE) {
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
            if (iteratingState.getFeature() == Identifier.VEHICLE_LOCATION) {
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
            if (iteratingState.getFeature() == Identifier.ROOFTOP) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == RooftopState.class);
        assertTrue(((RooftopState)state).getDimmingPercentage() == .01f);
        assertTrue(((RooftopState)state).getOpenPercentage() == .53f);
    }

    @Test
    public void diagnostics() {
        /*
          0x00, 0x33, # MSB, LSB Message Identifier for Diagnostics
  0x01, # Message Type for Diagnostics State

  0x0249F0,   # Odometer is 150'000 km

  0x0063,     # Engine oil teperature is 99C

  0x003C,     # Car speed is 60km/h

  0x09C4,     # RPM is 2500

  0x5A,       # 90% fuel level

  0x01,       # Washer fluid filled

  0x04,       # Tire pressure of 4 tires follows

  0x00,       # Front Left tire pressure
  0x4013d70a, # 2.31 BAR

  0x01,       # Front Right tire pressure
  0x4013d70a, # 2.31 BAR

  0x02,       # Rear Right tire pressure
  0x40166666, # 2.35 BAR

  0x03,       # Rear Left tire pressure
  0x40166666  # 2.35 BAR
         */

        FeatureState state = null;
        for (int i = 0; i < vehicleStatus.getFeatureStates().length; i++) {
            FeatureState iteratingState = vehicleStatus.getFeatureStates()[i];
            if (iteratingState.getFeature() == Identifier.DIAGNOSTICS) {
                state = iteratingState;
                break;
            }
        }

        assertTrue(state != null);
        assertTrue(state.getClass() == Diagnostics.class);
        assertTrue(((Diagnostics)state).getMileage() == 150000);
        assertTrue(((Diagnostics)state).getOilTemperature() == 99);
        assertTrue(((Diagnostics)state).getSpeed() == 60);
        assertTrue(((Diagnostics)state).getRpm() == 2500);
        assertTrue(((Diagnostics)state).getFuelLevel() == .9f);
        assertTrue(((Diagnostics)state).getWasherFluidLevel() == Constants.WasherFluidLevel.FULL);
    }
}
