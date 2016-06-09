package com.high_mobility.HMLink.Commands;

/**
 * Created by ttiganik on 07/06/16.
 */
public class AutoCommandNotification extends AutoCommand {
    public static AutoCommandNotification create(byte[] bytes) throws CommandParseException {
        if (bytes.length > 0) {
            if (bytes[0] == Type.LOCK_STATUS_CHANGED.getValue()) {
                return new LockStatusChangedNotification(bytes);
            }
            else {
                throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
            }
        }
        else {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }
    }

    public AutoCommandNotification(byte[] bytes) {
        super(bytes);
    }

    public byte[] ackBytes() {
        return new byte[]{ ACK_BYTE, type.getValue() };
    }

    public byte[] errorBytes() {
        return new byte[]{ ERROR_BYTE, type.getValue() };
    }

}