package com.high_mobility.HMLink;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.Calendar;

/**
 * The ConnectedLink is a representation of the connection between the Broadcaster and a Device
 * that has connected to it. The ConnectedLink is created by the other Device via discovering
 * and connecting to the Broadcaster. The ConnectedLink's interface provides the ability
 * to send commands and handle incoming requests from the ConnectedLink.
 *
 * Created by ttiganik on 13/04/16.
 */
public class ConnectedLink extends Link {
    Broadcaster broadcaster;

    ConnectedLink(BluetoothDevice btDevice, Broadcaster broadcaster) {
        super(broadcaster.manager, btDevice);
        this.broadcaster = broadcaster;
    }

    /**
     * In order to receive ConnectedLink events, a listener must be set.
     *
     * @param listener The listener instance to receive ConnectedLink events.
     */
    public void setListener(ConnectedLinkListener listener) {
        this.listener = listener;
    }

    int pairingResponse = -1;
    int didReceivePairingRequest() {
        if (listener == null) {
            Log.e(Broadcaster.TAG, "link listener not set");
            return 1;
        }

        final ConnectedLink reference = this;
        pairingResponse = -1;
        broadcaster.manager.mainHandler.post(new Runnable() {
            @Override
            public void run() {
                ((ConnectedLinkListener) listener).onPairingRequested(reference, new Constants.ApprovedCallback() {
                    @Override
                    public void approve() {
                        pairingResponse = 0;
                    }

                    @Override
                    public void decline() {
                        pairingResponse = 1;
                    }
                });
            }
        });

        Calendar c = Calendar.getInstance();
        int startSeconds = c.get(Calendar.SECOND);

        while (pairingResponse < 0) {
            int passedSeconds = Calendar.getInstance().get(Calendar.SECOND);
            if (passedSeconds - startSeconds > Constants.registerTimeout) {
                if (listener != null) {
                    broadcaster.manager.mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((ConnectedLinkListener) listener).onPairingRequestTimeout(reference);
                        }
                    });

                    if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                        Log.d(Broadcaster.TAG, "pairing timer exceeded");
                    return 1; // TODO: use correct code
                }
            }
        }

        return pairingResponse;
    }
}
