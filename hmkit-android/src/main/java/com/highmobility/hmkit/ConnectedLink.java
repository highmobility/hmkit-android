package com.highmobility.hmkit;

import android.bluetooth.BluetoothDevice;

import java.util.Calendar;

import javax.annotation.Nullable;

import static com.highmobility.hmkit.HMLog.d;
import static com.highmobility.hmkit.HMLog.e;

/**
 * The ConnectedLink is a representation of the connection between the Broadcaster and a Device that
 * has connected to it. The ConnectedLink is created by the other Device via discovering and
 * connecting to the Broadcaster.
 * <p>
 * The ConnectedLink inherits from Link, which provides the ability to send and receive commands.
 * ConnectedLink is used to provide the authorization callbacks.
 */
public class ConnectedLink extends Link {
    ConnectedLink(Core core, ThreadManager threadManager, BluetoothDevice btDevice) {
        super(core, threadManager, btDevice);
    }

    /**
     * In order to receive ConnectedLink events, a listener must be set.
     *
     * @param listener The listener instance to receive ConnectedLink events.
     */
    public void setListener(@Nullable ConnectedLinkListener listener) {
        this.listener = listener;
    }

    private int pairingResponse = -1;

    int didReceivePairingRequest() {
        if (listener == null) {
            e("link listener not set");
            return 1;
        }

        final ConnectedLink reference = this;
        pairingResponse = -1;

        threadManager.postToMain(new Runnable() {
            @Override public void run() {
                if (listener == null) {
                    pairingResponse = 1;
                    return;
                }

                ((ConnectedLinkListener) listener).onAuthenticationRequested(reference, new
                        ConnectedLinkListener.AuthenticationRequestCallback() {
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
                    threadManager.postToMain(new Runnable() {
                        @Override public void run() {
                            if (listener == null) return;
                            ((ConnectedLinkListener) listener).onAuthenticationRequestTimeout(reference);
                        }
                    });

                    d("pairing timer exceeded");
                    return 1; // TOD1O: use correct code
                }
            }
        }

        return pairingResponse;
    }
}
