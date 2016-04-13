package com.high_mobility.digitalkey.MajesticLink.Shared;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by ttiganik on 13/04/16.
 */
public class AccessCertificate extends Certificate {
    // TODO:
    public byte[] gainerPublicKey;
    public byte[] gainerSerial;
    public byte[] providerSerial;

    public Date permissionsValidityStartDate;
    public Date permissionsValidityEndDate;
    public byte[] permissions;

    public byte[] bytes;
    public byte[] signature;
/*
    // bytes without the signature
    public Certificate(byte[] bytes) {
        this.gainerSerial = Arrays.copyOfRange(bytes, 0, 9);
        this.gainerPublicKey = Arrays.copyOfRange(bytes, 9, 73);
        this.providerSerial = Arrays.copyOfRange(bytes, 73, 82);

        byte[] startPeriod = Arrays.copyOfRange(bytes, 82, 87);
        byte[] endPeriod = Arrays.copyOfRange(bytes, 87, 92);

        this.permissionsValidityStartDate = dateFromBytes(startPeriod);
        this.permissionsValidityEndDate = dateFromBytes(endPeriod);

        if (bytes.length > 92) {
            this.permissions = Arrays.copyOfRange(bytes, 93, bytes.length);
        }
    }

    public Certificate(byte[] gainerSerial, byte[] gainerPubKey,
                       byte[] providerSN, byte[] permissions) {
        // serials / keys
        if (gainerSerial.length != 9) {
            System.out.println("Invalid wearable serial");
            return;
        }

        if (gainerPubKey.length != 64) {
            System.out.println("Invalid wearable public key");
            return;
        }

        if (providerSN.length != 9) {
            System.out.println("Invalid car serial");
            return;
        }

        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400 * 1000);
        SimpleDateFormat formatter = new SimpleDateFormat("YYMMddHHmm");

        String startDateString = formatter.format(startDate);
        String endDateString = formatter.format(endDate);

        try {
            File temp = new File("/tmp/hmcrypto");

            Files.copy(CAMock.class.getResource("/global/resources/hmcrypto").openStream()
                    , temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            temp.setExecutable(true);

            ProcessBuilder builder = new ProcessBuilder(
                    temp.getAbsolutePath(),
                    "cert",
                    Utils.byteArrayToHexString(gainerSerial),
                    Utils.byteArrayToHexString(gainerPubKey),
                    Utils.byteArrayToHexString(providerSN),
                    startDateString,
                    endDateString,
                    permissions != null ? Utils.byteArrayToHexString(permissions) : "",
                    "r");

            builder.directory(new File(temp.getParent())); // this is where you set the root folder for the executable to run with
            builder.redirectErrorStream(true);
            Process process = builder.start();

            Scanner s = new Scanner(process.getInputStream());
            StringBuilder text = new StringBuilder();
            while (s.hasNextLine()) {
                text.append(s.nextLine());
            }

            s.close();
            int result = process.waitFor();
            byte[] certBytes = Utils.hexStringToByteArray(text.toString());

            this.signature = CAMock.sign(certBytes);
            this.bytes = Utils.concatBytes(certBytes, this.signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Date dateFromBytes(byte[] bytes) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(2000 + bytes[0], bytes[1], bytes[2], bytes[3], bytes[4]);
        return cal.getTime(); // get back a Date object
    }
    */
}
