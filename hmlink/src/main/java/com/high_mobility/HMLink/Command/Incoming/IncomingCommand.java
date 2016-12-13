package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.Command;
import com.high_mobility.HMLink.Command.CommandParseException;

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
            else if (ByteUtils.startsWith(bytes, Command.General.CAPABILITY.getIdentifier())) {
                return new Capability(bytes);
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
                return new WindshieldHeatingState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.Chassis.ROOFTOP_STATE.getIdentifier())) {
                return new RooftopState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.RemoteControl.CONTROL_MODE.getIdentifier())) {
                return new ControlMode(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.ParcelDelivery.DELIVERED_PARCELS.getIdentifier())) {
                return new DeliveredParcels(bytes);
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

    byte[] identifier = new byte[2];
    byte[] bytes;

    IncomingCommand(byte[] bytes) {
        this.bytes = bytes;
        identifier[0] = bytes[0];
        identifier[1] = bytes[1];
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
