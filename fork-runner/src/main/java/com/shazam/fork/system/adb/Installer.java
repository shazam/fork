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
package com.shazam.fork.system.adb;

import com.android.ddmlib.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javax.annotation.Nonnull;

import static java.lang.String.format;

public class Installer {
    private static final Logger logger = LoggerFactory.getLogger(Installer.class);
	private final String applicationPackage;
	private final String instrumentationPackage;
	private final File apk;
	private final File testApk;
    private final boolean autoGrantRuntimePermissions;

	public Installer(String applicationPackage,
					 String instrumentationPackage,
					 File apk,
					 File testApk,
					 boolean autoGrantRuntimePermissions) {
		this.applicationPackage = applicationPackage;
		this.instrumentationPackage = instrumentationPackage;
		this.apk = apk;
		this.testApk = testApk;
        this.autoGrantRuntimePermissions = autoGrantRuntimePermissions;
	}

	public void prepareInstallation(IDevice device) {
		clearLogcat(device);
		DdmPreferences.setTimeOut(30000);
		reinstall(device, applicationPackage, apk);
		reinstall(device, instrumentationPackage, testApk);
        grantMockLocationInMarshmallow(device, applicationPackage);
	}

	private void reinstall(final IDevice device, final String appPackage, final File appApk) {
		final String message = format("Error while installing %s on %s", appPackage, device.getSerialNumber());
		tryThrice(true, message, new Runnable() {
			public void run() {
				try {
                    logger.debug("Uninstalling {} from {}", appPackage, device.getSerialNumber());
                    device.uninstallPackage(appPackage);
                    logger.debug("Installing {} to {}", appPackage, device.getSerialNumber());
                    boolean autoGrant = isMarshmallowOrMore(device) && autoGrantRuntimePermissions;
                    device.installPackage(appApk.getAbsolutePath(), true,
                            autoGrant ? "-g" : "");
				} catch (InstallException e) {
					throw new RuntimeException(message, e);
				}
			}
		});
	}

	private void clearLogcat(final IDevice device) {
		final String message = "Could not clear logcat on device: " + device.getSerialNumber();
		tryThrice(false, message, new Runnable() {
			public void run() {
				try {
					device.executeShellCommand("logcat -c", new NullOutputReceiver());
				} catch (Exception e) {
					throw new RuntimeException(message, e);
				}
			}
		});
	}

	private void tryThrice(boolean disaster, String message, Runnable runnable) {
		for (int attempt = 1;; ++attempt) {
			try {
				runnable.run();
				if (attempt > 1) {
                    logger.warn("Success after: " + message);
				}
				return;
			} catch (RuntimeException e) {
                logger.error("Attempt #{} failed: {}", attempt, message);
                if (attempt == 3) {
                    if (disaster) {
                        throw e;
                    }
                    return;
                }
            }
        }
    }

    private void grantMockLocationInMarshmallow(final IDevice device, final String appPackage) {
        if (isMarshmallowOrMore(device)) {
            CollectingShellOutputReceiver receiver = new CollectingShellOutputReceiver();
            String command = " appops set " + appPackage + " android:mock_location allow";
            try {
                device.executeShellCommand(command, receiver);
                logger.debug("When mock location granted for " + appPackage + " :" + receiver.getOutput());
            } catch (Exception e) {
                logger.warn("Error when executing " + command + " on " + device.getSerialNumber(), e);
            }
        }
    }

    private boolean isMarshmallowOrMore(@Nonnull IDevice device){
        return device.getVersion().getApiLevel() >= 23;
    }
}
