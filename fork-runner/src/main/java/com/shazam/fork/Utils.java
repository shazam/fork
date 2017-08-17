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

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.shazam.fork.injector.ConfigurationInjector;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nonnull;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class Utils {

    private Utils() {
    }

    public static ExecutorService namedExecutor(int numberOfThreads, String nameFormat) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat(nameFormat).build();
        return newFixedThreadPool(numberOfThreads, namedThreadFactory);
    }

    private static final NullOutputReceiver NO_OP_RECEIVER = new NullOutputReceiver();

    public static void revokePermissions(@Nonnull String applicationPackage,
                                         @Nonnull IDevice device, @Nonnull List<String> permissionsToRevoke) {
        for (String permissionToRevoke : permissionsToRevoke) {
            try {
                device.executeShellCommand(format("pm revoke %s %s",
                        applicationPackage, permissionToRevoke), NO_OP_RECEIVER);
            } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
                throw new RuntimeException(format("Unable to revoke permission %s", permissionToRevoke), e);
            }
        }
    }

    public static void grantAllPermissionsIfAllowed(@Nonnull String applicationPackage, @Nonnull IDevice device){
        Configuration configuration = ConfigurationInjector.configuration();
        if(configuration.isAutoGrantingPermissions()) {
            List<String> permissions = configuration.getApplicationInfo().getPermissions();
            for (String permissionToGrant : permissions) {
                try {
                    device.executeShellCommand(format("pm grant %s %s",
                            applicationPackage, permissionToGrant), NO_OP_RECEIVER);
                } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
                    throw new RuntimeException(format("Unable to grant permission %s", permissionToGrant), e);
                }
            }
        }
    }
}
