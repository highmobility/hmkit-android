package com.high_mobility.digitalkey;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.high_mobility.btcore.HMBTCore;
import com.high_mobility.btcore.HMBTCoreInterface;
import com.high_mobility.btcore.HMDevice;
import com.high_mobility.digitalkey.HMLink.Broadcasting.Link;
import com.high_mobility.digitalkey.HMLink.Broadcasting.LinkCallback;
import com.high_mobility.digitalkey.HMLink.Broadcasting.LocalDevice;
import com.high_mobility.digitalkey.HMLink.Broadcasting.LocalDeviceCallback;
import com.high_mobility.digitalkey.HMLink.Constants;
import com.high_mobility.digitalkey.HMLink.LinkException;
import com.high_mobility.digitalkey.HMLink.Shared.DeviceCertificate;

import java.util.Random;

public class PeripheralActivity extends Activity implements LocalDeviceCallback, LinkCallback, HMBTCoreInterface {
    private static final byte[] CA_PRIVATE_KEY = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] CA_PUBLIC_KEY = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] CA_APP_IDENTIFIER = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] CA_ISSUER = Utils.bytesFromHex("47494D4F");

    private static final byte[] DEVICE_PUBLIC_KEY = Utils.bytesFromHex("***REMOVED***");
    private static final byte[] DEVICE_PRIVATE_KEY = Utils.bytesFromHex("***REMOVED***");

    private static final String TAG = "PeripheralActivity";

    //LocalDevice device = LocalDevice.getInstance();

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i(TAG, "create");

        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                Log.i(TAG, "did inflate");
            }
        });

        ListView list = new ListView(this);
        setContentView(list);

        HMBTCore core = new HMBTCore();
        core.HMBTCoreInit(this);

        /*setDeviceCertificate();
        device.registerCallback(this);

        try {
            device.startBroadcasting();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "cannot start broadcasting");
        }*/
    }

    @Override
    protected void onDestroy() {
        //device.stopBroadcasting();
        //device.closeGATTServer();

        super.onDestroy();
    }

    private void setDeviceCertificate() {
        DeviceCertificate cert = new DeviceCertificate(CA_ISSUER, CA_APP_IDENTIFIER, getSerial(), DEVICE_PUBLIC_KEY);

        // TODO: add signature to cert

        //device.setDeviceCertificate(cert, DEVICE_PRIVATE_KEY, CA_PUBLIC_KEY, getApplicationContext());
        // TODO: show the serial on screen
    }

    private byte[] getSerial() {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = getApplicationContext().getSharedPreferences("com.hm.wearable.UserPrefs",
                Context.MODE_PRIVATE );
        editor = settings.edit();


        String serialKey = "serialUserDefaultsKey";

        if (settings.contains(serialKey)) {
            return Utils.bytesFromHex(settings.getString(serialKey, ""));
        }
        else {
            byte[] serialBytes = new byte[9];
            new Random().nextBytes(serialBytes);
            editor.putString(serialKey, Utils.hexFromBytes(serialBytes));
            return serialBytes;
        }
    }

    @Override
    public void localDeviceStateChanged(LocalDevice.State state, LocalDevice.State oldState) {

    }

    @Override
    public void localDeviceDidReceiveLink(Link link) {
        Log.i(TAG, "localDeviceDidReceiveLink");
    }

    @Override
    public void localDeviceDidLoseLink(Link link) {
        Log.i(TAG, "localDeviceDidLoseLink");
    }

    @Override
    public void linkStateDidChange(Link link, Link.State oldState) {

    }

    @Override
    public void linkDidExecuteCommand(Link link, Constants.Command command, LinkException exception) {

    }

    @Override
    public byte[] linkDidReceiveCustomCommand(Link link, byte[] bytes) {
        return new byte[0];
    }

    @Override
    public void linkDidReceivePairingRequest(Link link, byte[] serialNumber, Constants.ApprovedCallback approvedCallback, float timeout) {

    }

    @Override
    public int HMBTHalInit() {
        Log.d("CALLBACK","OK");
        return 0;
    }

    @Override
    public int HMBTHalScanStart() {
        return 0;
    }

    @Override
    public int HMBTHalScanStop() {
        return 0;
    }

    @Override
    public int HMBTHalAdvertisementStart() {
        return 0;
    }

    @Override
    public int HMBTHalAdvertisementStop() {
        return 0;
    }

    @Override
    public int HMBTHalConnect(byte[] mac) {
        return 0;
    }

    @Override
    public int HMBTHalDisconnect(byte[] mac) {
        return 0;
    }

    @Override
    public int HMBTHalServiceDiscovery(byte[] mac) {
        return 0;
    }

    @Override
    public int HMBTHalWriteData(byte[] mac, int length, byte[] data) {
        return 0;
    }

    @Override
    public int HMBTHalReadData(byte[] mac, int offset) {
        return 0;
    }

    @Override
    public int HMPersistenceHalgetSerial(byte[] serial) {
        return 0;
    }

    @Override
    public int HMPersistenceHalgetLocalPublicKey(byte[] publicKey) {
        return 0;
    }

    @Override
    public int HMPersistenceHaladdPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, int commandSize, byte[] command) {
        return 0;
    }

    @Override
    public int HMPersistenceHalgetPublicKey(byte[] serial, byte[] publicKey, byte[] startDate, byte[] endDate, byte[] commandSize, byte[] command) {
        return 0;
    }

    @Override
    public int HMPersistenceHalremovePublicKey(byte[] serial) {
        return 0;
    }

    @Override
    public int HMPersistenceHaladdStoredCertificate(byte[] cert, int size) {
        return 0;
    }

    @Override
    public int HMPersistenceHalgetStoredCertificate(byte[] cert, int size) {
        return 0;
    }

    @Override
    public int HMPersistenceHaleraseStoredCertificate() {
        return 0;
    }

    @Override
    public void HMCtwEnteredProximity(HMDevice device) {

    }

    @Override
    public void HMCtwExitedProximity(HMDevice device) {

    }

    @Override
    public void HMCtwCustomCommandReceived(HMDevice device, int data, int length, int error) {

    }

    @Override
    public int HMCtwGetDeviceCertificateFailed(HMDevice device, int nonce) {
        return 0;
    }
}
