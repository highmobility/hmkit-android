package com.highmobility.hmkit.Command.Incoming;
import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.CommandParseException;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This is an evented message that is sent from the car every time the parking ticket state changes.
 * This message is also sent when a Get Parking Ticket message is received by the car. The state is
 * 0x00 Ended also when the parking ticket has never been set. Afterwards the car always keeps the
 * last parking ticket information.
 */
public class ParkingTicket extends IncomingCommand {
    public enum State {
        STARTED, ENDED;
        public static State fromByte(byte value) {
            switch (value) {
                case 0x00:
                    return ENDED;
                case 0x01:
                    return STARTED;
            }

            return STARTED;
        }
    }

    String operatorName;
    int operatorTicketId;
    Date ticketStart;
    Date ticketEnd;
    State state;

    /**
     *
     * @return The ticket state
     */
    public State getState() {
        return state;
    }

    /**
     *
     * @return The operator name
     */
    public String getOperatorName() {
        return operatorName;
    }

    /**
     *
     * @return The ticket id
     */
    public int getOperatorTicketId() {
        return operatorTicketId;
    }

    /**
     *
     * @return Ticket start date
     */
    public Date getTicketStartDate() {
        return ticketStart;
    }

    /**
     *
     * @return Ticket end date. null if not set
     */
    public Date getTicketEndDate() {
        return ticketEnd;
    }

    public ParkingTicket(byte[] bytes) throws CommandParseException {
        super(bytes);
        if (bytes.length < 4) throw new CommandParseException();

        state = State.fromByte(bytes[3]);
        int operatorNameSize = bytes[4];
        int position = 5;
        try {
            operatorName = new String(Arrays.copyOfRange(bytes, position, position + operatorNameSize), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new CommandParseException();
        }

        int ticketIdSize = bytes[position + operatorNameSize];
        position = position + operatorNameSize + 1;

        operatorTicketId = ByteUtils.getInt(Arrays.copyOfRange(bytes, position, position + ticketIdSize));
        position = position + ticketIdSize;

        ticketStart = ByteUtils.getDate(Arrays.copyOfRange(bytes, position, position + 6));
        position = position + 6;
        ticketEnd = ByteUtils.getDate(Arrays.copyOfRange(bytes, position, position + 6));
    }
}
