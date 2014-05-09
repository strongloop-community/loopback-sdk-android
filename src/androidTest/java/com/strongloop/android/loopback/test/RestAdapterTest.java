package com.strongloop.android.loopback.test;

import android.content.Context;

import com.strongloop.android.loopback.RestAdapter;

public class RestAdapterTest extends AsyncTestCase {
    private RestAdapter adapter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testContext.clearSharedPreferences(
                RestAdapter.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        adapter = createRestAdapter();
    }

    public void testAccessTokenIsStoredInSharedPreferences() {
        final String[] accessTokenRef = new String[1];
        adapter.setAccessToken("an-access-token");

        // android-async-http client does not allow inspection of request headers
        // the workaround is to override the setter method
        new RestAdapter(testContext, REST_SERVER_URL) {
            @Override
            public void setAccessToken(String value) {
                accessTokenRef[0] = value;
            }
        };

        assertEquals("an-access-token", accessTokenRef[0]);
    }
}
