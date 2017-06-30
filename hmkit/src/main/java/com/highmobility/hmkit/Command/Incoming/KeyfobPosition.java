package com.highmobility.hmkit.Command.Incoming;
import com.highmobility.hmkit.Command.CommandParseException;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This is an evented message that is sent by the car every time the relative position of the
 * keyfob changes. This message is also sent when a Get Keyfob Position message is received by the
 * car.
 */
public class KeyfobPosition extends IncomingCommand {
    public enum Position {
        OUT_OF_RANGE, OUTSIDE_DRIVER_SIDE, OUTSIDE_IN_FRONT_OF_CAR, OUTSIDE_PASSENGER_SIDE,
        OUTSIDE_BEHIND_CAR, INSIDE_CAR;

        static Position fromByte(byte value) {
            switch (value) {
                case 0x00:
                    return OUT_OF_RANGE;
                case 0x01:
                    return OUTSIDE_DRIVER_SIDE;
                case 0x02:
                    return OUTSIDE_IN_FRONT_OF_CAR;
                case 0x03:
                    return OUTSIDE_PASSENGER_SIDE;
                case 0x04:
                    return OUTSIDE_BEHIND_CAR;
                case 0x05:
                    return INSIDE_CAR;
            }

            return OUT_OF_RANGE;
        }
    }

    Position position;

    /**
     *
     * @return Keyfob relative position to the car
     */
    public Position getPosition() {
        return position;
    }

    public KeyfobPosition(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 4) throw new CommandParseException();

        position = Position.fromByte(bytes[3]);
    }
}
