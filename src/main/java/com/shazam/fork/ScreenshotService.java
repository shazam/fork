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
package com.shazam.fork;

import com.github.rtyley.android.screenshot.paparazzo.OnDemandScreenshotService;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Devices;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.io.FileUtils.forceMkdir;

/**
 * Starts and stops the {@link OnDemandScreenshotService} for all devices connected to adb
 */
public class ScreenshotService {

    private final File screenshotParentDir;
    private final List<OnDemandScreenshotService> screenshotServices = new ArrayList<>();

    public ScreenshotService(File outputDirectory) {
		screenshotParentDir = new File(outputDirectory, "screenshots");
		create(screenshotParentDir);
	}

	public void start(Devices devices) {
		for (Device device : devices.getDevices()) {
			String deviceName = device.getLongName().replaceAll("\\s+", "_");
			File deviceScreenshotDir = new File(screenshotParentDir, deviceName);
			create(deviceScreenshotDir);
			OnDemandScreenshotService screenshotService = new OnDemandScreenshotService(
					device.getDeviceInterface(), new ShazamImageSaver(deviceScreenshotDir));
			screenshotServices.add(screenshotService);
			screenshotService.start();
		}
	}

	public void stop() {
		for (OnDemandScreenshotService screenshotService : screenshotServices) {
			if (screenshotService != null) {
				screenshotService.finish();
			}
		}
	}

	private void create(File dir) {
		try {
			forceMkdir(dir);
		} catch (IOException e) {
            // Will not handle this specially
		}
	}

}