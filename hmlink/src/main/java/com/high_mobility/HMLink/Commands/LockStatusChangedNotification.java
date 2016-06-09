package com.high_mobility.HMLink.Commands;

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

        this.type = Type.GET_VEHICLE_STATUS;
        if (bytes[1] == 0x00) {
            lockStatus = Types.LockStatus.UNLOCKED;
        }
        else if (bytes[1] == 0x01) {
            lockStatus = Types.LockStatus.LOCKED;
        }
        else {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }
    }
}
