package com.highmobility.hmkit.Command.VehicleStatus;

import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.CommandParseException;

/**
 * Created by ttiganik on 21/12/2016.
 */

public class Diagnostics extends FeatureState {
    public enum WasherFluidLevel { LOW, FULL }
    int mileage;
    int oilTemperature;
    int speed;
    int rpm;
    float fuelLevel;
    WasherFluidLevel washerFluidLevel;

    Diagnostics(byte[] bytes) throws CommandParseException {
        super(Identifier.DIAGNOSTICS);

        // TODO: implement when there is an example

        //if (bytes.length != 13) throw new CommandParseException();

//        dimmingPercentage =  (int)bytes[3] / 100f;
//        openPercentage =  (int)bytes[4] / 100f;

    }

    public int getMileage() {
        return mileage;
    }

    public int getOilTemperature() {
        return oilTemperature;
    }

    public int getSpeed() {
        return speed;
    }

    public int getRpm() {
        return rpm;
    }

    public float getFuelLevel() {
        return fuelLevel;
    }

    public WasherFluidLevel getWasherFluidLevel() {
        return washerFluidLevel;
    }
}
