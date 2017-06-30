package com.highmobility.hmkit.Command.Incoming;

import com.highmobility.hmkit.Command.CommandParseException;

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

    /**
     *
     * @return An evented message that notifies about driver fatigue. Sent continously when level 1 or higher.
     */
    public FatigueLevel getFatigueLevel() {
        return fatigueLevel;
    }

    public DriverFatigue(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length < 4) throw new CommandParseException();

        fatigueLevel = FatigueLevel.fromByte(bytes[3]);
    }
}
