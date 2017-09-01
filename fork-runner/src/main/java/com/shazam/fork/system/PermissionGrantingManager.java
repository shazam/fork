package com.shazam.fork.system;

import com.android.ddmlib.*;
import com.shazam.fork.Configuration;
import com.shazam.fork.model.Permission;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

public class PermissionGrantingManager {

    private static final NullOutputReceiver NO_OP_RECEIVER = new NullOutputReceiver();

    private PermissionGrantingManager() {
    }

    public static PermissionGrantingManager permissionGrantingManager(){
        return new PermissionGrantingManager();
    }

    public void revokePermissions(@Nonnull String applicationPackage,
                                         @Nonnull IDevice device, @Nonnull List<String> permissionsToRevoke) {
        for (String permissionToRevoke : permissionsToRevoke) {
            try {
                device.executeShellCommand(format("pm revoke %s %s",
                        applicationPackage, permissionToRevoke), NO_OP_RECEIVER);
            } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
                throw new UnsupportedOperationException(format("Unable to revoke permission %s", permissionToRevoke), e);
            }
        }
    }

    public void grantAllPermissionsIfAllowed(@Nonnull Configuration configuration, @Nonnull String applicationPackage, @Nonnull IDevice device) {
        if (configuration.isAutoGrantingPermissions()) {
            List<Permission> permissions = configuration.getApplicationInfo().getPermissions();
            for (Permission permissionToGrant : permissions) {
                if (deviceApiLevelInRange(device, permissionToGrant)) {
                    try {
                        device.executeShellCommand(format("pm grant %s %s",
                                applicationPackage, permissionToGrant.getPermissionName()), NO_OP_RECEIVER);
                    } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
                        throw new UnsupportedOperationException(format("Unable to grant permission %s", permissionToGrant), e);
                    }
                }
            }
        }
    }

    private static boolean deviceApiLevelInRange(@Nonnull IDevice device, Permission permissionToGrant) {
        return permissionToGrant.getMinSdkVersion() <= device.getVersion().getFeatureLevel()
                && permissionToGrant.getMaxSdkVersion() >= device.getVersion().getFeatureLevel();
    }
}
