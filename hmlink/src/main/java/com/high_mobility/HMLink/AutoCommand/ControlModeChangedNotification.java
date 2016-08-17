package com.high_mobility.HMLink.AutoCommand;

import com.high_mobility.HMLink.AutoCommand.AutoCommandNotification;
import com.high_mobility.HMLink.AutoCommand.CommandParseException;

/**
 * Created by ttiganik on 16/08/16.
 */
public class ControlModeChangedNotification extends AutoCommandNotification {
    public enum ControlMode {
        UNAVAILABLE((byte)0x01),
        AVAILABLE((byte)0x02),
        STARTED((byte)0x03),
        FAILED_TO_START((byte)0x04),
        ABORTED((byte)0x05),
        ENDED((byte)0x06);

        private byte type;

        ControlMode(byte command) {
            this.type = command;
        }

        public byte getValue() {
            return type;
        }

        static ControlMode controlModeFromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x01:
                    return UNAVAILABLE;
                case 0x02:
                    return AVAILABLE;
                case 0x03:
                    return STARTED;
                case 0x04:
                    return FAILED_TO_START;
                case 0x05:
                    return ABORTED;
                case 0x06:
                    return ENDED;
                default:
                    throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);
            }
        }
    }

    ControlMode mode;
    int angle;

    public ControlModeChangedNotification(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 4) throw new CommandParseException(CommandParseException.CommandExceptionCode.PARSE_ERROR);

        mode = ControlMode.controlModeFromByte(bytes[1]);
        angle = (bytes[2] << 8) + bytes[3];
    }
}
