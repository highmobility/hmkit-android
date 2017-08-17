package com.highmobility.hmkit.Command.Incoming;
import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.CommandParseException;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This message is sent when a Get Vehicle Time message is received by the car.
 * The local time of the car is returned, hence the UTC timezone offset is included as well.
 */
public class VehicleTime extends IncomingCommand {
    Calendar vehicleTime;

    /**
     *
     * @return Vehicle time
     */
    public Calendar getVehicleTime() {
        return vehicleTime;
    }


    public VehicleTime(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length < 11) throw new CommandParseException();

        vehicleTime = ByteUtils.getCalendar(Arrays.copyOfRange(bytes, 3, 11));
    }
}
