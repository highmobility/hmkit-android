package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.Command;
import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.VehicleFeature;

import java.util.Arrays;

/**
 * Created by ttiganik on 07/06/16.
 */
public class IncomingCommand {
    public static IncomingCommand create(byte[] bytes) throws CommandParseException {
        if (bytes.length > 2) {
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
            else if (ByteUtils.startsWith(bytes, Command.Charging.CHARGE_STATE.getMessageIdentifierAndType())) {
                return new ChargeState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.Climate.CLIMATE_STATE.getMessageIdentifierAndType())) {
                return new ClimateState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.RooftopControl.ROOFTOP_STATE.getMessageIdentifierAndType())) {
                return new RooftopState(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.RemoteControl.CONTROL_MODE.getMessageIdentifierAndType())) {
                return new ControlMode(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.ValetMode.VALET_MODE.getMessageIdentifierAndType())) {
                return new ValetMode(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.VehicleLocation.VEHICLE_LOCATION.getMessageIdentifierAndType())) {
                return new VehicleLocation(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.DeliveredParcels.DELIVERED_PARCELS.getMessageIdentifierAndType())) {
                return new DeliveredParcels(bytes);
            }
            else if (ByteUtils.startsWith(bytes, Command.FailureMessage.FAILURE_MESSAGE.getMessageIdentifierAndType())) {
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

    VehicleFeature feature;
    byte type;
    byte[] bytes;

    IncomingCommand(byte[] bytes) throws CommandParseException {
        if (bytes.length < 3) throw new CommandParseException();
        this.bytes = bytes;
        feature = VehicleFeature.fromIdentifier(bytes);
        type = bytes[2];
    }

    public VehicleFeature getFeature() {
        return feature;
    }

    public byte getType() {
        return type;
    }

    public byte[] getIdentifierAndType() {
        return ByteUtils.concatBytes(feature.getIdentifier(), type);
    }

    public byte[] getBytes() {
        return bytes;
    }

    public boolean is(Command.Type type) {
        if (Arrays.equals(getIdentifierAndType(), type.getMessageIdentifierAndType())) {
            return true;
        }

        return false;
    }
}
