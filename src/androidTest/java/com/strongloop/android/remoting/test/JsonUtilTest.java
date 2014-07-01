package com.strongloop.android.remoting.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.strongloop.android.remoting.JsonUtil;
import com.strongloop.android.remoting.adapters.Adapter;

public class JsonUtilTest extends TestCase {

    private void doPlainObjectToJsonTest(Object object) throws JSONException {
        assertEquals(object, JsonUtil.toJson(object));
    }

    private void doToJsonShouldFailTest(Object object) {
        try {
            JsonUtil.toJson(object);
        }
        catch (JSONException ex) {
            assertTrue(true);
            return;
        }
        fail("Object conversion should throw an exception: " + object);
    }

    public void testPrimitives() throws JSONException {
        assertEquals(JSONObject.NULL, JsonUtil.toJson(null));

        doPlainObjectToJsonTest(JSONObject.NULL);
        doPlainObjectToJsonTest(false);
        doPlainObjectToJsonTest((byte)1);
        doPlainObjectToJsonTest((short)1);
        doPlainObjectToJsonTest((int)1);
        doPlainObjectToJsonTest((long)1);
        doPlainObjectToJsonTest(1.0f);
        doPlainObjectToJsonTest(1.0);
        doPlainObjectToJsonTest("string");

        doToJsonShouldFailTest(Double.NaN);
        doToJsonShouldFailTest(Double.POSITIVE_INFINITY);
        doToJsonShouldFailTest(Double.NEGATIVE_INFINITY);

        int[] intArray = { 0, 1, 2, 3 };
        JSONArray json = (JSONArray)JsonUtil.toJson(intArray);
        for (int i = 0; i < json.length(); i++) {
            assertEquals(intArray[i], json.get(i));
        }

        Object[] objectArray = { 0, "hello", 1.0f, false };
        json = (JSONArray)JsonUtil.toJson(objectArray);
        for (int i = 0; i < json.length(); i++) {
            assertEquals(objectArray[i], json.get(i));
        }

        assertNull(JsonUtil.fromJson((JSONArray)null));
        assertNull(JsonUtil.fromJson((JSONObject)null));
    }

    public void testObjectsAndArrays() throws JSONException {
        Map<?, ?> fromMap = ImmutableMap.of(
                "name", "fred",
                "age", 100,
                "scores", ImmutableList.of(5000, 4000, 3000),
                "location", ImmutableMap.of(
                        "lat", 37.7833,
                        "long", 122.4167),
                "isACoolPerson", true
        );

        // Convert to JSON and back to collections, and check for equality.
        Map<?, ?> toMap = JsonUtil.fromJson(
                (JSONObject)JsonUtil.toJson(fromMap));
        assertEquals(fromMap, toMap);

        List<?> fromList = ImmutableList.of(fromMap, fromMap, 1.0f, true);
        List<?> toList = JsonUtil.fromJson(
                (JSONArray)JsonUtil.toJson(fromList));
        assertEquals(fromList, toList);
    }

    // JSONObject doesn't implement an equals() method, so this is required.
    private void assertJsonEquals(String message, Object obj1, Object obj2) {
        if (obj1 instanceof JSONObject && obj2 instanceof JSONObject) {
            Map<?,?> json1 = JsonUtil.fromJson((JSONObject)obj1);
            Map<?,?> json2 = JsonUtil.fromJson((JSONObject)obj2);
            assertEquals(message, json1, json2);
        }
        else {
            assertEquals(message, obj1, obj2);
        }
    }

    private void doJsonParseTest(final String jsonString,
            final Object expectedValue) {
        // Simple test
        Adapter.JsonCallback callback = new Adapter.JsonCallback() {

            @Override
            public void onError(Throwable t) {
                fail(t.getLocalizedMessage());
            }

            @Override
            public void onSuccess(Object response) {
                assertJsonEquals("Parse error for json string: " + jsonString,
                        response, expectedValue);
            }
        };
        callback.onSuccess(jsonString);

        // Test with extra spacing
        callback.onSuccess("\n  " + jsonString + "  \n");

        // Test expected object or null
        Adapter.JsonObjectCallback objectCallback =
                new Adapter.JsonObjectCallback() {

            @Override
            public void onError(Throwable t) {
                if (t instanceof JSONException &&
                        !(expectedValue instanceof JSONObject)) {
                    assertTrue("Expected JSONException", true);
                }
                else {
                    fail("Did not expect JSONException: " + jsonString);
                }
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (expectedValue instanceof JSONObject) {
                    assertJsonEquals("Parse error for json string: " +
                            jsonString, response, expectedValue);
                }
                else if (response == null) {
                    assertJsonEquals("Expected null", expectedValue,
                            JSONObject.NULL);
                }
                else {
                    fail("Did not expect object for string: " + jsonString);
                }
            }
        };
        objectCallback.onSuccess(jsonString);

        // Test expected array or null
        Adapter.JsonArrayCallback arrayCallback =
                new Adapter.JsonArrayCallback() {

            @Override
            public void onError(Throwable t) {
                if (t instanceof JSONException &&
                        !(expectedValue instanceof JSONArray)) {
                    assertTrue("Expected JSONException", true);
                }
                else {
                    assertTrue("Didn not expect JSONException: " + jsonString,
                            false);
                }
            }

            @Override
            public void onSuccess(JSONArray response) {
                if (expectedValue instanceof JSONArray) {
                    assertJsonEquals("Parse error for json string: " +
                            jsonString, response, expectedValue);
                }
                else if (response == null) {
                    assertJsonEquals("Expected null", expectedValue,
                            JSONObject.NULL);
                }
                else {
                    fail("Did not expect array for string: " + jsonString);
                }
            }
        };
        arrayCallback.onSuccess(jsonString);
    }

    private void doJsonParseTestShouldFail(final String jsonString) {
        Adapter.JsonCallback callback = new Adapter.JsonCallback() {

            @Override
            public void onError(Throwable t) {
                assertTrue("Expected JSONException",
                        t instanceof JSONException);
            }

            @Override
            public void onSuccess(Object response) {
                String responseString = String.valueOf(response);
                if (response != null) {
                    responseString = "(" + response.getClass() + ") + " +
                            responseString;
                }
                fail("JSON Parsing should fail for json string: " +
                        jsonString + " but parsed to: " + responseString);
            }
        };
        callback.onSuccess(jsonString);
    }

    public void testJsonParsingInCallbacks() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", "fred");
        object.put("score", 100);
        JSONArray array = new JSONArray(Arrays.asList(
                JSONObject.NULL,
                "hello",
                1.0,
                true));
        doJsonParseTest("null", JSONObject.NULL);
        doJsonParseTest("\"hello\"", "hello");
        doJsonParseTest("1.0", 1.0);
        doJsonParseTest("true", true);
        doJsonParseTest("{ \"name\":\"fred\", \"score\": 100 }", object);
        doJsonParseTest("[ null, \"hello\", 1.0, true ]", array);

        doJsonParseTestShouldFail(null);
        doJsonParseTestShouldFail("");
        doJsonParseTestShouldFail("   \n\n\n ");
        doJsonParseTestShouldFail("[}");

        // The Android JSON parser is lenient, so these should fail but don't:
        //doJsonParseTestShouldFail("blah"); // String
        //doJsonParseTestShouldFail("[blah]"); // Array with a String
        //doJsonParseTestShouldFail("0x100"); // Integer
        //doJsonParseTestShouldFail("[1,]"); // Parsed as [1,null]
    }
}
