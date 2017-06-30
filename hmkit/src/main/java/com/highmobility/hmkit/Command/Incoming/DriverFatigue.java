package com.highmobility.hmkit.Command.Incoming;

import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Constants;

import java.util.Arrays;

/**
 * Created by root on 6/28/17.
 */
public class DriverFatigue extends IncomingCommand {
    public enum FatigueLevel {
        LIGHT, PAUSE_RECOMMENDED, DRIVER_NEEDS_REST, READY_TO_TAKE_OVER;

        static FatigueLevel fromByte(byte value) {
            switch (value) {
                case 0x00:
                    return LIGHT;
                case 0x01:
                    return PAUSE_RECOMMENDED;
                case 0x02:
                    return DRIVER_NEEDS_REST;
                case 0x03:
                    return READY_TO_TAKE_OVER;
            }
            return LIGHT;
        }
    }

    FatigueLevel fatigueLevel;

    public FatigueLevel getFatigueLevel() {
        return fatigueLevel;
    }

    public DriverFatigue(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length < 4) throw new CommandParseException();

        fatigueLevel = FatigueLevel.fromByte(bytes[3]);
    }
}
