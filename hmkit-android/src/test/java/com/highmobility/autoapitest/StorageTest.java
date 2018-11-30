package com.highmobility.autoapitest;

import com.highmobility.hmkit.Storage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import androidx.test.core.app.ApplicationProvider;

import static junit.framework.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class StorageTest  {


    @Test public void testCertAdded() {
        /*
        SharedPreferences sharedPreferences = ShadowPreferenceManager.getDefaultSharedPreferences(Robolectric.application.getApplicationContext());
        sharedPreferences.edit().putString("testId", "12345").commit();


        SharedPreferences sharedPreferences = Robolectric.application.getSharedPreferences("you_custom_pref_name", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("testId", "12345").commit();

         */
        Storage storage = new Storage(ApplicationProvider.getApplicationContext());
    }

    @Test public void testCertDeleted() {
        fail();
    }

    @Test public void testMultipleCertsWithSameSerialDeleted() {
        fail();
    }
}
