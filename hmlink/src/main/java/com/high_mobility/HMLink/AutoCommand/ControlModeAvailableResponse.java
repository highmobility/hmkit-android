package com.high_mobility.HMLink.AutoCommand;

import com.high_mobility.HMLink.AutoCommand.AutoCommandResponse;
import com.high_mobility.HMLink.AutoCommand.CommandParseException;

/**
 * Created by ttiganik on 16/08/16.
 */
public class ControlModeAvailableResponse extends AutoCommandResponse {
    boolean available;

    public ControlModeAvailableResponse(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 3) {
            throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
        }
        else {
            available = bytes[2] != 0;
        }
    }

    public boolean isAvailable() {
        return available;
    }

}
