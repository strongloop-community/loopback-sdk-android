package com.strongloop.android.loopback.test;

import com.strongloop.android.loopback.Installation;
import com.strongloop.android.loopback.InstallationRepository;
import com.strongloop.android.loopback.RestAdapter;

public class InstallationTest extends AsyncTestCase {
    private InstallationRepository repository;
    private RestAdapter adapter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        adapter = new RestAdapter(getActivity(), REST_SERVER_URL);
        repository = adapter.createRepository(InstallationRepository.class);
    }

    public void testCreateAndroidInstallationFillsProperties() throws Throwable {
        final Installation model = repository.createAndroidInstallation(
                getInstrumentation().getContext());

        assertEquals("deviceType", "android", model.getDeviceType());
        assertEquals("status", "Active", model.getStatus());

        // Note: The manifest created by Android unit-test runner
        // does not specify versionName, therefore appVersion is null
        // and the following check would fail.
        // assertNotNull("appVersion", model.getAppVersion());
    }

    public void testServerRoundTrip() throws Throwable {
        final Installation model = repository.createAndroidInstallation(
                getInstrumentation().getContext());

        model.setAppId("an-app-id");
        model.setAppVersion("an-app-version");
        model.setUserId("an-user-id");
        model.setDeviceToken("a-device-token");

        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                model.save(new ModelCallback() {
                    @Override
                    public void onSuccess() {
                        assertNotNull(model.getId());
                        notifyFinished();
                    }
                });
            }
        });

        Installation found = fetchModelById(repository, model.getId());
        assertEquals("an-app-id", found.getAppId());
        assertEquals("an-app-version", found.getAppVersion());
        assertEquals("an-user-id", found.getUserId());
        assertEquals("a-device-token",found.getDeviceToken());
        assertEquals(Installation.DEVICE_TYPE_ANDROID, found.getDeviceType());
    }
}
