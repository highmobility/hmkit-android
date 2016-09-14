package com.high_mobility.HMLink.Command;

/**
 * Created by ttiganik on 13/09/16.
 */
public class LockState extends Incoming {
    public Command.DigitalKey.LockStatus getLockStatus() {
        return lockStatus;
    }

    Command.DigitalKey.LockStatus lockStatus;

    LockState(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length != 2) {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }

        lockStatus = bytes[1] == 0x00 ? Command.DigitalKey.LockStatus.UNLOCKED : Command.DigitalKey.LockStatus.LOCKED;
    }
}
