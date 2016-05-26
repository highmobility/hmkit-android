package com.high_mobility.HMLink.Shared;

/**
 * Created by ttiganik on 25/05/16.
 */
public class AutoCommand {
    static final byte ACK_BYTE = 0x01;
    static final byte ERROR_BYTE = 0x02;

    public enum Command {
        UNKNOWN((byte)0xFF), ACCESS((byte)0x17);

        private byte command;

        Command(byte command) {
            this.command = command;
        }

        public byte getValue() {
            return command;
        }
    }

    public static byte[] lockDoorsBytes() {
        return new byte[] { 0x17, 0x01 };
    }

    public static byte[] unlockDoorsBytes() {
        return new byte[] { 0x17, 0x00 };
    }

    public static byte[] ackBytes(AutoCommand command) {
        return new byte[]{ ACK_BYTE, command.type.getValue() };
    }

    public static byte[] errorBytes(AutoCommand command) {
        return new byte[]{ ERROR_BYTE, command.type.getValue() };
    }

    public Command type;
    public byte[] bytes;

    public AutoCommand(byte[] bytes) {
        if (bytes.length > 0) {
            if (bytes[0] == 0x17) {
                type = Command.ACCESS;
            }
            else {
                type = Command.UNKNOWN;
            }
        }
        else {
            type = Command.UNKNOWN;
        }
    }
}
