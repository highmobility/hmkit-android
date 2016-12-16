package com.high_mobility.HMLink.Command.VehicleStatus;

import com.high_mobility.HMLink.Command.VehicleFeature;

/**
 * Created by ttiganik on 16/12/2016.
 */

public class ValetMode extends FeatureState {
    ValetMode(byte[] bytes) {
        super(VehicleFeature.VALET_MODE);
        // TODO:
    }
}
