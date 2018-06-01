package com.highmobility.hmkit;

import android.bluetooth.le.AdvertiseSettings;

import com.highmobility.value.DeviceSerial;

public class BroadcastConfiguration {
    private int advertiseMode = AdvertiseSettings.ADVERTISE_MODE_BALANCED;
    private int txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;
    private DeviceSerial broadcastTarget = null;
    private boolean overrideAdvertisementName = true;

    public BroadcastConfiguration() {

    }

    /**
     * @return The advertise mode
     */
    public int getAdvertiseMode() {
        return advertiseMode;
    }

    /**
     * @return The advertise TX Power level
     */
    public int getTxPowerLevel() {
        return txPowerLevel;
    }

    /**
     * @return The broadcasters targets serial number
     */
    public DeviceSerial getBroadcastTarget() {
        return broadcastTarget;
    }

    /**
     * @return Whether HM name is
     */
    public boolean isOverridingAdvertisementName() {
        return overrideAdvertisementName;
    }

    BroadcastConfiguration(Builder builder) {
        this.advertiseMode = builder.advertiseMode;
        this.txPowerLevel = builder.txPowerLevel;
        this.broadcastTarget = builder.broadcastTarget;
        this.overrideAdvertisementName = builder.overridesAdvertisementName;
    }

    public static final class Builder {
        private int advertiseMode = AdvertiseSettings.ADVERTISE_MODE_BALANCED;
        private int txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;
        private DeviceSerial broadcastTarget = null;
        private boolean overridesAdvertisementName = true;

        /**
         * Sets the advertise mode for the Bluetooth's AdvertiseSettings. Default is
         * ADVERTISE_MODE_BALANCED.
         *
         * @param advertiseMode the advertise mode
         * @return The
         * @see AdvertiseSettings
         */
        public Builder setAdvertiseMode(int advertiseMode) {
            if (advertiseMode > AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
                    || advertiseMode < AdvertiseSettings.ADVERTISE_MODE_LOW_POWER) return this;
            this.advertiseMode = advertiseMode;
            return this;
        }

        /**
         * Sets the TX power level for the Bluetooth's AdvertiseSettings. Default is
         * ADVERTISE_TX_POWER_HIGH.
         *
         * @param txPowerLevel the TX power level
         * @see AdvertiseSettings
         */
        public Builder setTxPowerLevel(int txPowerLevel) {
            if (txPowerLevel > AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
                    || txPowerLevel < AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW) return this;
            this.txPowerLevel = txPowerLevel;
            return this;
        }

        /**
         * Sets the given serial number in the broadcast info, so other devices know before
         * connecting if this device is interesting to them or not. This is not required to be set.
         *
         * @param serial the serial set in the broadcast info
         */
        public Builder setBroadcastingTarget(DeviceSerial serial) {
            this.broadcastTarget = serial;
            return this;
        }

        /**
         * Indicates whether HMKit will overwrite the phone's bluetooth name. By default this
         * behaviour is on. If this is false the Chrome emulator will not recognize the device.
         *
         * @param overridesAdvertisementName Indication on whether to overwrite the phone's bluetooth name
         */
        public Builder setOverridesAdvertisementName(boolean overridesAdvertisementName) {
            this.overridesAdvertisementName = overridesAdvertisementName;
            return this;
        }

        public BroadcastConfiguration build() {
            return new BroadcastConfiguration(this);
        }
    }
}
