package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 13/09/16.
 */
public class LockState extends Incoming {
    public Command.DigitalKey.LockStatus getLockStatus() {
        return lockStatus;
    }

    Command.DigitalKey.LockStatus lockStatus;

    public LockState(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length != 3) {
            throw new CommandParseException();
        }

        lockStatus = bytes[1] == 0x00 ? Command.DigitalKey.LockStatus.UNLOCKED : Command.DigitalKey.LockStatus.LOCKED;
    }
}
