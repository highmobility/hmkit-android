package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 14/09/16.
 *
 * This command is sent when a Get Capabilities command is received by the car.
 */
public class Capabilities extends IncomingCommand {

    public enum TrunkAccessCapability {
        UNAVAILABLE, AVAILABLE, GET_STATE_AVAILABLE, GET_STATE_LOCK_AVAILABLE, GET_STATE_POSITION_AVAILABLE
    }

    public enum Available {
        UNAVAILABLE, AVAILABLE
    }

    public enum AvailableGetState {
        UNAVAILABLE, AVAILABLE, GET_STATE_AVAILABLE
    }

    AvailableGetState doorLocksCapability;
    Capabilities.TrunkAccessCapability trunkAccessCapability;
    AvailableGetState windshieldHeatingCapability;
    AvailableGetState rooftopCapability;
    Available remoteControlCapability;
    Available heartRateCapability;
    Available setDestinationCapability;

    public AvailableGetState getDoorLocksCapability() {
        return doorLocksCapability;
    }

    public TrunkAccessCapability getTrunkAccessCapability() {
        return trunkAccessCapability;
    }

    public AvailableGetState getWindshieldHeatingCapability() {
        return windshieldHeatingCapability;
    }

    public AvailableGetState getRooftopCapability() {
        return rooftopCapability;
    }

    public Available getRemoteControlCapability() {
        return remoteControlCapability;
    }

    public Available getHeartRateCapability() {
        return heartRateCapability;
    }

    public Available getSetDestinationCapability() {
        return setDestinationCapability;
    }

    public Available getDeliveredParcelsCapability() {
        return deliveredParcelsCapability;
    }

    Available deliveredParcelsCapability;

    public Capabilities(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 10) throw new CommandParseException();

        doorLocksCapability = getStateCapability(bytes[2]);

        switch (bytes[3]) {
            case 0x00: trunkAccessCapability = TrunkAccessCapability.UNAVAILABLE; break;
            case 0x01: trunkAccessCapability = TrunkAccessCapability.AVAILABLE; break;
            case 0x02: trunkAccessCapability = TrunkAccessCapability.GET_STATE_AVAILABLE; break;
            case 0x03: trunkAccessCapability = TrunkAccessCapability.GET_STATE_LOCK_AVAILABLE; break;
            case 0x04: trunkAccessCapability = TrunkAccessCapability.GET_STATE_POSITION_AVAILABLE; break;
            default: throw new CommandParseException();
        }

        windshieldHeatingCapability = getStateCapability(bytes[4]);
        rooftopCapability = getStateCapability(bytes[5]);
        remoteControlCapability = availableCapability(bytes[6]);
        heartRateCapability = availableCapability(bytes[7]);
        setDestinationCapability = availableCapability(bytes[8]);
        deliveredParcelsCapability = availableCapability(bytes[9]);
    }

    static AvailableGetState getStateCapability(byte value) throws CommandParseException {
        switch (value) {
            case 0x00: return AvailableGetState.UNAVAILABLE;
            case 0x01: return AvailableGetState.AVAILABLE;
            case 0x02: return AvailableGetState.GET_STATE_AVAILABLE;
            default: throw new CommandParseException();
        }
    }

    static Available availableCapability(byte value) throws CommandParseException {
        switch (value) {
            case 0x00: return Available.UNAVAILABLE;
            case 0x01: return Available.AVAILABLE;
            default: throw new CommandParseException();
        }
    }
}

