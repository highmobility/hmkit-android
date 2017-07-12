package com.highmobility.hmkit.Command.VehicleStatus;

import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Constants;
import com.highmobility.hmkit.Command.Command;
import com.highmobility.hmkit.Command.Incoming.ChargeState;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by ttiganik on 16/12/2016.
 */

public class Charging extends FeatureState {
    ChargeState.ChargingState chargingState;
    float estimatedRange;
    float batteryLevel;
    float batteryCurrent;

    Charging(byte[] bytes) throws CommandParseException {
        super(Command.Identifier.CHARGING);

        if (bytes.length != 11) throw new CommandParseException();

        chargingState = ChargeState.ChargingState.fromByte(bytes[3]);
        estimatedRange = ((bytes[4] & 0xff) << 8) | (bytes[5] & 0xff);
        batteryLevel = bytes[6] / 100f;
        byte[] batteryCurrentBytes = Arrays.copyOfRange(bytes, 7, 7 + 4);
        batteryCurrent = ByteBuffer.wrap(batteryCurrentBytes).getFloat();
    }

    public ChargeState.ChargingState getChargingState() {
        return chargingState;
    }

    /**
     *
     * @return Estimated range in km
     */
    public float getEstimatedRange() {
        return estimatedRange;
    }

    /**
     *
     * @return Battery level percentage
     */
    public float getBatteryLevel() {
        return batteryLevel;
    }

    /**
     *
     * @return Battery current
     */
    public float getBatteryCurrent() {
        return batteryCurrent;
    }
}
