package com.high_mobility.hmkit.Command.Incoming;

import com.high_mobility.hmkit.Command.Command;
import com.high_mobility.hmkit.Command.CommandParseException;
import com.high_mobility.hmkit.Command.Constants;
import com.high_mobility.hmkit.Command.DoorLockState;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This is an evented notification that is sent from the car every time the lock state changes. This
 * notificaiton is also sent when a Get Lock State is received by the car. The new status is included
 * and may be the result of user, device or car triggered action.
 */
public class LockState extends IncomingCommand {
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

    public LockState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 4) throw new CommandParseException();
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

        //002001040001000100000200010300
    }
}
