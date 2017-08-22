package com.shazam.forktest;

import android.Manifest;

import com.shazam.fork.RevokePermission;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RevokePermissionsClassTest {

    //Annotation `RevokePermission` is in fork. If not found, please build fork  (eg. go to the fork main folder and run `./gradlew assemble` )
    @RevokePermission({Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION})
    @Test
    public void methodAnnotatedWithRevokePermissionsTest() {
        assertEquals(4, 2 + 2);
    }

    @RevokePermission() //This makes no sense but useful to test fork being resiliant..
    @Test
    public void methodAnnotatedWithEmptyRevokePermissionsTest() {
        assertEquals(4, 2 + 2);
    }
}

