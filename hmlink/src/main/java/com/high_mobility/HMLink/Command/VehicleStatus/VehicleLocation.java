package com.high_mobility.HMLink.Command.VehicleStatus;

import com.high_mobility.HMLink.Command.VehicleFeature;

/**
 * Created by ttiganik on 16/12/2016.
 */

public class VehicleLocation extends FeatureState {
    VehicleLocation(byte[] bytes) {
        super(VehicleFeature.VEHICLE_LOCATION);
        // TODO:
    }
}
