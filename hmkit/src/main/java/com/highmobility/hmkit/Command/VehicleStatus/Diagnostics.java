package com.highmobility.hmkit.Command.VehicleStatus;

import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Constants;

import java.util.Arrays;

/**
 * Created by ttiganik on 21/12/2016.
 */
public class Diagnostics extends FeatureState {
    int mileage;
    int oilTemperature;
    int speed;
    int rpm;
    float fuelLevel;
    Constants.WasherFluidLevel washerFluidLevel;

    Diagnostics(byte[] bytes) throws CommandParseException {
        super(Identifier.DIAGNOSTICS);

        if (bytes.length < 14) throw new CommandParseException();

        mileage = ByteUtils.getInt(Arrays.copyOfRange(bytes, 3, 3 + 3));
        oilTemperature = ByteUtils.getInt(Arrays.copyOfRange(bytes, 6, 6 + 2));
        speed = ByteUtils.getInt(Arrays.copyOfRange(bytes, 8, 8 + 2));
        rpm = ByteUtils.getInt(Arrays.copyOfRange(bytes, 10, 10 + 2));
        fuelLevel = (int)bytes[12] / 100f;
        if (bytes[13] == 0x00) washerFluidLevel = Constants.WasherFluidLevel.LOW;
        else washerFluidLevel = Constants.WasherFluidLevel.FULL;
    }

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
    public Constants.WasherFluidLevel getWasherFluidLevel() {
        return washerFluidLevel;
    }
}
