package com.high_mobility.HMLink.Command;

/**
 * Created by ttiganik on 13/09/16.
 */
public class VehicleStatus extends Incoming {
    // TODO:
    VehicleStatus(byte[] bytes) {
        super(bytes);
    }


    /*
    Command.LockStatus lockStatus;

    public GetVehicleStatusResponse(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 3) {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }
        else {
            lockStatus = bytes[2] == 0x00 ? Command.LockStatus.UNLOCKED : Command.LockStatus.LOCKED;
        }
    }

    public Command.LockStatus getLockStatus() {
        return lockStatus;
    }
     */
}
