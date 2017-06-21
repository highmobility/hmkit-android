package com.highmobility.hmkit.Command.Incoming;

import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.AutoHvacState;
import com.highmobility.hmkit.Command.CommandParseException;

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
    float defrostingTemperature;
    boolean autoHvacConstant;
    AutoHvacState[] autoHvacStates;

    ClimateState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 41) throw new CommandParseException();

        insideTemperature = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 3, 3 + 4)).getFloat();
        outsideTemperature = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 7, 7 + 4)).getFloat();
        driverTemperatureSetting = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 11, 11 + 4)).getFloat();
        passengerTemperatureSetting = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 15, 15 + 4)).getFloat();

        hvacActive = bytes[19] == 0x00 ? false : true;
        defoggingActive = bytes[20] == 0x00 ? false : true;
        defrostingActive = bytes[21] == 0x00 ? false : true;
        defrostingTemperature = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 22, 22 + 4)).getFloat();

        int hvacActiveOnDays = bytes[26];

        autoHvacConstant = ByteUtils.getBit(hvacActiveOnDays, 7);
        autoHvacStates = new AutoHvacState[7];

        for (int i = 0; i < 7; i ++) {
            boolean active = ByteUtils.getBit(hvacActiveOnDays, i);
            int hour = bytes[27 + i * 2];
            int minute = bytes[27 + i * 2 + 1];
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
     * @return The defrosting temperature
     */
    public float getDefrostingTemperature() {
        return defrostingTemperature;
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

    public void setDefrostingTemperature(float defrostingTemperature) {
        this.defrostingTemperature = defrostingTemperature;
    }
}
