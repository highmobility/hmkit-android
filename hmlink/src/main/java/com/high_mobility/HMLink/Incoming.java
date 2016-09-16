package com.high_mobility.HMLink;

import java.util.Arrays;

/**
 * Created by ttiganik on 07/06/16.
 */
public class Incoming extends Command {
    public static Incoming create(byte[] bytes) throws CommandParseException {
        if (bytes.length > 1) {
            if (ByteUtils.startsWith(bytes, Auto.CAPABILITIES.getIdentifier())) {
                return new Capabilities(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Auto.VEHICLE_STATUS.getIdentifier())) {
                return new VehicleStatus(bytes);
            }
            else if (ByteUtils.startsWith(bytes, DigitalKey.LOCK_STATE.getIdentifier())) {
                return new LockState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Chassis.WINDSHIELD_HEATING_STATE.getIdentifier())) {
                return new VehicleStatus(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Chassis.ROOFTOP_STATE.getIdentifier())) {
                return new VehicleStatus(bytes);
            }
            else if (ByteUtils.startsWith(bytes, RemoteControl.CONTROL_MODE.getIdentifier())) {
                return new VehicleStatus(bytes);
            }
            else if (ByteUtils.startsWith(bytes, ParcelDelivery.DELIVERED_PARCELS.getIdentifier())) {
                return new VehicleStatus(bytes);
            }
            else {
                throw new CommandParseException();
            }
        }
        else {
            throw new CommandParseException();
        }
    }

    Incoming(byte[] bytes) {
        super(bytes);
    }

    public boolean is(Command.Type type) {
        if (Arrays.equals(getIdentifier(), type.getIdentifier())) {
            return true;
        }

        return false;
    }
}