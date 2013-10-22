package com.strongloop.android.loopback.test;

import android.util.Log;

import com.strongloop.android.loopback.Model;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.ModelPrototype;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.strongloop.android.loopback.test.TestHelpers.assertPropertyNames;

public class ModelSubclassingTest extends AsyncTestCase {

    public static class Widget extends Model {

        private String name;
        private int bars;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getBars() {
            return bars;
        }

        public void setBars(int bars) {
            this.bars = bars;
        }

    }

    public static class WidgetPrototype extends ModelPrototype<Widget> {

        public WidgetPrototype() {
            super("widget", Widget.class);
        }
    }

    private WidgetPrototype prototype;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // NOTE: "10.0.2.2" is the "localhost" of the Android emulator's
        // host computer.
        RestAdapter adapter = new RestAdapter(getActivity(),
                "http://10.0.2.2:3000");
        prototype = adapter.createPrototype(WidgetPrototype.class);
    }

    public void testCreateAndRemove() throws Throwable {
        final Object[] lastId = new Object[1];

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "Foobar");
        params.put("bars", 1);

        final Widget model = prototype.createModel(params);

        assertEquals("Foobar", model.getName());
        assertEquals(1, model.getBars());
        assertNull(model.getId());

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                model.save(new Model.Callback() {

                    @Override
                    public void onSuccess() {
                        lastId[0] = model.getId();
                        Log.i("ModelSubclassingTest", "id: " + model.getId());
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
                        new ModelPrototype.FindCallback<Widget>() {

                    @Override
                    public void onSuccess(Widget model) {
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
                prototype.findById(2, new ModelPrototype.FindCallback<Widget>() {

                    @Override
                    public void onSuccess(Widget model) {
                        assertNotNull("No model found with id 2", model);
                        assertTrue("Invalid class", (model instanceof Model));
                        assertEquals("Invalid name", "Bar", model.getName());
                        assertEquals("Invalid bars", 1, model.getBars());
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
                prototype.findAll(new ModelPrototype.FindAllCallback<Widget>() {

                    @Override
                    public void onSuccess(List<Widget> list) {
                        assertNotNull("No models returned.", list);
                        assertTrue("Invalid # of models returned: " +
                                list.size(), list.size() >= 2);
                        assertTrue("Invalid class",
                                (list.get(0) instanceof Widget));
                        assertEquals("Invalid name", "Foo",
                                list.get(0).getName());
                        assertEquals("Invalid bars", 0,
                                list.get(0).getBars());
                        assertEquals("Invalid name", "Bar",
                                list.get(1).getName());
                        assertEquals("Invalid bars", 1,
                                list.get(1).getBars());
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

            ModelPrototype.FindCallback<Widget> verify =
                    new ModelPrototype.FindCallback<Widget>() {

                @Override
                public void onSuccess(Widget model) {
                    assertNotNull("No model found with id 2", model);
                    assertTrue("Invalid class", (model instanceof Widget));
                    assertEquals("Invalid name", "Barfoo", model.getName());
                    assertEquals("Invalid bars", 1, model.getBars());

                    model.setName("Bar");
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

            ModelPrototype.FindCallback<Widget> update =
                    new ModelPrototype.FindCallback<Widget>() {

                @Override
                public void onSuccess(Widget model) {
                    assertNotNull("No model found with ID 2", model);
                    model.setName("Barfoo");
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
}
