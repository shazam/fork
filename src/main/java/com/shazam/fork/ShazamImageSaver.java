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

import com.github.rtyley.android.screenshot.paparazzo.processors.ScreenshotProcessor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import static org.apache.commons.io.FileUtils.forceMkdir;

/**
 * Saves images and supports a sub directory
 */
class ShazamImageSaver implements ScreenshotProcessor {

	private final File screenshotDirectory;
	private int screenshotCount;

	public ShazamImageSaver(File screenshotDirectory) {
		screenshotCount = 0;
		this.screenshotDirectory = screenshotDirectory;
	}

	@Override
	public void process(BufferedImage image, Map<String, String> request) {
		try {
			File directoryToSaveIn = screenshotDirectory;
			String name = request.containsKey("name") ? request.get("name")
					: String.format("%04d", screenshotCount++);
			if (request.containsKey("dirName")) {
				directoryToSaveIn = new File(screenshotDirectory,
						request.get("dirName"));
				forceMkdir(directoryToSaveIn);

			}
			File screenshotFile = new File(directoryToSaveIn,
					(new StringBuilder()).append(name).append(".png")
							.toString());

			ImageIO.write(image, "png", screenshotFile);
		} catch (IOException e) {
		}
	}

	@Override
	public void finish() {
	}

}
