package com.high_mobility.HMLink.AutoCommand;

/**
 * Created by ttiganik on 07/06/16.
 */
public class LockStatusChangedNotification extends AutoCommandNotification {

    public Types.LockStatus getLockStatus() {
        return lockStatus;
    }

    Types.LockStatus lockStatus;

    public LockStatusChangedNotification(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 2) {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }

        lockStatus = bytes[1] == 0x00 ? Types.LockStatus.UNLOCKED : Types.LockStatus.LOCKED;
    }
}
