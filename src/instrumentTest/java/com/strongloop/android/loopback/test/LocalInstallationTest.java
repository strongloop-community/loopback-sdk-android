package com.strongloop.android.loopback.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.test.mock.MockContext;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.test.MoreAsserts;

import com.google.common.collect.ImmutableMap;
import com.strongloop.android.loopback.LocalInstallation;
import com.strongloop.android.loopback.Model;
import com.strongloop.android.loopback.ModelRepository;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.remoting.adapters.Adapter;

import java.util.ArrayList;
import java.util.TimeZone;

import static android.test.MoreAsserts.assertEquals;

public class LocalInstallationTest extends AsyncTestCase {
    private final static String TAG = LocalInstallationTest.class.getSimpleName();

    private RestAdapter adapter;
    private TestContext context;

    int packageVersionCode;
    String packageVersionName;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        adapter = createRestAdapter();

        packageVersionCode = 1;
        packageVersionName = "a-version-name";
        context = new TestContext();
        context.clearSharedPreferences();
    }

    public void testConstructorFillsProperties() throws Throwable {
        packageVersionName = "1.2.3";

        final LocalInstallation install = new LocalInstallation(context, adapter);

        assertEquals("deviceType", "android", install.getDeviceType());
        assertEquals("status", "Active", install.getStatus());
        assertEquals("appVersion", "1.2.3", install.getAppVersion());
        assertEquals(TimeZone.getDefault().getID(), install.getTimeZone());
    }

    public void testServerRoundTrip() throws Throwable {
        final LocalInstallation install = new LocalInstallation(context, adapter);
        final String[] subscriptions = {"all"};

        install.setAppId("an-app-id");
        install.setAppVersion("an-app-version");
        install.setUserId("an-user-id");
        install.setDeviceToken("a-device-token");
        install.setTimeZone("Europe/London");
        install.setSubscriptions(subscriptions);

        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                install.save(new VoidTestCallback() {
                    @Override
                    public void onSuccess() {
                        assertNotNull(install.getId());
                        notifyFinished();
                    }
                });
            }
        });

        Model found = fetchInstallationById(install.getId());
        assertEquals("an-app-id", found.get("appId"));
        assertEquals("an-app-version", found.get("appVersion"));
        assertEquals("an-user-id", found.get("userId"));
        assertEquals("a-device-token", found.get("deviceToken"));
        assertEquals(LocalInstallation.DEVICE_TYPE_ANDROID, found.get("deviceType"));
        assertEquals("Europe/London", found.get("timeZone"));

        Object[] subscriptionsFound = ((ArrayList) found.get("subscriptions")).toArray();
        MoreAsserts.assertEquals(subscriptions, subscriptionsFound);
    }

    public void testCachingOfInstallationId() throws Throwable {
        final LocalInstallation install = aLocalInstallation();
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                install.save(new VoidTestCallback() {
                    @Override
                    public void onSuccess() {
                        notifyFinished();
                    }
                });
            }
        });

        LocalInstallation copy = new LocalInstallation(context, adapter);
        assertEquals(install.getId(), copy.getId());
    }

    public void testCachingOfDeviceToken() throws Throwable {
        final LocalInstallation install = aLocalInstallation();
        install.setDeviceToken("a-device-token");

        LocalInstallation copy = new LocalInstallation(context, adapter);
        assertEquals(install.getDeviceToken(), copy.getDeviceToken());
    }

    public void testInvalidationOfDeviceTokenWithNewVersion() throws Throwable {
        packageVersionCode = 1;
        LocalInstallation install = aLocalInstallation();
        install.setDeviceToken("a-device-token");

        packageVersionCode = 2;
        LocalInstallation copy = new LocalInstallation(context, adapter);

        assertNull(copy.getDeviceToken());
    }

    public void testRecoveryWhenRemoteWasDeleted() throws Throwable {
        final LocalInstallation install = aLocalInstallation();

        Log.i(TAG, "1. Save the installation to the server");
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                install.save(new VoidTestCallback() {
                    @Override
                    public void onSuccess() {
                        notifyFinished();
                    }
                });
            }
        });

        Log.i(TAG, "2. Delete the installation from the server");
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                ModelRepository repo = adapter.createRepository("installation");
                repo.invokeStaticMethod(
                        "prototype.remove",
                        ImmutableMap.of("id", install.getId()),
                        new Adapter.Callback() {
                            @Override
                            public void onSuccess(String response) {
                                notifyFinished();
                            }

                            @Override
                            public void onError(Throwable t) {
                                notifyFailed(t);
                            }
                        });
            }
        });

        final Object oldId = install.getId();

        Log.i(TAG, "3. The installation should be successfully saved with a new id");
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                install.save(new VoidTestCallback() {
                    @Override
                    public void onSuccess() {
                        final Object newId = install.getId();
                        String msg = "Expected <" + oldId
                                + "> to differ from <" + newId + ">";
                        assertTrue(msg, oldId != newId);
                        notifyFinished();
                    }
                });
            }
        });
    }

    /*--- Helpers ---*/

    private Model fetchInstallationById(Object id) throws Throwable {
        final ModelRepository<Model> repository =
                adapter.createRepository("installation");
        return fetchModelById(repository, id);
    }

    LocalInstallation aLocalInstallation() {
        final LocalInstallation install = new LocalInstallation(context, adapter);
        install.setAppId("an-app-id");
        install.setAppVersion("an-app-version");
        install.setUserId("an-user-id");
        install.setDeviceToken("a-device-token");
        return install;
    }

    class TestContext extends MockContext {
        @Override
        public String getPackageName() {
            return "a-package-name";
        }

        @Override
        public PackageManager getPackageManager() {
            return new TestPackageManager();
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            return getRealContext().getSharedPreferences(name, mode);
        }

        public void clearSharedPreferences() {
            getRealContext()
                    .getSharedPreferences(
                            LocalInstallation.SHARED_PREFERENCES_NAME,
                            Context.MODE_PRIVATE)
                    .edit().clear().commit();
        }

        private Context getRealContext() {
            return getInstrumentation().getContext();
        }
    }

    class TestPackageManager extends MockPackageManager {
        @Override
        public PackageInfo getPackageInfo(String packageName, int flags)
                throws NameNotFoundException {

            if (packageName != "a-package-name")
                throw new NameNotFoundException();

            PackageInfo info = new PackageInfo();
            info.versionName = packageVersionName;
            info.versionCode = packageVersionCode;
            return info;
        }
    }
}
