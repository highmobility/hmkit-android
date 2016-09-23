package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 13/09/16.
 *
 * This is an evented command that is sent from the car every time the windshield heating state
 * changes. This command is also sent when a Get Windshield Heating State is received by the car.
 * The state is Active when the heating is turned on.
 */
public class WindshieldHeatingState extends IncomingCommand {
    boolean active;

    public WindshieldHeatingState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 3) throw new CommandParseException();

        active = bytes[2] != 0x00;
    }

    /**
     *
     * @return whether the windshield heating state is active or not
     */
    public boolean isActive() {
        return active;
    }
}