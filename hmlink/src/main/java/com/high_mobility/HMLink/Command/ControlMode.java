package com.high_mobility.HMLink.Command;

/**
 * Created by ttiganik on 13/09/16.
 */
public class ControlMode extends Incoming {
    public enum Mode {
        UNAVAILABLE((byte)0x00),
        AVAILABLE((byte)0x01),
        STARTED((byte)0x02),
        FAILED_TO_START((byte)0x03),
        ABORTED((byte)0x04),
        ENDED((byte)0x05);

        private byte type;

        Mode(byte command) {
            this.type = command;
        }

        public byte getValue() {
            return type;
        }

        static Mode controlModeFromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00:
                    return UNAVAILABLE;
                case 0x01:
                    return AVAILABLE;
                case 0x02:
                    return STARTED;
                case 0x03:
                    return FAILED_TO_START;
                case 0x04:
                    return ABORTED;
                case 0x05:
                    return ENDED;
                default:
                    throw new CommandParseException();
            }
        }
    }

    Mode mode;
    int angle;

    public ControlMode(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length != 5) throw new CommandParseException();

        mode = Mode.controlModeFromByte(bytes[2]);
        angle = (bytes[3] << 8) + bytes[4];
    }


    public int getAngle() {
        return angle;
    }

    public Mode getMode() {
        return mode;
    }

}
