package com.highmobility.hmkit.Command.VehicleStatus;
import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Constants;
import com.highmobility.hmkit.Command.DoorLockState;

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

    /**
     *
     * @return true if all doors are closed and locked, otherwise false
     */
    public boolean isLocked() {
        if (getFrontLeft() != null && getFrontLeft().getLockState() != DoorLockState.LockState.LOCKED) {
            return false;
        }

        if (getFrontRight() != null && getFrontRight().getLockState() != DoorLockState.LockState.LOCKED) {
            return false;
        }

        if (getRearLeft() != null && getRearLeft().getLockState() != DoorLockState.LockState.LOCKED) {
            return false;
        }

        if (getRearRight() != null && getRearRight().getLockState() != DoorLockState.LockState.LOCKED) {
            return false;
        }

        return true;
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