// Copyright (c) 2013 StrongLoop. All rights reserved.

package com.strongloop.android.remoting;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility methods for converting JSON objects to Java collection objects
 * (and vice versa).
 */
public class JsonUtil {

    /**
     * Converts a Java object to a JSON object. A {@link java.util.Map} is
     * converted to a {@link org.json.JSONObject}, a {@link java.util.List} or
     * array is converted to a {@link org.json.JSONArray},
     * and <code>null</code> is converted to  {@link org.json.JSONObject#NULL}.
     * Other objects, like {@link java.lang.Number}, {@link java.lang.String},
     * and {@link java.lang.Boolean} are returned without conversion.
     * @param object The object to convert.
     * @return a JSON object.
     * @throws JSONException If the object cannot be converted.
     */
    public static Object toJson(Object object) throws JSONException {
        if (object == null || object == JSONObject.NULL) {
            return JSONObject.NULL;
        }
        else if (object instanceof Map) {
            Map<?,?> map = ((Map<?,?>)object);
            JSONObject json = new JSONObject();
            for (Map.Entry<?,?> entry : map.entrySet()) {
                json.put(String.valueOf(entry.getKey()),
                		toJson(entry.getValue()));
            }
            return json;
        }
        else if (object instanceof Iterable) {
            JSONArray json = new JSONArray();
            for (Object value : (Iterable<?>)object) {
                json.put(toJson(value));
            }
            return json;
        }
        else if (object.getClass().isArray()) {
            JSONArray json = new JSONArray();
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                json.put(toJson(Array.get(object, i)));
            }
            return json;
        }
        else if (object instanceof Number) {
            double d = ((Number)object).doubleValue();
            if (Double.isInfinite(d) || Double.isNaN(d)) {
                throw new JSONException("Numbers cannot be infinite or NaN.");
            }
            return object;
        }
        else if (object instanceof JSONObject ||
                object instanceof JSONArray ||
                object instanceof Boolean ||
                object instanceof String) {
            return object;
        }
        else {
            return object.toString();
        }
    }

    /**
     * Converts a {@link org.json.JSONObject} to a {@link java.util.Map}.
     * @param object The JSON object to convert.
     * @return a map, or <code>null</code> if the object is <code>null</code>.
     */
    public static Map<String, Object> fromJson(JSONObject object) {
        if (object == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        if (object != null) {
            Iterator<?> keys = object.keys();
            while (keys.hasNext()) {
                String key = (String)keys.next();
                map.put(key, fromJson(object.opt(key)));
            }
        }
        return map;
    }

    /**
     * Converts a {@link org.json.JSONArray} to a {@link java.util.List}.
     * @param array The JSON array to convert.
     * @return a list, or <code>null</code> if the array is <code>null</code>.
     */
    public static List<Object> fromJson(JSONArray array) {
        if (array == null) {
            return null;
        }
        List<Object> list = new ArrayList<Object>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                list.add(fromJson(array.opt(i)));
            }
        }
        return list;
    }

    private static Object fromJson(Object json) {
        if (json == JSONObject.NULL) {
            return null;
        }
        else if (json instanceof JSONObject) {
            return fromJson((JSONObject)json);
        }
        else if (json instanceof JSONArray) {
            return fromJson((JSONArray)json);
        }
        else {
            return json;
        }
    }
}
