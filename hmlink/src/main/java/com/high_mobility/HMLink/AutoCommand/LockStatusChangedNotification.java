package com.high_mobility.HMLink.AutoCommand;

/**
 * Created by ttiganik on 07/06/16.
 */
public class LockStatusChangedNotification extends AutoCommandNotification {

    public AutoCommand.LockStatus getLockStatus() {
        return lockStatus;
    }

    AutoCommand.LockStatus lockStatus;

    public LockStatusChangedNotification(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 2) {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }

        lockStatus = bytes[1] == 0x00 ? AutoCommand.LockStatus.UNLOCKED : AutoCommand.LockStatus.LOCKED;
    }
}
