package com.strongloop.android.remoting.test;

import android.test.MoreAsserts;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.strongloop.android.remoting.Repository;
import com.strongloop.android.remoting.VirtualObject;
import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.RestAdapter;
import com.strongloop.android.remoting.adapters.RestContract;
import com.strongloop.android.remoting.adapters.RestContractItem;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RestContractTest extends AsyncTestCase {
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
        adapter = createRestAdapter();

        final RestContract contract = adapter.getContract();

        contract.addItem(
                new RestContractItem("/contract/customizedGetSecret", "GET"),
                "contract.getSecret");
        contract.addItem(
                new RestContractItem("/contract/customizedTransform", "GET"),
                "contract.transform");
        contract.addItem(
                new RestContractItem("/contract/geopoint", "GET"),
                "contract.geopoint");
        contract.addItem(
                new RestContractItem("/contract/list", "GET"),
                "contract.list");
        contract.addItem(
                new RestContractItem("/ContractClass/:name/getName", "POST"),
                "ContractClass.prototype.getName");
        contract.addItem(
                new RestContractItem("/ContractClass/:name/greet", "POST"),
                "ContractClass.prototype.greet");
        contract.addItem(
                new RestContractItem("/contract/binary", "GET"),
                "contract.binary");

        testClass = new Repository("ContractClass");
        testClass.setAdapter(adapter);
    }

    public void testAddItemsFromContract() {
        RestContract parent = new RestContract();
        RestContract child = new RestContract();

        parent.addItem(new RestContractItem("/wrong/route", "OOPS"),
                "test.route");
        child.addItem(new RestContractItem("/test/route", "GET"),
                "test.route");
        child.addItem(new RestContractItem("/new/route", "POST"),
                "new.route");

        parent.addItemsFromContract(child);

        assertEquals("Wrong URL", "/test/route",
                parent.getUrlForMethod("test.route", null));
        assertEquals("Wrong verb", "GET",
                parent.getVerbForMethod("test.route"));
        assertEquals("Wrong URL", "/new/route",
                parent.getUrlForMethod("new.route", null));
        assertEquals("Wrong verb", "POST",
                parent.getVerbForMethod("new.route"));
    }

    public void testGet() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                adapter.invokeStaticMethod("contract.getSecret", null,
                        expectJsonResponse("shhh!"));
            }
        });
    }

    public void testTransform() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                adapter.invokeStaticMethod("contract.transform",
                        param("str", "somevalue"),
                        expectJsonResponse("transformed: somevalue"));
            }
        });
    }


    public void testTestClassGet() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                adapter.invokeInstanceMethod("ContractClass.prototype.getName",
                        param("name", "somename"), null,
                        expectJsonResponse("somename"));
            }
        });
    }

    public void testTestClassTransform() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                adapter.invokeInstanceMethod("ContractClass.prototype.greet",
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
                        param("name", "somename"));
                test.invokeMethod("greet",
                        param("other", "othername"),
                        expectJsonResponse("Hi, othername!"));
            }
        });
    }

    public void testNestedParameterObjectsAreFlattened() throws Throwable {
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                adapter.invokeStaticMethod(
                        "contract.geopoint",
                        ImmutableMap.of(
                                "here",
                                ImmutableMap.of(
                                        "lat", 10,
                                        "lng", 20
                                )
                        ),
                        new Adapter.JsonObjectCallback() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                assertEquals("lat", 10, response.opt("lat"));
                                assertEquals("lng", 20, response.opt("lng"));
                                notifyFinished();
                            }

                            @Override
                            public void onError(Throwable t) {
                                notifyFailed(t);
                            }
                        }
                );
            }
        });
    }

    public void testDeeplyNestedParameterObjectsAreFlattened() throws Throwable {
        // In this test, we do not check for the exact value of query-string,
        // but ensure that the value created by the android library
        // is correctly parsed by the strong-remoting server.
        // This way the test stays relevant (and passing) even if
        // the query-string format changes in the future.
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                final Map filter =
                        ImmutableMap.of("where",
                            ImmutableMap.of("age",
                                    ImmutableMap.of("gt", 21)));

                adapter.invokeStaticMethod(
                        "contract.list",
                        ImmutableMap.of("filter", filter),
                        expectJsonResponse("{\"where\":{\"age\":{\"gt\":21}}}")
                );
            }
        });
    }

    public void testListNestedInObjectParameter() throws Throwable {
        // In this test, we do not check for the exact value of query-string,
        // but ensure that the value created by the android library
        // is correctly parsed by the strong-remoting server.
        // This way the test stays relevant (and passing) even if
        // the query-string format changes in the future.
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                final Map parameters = ImmutableMap.of("filter",
                        ImmutableMap.of("include",
                            ImmutableList.of("homeTeam", "visitingTeam")));
                adapter.invokeStaticMethod(
                        "contract.list",
                        ImmutableMap.of("filter", parameters),
                        expectJsonResponse("{\"filter\":{\"include\":" +
                            "[\"homeTeam\",\"visitingTeam\"]}}"));
            }
        });
    }

    public void testMapNestedInListNestedInObjectParameter() throws Throwable {
        // In this test, we do not check for the exact value of query-string,
        // but ensure that the value created by the android library
        // is correctly parsed by the strong-remoting server.
        // This way the test stays relevant (and passing) even if
        // the query-string format changes in the future.
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                final Map parameters = ImmutableMap.of("where",
                        ImmutableMap.of("and",
                                ImmutableList.of("homeTeam",
                                        "visitingTeam",
                                        ImmutableMap.of("other", "3"))));

                adapter.invokeStaticMethod(
                        "contract.list",
                        ImmutableMap.of("filter", parameters),
                        expectJsonResponse("{\"where\":{\"and\":" +
                                "[\"homeTeam\",\"visitingTeam\"," +
                                "{\"other\":\"3\"}]}}"));
            }
        });
    }

    public void testCustomRequestHeader() throws Throwable {
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                RestAdapter customAdapter = new RestAdapter(getActivity(), REST_SERVER_URL) {
                    {
                        this.getClient().addHeader("Authorization", "auth-token");
                    }
                };
                customAdapter.getContract().addItem(
                        new RestContractItem("/contract/get-auth", "GET"),
                        "contract.getAuthorizationHeader");
                customAdapter.invokeStaticMethod(
                        "contract.getAuthorizationHeader",
                        new HashMap<String, Object>(),
                        expectJsonResponse("auth-token"));
            }
        });
    }

    public void testBinaryResponseBody() throws Throwable {
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                adapter.invokeStaticMethod("contract.binary", null,
                        new Adapter.BinaryCallback() {
                            @Override
                            public void onSuccess(byte[] response, String contentType) {
                                // The values are hard-coded in test-server/contract.js
                                assertEquals("application/octet-stream", contentType);
                                MoreAsserts.assertEquals(new byte[]{1, 2, 3}, response);
                                notifyFinished();
                            }

                            @Override
                            public void onError(Throwable t) {
                                notifyFailed(t);
                            }
                        });
            }
        });
    }
}
