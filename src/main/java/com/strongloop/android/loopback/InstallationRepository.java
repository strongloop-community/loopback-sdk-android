package com.strongloop.android.loopback;

import android.content.Context;
import android.content.pm.PackageManager;

public class InstallationRepository extends ModelRepository<Installation> {

    public InstallationRepository() {
        super("installation", Installation.class);
    }

    /**
     * Creates a new Installation model and pre-populates properties
     * shared by all instances of this application.
     *
     * The calling code needs to fill deviceToken, appId and userId
     * and save the instance on the server.
     * @param context The context of this application.
     * @return The Device model created.
     */
    @SuppressWarnings("ConstantConditions")
    public Installation createAndroidInstallation(final Context context) {
        final Installation d = createModel(null);

        final String appName = context.getPackageName();
        try {
            d.appVersion = context.getPackageManager()
                    .getPackageInfo(appName, 0)
                    .versionName;
        } catch (final PackageManager.NameNotFoundException e) {
            // This should never happen, as it would mean the currently
            // running application is not installed on the device
        }

        d.status = Installation.STATUS_ACTIVE;

        return d;
    }

}
