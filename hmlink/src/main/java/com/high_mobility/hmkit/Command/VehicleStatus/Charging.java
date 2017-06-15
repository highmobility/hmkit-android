package com.high_mobility.hmkit.Command.VehicleStatus;

import com.high_mobility.hmkit.Command.CommandParseException;
import com.high_mobility.hmkit.Command.Constants;
import com.high_mobility.hmkit.Command.Command.Identifier;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by ttiganik on 16/12/2016.
 */

public class Charging extends FeatureState {
    Constants.ChargingState chargingState;
    float estimatedRange;
    float batteryLevel;
    float batteryCurrent;

    Charging(byte[] bytes) throws CommandParseException {
        super(Identifier.CHARGING);

        if (bytes.length != 11) throw new CommandParseException();

        chargingState = Constants.ChargingState.fromByte(bytes[3]);
        estimatedRange = ((bytes[4] & 0xff) << 8) | (bytes[5] & 0xff);
        batteryLevel = bytes[6] / 100f;
        byte[] batteryCurrentBytes = Arrays.copyOfRange(bytes, 7, 7 + 4);
        batteryCurrent = ByteBuffer.wrap(batteryCurrentBytes).getFloat();
    }

    public Constants.ChargingState getChargingState() {
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
