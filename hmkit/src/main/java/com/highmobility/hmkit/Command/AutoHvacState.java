package com.highmobility.hmkit.Command;

/**
 * Created by ttiganik on 20/12/2016.
 */
public class AutoHvacState {
    boolean active;
    int day;
    int startHour;
    int startMinute;

    public AutoHvacState(boolean active, int day, int startHour, int startMinute) {
        this.active = active;
        this.day = day;
        this.startHour = startHour;
        this.startMinute = startMinute;
    }

    /**
     *
     * @return The starting hour of the auto HVAC
     */
    public int getStartHour() {
        return startHour;
    }

    /**
     *
     * @return The starting minute of the auto HVAC
     */
    public int getStartMinute() {
        return startMinute;
    }

    /**
     *
     * @return Whether the auto HVAC is active or not
     */
    public boolean isActive() {
        return active;
    }

    /**
     *
     * @return The weekday from 0 - 6
     */
    public int getDay() {
        return day;
    }
}