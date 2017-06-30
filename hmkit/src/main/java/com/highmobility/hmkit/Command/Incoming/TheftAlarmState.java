package com.highmobility.hmkit.Command.Incoming;

import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Constants;

/**
 * This is an evented message that is sent from the car every time the theft alarm state changes.
 * This message is also sent when a Get Theft Alarm State message is received by the car.
 */
public class TheftAlarmState extends IncomingCommand {
    public enum State {
        NOT_ARMED, ARMED, TRIGGERED;

        public static State fromByte(byte value) {
            switch (value) {
                case 0x00:
                    return NOT_ARMED;
                case 0x01:
                    return ARMED;
                case 0x02:
                    return TRIGGERED;
            }

            return NOT_ARMED;
        }
    }

    State state;

    /**
     *
     * @return Theft alarm state
     */
    public State getState() {
        return state;
    }

    public TheftAlarmState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 4) throw new CommandParseException();

        state = State.fromByte(bytes[3]);
    }
}