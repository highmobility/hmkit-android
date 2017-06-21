package com.highmobility.hmkit.Command.Incoming;

import com.highmobility.hmkit.Command.Command;
import com.highmobility.hmkit.Command.CommandParseException;

import java.util.Arrays;

/**
 * Created by ttiganik on 28/09/2016.
 */

public class Failure extends IncomingCommand {
    public enum Reason {
        UNSUPPORTED_CAPABILITY, UNAUTHORIZED, INCORRECT_STATE, EXECUTION_TIMEOUT, VEHICLE_ASLEEP
    }

    private Command.Type failedType;
    private Reason failureReason;

    public Failure(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 7) throw new CommandParseException();

        failedType = Command.typeFromBytes(bytes[3], bytes[4], bytes[5]);

        switch (bytes[6]) {
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

    /**
     *
     * @return The type of the failed command.
     */
    public Command.Type getFailedType() {
        return failedType;
    }

    /**
     *
     * @return The failure reason.
     */
    public Reason getFailureReason() {
        return failureReason;
    }

    /**
     *
     * @param type The command type to compare the failed command's type with.
     * @return True if the failed type is the same as the argument.
     */
    public boolean is(Command.Type type) {
        if (Arrays.equals(failedType.getIdentifierAndType(), type.getIdentifierAndType())) {
            return true;
        }

        return false;
    }
}