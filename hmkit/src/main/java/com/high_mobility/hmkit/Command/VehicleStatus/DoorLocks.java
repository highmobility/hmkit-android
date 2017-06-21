package com.high_mobility.hmkit.Command.VehicleStatus;
import com.high_mobility.hmkit.Command.CommandParseException;
import com.high_mobility.hmkit.Command.Constants;
import com.high_mobility.hmkit.Command.Command.Identifier;
import com.high_mobility.hmkit.Command.DoorLockState;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class DoorLocks extends FeatureState {
    DoorLockState frontLeft;
    DoorLockState frontRight;
    DoorLockState rearLeft;
    DoorLockState rearRight;

    /**
     *
     * @return the current lock/position state of the front left door
     */
    public DoorLockState getFrontLeft() {
        return frontLeft;
    }

    /**
     *
     * @return the current lock/position state of the front right door
     */
    public DoorLockState getFrontRight() {
        return frontRight;
    }

    /**
     *
     * @return the current lock/position state of the rear left door
     */
    public DoorLockState getRearLeft() {
        return rearLeft;
    }

    /**
     *
     * @return the current lock/position state of the rear right door
     */
    public DoorLockState getRearRight() {
        return rearRight;
    }

    public DoorLocks(byte[] bytes) throws CommandParseException {
        super(Identifier.DOOR_LOCKS);

        if (bytes.length < 5) throw new CommandParseException();
        int numberOfDoors = bytes[3];
        int position = 4;

        for (int i = 0; i < numberOfDoors; i++) {
            byte location = bytes[position];
            DoorLockState doorLockState = new DoorLockState(bytes[position + 1], bytes[position + 2]);
            if (location == 0x00) {
                frontLeft = doorLockState;
            }
            else if (location == 0x01) {
                frontRight = doorLockState;
            }
            else if (location == 0x02) {
                rearLeft = doorLockState;
            }
            else if (location == 0x03) {
                rearRight = doorLockState;
            }

            position += 3;
        }
    }
}
