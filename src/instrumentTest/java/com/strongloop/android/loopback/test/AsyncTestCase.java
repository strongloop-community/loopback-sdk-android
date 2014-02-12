package com.strongloop.android.loopback.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import android.test.ActivityTestCase;

import com.strongloop.android.loopback.Model;
import com.strongloop.android.loopback.ModelRepository;
import com.strongloop.android.loopback.User;
import com.strongloop.android.loopback.UserRepository;
import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.Adapter.JsonObjectCallback;

/**
 * Convenience class to easily perform asynchronous JUnit tests in Android.
 */
public class AsyncTestCase extends ActivityTestCase {

    // NOTE: "10.0.2.2" is the "localhost" of the Android emulator's
    // host computer.
    public static final String REST_SERVER_URL = "http://10.0.2.2:3000";

    public abstract class AsyncTest implements Runnable {

        private CountDownLatch signal;
        private Throwable failException;

        public Throwable getFailException() {
            return failException;
        }

        public void notifyFailed(Throwable reason) {
            failException = reason;
            notifyFinished();
        }

        public void notifyFinished() {
            signal.countDown();
        }

        private void setSignal(final CountDownLatch signal) {
            this.signal = signal;
        }


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

        /**
         * Model.Callback that reports error as test failures.
         */
        public abstract class ModelCallback implements Model.Callback {
            @Override
            public void onError(Throwable t) {
                notifyFailed(t);
            }
        }

        /**
         * ModelRepository.FindCallback<T> that reports errors as test failures.
         * @param <T> The Model type.
         */
        public abstract class FindModelCallback<T extends Model>
                implements ModelRepository.FindCallback<T> {

            @Override
            public void onError(Throwable t) {
                notifyFailed(t);
            }
        }

        /**
         * ModelRepository.FindAllCallback<T> that reports errors
         * as test failures.
         * @param <T> The Model type.
         */
        public abstract class FindAllModelsCallback<T extends Model>
                implements ModelRepository.FindAllCallback<T> {

            @Override
            public void onError(Throwable t) {
                notifyFailed(t);
            }
        }

        public abstract class LoginTestCallback implements UserRepository.LoginCallback {

            @Override
            public void onError(Throwable t) {
                notifyFailed(t);
            }
        }
    }

    public void doAsyncTest(final AsyncTest asyncTest) throws Throwable {
        TestRunner runner = new TestRunner(asyncTest);
        runTestOnUiThread(runner);

        boolean success = runner.await();
        if (runner.getUncaughtException() != null) {
            throw runner.getUncaughtException();
        }
        if (asyncTest.getFailException() != null) {
            throw asyncTest.getFailException();
        }
        assertTrue(success);
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
                repository.findById(id, new FindModelCallback<T>() {
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

    private static class TestRunner implements Runnable {

        private final AsyncTest asyncTest;
        private final CountDownLatch signal = new CountDownLatch(1);
        private Throwable uncaughtException;

        public TestRunner(AsyncTest asyncTest) {
            this.asyncTest = asyncTest;
            this.asyncTest.setSignal(signal);
        }

        @Override
        public void run() {
            try {
                asyncTest.run();
            }
            catch (Throwable t) {
                uncaughtException = t;
            }
        }

        public boolean await() {
            try {
                signal.await(30, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
            }
            return signal.getCount() == 0;
        }

        public Throwable getUncaughtException() {
            return uncaughtException;
        }
    }
}
