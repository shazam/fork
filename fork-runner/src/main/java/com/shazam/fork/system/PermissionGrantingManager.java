package com.shazam.fork.system;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import static java.lang.String.format;

public class PermissionGrantingManager {

    private static final NullOutputReceiver NO_OP_RECEIVER = new NullOutputReceiver();

    private final Logger logger = LoggerFactory.getLogger(PermissionGrantingManager.class);

    private PermissionGrantingManager() {
    }

    public static PermissionGrantingManager permissionGrantingManager() {
        return new PermissionGrantingManager();
    }

    public void revokePermissions(@Nonnull String applicationPackage,
                                  @Nonnull IDevice device,
                                  @Nonnull List<String> permissionsToRevoke) {
        if (permissionsToRevoke.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();
        for (String permissionToRevoke : permissionsToRevoke) {
            try {
                device.executeShellCommand(format("pm revoke %s %s",
                        applicationPackage, permissionToRevoke), NO_OP_RECEIVER);
            } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
                throw new UnsupportedOperationException(format("Unable to revoke permission %s", permissionToRevoke), e);
            }
        }
        logger.debug("Revoking permissions: {} (took {}ms)", permissionsToRevoke, (System.currentTimeMillis() - start));
    }

    public void grantPermissions(@Nonnull String applicationPackage,
                                 @Nonnull IDevice device,
                                 @Nonnull List<String> permissionsToGrant) {
        if (permissionsToGrant.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();
        for (String permissionToGrant : permissionsToGrant) {
            try {
                device.executeShellCommand(format("pm grant %s %s",
                        applicationPackage, permissionToGrant), NO_OP_RECEIVER);
            } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
                throw new UnsupportedOperationException(format("Unable to grant permission %s", permissionToGrant), e);
            }
        }

        logger.debug("Granting permissions: {} (took {}ms)", permissionsToGrant, (System.currentTimeMillis() - start));
    }

}
