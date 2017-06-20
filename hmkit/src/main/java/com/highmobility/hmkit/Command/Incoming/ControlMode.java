package com.highmobility.hmkit.Command.Incoming;

import com.highmobility.hmkit.Command.CommandParseException;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This is an evented command that is sent from the car every time the remote control mode changes.
 * It is also sent when a Get Control Mode is received by the car. The new mode is
 * included in the command and may be the result of both user or car triggered action.
 */
public class ControlMode extends IncomingCommand {
    /**
     * The possible control modes
     */
    public enum Mode {
        UNAVAILABLE((byte)0x00),
        AVAILABLE((byte)0x01),
        STARTED((byte)0x02),
        FAILED_TO_START((byte)0x03),
        ABORTED((byte)0x04),
        ENDED((byte)0x05),
        UNSUPPORTED((byte)0xFF);

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
                case (byte)0xFF:
                    return UNSUPPORTED;
                default:
                    throw new CommandParseException();
            }
        }
    }

    Mode mode;
    int angle;

    public ControlMode(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length != 6) throw new CommandParseException();

        mode = Mode.controlModeFromByte(bytes[3]);
        angle = (bytes[4] << 8) + bytes[5];
    }

    /**
     *
     * @return the angle
     */
    public int getAngle() {
        return angle;
    }

    /**
     *
     * @return the control mode
     */
    public Mode getMode() {
        return mode;
    }

}
