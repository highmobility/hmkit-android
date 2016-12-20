package com.high_mobility.HMLink.Command.Incoming;

import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.AutoHvacState;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by ttiganik on 16/12/2016.
 *
 * This message is sent when a Get Climate State message is received by the car. It is also
 * sent once the HVAC system has been turned on/off, when the defrosting/defogging states
 * changes or when the profile is updated.
 */
public class ClimateState extends IncomingCommand {
    float insideTemperature;
    float outsideTemperature;
    float driverTemperatureSetting;
    float passengerTemperatureSetting;
    boolean hvacActive;
    boolean defoggingActive;
    boolean defrostingActive;
    boolean autoHvacConstant;
    AutoHvacState[] autoHvacStates;

    ClimateState(byte[] bytes) {
        super(bytes);

        insideTemperature = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 3, 3 + 4)).getFloat();
        outsideTemperature = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 7, 7 + 4)).getFloat();
        driverTemperatureSetting = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 11, 11 + 4)).getFloat();
        passengerTemperatureSetting = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 15, 15 + 4)).getFloat();

        hvacActive = bytes[19] == 0x00 ? false : true;
        defoggingActive = bytes[20] == 0x00 ? false : true;
        defrostingActive = bytes[21] == 0x00 ? false : true;

        int hvacActiveOnDays = bytes[22];

        autoHvacConstant = ByteUtils.getBit(hvacActiveOnDays, 7);
        autoHvacStates = new AutoHvacState[7];

        for (int i = 0; i < 7; i ++) {
            boolean active = ByteUtils.getBit(hvacActiveOnDays, i);
            int hour = bytes[23 + i * 2];
            int minute = bytes[23 + i * 2 + 1];
            autoHvacStates[i] = new AutoHvacState(active, i, hour, minute);
        }
    }

    /**
     *
     * @return Inside temperature in celsius
     */
    public float getInsideTemperature() {
        return insideTemperature;
    }

    /**
     *
     * @return Outside temperature in celsius
     */
    public float getOutsideTemperature() {
        return outsideTemperature;
    }

    /**
     *
     * @return Driver temperature setting in celsius
     */
    public float getDriverTemperatureSetting() {
        return driverTemperatureSetting;
    }

    /**
     *
     * @return Passenger temperature setting in celsius
     */
    public float getPassengerTemperatureSetting() {
        return passengerTemperatureSetting;
    }

    /**
     *
     * @return Whether the HVAC is active or not
     */
    public boolean isHvacActive() {
        return hvacActive;
    }

    /**
     *
     * @return Whether the Defogging is active or not
     */
    public boolean isDefoggingActive() {
        return defoggingActive;
    }

    /**
     *
     * @return Whether Defrosting is active or not
     */
    public boolean isDefrostingActive() {
        return defrostingActive;
    }

    /**
     *
     * @return Whether autoHVAC is constant(based on the car surroundings)
     */
    public boolean isAutoHvacConstant() {
        return autoHvacConstant;
    }

    /**
     *
     * @return Array of AutoHvacState's that describe if and when the AutoHVAC is active.
     */
    public AutoHvacState[] getAutoHvacStates() {
        return autoHvacStates;
    }

}
