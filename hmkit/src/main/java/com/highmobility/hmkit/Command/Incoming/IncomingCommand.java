package com.highmobility.hmkit.Command.Incoming;

import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.Command;
import com.highmobility.hmkit.Command.CommandParseException;

import java.util.Arrays;

/**
 * Created by ttiganik on 07/06/16.
 */
public class IncomingCommand {
    public static IncomingCommand create(byte[] bytes) throws CommandParseException {
        if (bytes.length > 2) {
            if (ByteUtils.startsWith(bytes, Command.Capabilities.CAPABILITIES.getIdentifierAndType())) {
                return new Capabilities(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.Capabilities.CAPABILITY.getIdentifierAndType())) {
                return new Capability(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.VehicleStatus.VEHICLE_STATUS.getIdentifierAndType())) {
                return new VehicleStatus(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.DoorLocks.LOCK_STATE.getIdentifierAndType())) {
                return new LockState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.TrunkAccess.TRUNK_STATE.getIdentifierAndType())) {
                return new TrunkState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.Charging.CHARGE_STATE.getIdentifierAndType())) {
                return new ChargeState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.Climate.CLIMATE_STATE.getIdentifierAndType())) {
                return new ClimateState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.RooftopControl.ROOFTOP_STATE.getIdentifierAndType())) {
                return new RooftopState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.RemoteControl.CONTROL_MODE.getIdentifierAndType())) {
                return new ControlMode(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.ValetMode.VALET_MODE.getIdentifierAndType())) {
                return new ValetMode(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.VehicleLocation.VEHICLE_LOCATION.getIdentifierAndType())) {
                return new VehicleLocation(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.DeliveredParcels.DELIVERED_PARCELS.getIdentifierAndType())) {
                return new DeliveredParcels(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.FailureMessage.FAILURE_MESSAGE.getIdentifierAndType())) {
                return new Failure(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.Diagnostics.DIAGNOSTICS_STATE.getIdentifierAndType())) {
                return new Diagnostics(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.Maintenance.MAINTENANCE_STATE.getIdentifierAndType())) {
                return new Maintenance(bytes);
            }
            else {
                throw new CommandParseException();
            }
        }
        else {
            throw new CommandParseException();
        }
    }

    Command.Identifier feature;
    byte type;
    byte[] bytes;

    IncomingCommand(byte[] bytes) throws CommandParseException {
        if (bytes.length < 3) throw new CommandParseException();
        this.bytes = bytes;
        feature = Command.Identifier.fromIdentifier(bytes);
        type = bytes[2];
    }

    public Command.Identifier getIdentifier() {
        return feature;
    }

    byte getType() {
        return type;
    }

    byte[] getIdentifierAndType() {
        return ByteUtils.concatBytes(feature.getIdentifier(), type);
    }

    byte[] getBytes() {
        return bytes;
    }

    /**
     *
     * @param type The type to compare the command with.
     * @return True if the command has the given type.
     */
    public boolean is(Command.Type type) {
        if (Arrays.equals(getIdentifierAndType(), type.getIdentifierAndType())) {
            return true;
        }

        return false;
    }
}
