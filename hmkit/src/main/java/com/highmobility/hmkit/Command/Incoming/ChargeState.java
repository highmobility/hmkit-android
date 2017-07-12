package com.highmobility.hmkit.Command.Incoming;

import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Constants;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by ttiganik on 16/12/2016.
 *
 * This message is sent when a Get Charge State message is received by the car. It is also sent
 * when the car is plugged in, disconnected, starts or stops charging, or when the charge limit
 * is changed.
 */
public class ChargeState extends IncomingCommand {
    /**
     * The possible charge states
     */
    public enum ChargingState {
        DISCONNECTED, PLUGGED_IN, CHARGING, CHARGING_COMPLETE;

        public static ChargingState fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return DISCONNECTED;
                case 0x01: return PLUGGED_IN;
                case 0x02: return CHARGING;
                case 0x03: return CHARGING_COMPLETE;
            }

            throw new CommandParseException();
        }
    }

    /**
     * The possible charge port states
     */
    public enum PortState {
        CLOSED, OPEN, UNAVAILABLE;

        public static PortState fromByte(byte value) throws CommandParseException {
            switch (value) {
                case 0x00: return CLOSED;
                case 0x01: return OPEN;
                case (byte)0xFF: return UNAVAILABLE;
            }

            throw new CommandParseException();
        }
    }

    ChargingState chargingState;
    float estimatedRange;
    float batteryLevel;
    float batteryCurrent;
    float chargerVoltage;
    float chargeLimit;
    float timeToCompleteCharge;
    float chargingRate;
    PortState chargePortState;

    /**
     *
     * @return The Charge State
     */
    public ChargingState getChargingState() {
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
    public PortState getChargePortState() {
        return chargePortState;
    }

    ChargeState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 21) throw new CommandParseException();

        chargingState = ChargingState.fromByte(bytes[3]);
        estimatedRange = ((bytes[4] & 0xff) << 8) | (bytes[5] & 0xff);
        batteryLevel = bytes[6] / 100f;
        byte[] batteryCurrentBytes = Arrays.copyOfRange(bytes, 7, 7 + 4);
        batteryCurrent = ByteBuffer.wrap(batteryCurrentBytes).getFloat();
        chargerVoltage = ((bytes[11] & 0xff) << 8) | (bytes[12] & 0xff);
        chargeLimit = bytes[13] / 100f;
        timeToCompleteCharge = ((bytes[14] & 0xff) << 8) | (bytes[15] & 0xff);
        byte[] chargingRateBytes = Arrays.copyOfRange(bytes, 16, 16 + 4);
        chargingRate = ByteBuffer.wrap(chargingRateBytes).getFloat();
        chargePortState = PortState.fromByte(bytes[20]);
    }
}
