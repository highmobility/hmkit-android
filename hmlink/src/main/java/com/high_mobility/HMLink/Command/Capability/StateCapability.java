package com.high_mobility.HMLink.Command.Capability;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Incoming.VehicleStatus;

/**
 * Created by ttiganik on 14/10/2016.
 */
public class StateCapability {
    public VehicleStatus.State getState() {
        return state;
    }
    VehicleStatus.State state;

    StateCapability(VehicleStatus.State state) {
        this.state = state;
    }

    public static StateCapability fromBytes(byte[] capabilityBytes) throws CommandParseException {
        if (capabilityBytes.length < 3) throw new CommandParseException();

        StateCapability capability = null;
        VehicleStatus.State state = VehicleStatus.State.fromIdentifier(capabilityBytes[0], capabilityBytes[1]);

        if (state == VehicleStatus.State.DOOR_LOCKS ||
                state == VehicleStatus.State.CHARGING ||
                state == VehicleStatus.State.VALET_MODE) {
            capability = new AvailableGetStateCapability(state, capabilityBytes);
        }
        else if (state == VehicleStatus.State.TRUNK_ACCESS) {
            capability = new TrunkAccessCapability(capabilityBytes);
        }
        else if (state == VehicleStatus.State.WAKE_UP
                || state == VehicleStatus.State.REMOTE_CONTROL
                || state == VehicleStatus.State.HEART_RATE
                || state == VehicleStatus.State.VEHICLE_LOCATION
                || state == VehicleStatus.State.NAVI_DESTINATION
                || state == VehicleStatus.State.DELIVERED_PARCELS) {
            capability =  new AvailableCapability(state, capabilityBytes);
        }
        else if (state == VehicleStatus.State.CLIMATE) {
            capability = new ClimateCapability(capabilityBytes);
        }
        else if (state == VehicleStatus.State.ROOFTOP) {
            capability = new RooftopCapability(capabilityBytes);
        }
        else if (state == VehicleStatus.State.HONK_FLASH) {
            capability = new HonkFlashCapability(capabilityBytes);
        }

        return capability;
    }
}
