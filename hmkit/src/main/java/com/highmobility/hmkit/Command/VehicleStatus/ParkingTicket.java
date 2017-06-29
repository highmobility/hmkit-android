package com.highmobility.hmkit.Command.VehicleStatus;
import com.highmobility.hmkit.Command.Command.Identifier;
import com.highmobility.hmkit.Command.CommandParseException;

/**
 * Created by ttiganik on 14/12/2016.
 */

public class ParkingTicket extends FeatureState {
    public ParkingTicket(byte[] bytes) throws CommandParseException {
        super(Identifier.PARKING_TICKET);

        if (bytes.length < 5) throw new CommandParseException();

    }
}