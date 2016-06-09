package com.high_mobility.HMLink.Commands;

/**
 * Created by ttiganik on 07/06/16.
 */
public class AutoCommandResponse extends AutoCommand {
    byte errorCode = 0;

    public static AutoCommandResponse create(byte[] bytes) throws CommandParseException {
        if (bytes.length > 1) {
            if (bytes[1] == Type.GET_VEHICLE_STATUS.getValue()) {
                return new GetVehicleStatusResponse(bytes);
            }
            else if (bytes[1] == Type.ACCESS.getValue()) {
                return new AccessResponse(bytes);
            }
            else {
                throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
            }
        }
        else {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }
    }

    public AutoCommandResponse(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes[0] == 0x01) {
            return;
        }
        else if (bytes[0] == 0x02) {
            errorCode = bytes[1];
        }
        else {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }
    }

    public byte getErrorCode() {
        return errorCode;
    }
}