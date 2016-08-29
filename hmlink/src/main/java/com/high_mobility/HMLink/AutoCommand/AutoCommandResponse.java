package com.high_mobility.HMLink.AutoCommand;

/**
 * Created by ttiganik on 07/06/16.
 */
public class AutoCommandResponse extends AutoCommand {
    byte errorCode = 0;

    public static AutoCommandResponse create(byte[] bytes) throws CommandParseException {
        if (bytes != null && bytes.length > 1) {
            if (bytes[0] != 0x01 && bytes[0] != 0x02)
                throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);

            AutoCommandResponse response;
            byte typeByte = bytes[1];

            if (typeByte == Type.CONTROL_MODE_AVAILABLE.getValue()) {
                response = new ControlModeAvailableResponse(bytes);
            }
            else if (typeByte == Type.GET_VEHICLE_STATUS.getValue()) {
                response = new GetVehicleStatusResponse(bytes);
            }
            else {
                // regular ack/error
                response = new AutoCommandResponse(bytes);
            }

            return response;
        }
        else {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }
    }

    AutoCommandResponse(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes[0] == 0x02) {
            errorCode = bytes[1];
        }
    }

    public byte getErrorCode() {
        return errorCode;
    }
}