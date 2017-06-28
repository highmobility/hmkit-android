package com.highmobility.hmkit.Command.Incoming;

import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.CommandParseException;

import java.util.Arrays;

/**
 * Created by root on 6/28/17.
 */

public class Maintenance extends IncomingCommand {
    private int kilometersToNextService;
    private int daysToNextService;

    /**
     *
     * @return Amount of kilometers until next servicing of the car
     */
    public int getKilometersToNextService() {
        return kilometersToNextService;
    }

    /**
     *
     * @return Number of days until next servicing of the car, whereas negative is overdue
     */
    public int getDaysToNextService() {
        return daysToNextService;
    }

    public Maintenance(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 8) throw new CommandParseException();
        daysToNextService = ByteUtils.getInt(Arrays.copyOfRange(bytes, 3, 3 + 2));
        kilometersToNextService = ByteUtils.getInt(Arrays.copyOfRange(bytes, 5, 5 + 3));
    }
}
