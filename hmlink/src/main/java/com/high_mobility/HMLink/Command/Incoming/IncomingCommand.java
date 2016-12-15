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
        if (bytes.length > 2) { // TODO: this is invalid if VS or capabilities does not use type byte
            if (ByteUtils.startsWith(bytes, Command.Capabilities.CAPABILITIES.getMessageIdentifierAndType())) {
                return new Capabilities(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.Capabilities.CAPABILITY.getMessageIdentifierAndType())) {
                return new Capability(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.VehicleStatus.VEHICLE_STATUS.getMessageIdentifierAndType())) {
                return new VehicleStatus(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.DoorLocks.LOCK_STATE.getMessageIdentifierAndType())) {
                return new LockState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.TrunkAccess.TRUNK_STATE.getMessageIdentifierAndType())) {
                return new TrunkState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.RooftopControl.ROOFTOP_STATE.getMessageIdentifierAndType())) {
                return new RooftopState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.RemoteControl.CONTROL_MODE.getMessageIdentifierAndType())) {
                return new ControlMode(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.DeliveredParcels.DELIVERED_PARCELS.getMessageIdentifierAndType())) {
                return new DeliveredParcels(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.failureIdentifier)) {
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

    byte[] identifier = new byte[3];
    byte[] bytes;

    IncomingCommand(byte[] bytes) {
        this.bytes = bytes;
        identifier[0] = bytes[0];
        identifier[1] = bytes[1];
        identifier[2] = bytes[2]; // TODO: this is invalid if VS or capabilities does not use type byte
    }

    public byte[] getIdentifier() {
        return identifier;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public boolean is(Command.Type type) {
        if (Arrays.equals(getIdentifier(), type.getMessageIdentifierAndType())) {
            return true;
        }

        return false;
    }
}
