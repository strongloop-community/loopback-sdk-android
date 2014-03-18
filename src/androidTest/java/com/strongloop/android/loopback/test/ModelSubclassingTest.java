package com.strongloop.android.loopback.test;

import android.util.Log;

import com.strongloop.android.loopback.Model;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.ModelRepository;
import com.strongloop.android.loopback.callbacks.ObjectCallback;
import com.strongloop.android.loopback.callbacks.VoidCallback;

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

    public static class WidgetRepository extends ModelRepository<Widget> {

        public WidgetRepository() {
            super("widget", Widget.class);
        }
    }

    private WidgetRepository widgetRepository;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RestAdapter adapter = createRestAdapter();
        widgetRepository = adapter.createRepository(WidgetRepository.class);
    }

    public void testCreateAndRemove() throws Throwable {
        final Object[] lastId = new Object[1];

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "Foobar");
        params.put("bars", 1);

        final Widget model = widgetRepository.createModel(params);

        assertEquals("Foobar", model.getName());
        assertEquals(1, model.getBars());
        assertNull(model.getId());

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                model.save(new VoidTestCallback() {

                    @Override
                    public void onSuccess() {
                        lastId[0] = model.getId();
                        Log.i("ModelSubclassingTest", "id: " + model.getId());
                        assertNotNull(model.getId());
                        notifyFinished();
                    }
                });
            }
        });
        assertNotNull(lastId[0]);

        JSONObject remoteJson = fetchJsonObjectById(widgetRepository, lastId[0]);
        assertNotNull(remoteJson);
        assertPropertyNames(remoteJson, "id", "name", "bars");

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                widgetRepository.findById(lastId[0],
                        new ObjectTestCallback<Widget>() {

                    @Override
                    public void onSuccess(Widget model) {
                        model.destroy(new VoidTestCallback());
                    }
                });
            }
        });
    }

    public void testFind() throws Throwable {
        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                widgetRepository.findById(2, new ObjectTestCallback<Widget>() {

                    @Override
                    public void onSuccess(Widget model) {
                        assertNotNull("No model found with id 2", model);
                        assertTrue("Invalid class", (model instanceof Model));
                        assertEquals("Invalid name", "Bar", model.getName());
                        assertEquals("Invalid bars", 1, model.getBars());
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
                widgetRepository.findAll(new ListTestCallback<Widget>() {

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
                });
            }
        });
    }

    public void testUpdate() throws Throwable {
        doAsyncTest(new AsyncTest() {

            ObjectCallback<Widget> verify =
                    new ObjectTestCallback<Widget>() {

                @Override
                public void onSuccess(Widget model) {
                    assertNotNull("No model found with id 2", model);
                    assertTrue("Invalid class", (model instanceof Widget));
                    assertEquals("Invalid name", "Barfoo", model.getName());
                    assertEquals("Invalid bars", 1, model.getBars());

                    model.setName("Bar");
                    model.save(new VoidTestCallback());
                }
            };

            VoidCallback findAgain = new VoidTestCallback() {

                @Override
                public void onSuccess() {
                    widgetRepository.findById(2, verify);
                }
            };

            ObjectCallback<Widget> update =
                    new ObjectTestCallback<Widget>() {

                @Override
                public void onSuccess(Widget model) {
                    assertNotNull("No model found with ID 2", model);
                    model.setName("Barfoo");
                    model.save(findAgain);
                }
            };

            @Override
            public void run() {
                widgetRepository.findById(2, update);
            }
        });
    }
}
