/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.runner.listeners;

import com.android.ddmlib.*;
import com.android.ddmlib.testrunner.TestIdentifier;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.shazam.fork.model.*;
import com.shazam.fork.system.io.FileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import static com.shazam.fork.utils.Utils.millisSinceNanoTime;
import static com.shazam.fork.system.io.FileType.ANIMATION;
import static com.shazam.fork.system.io.FileType.SCREENSHOT;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.System.nanoTime;
import static java.util.Arrays.asList;

class ScreenCapturer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ScreenCapturer.class);
    private final IDevice deviceInterface;
    private final FileManager fileManager;
    private final Pool pool;
    private final Device device;
    private final TestIdentifier test;

    private boolean capturing;
    private boolean hasFailed;

    ScreenCapturer(IDevice deviceInterface, FileManager fileManager, Pool pool, Device device, TestIdentifier test) {
        this.deviceInterface = deviceInterface;
        this.fileManager = fileManager;
        this.pool = pool;
        this.device = device;
        this.test = test;
    }

    @Override
    public void run() {
        int count = 0;
        capturing = true;
        while (capturing) {
            getScreenshot(test, count++);
            pauseTillNextScreenCapture();
        }

        List<File> files = asList(fileManager.getFiles(SCREENSHOT, pool, device, test));
        if (hasFailed) {
            File file = fileManager.createFile(ANIMATION, pool, device, test);
            createGif(files, file);
        }
        deleteFiles(files);

    }

    private void pauseTillNextScreenCapture() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {
        }
    }

    private void getScreenshot(TestIdentifier test, int sequenceNumber) {
        try {
            logger.trace("Started getting screenshot");
            long startNanos = nanoTime();
            RawImage screenshot = deviceInterface.getScreenshot();
            File file = fileManager.createFile(SCREENSHOT, pool, device, test, sequenceNumber);
            ImageIO.write(bufferedImageFrom(screenshot), SCREENSHOT.getSuffix(), file);
            logger.trace("Finished writing screenshot in {}ms to: {}", millisSinceNanoTime(startNanos), file);
        } catch (TimeoutException | AdbCommandRejectedException | IOException e) {
            logger.error("Error when getting screenshot of device", e);
        }
    }

    public void stopCapturing(boolean hasFailed) {
        this.hasFailed = hasFailed;
        capturing = false;
    }

    private BufferedImage bufferedImageFrom(RawImage rawImage) {
        BufferedImage image = new BufferedImage(rawImage.width, rawImage.height, TYPE_INT_ARGB);

        int index = 0;
        int bytesPerPixel = rawImage.bpp >> 3;
        for (int y = 0; y < rawImage.height; y++) {
            for (int x = 0; x < rawImage.width; x++) {
                image.setRGB(x, y, rawImage.getARGB(index) | 0xff000000);
                index += bytesPerPixel;
            }
        }
        return image;
    }

    private void createGif(List<File> files, File file) {
        try {
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.start(file.getAbsolutePath());
            encoder.setDelay(1500);
            encoder.setQuality(1);
            encoder.setRepeat(0);
            encoder.setTransparent(Color.WHITE);

            int width = 0;
            int height = 0;
            for (File testScreenshot : files) {
                BufferedImage bufferedImage = ImageIO.read(testScreenshot);
                width = Math.max(bufferedImage.getWidth(), width);
                height = Math.max(bufferedImage.getHeight(), height);
            }
            encoder.setSize(width, height);

            for (File testScreenshot : files) {
                encoder.addFrame(ImageIO.read(testScreenshot));
            }

            encoder.finish();
        } catch (IOException e) {
            logger.error("Error saving animated GIF", e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteFiles(List<File> files) {
        for (File file : files) {
            file.delete();
        }
    }
}
