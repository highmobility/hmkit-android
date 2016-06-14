package com.high_mobility.HMLink.Commands;

/**
 * Created by ttiganik on 07/06/16.
 */
public class AutoCommandResponse extends AutoCommand {
    byte errorCode = 0;

    public AutoCommandResponse(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length < 2) {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }

        if (bytes[1] == Type.GET_VEHICLE_STATUS.getValue()) {
            type = Type.GET_VEHICLE_STATUS;
        }
        else if (bytes[1] == Type.ACCESS.getValue()) {
            type = Type.ACCESS;
        }

        if (bytes[0] == 0x02) {
            errorCode = bytes[1];
        }
        else if (bytes[0] != 0x01) {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }
    }

    public byte getErrorCode() {
        return errorCode;
    }
}