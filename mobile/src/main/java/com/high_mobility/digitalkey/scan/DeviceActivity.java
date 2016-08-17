package com.high_mobility.digitalkey.scan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.high_mobility.HMLink.Constants;
import com.high_mobility.HMLink.LinkException;
import com.high_mobility.HMLink.Shared.ByteUtils;
import com.high_mobility.HMLink.Shared.ExternalDevice;
import com.high_mobility.HMLink.Shared.ExternalDeviceListener;
import com.high_mobility.HMLink.Shared.Shared;
import com.high_mobility.digitalkey.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ttiganik on 02/06/16.
 */
public class DeviceActivity extends AppCompatActivity {
    public final static String TAG = "DeviceActivity";
    public final static String DEVICE_POSITION = "com.high_mobility.DEVICE_POSITION";
    ExternalDevice device;
    @BindView(R.id.send_command_button) Button sendButton;
    @BindView(R.id.command_edit_text) EditText commandEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.external_device_view);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        int position = intent.getIntExtra(DEVICE_POSITION, 0);
        device = Shared.getInstance().getExternalDeviceManager().getDevices().get(position);
        device.setListener(listener);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device.sendCommand(ByteUtils.bytesFromHex(commandEditText.getText().toString()), new Constants.DataResponseCallback() {
                    @Override
                    public void response(byte[] bytes, LinkException exception) {
                        Log.d(TAG, "command response " + ByteUtils.hexFromBytes(bytes) + " " + exception.code);
                    }
                });
            }
        });
    }

    private ExternalDeviceListener listener = new ExternalDeviceListener() {
        @Override
        public void onStateChanged(ExternalDevice.State oldState) {

        }

        @Override
        public byte[] onCommandReceived(byte[] command) {
            return new byte[] { 0x01, command.length > 0 ? command[0] : (byte)0x99 };
        }
    };
}
