package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.Command.CommandParseException;

/**
 * Created by ttiganik on 28/09/2016.
 */

public class Failure extends IncomingCommand {
    public enum Reason {
        UNSUPPORTED_CAPABILITY, UNAUTHORIZED, INCORRECT_STATE, EXECUTION_TIMEOUT, VEHICLE_ASLEEP
    }

    private byte[] failedIdentifier;
    private Reason failureReason;

    public Failure(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 5) throw new CommandParseException();

        failedIdentifier = new byte[2];
        failedIdentifier[0] = bytes[2];
        failedIdentifier[1] = bytes[3];

        switch (bytes[4]) {
            case 0x00:
                failureReason = Reason.UNSUPPORTED_CAPABILITY;
                break;
            case 0x01:
                failureReason = Reason.UNAUTHORIZED;
                break;
            case 0x02:
                failureReason = Reason.INCORRECT_STATE;
                break;
            case 0x03:
                failureReason = Reason.EXECUTION_TIMEOUT;
                break;
            case 0x04:
                failureReason = Reason.VEHICLE_ASLEEP;
                break;
            default:
                throw new CommandParseException();
        }
    }

    public byte[] getFailedCommandIdentifier() {
        return failedIdentifier;
    }

    public Reason getFailureReason() {
        return failureReason;
    }
}