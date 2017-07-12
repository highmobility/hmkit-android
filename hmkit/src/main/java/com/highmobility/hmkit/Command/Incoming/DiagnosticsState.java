package com.highmobility.hmkit.Command.Incoming;

import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Constants;

import java.util.Arrays;

/**
 * Created by root on 6/28/17.
 */
public class DiagnosticsState extends IncomingCommand {
    public enum WasherFluidLevel { LOW, FULL }

    int mileage;
    int oilTemperature;
    int speed;
    int rpm;
    float fuelLevel;
    WasherFluidLevel washerFluidLevel;

    Float frontRightTirePressure;
    Float frontLeftTirePressure;
    Float rearRightTirePressure;
    Float rearLeftTirePressure;

    /**
     *
     * @return The car mileage (odometer) in km
     */
    public int getMileage() {
        return mileage;
    }

    /**
     *
     * @return Engine oil temperature in Celsius, whereas can be negative
     */
    public int getOilTemperature() {
        return oilTemperature;
    }

    /**
     *
     * @return The car speed in km/h, whereas can be negative
     */
    public int getSpeed() {
        return speed;
    }

    /**
     *
     * @return RPM of the Engine
     */
    public int getRpm() {
        return rpm;
    }

    /**
     *
     * @return Fuel level percentage between 0-100
     */
    public float getFuelLevel() {
        return fuelLevel;
    }

    /**
     *
     * @return Washer fluid level
     */
    public WasherFluidLevel getWasherFluidLevel() {
        return washerFluidLevel;
    }

    /**
     *
     * @return The front right tire pressure in BAR
     *          null if there is no info
     */
    public Float getFrontRightTirePressure() {
        return frontRightTirePressure;
    }

    /**
     *
     * @return The front left tire pressure in BAR
     *          null if there is no info
     */
    public Float getFrontLeftTirePressure() {
        return frontLeftTirePressure;
    }

    /**
     *
     * @return The rear right tire pressure in BAR
     *          null if there is no info
     */
    public Float getRearRightTirePressure() {
        return rearRightTirePressure;
    }

    /**
     *
     * @return The rear left tire pressure in BAR
     *          null if there is no info
     */
    public Float getRearLeftTirePressure() {
        return rearLeftTirePressure;
    }

    public DiagnosticsState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 13) throw new CommandParseException();

        mileage = ByteUtils.getInt(Arrays.copyOfRange(bytes, 3, 3 + 3));
        oilTemperature = ByteUtils.getInt(Arrays.copyOfRange(bytes, 6, 6 + 2));
        speed = ByteUtils.getInt(Arrays.copyOfRange(bytes, 8, 8 + 2));
        rpm = ByteUtils.getInt(Arrays.copyOfRange(bytes, 10, 10 + 2));
        fuelLevel = (int)bytes[12] / 100f;
        if (bytes[13] == 0x00) washerFluidLevel = WasherFluidLevel.LOW;
        else washerFluidLevel = WasherFluidLevel.FULL;

        int numberOfTires = bytes[14];
        int position = 15;

        for (int i = 0; i < numberOfTires; i++) {
            byte location = bytes[position];
            Float pressure = ByteUtils.getFloat(Arrays.copyOfRange(bytes, position + 1, position + 1 + 4));

            if (location == 0x00) {
                frontLeftTirePressure = pressure;
            }
            else if (location == 0x01) {
                frontRightTirePressure = pressure;
            }
            else if (location == 0x02) {
                rearRightTirePressure = pressure;
            }
            else if (location == 0x03) {
                rearLeftTirePressure = pressure;
            }

            position += 5;
        }
    }
}
