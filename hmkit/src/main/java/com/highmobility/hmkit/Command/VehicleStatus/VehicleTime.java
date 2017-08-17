package com.highmobility.hmkit.Command.VehicleStatus;
import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.CommandParseException;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class VehicleTime extends FeatureState {
    Calendar time;

    /**
     *
     * @return Vehicle time
     */
    public Calendar getVehicleTime() {
        return time;
    }

    public VehicleTime(byte[] bytes) throws CommandParseException {
        super(Identifier.VEHICLE_TIME);
        if (bytes.length < 4) throw new CommandParseException();
        time = ByteUtils.getCalendar(Arrays.copyOfRange(bytes, 3, 11));
    }
}