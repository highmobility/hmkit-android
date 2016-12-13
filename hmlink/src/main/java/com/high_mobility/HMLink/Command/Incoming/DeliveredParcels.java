package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.CommandParseException;

import java.util.Arrays;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This is an evented command that is sent from the car every time a new parcel is delivered or
 * removed. This command is also sent when a Get Delivered Parcels is received by the car.
 */
public class DeliveredParcels extends IncomingCommand {
    static final String TAG = "DeliveredParcels";
    String[] deliveredParcels;

    public DeliveredParcels(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 3) throw new CommandParseException();

        int count = bytes[2];

        if (count < 1) return;

        if (bytes.length < 3 + count * 8) throw new CommandParseException();

        deliveredParcels = new String[count];
        for (int i = 0; i < count; i++) {
            byte[] identifier = Arrays.copyOfRange(bytes, 3 + i * 8, 3 + i * 8 + 8);
            deliveredParcels[i] = ByteUtils.hexFromBytes(identifier);
        }
    }

    /**
     *
     * @return Array of tracking numbers of the parcels.
     */
    public String[] getDeliveredParcels() {
        return deliveredParcels;
    }
}
