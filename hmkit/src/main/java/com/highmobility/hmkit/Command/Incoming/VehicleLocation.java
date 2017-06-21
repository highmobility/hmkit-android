package com.highmobility.hmkit.Command.Incoming;

import com.highmobility.hmkit.Command.CommandParseException;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by ttiganik on 16/12/2016.
 *
 * This message is sent when a Get Vehicle Location message is received by the car.
 */
public class VehicleLocation extends IncomingCommand {
    private float latitude;
    private float longitude;

    VehicleLocation(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 11) throw new CommandParseException();

        latitude = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 3, 3 + 4)).getFloat();
        longitude = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 7, 7 + 4)).getFloat();
    }

    /**
     *
     * @return The latitude
     */
    public float getLatitude() {
        return latitude;
    }

    /**
     *
     * @return The longitude
     */
    public float getLongitude() {
        return longitude;
    }
}
