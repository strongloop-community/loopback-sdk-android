package com.strongloop.android.loopback.test;

import android.test.ActivityTestCase;

import com.strongloop.android.loopback.Container;
import com.strongloop.android.loopback.ContainerRepository;
import com.strongloop.android.loopback.File;
import com.strongloop.android.loopback.Model;
import com.strongloop.android.loopback.ModelRepository;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.test.helpers.TestContext;
import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.Adapter.JsonObjectCallback;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class to easily perform asynchronous JUnit tests in Android.
 */
public class AsyncTestCase extends ActivityTestCase {

    // NOTE: "10.0.2.2" is the "localhost" of the Android emulator's
    // host computer.
    public static final String REST_SERVER_URL = "http://10.0.2.2:3000";

    public TestContext testContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testContext = new TestContext(getInstrumentation());
    }

    protected RestAdapter createRestAdapter() {
        return new RestAdapter(testContext, REST_SERVER_URL);
    }

    public abstract class AsyncTest extends AsyncTask {

        public JsonObjectCallback expectJsonResponse(String expectedData) {
            return new ExpectedDataCallback(expectedData);
        }

        /**
         * Simple JsonObjectCallback to check for the "data" field of
         * the response.
         */
        public class ExpectedDataCallback extends JsonObjectCallback {

            private String expectedData;

            public ExpectedDataCallback(String expectedData) {
                this.expectedData = expectedData;
            }

            @Override
            public void onError(Throwable t) {
                notifyFailed(t);
            }

            @Override
            public void onSuccess(JSONObject response) {
                assertNotNull("No value returned", response);
                assertEquals("Incorrect value returned.", expectedData,
                        response.optString("data"));
                notifyFinished();
            }
        };
    }

    public void doAsyncTest(final AsyncTest asyncTest) throws Throwable {
        await(asyncTest);
    }

    public void await(final AsyncTask asyncTask) throws Throwable {
        AsyncTask.Runner runner = new AsyncTask.Runner(asyncTask);
        runTestOnUiThread(runner);
        runner.await();
    }

    public JSONObject fetchJsonObjectById(final ModelRepository<?> repository, final Object id)
            throws Throwable {
        final JSONObject[] remoteObject = new JSONObject[1];

        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", id);
                repository.invokeStaticMethod("findById", params, new Adapter.JsonObjectCallback() {
                    @Override
                    public void onError(Throwable t) {
                        notifyFailed(t);
                    }

                    @Override
                    public void onSuccess(JSONObject response) {
                        remoteObject[0] = response;
                        notifyFinished();
                    }
                });
            }
        });
        return remoteObject[0];
    }

    @SuppressWarnings("unchecked")
    public<T extends Model> T fetchModelById(
            final ModelRepository<T> repository, final Object id)
            throws Throwable {

        final Model[] remoteObject = new Model[1];
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                repository.findById(id, new ObjectTestCallback<T>() {
                    @Override
                    public void onSuccess(T model) {
                        remoteObject[0] = model;
                        notifyFinished();
                    }
                });
            }
        });
        return (T) remoteObject[0];
    }

    public Container givenContainer(final ContainerRepository repository) throws Throwable {
        final Container[] ref = new Container[1];
        await(new AsyncTask() {
            @Override
            public void run() {
                repository.create("a-container", new ObjectTestCallback<Container>() {
                    @Override
                    public void onSuccess(Container object) {
                        ref[0] = object;
                        notifyFinished();
                    }
                });
            }
        });
        return ref[0];
    }

    public File givenFile(final ContainerRepository repository, byte[] content)
            throws Throwable {
       return givenFile(repository, "a-file", content);
    }

    public File givenFile(final ContainerRepository repository, final String name)
            throws Throwable {
        return givenFile(repository, name, new byte[0]);
    }

    public File givenFile(final ContainerRepository repository, final String name, final byte[] content)
            throws Throwable {
        final File[] ref = new File[1];
        final Container container = givenContainer(repository);

        await(new AsyncTask() {
            @Override
            public void run() {
                container.upload(name, content, null,
                        new ObjectTestCallback<File>() {
                            @Override
                            public void onSuccess(File object) {
                                ref[0] = object;
                                notifyFinished();
                            }
                        });
            }
        });
        return ref[0];
    }
}
