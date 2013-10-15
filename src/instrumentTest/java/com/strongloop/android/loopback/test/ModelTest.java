package com.strongloop.android.loopback.test;

import android.util.Log;

import com.strongloop.android.loopback.Model;
import com.strongloop.android.loopback.ModelAdapter;
import com.strongloop.android.loopback.ModelPrototype;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.strongloop.android.loopback.test.TestHelpers.assertPropertyNames;

public class ModelTest extends AsyncTestCase {

    private ModelPrototype<Model> prototype;
    private ModelAdapter<Model> adapter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // NOTE: "10.0.2.2" is the "localhost" of the Android emulator's
        // host computer.
        adapter = new ModelAdapter<Model>(getActivity(),  "http://10.0.2.2:3000");
        prototype = adapter.createPrototype("widget");
    }

    public void testCreateAndRemove() throws Throwable {
        final Object[] lastId = new Object[1];

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "Foobar");
        params.put("bars", 1);

        final Model model = prototype.createModel(params);

        assertEquals("Foobar", model.get("name"));
        assertEquals(1, model.get("bars"));
        assertNull(model.getId());

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                model.save(new Model.Callback() {

                    @Override
                    public void onSuccess() {
                        lastId[0] = model.getId();
                        Log.i("ModelTest", "id: " + model.getId());
                        assertNotNull(model.getId());
                        notifyFinished();
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail(t.getMessage());
                        notifyFinished();
                    }
                });
            }
        });
        assertNotNull(lastId[0]);

        JSONObject remoteJson = fetchJsonObjectById(prototype, lastId[0]);
        assertNotNull(remoteJson);
        assertPropertyNames(remoteJson, "id", "name", "bars");

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                prototype.findById(lastId[0],
                        new ModelPrototype.FindCallback<Model>() {

                    @Override
                    public void onSuccess(Model model) {
                        model.destroy(new Model.Callback() {

                            @Override
                            public void onSuccess() {
                                assertTrue(true);
                                notifyFinished();
                            }

                            @Override
                            public void onError(Throwable t) {
                                fail(t.getMessage());
                                notifyFinished();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail(t.getMessage());
                        notifyFinished();
                    }
                });
            }
        });
    }

    public void testFind() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                prototype.findById(2, new ModelPrototype.FindCallback<Model>() {

                    @Override
                    public void onSuccess(Model model) {
                        assertNotNull("No model found with id 2", model);
                        assertTrue("Invalid class", (model instanceof Model));
                        assertEquals("Invalid name", "Bar", model.get("name"));
                        assertEquals("Invalid bars", 1, model.get("bars"));
                        notifyFinished();
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail(t.getMessage());
                        notifyFinished();
                    }
                });
            }
        });
    }

    public void testFindAll() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                prototype.findAll(new ModelPrototype.FindAllCallback<Model>() {

                    @Override
                    public void onSuccess(List<Model> list) {
                        assertNotNull("No models returned.", list);
                        assertTrue("Invalid # of models returned: " +
                                list.size(), list.size() >= 2);
                        assertTrue("Invalid class",
                                (list.get(0) instanceof Model));
                        assertEquals("Invalid name", "Foo",
                                list.get(0).get("name"));
                        assertEquals("Invalid bars", 0,
                                list.get(0).get("bars"));
                        assertEquals("Invalid name", "Bar",
                                list.get(1).get("name"));
                        assertEquals("Invalid bars", 1,
                                list.get(1).get("bars"));
                        notifyFinished();
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail(t.getMessage());
                        notifyFinished();
                    }
                });
            }
        });
    }

    public void testUpdate() throws Throwable {
        doAsyncTest(new AsyncTest() {

            ModelPrototype.FindCallback<Model> verify =
                    new ModelPrototype.FindCallback<Model>() {

                @Override
                public void onSuccess(Model model) {
                    assertNotNull("No model found with id 2", model);
                    assertTrue("Invalid class", (model instanceof Model));
                    assertEquals("Invalid name", "Barfoo", model.get("name"));
                    assertEquals("Invalid bars", 1, model.get("bars"));

                    model.put("name", "Bar");
                    model.save(new Model.Callback() {

                        @Override
                        public void onSuccess() {
                            notifyFinished();
                        }

                        @Override
                        public void onError(Throwable t) {
                            fail(t.getMessage());
                            notifyFinished();
                        }

                    });
                }

                @Override
                public void onError(Throwable t) {
                    fail(t.getMessage());
                    notifyFinished();
                }
            };

            Model.Callback findAgain = new Model.Callback() {

                @Override
                public void onSuccess() {
                    prototype.findById(2, verify);
                }

                @Override
                public void onError(Throwable t) {
                    fail(t.getMessage());
                    notifyFinished();
                }
            };

            ModelPrototype.FindCallback<Model> update =
                    new ModelPrototype.FindCallback<Model>() {

                @Override
                public void onSuccess(Model model) {
                    assertNotNull("No model found with ID 2", model);
                    model.put("name", "Barfoo");
                    model.save(findAgain);
                }

                @Override
                public void onError(Throwable t) {
                    fail(t.getMessage());
                    notifyFinished();
                }
            };

            @Override
            public void run() {
                prototype.findById(2, update);
            }
        });
    }

    public void testRestContractUsesPluralizedNameInUrl() {
        adapter.createPrototype("weapon");

        String methodUrl = adapter.getContract().getUrlForMethod("weapon.all", null);
        assertEquals("/weapons", methodUrl);
    }

    public void testRestContractUsesCustomNameInUrl() {
        adapter.createPrototype("ammo", "ammo");

        String methodUrl = adapter.getContract().getUrlForMethod("ammo.all", null);
        assertEquals("/ammo", methodUrl);
    }
}
