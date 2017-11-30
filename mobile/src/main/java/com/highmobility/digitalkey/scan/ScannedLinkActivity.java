package com.highmobility.digitalkey.scan;

import android.support.v7.app.Activity;

/**
 * Created by ttiganik on 02/06/16.
 */
public class ScannedLinkActivity extends Activity /*{
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
                link.sendCommand(Bytes.bytesFromHex(commandEditText.getText().toString()), true, new Constants.DataResponseCallback() {
                    @Override
                    public void response(byte[] bytes, Link.exception) {
                        Log.d(TAG, "command response " + Bytes.hexFromBytes(bytes) + " " + (exception != null ? exception.code : ""));
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
*/ {}