package com.high_mobility.HMLink.Shared;

import android.content.Context;

import com.high_mobility.btcore.HMBTCore;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ttiganik on 03/08/16.
 */
public class Shared {
    public HMBTCore core = new HMBTCore();
    public SharedBle ble;
    static Shared instance;
    public BTCoreInterface coreInterface;

    public ExternalDeviceManager externalDeviceManager;
    public LocalDevice localDevice;
    Timer t;

    public static Shared getInstance(Context context) {
        if (instance == null) {
            instance = new Shared();
            instance.ble = new SharedBle(context);
        }

        return instance;
    }

    public void startClock() {
        if (t != null) return;

        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                core.HMBTCoreClock(coreInterface);
            }
        }, 0, 1000);
    }

    Shared() {
        coreInterface = new BTCoreInterface(this);
        core.HMBTCoreInit(coreInterface);
    }
}
