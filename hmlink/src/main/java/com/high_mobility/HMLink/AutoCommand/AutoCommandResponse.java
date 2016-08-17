package com.high_mobility.HMLink.AutoCommand;

/**
 * Created by ttiganik on 07/06/16.
 */
public class AutoCommandResponse extends AutoCommand {
    byte errorCode = 0;

    public AutoCommandResponse(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes == null || bytes.length < 2) {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }

        if (bytes[0] == 0x02) {
            errorCode = bytes[1];
        }
        else if (bytes[0] != 0x01) {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }

        byte typeByte = bytes[1];

        if (typeByte == Type.CONTROL_MODE_AVAILABLE.getValue()) {
            type = Type.CONTROL_MODE_AVAILABLE;
        }
        else if (typeByte == Type.CONTROL_MODE_CHANGED.getValue()) {
            type = Type.CONTROL_MODE_CHANGED;
        }
        else if (typeByte == Type.START_CONTROL_MODE.getValue()) {
            type = Type.START_CONTROL_MODE;
        }
        else if (typeByte == Type.STOP_CONTROL_MODE.getValue()) {
            type = Type.STOP_CONTROL_MODE;
        }
        else if (typeByte == Type.CONTROL_COMMAND.getValue()) {
            type = Type.CONTROL_COMMAND;
        }
        else if (typeByte == Type.ACCESS.getValue()) {
            type = Type.ACCESS;
        }
        else if (typeByte == Type.GET_VEHICLE_STATUS.getValue()) {
            type = Type.GET_VEHICLE_STATUS;
        }
        else if (typeByte == Type.LOCK_STATUS_CHANGED.getValue()) {
            type = Type.LOCK_STATUS_CHANGED;
        }
    }

    public byte getErrorCode() {
        return errorCode;
    }
}