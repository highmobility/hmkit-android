package com.high_mobility.digitalkey.scan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.high_mobility.HMLink.Constants;
import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Link;
import com.high_mobility.HMLink.LinkListener;
import com.high_mobility.HMLink.Shared.ScannedLink;
import com.high_mobility.HMLink.Manager;
import com.high_mobility.digitalkey.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ttiganik on 02/06/16.
 */
public class ScannedLinkActivity extends AppCompatActivity {
    public final static String TAG = "ScannedLinkActivity";
    public final static String DEVICE_POSITION = "com.high_mobility.DEVICE_POSITION";
    ScannedLink link;
    @BindView(R.id.send_command_button) Button sendButton;
    @BindView(R.id.command_edit_text) EditText commandEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.external_device_view);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        int position = intent.getIntExtra(DEVICE_POSITION, 0);
        link = Manager.getInstance().getScanner().getLinks().get(position);
        link.setListener(listener);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                link.sendCommand(ByteUtils.bytesFromHex(commandEditText.getText().toString()), true, new Constants.DataResponseCallback() {
                    @Override
                    public void response(byte[] bytes, Link.exception) {
                        Log.d(TAG, "command response " + ByteUtils.hexFromBytes(bytes) + " " + (exception != null ? exception.code : ""));
                    }
                });
            }
        });

    }

    private LinkListener listener = new LinkListener() {
        @Override
        public void onStateChanged(Link link, Link.State oldState) {
            final ScannedLink scannedLink = (ScannedLink) link;
            if (link.getState() == Link.State.CONNECTED && scannedLink.versionInfo() == null) {
                scannedLink.readVersionInfo(new Constants.ResponseCallback() {
                    @Override
                    public void response(Link.exception) {
                        Log.d(TAG, "version info " + scannedLink.versionInfo());
                    }
                });
            }
        }

        @Override
        public byte[] onCommandReceived(Link link, byte[] bytes) {
            return new byte[] { 0x01, bytes.length > 0 ? bytes[0] : (byte)0x99 };
        }
    };
}
