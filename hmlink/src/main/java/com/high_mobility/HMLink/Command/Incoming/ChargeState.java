package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.Command.CommandParseException;
import com.high_mobility.HMLink.Command.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by ttiganik on 16/12/2016.
 *
 * This message is sent when a Get Charge State message is received by the car. It is also sent
 * when the car is plugged in, disconnected, starts or stops charging, or when the charge limit
 * is changed.
 */
public class ChargeState extends IncomingCommand {
    Constants.ChargingState chargingState;
    float estimatedRange;
    float batteryLevel;
    float batteryCurrent;
    float chargerVoltage;
    float chargeLimit;
    float timeToCompleteCharge;
    float chargingRate;
    Constants.ChargePortState chargePortState;

    ChargeState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 21) throw new CommandParseException();

        chargingState = Constants.ChargingState.fromByte(bytes[3]);
        estimatedRange = ((bytes[4] & 0xff) << 8) | (bytes[5] & 0xff);
        batteryLevel = bytes[6] / 100f;
        byte[] batteryCurrentBytes = Arrays.copyOfRange(bytes, 7, 7 + 4);
        batteryCurrent = ByteBuffer.wrap(batteryCurrentBytes).getFloat();
        chargerVoltage = ((bytes[11] & 0xff) << 8) | (bytes[12] & 0xff);
        chargeLimit = bytes[13] / 100f;
        timeToCompleteCharge = ((bytes[14] & 0xff) << 8) | (bytes[15] & 0xff);
        byte[] chargingRateBytes = Arrays.copyOfRange(bytes, 16, 16 + 4);
        chargingRate = ByteBuffer.wrap(chargingRateBytes).getFloat();
        chargePortState = Constants.ChargePortState.fromByte(bytes[20]);
    }

    /**
     *
     * @return The Charge State
     */
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
     * @return battery level percentage
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

    /**
     *
     * @return Charger voltage
     */
    public float getChargerVoltage() {
        return chargerVoltage;
    }

    /**
     *
     * @return Charge limit percentage
     */
    public float getChargeLimit() {
        return chargeLimit;
    }

    /**
     *
     * @return The time to complete the charge in minutes
     */
    public float getTimeToCompleteCharge() {
        return timeToCompleteCharge;
    }

    /**
     *
     * @return Charging rate
     */
    public float getChargingRate() {
        return chargingRate;
    }

    /**
     *
     * @return Charge Port State
     */
    public Constants.ChargePortState getChargePortState() {
        return chargePortState;
    }
}
