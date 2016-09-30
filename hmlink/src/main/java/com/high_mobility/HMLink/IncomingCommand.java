package com.high_mobility.HMLink;

import java.util.Arrays;

/**
 * Created by ttiganik on 07/06/16.
 */
public class IncomingCommand {
    public static IncomingCommand create(byte[] bytes) throws CommandParseException {
        if (bytes.length > 1) {
            if (ByteUtils.startsWith(bytes, Command.General.CAPABILITIES.getIdentifier())) {
                return new Capabilities(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.General.VEHICLE_STATUS.getIdentifier())) {
                return new VehicleStatus(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.DigitalKey.LOCK_STATE.getIdentifier())) {
                return new LockState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.DigitalKey.TRUNK_STATE.getIdentifier())) {
                return new TrunkState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.Chassis.WINDSHIELD_HEATING_STATE.getIdentifier())) {
                return new VehicleStatus(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.Chassis.ROOFTOP_STATE.getIdentifier())) {
                return new VehicleStatus(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.RemoteControl.CONTROL_MODE.getIdentifier())) {
                return new VehicleStatus(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.ParcelDelivery.DELIVERED_PARCELS.getIdentifier())) {
                return new VehicleStatus(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.General.FAILURE.getIdentifier())) {
                return new Failure(bytes);
            }
            else {
                throw new CommandParseException();
            }
        }
        else {
            throw new CommandParseException();
        }
    }

    static int errorCode(byte[] bytes) {
        if (bytes == null || bytes.length < 2) return 0;
        if (bytes[0] != 0x02) return 0;
        return codeForByte(bytes[1]);
    }

    static int codeForByte(byte errorByte) {
        switch (errorByte) {
            case 0x05:
                return Link.STORAGE_FULL;
            case 0x09:
                return Link.TIME_OUT;
            case 0x07:
                return Link.UNAUTHORIZED;
            case 0x06:
            case 0x08:
                return Link.UNAUTHORIZED;
            default:
                return Link.INTERNAL_ERROR;
        }
    }

    byte[] identifier;
    byte[] bytes;

    IncomingCommand(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getIdentifier() {
        return identifier;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public boolean is(Command.Type type) {
        if (Arrays.equals(getIdentifier(), type.getIdentifier())) {
            return true;
        }

        return false;
    }
}
