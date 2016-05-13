package com.high_mobility.digitalkey;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by ttiganik on 12/05/16.
 */
public class PairingView extends LinearLayout {
    public static final String LINK_KEY = "LINK_KEY";
    public Button confirmButton;
    public Button declineButton;
    public TextView identifierView;


    public PairingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.pairing_view, this);
        confirmButton = (Button)findViewById(R.id.confirm_register_button);
        declineButton = (Button)findViewById(R.id.decline_register_button);
        identifierView = (TextView)findViewById(R.id.pairing_identifier_textview);
    }
}
