package com.strongloop.android.remoting.test;

import com.strongloop.android.remoting.Repository;
import com.strongloop.android.remoting.VirtualObject;
import com.strongloop.android.remoting.adapters.RestAdapter;

import java.util.HashMap;
import java.util.Map;

public class RestAdapterTest extends AsyncTestCase {

    /**
     * Convenience method to create a single-entry Map.
     */
    public static <T> Map<String, T> param(String name, T value) {
        Map<String, T> params = new HashMap<String, T>();
        params.put(name, value);
        return params;
    }

    private RestAdapter adapter;
    private Repository testClass;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // NOTE: "10.0.2.2" is the "localhost" of the Android emulator's
        // host computer.
        adapter = createRestAdapter();
        testClass = new Repository("SimpleClass");
        testClass.setAdapter(adapter);
    }

    public void testConnected() {
        assertTrue(adapter.isConnected());
    }

    public void testGet() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                adapter.invokeStaticMethod("simple.getSecret", null,
                        expectJsonResponse("shhh!"));
            }
        });
    }

    public void testTransform() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                adapter.invokeStaticMethod("simple.transform",
                        param("str", "somevalue"),
                        expectJsonResponse("transformed: somevalue"));
            }
        });
    }


    public void testTestClassGet() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                adapter.invokeInstanceMethod("SimpleClass.prototype.getName",
                        param("name", "somename"),
                        null,
                        expectJsonResponse("somename"));
            }
        });
    }

    public void testTestClassTransform() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                adapter.invokeInstanceMethod("SimpleClass.prototype.greet",
                        param("name", "somename"),
                        param("other", "othername"),
                        expectJsonResponse("Hi, othername!"));
            }
        });
    }

    public void testPrototypeStatic() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                testClass.invokeStaticMethod("getFavoritePerson", null,
                        expectJsonResponse("You"));
            }
        });
    }

    public void testPrototypeGet() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                VirtualObject test = testClass.createObject(
                        param("name", "somename"));
                test.invokeMethod("getName", null,
                        expectJsonResponse("somename"));
            }
        });
    }

    public void testPrototypeTransform() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                VirtualObject test = testClass.createObject(
                        param("name", param("somekey", "somevalue")));
                test.invokeMethod("greet",
                        param("other", "othername"),
                        expectJsonResponse("Hi, othername!"));
            }
        });
    }
}
