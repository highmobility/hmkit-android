package com.high_mobility.HMLink.AutoCommand;

/**
 * Created by ttiganik on 07/06/16.
 */
public class GetVehicleStatusResponse extends AutoCommandResponse {
    Types.LockStatus lockStatus;

    public GetVehicleStatusResponse(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 3) {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }
        else {
            lockStatus = bytes[2] == 0x00 ? Types.LockStatus.UNLOCKED : Types.LockStatus.LOCKED;
        }
    }

    public Types.LockStatus getLockStatus() {
        return lockStatus;
    }
}
