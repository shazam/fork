/*
 * Copyright 2014 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.client;

import android.content.Context;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static com.shazam.fork.client.AndroidUtils.isJellyBeanMr2OrAbove;
import static com.shazam.fork.client.FileUtils.saveFile;
import static java.util.UUID.randomUUID;

public class Fork {

    private static final String SCREENSHOT = "screenshot";

    public static File screenshot(Context context) {
        if (isJellyBeanMr2OrAbove()) {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            File target = new File(context.getCacheDir(), randomUUID().toString() + ".jpg");
            if (device.takeScreenshot(target)) {
                return saveAttachment(SCREENSHOT, target);
            } else {
                throw new RuntimeException("Couldn't snap screenshot.");
            }
        } else {
            Log.w("Fork", "Screenshot is only available on android 4.3 and above.");
            return null;
        }
    }

    private static File saveAttachment(String folder, File file) {
        try {
            return saveFile(folder, file);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't save file " + file);
        }
    }
}
