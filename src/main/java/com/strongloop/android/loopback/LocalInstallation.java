package com.strongloop.android.loopback;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.strongloop.android.loopback.callbacks.VoidCallback;
import com.strongloop.android.remoting.BeanUtil;
import com.strongloop.android.remoting.Transient;

import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.TimeZone;

/**
 * This class represents the Installation instance assigned to
 * the installation of the Android application that is running
 * this code.
 *
 * Certain properties are cached in {@link android.content.SharedPreferences}
 * to prevent duplicate registrations.
 *
 * <pre>{@code
 *
 * public class MyActivity extends Activity {
 *
 * // This method must be called from onCreate() method every time
 * // the application starts. See the example Android application
 * // in loopback-push-notification for more details.
 * private void updateRegistration() {
 *     final Context context = getApplicationContext();
 *
 *     // 1. Grab the shared RestAdapter instance.
 *     final DemoApplication app = (DemoApplication) getApplication();
 *     final RestAdapter adapter = app.getLoopBackAdapter();
 *
 *     // 2. Create LocalInstallation instance
 *     final LocalInstallation installation =
 *             new LocalInstallation(context, adapter);
 *
 *     // 3. Update Installation properties that were not pre-filled
 *
 *     // Enter the id of the application you registered in your LoopBack server
 *     // by calling Application.register()
 *     installation.setAppId(LOOPBACK_APP_ID);
 *
 *     // Substitute a real id of the user logged in this application
 *     installation.setUserId("loopback-android");
 *
 *     // 4. Check if we have a valid GCM registration id
 *     if (installation.getDeviceToken() != null) {
 *         // 5a. We have a valid GCM token, all we need to do now
 *         //     is to save the installation to the server
 *         saveInstallation(installation);
 *     } else {
 *         // 5b. We don't have a valid GCM token. Get one from GCM
 *         // and save the installation afterwards.
 *         registerInBackground(installation);
 *     }
 * }
 *
 * private void registerInBackground(final LocalInstallation installation) {
 *     new AsyncTask<Void, Void, String>() {
 *         //Override
 *         protected String doInBackground(final Void... params) {
 *             try {
 *                 final String regid = gcm.register(SENDER_ID);
 *                 installation.setDeviceToken(regid);
 *                 return "Device registered, registration ID=" + regid;
 *             } catch (final IOException ex) {
 *                 Log.e(TAG, "GCM registration failed.", ex);
 *                 return "Cannot register with GCM:" + ex.getMessage();
 *                 // If there is an error, don't just keep trying to register.
 *                 // Require the user to click a button again, or perform
 *                 // exponential back-off.
 *             }
 *         }
 *
 *         //Override
 *         protected void onPostExecute(final String message) {
 *             saveInstallation(installation);
 *             // optionally log GCM registration result in `message`
 *         }
 *     }.execute(null, null, null);
 * }
 *
 * void saveInstallation(final LocalInstallation installation) {
 *     installation.save(new Model.Callback() {
 *         //Override
 *         public void onSuccess() {
 *             // Log success
 *         }
 *         //Override
 *         public void onError(final Throwable t) {
 *             // Report error
 *         }
 *     });
 * }
 * }
 * }</pre>
 */
public class LocalInstallation {


    public static final String DEVICE_TYPE_ANDROID = "android";
    public static final String STATUS_ACTIVE = "Active";

    public static final String SHARED_PREFERENCES_NAME =
            LocalInstallation.class.getCanonicalName();
    private static final String PROPERTY_INSTALLATION_ID = "installationId";
    private static final String PROPERTY_DEVICE_TOKEN = "deviceToken";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private final Context applicationContext;
    private final RestAdapter loopbackAdapter;

    Object id;
    String appId;
    String appVersion;
    String userId;
    String deviceToken;
    String[] subscriptions;
    String timeZone;
    String status;

    /**
     * @return The Installation id as assigned by the LoopBack server.
     */
    public Object getId() {
        return id;
    }

    /**
     * @return The id of the LoopBack Application object this Installation
     * is bound to.
     */
    public String getAppId() {
        return appId;
    }
    /**
     * See {@link LocalInstallation#getAppId()}.
     */
    public void setAppId(final String appId) {
        this.appId = appId;
    }

    /**
     * @return The version of this Android application. The property is pre-filled
     * for you using the value in the Android manifest.
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * See {@link LocalInstallation#getAppVersion()}.
     */
    public void setAppVersion(final String appVersion) {
        this.appVersion = appVersion;
    }

    /**
     * @return Id of the user logged in the Android application.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * See {@link LocalInstallation#getUserId()}.
     */
    public void setUserId(final String userId) {
        this.userId = userId;
    }

    /**
     * @return One of the device types supported by LoopBack Push Notification
     * plugin. This is a read-only property that returns
     * {@link #DEVICE_TYPE_ANDROID}
     */
    public String getDeviceType() {
        return DEVICE_TYPE_ANDROID;
    }

    /**
     * @return The GCM registration id that can be used by the LoopBack server
     * to push notifications to this application/device.
     */
    public String getDeviceToken() {
        return deviceToken;
    }

    /**
     * See {@link LocalInstallation#getDeviceToken()}.
     */
    public void setDeviceToken(final String deviceToken) {
        this.deviceToken = deviceToken;
        saveDeviceToken();
    }

    /**
     * @return List subscriptions.
     */
    public String[] getSubscriptions() {
        return subscriptions;
    }

    /**
     * See {@link LocalInstallation#getSubscriptions()}.
     */
    public void setSubscriptions(final String[] subscriptions) {
        this.subscriptions = subscriptions;
    }

    /**
     * @return ID of the time zone preferred by the user.
     * Example: America/Los_Angeles, Europe/London
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * See {@link LocalInstallation#getTimeZone()}
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return Status of this installation, the value is one of the strings
     * recognized by LoopBack Push Notification plugin.
     * See also {@link #STATUS_ACTIVE}
     */
    public String getStatus() {
        return status;
    }

    /**
     * See {@link LocalInstallation#getStatus()}.
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * Creates a new instance of LocalInstallation class. Your application
     * should never instantiate more than one instance.
     * @param applicationContext The Android context of your application.
     * @param loopbackAdapter The adapter to use for communication with
     *                        the LoopBack server.
     */
    public LocalInstallation(Context applicationContext,
                             RestAdapter loopbackAdapter) {

        this.applicationContext = applicationContext;
        this.loopbackAdapter = loopbackAdapter;
        fillDefaults();
        loadSharedPreferences();
    }

    /**
     * Saves the Installation to the remote server. When called for the first
     * time, a new record is created and the id is stored in SharedPreferences.
     * Subsequent calls updates the existing record with the stored id.
     * @param callback The callback to be executed when finished.
     */
    public void save(final VoidCallback callback) {
        ModelRepository<Model> repository =
                loopbackAdapter.createRepository("installation");
        final Model model = repository.createModel(
                BeanUtil.getProperties(this, false, false));

        model.save(new VoidCallback() {
            @Override
            public void onSuccess() {
                id = model.getId();
                saveInstallationId();
                callback.onSuccess();
            }

            @Override
            public void onError(Throwable t) {
                if (t instanceof HttpResponseException) {
                    HttpResponseException ex = (HttpResponseException) t;
                    if (ex.getStatusCode() == 404 && id != null) {
                        // Our Installation ID is no longer valid.
                        // Try to create a new installation instead.
                        id = null;
                        save(callback);
                        return;
                    }
                }
                callback.onError(t);
            }
        });
    }

    private void fillDefaults() {
        try {
            setAppVersion(getPackageInfo().versionName);
        } catch (final PackageManager.NameNotFoundException e) {
            // This should never happen, as it would mean the currently
            // running application is not installed on the device
        }

        setStatus(LocalInstallation.STATUS_ACTIVE);
        setTimeZone(TimeZone.getDefault().getID());
    }

    private void loadSharedPreferences() {
        final SharedPreferences prefs = getSharedPreferences();

        // Get the stored Installation ID. We are storing the value
        // in a JSON array to preserve it's type (NoSQL databases usually
        // use String, SQL databases use Number).
        final String idJson = prefs.getString(PROPERTY_INSTALLATION_ID, null);
        if (idJson != null) {
            try {
                id = new JSONArray(idJson).get(0);
            } catch (JSONException e) {
                String msg = "Cannot parse installation id '" + idJson + "'";
                Log.e("LoopBack", msg, e);
            }
        }

        // Check if app was updated; if so, it must clear the device token
        // (the GCM registration ID) since the existing registration ID
        // is not guaranteed to work with the new app version.
        int savedVersionCode = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersionCode = fetchAppVersionCode();
        if (savedVersionCode == currentVersionCode)
            deviceToken = prefs.getString(PROPERTY_DEVICE_TOKEN, null);
    }

    private void saveInstallationId() {
        final SharedPreferences.Editor editor = getSharedPreferences().edit();
        final String json = new JSONArray().put(id).toString();
        editor.putString(PROPERTY_INSTALLATION_ID, json);
        editor.commit();
    }

    private void saveDeviceToken() {
        final SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PROPERTY_DEVICE_TOKEN, getDeviceToken());
        editor.putInt(PROPERTY_APP_VERSION, fetchAppVersionCode());
        editor.commit();
    }

    private int fetchAppVersionCode() {
        try {
            return getPackageInfo().versionCode;
        } catch (final PackageManager.NameNotFoundException e) {
            // This should never happen, as it would mean the currently
            // running application is not installed on the device
            return Integer.MIN_VALUE;
        }
    }

    @Transient
    @SuppressWarnings("ConstantConditions")
    private PackageInfo getPackageInfo()
            throws PackageManager.NameNotFoundException {
        return applicationContext.getPackageManager()
                .getPackageInfo(applicationContext.getPackageName(), 0);
    }

    @Transient
    private SharedPreferences getSharedPreferences() {
        return applicationContext.getSharedPreferences(
                SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }
}
