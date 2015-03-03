package com.strongloop.android.remoting;

import android.util.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for flattening a Map
 */
public class RestUtil {
    private static final String TAG = "remoting.RestUtil";

    public Multimap<String, Object> flattenParameters(
            final Map<String, ? extends Object> parameters) {
        Multimap<String, Object> ret = flattenParameters(null, Multimaps.forMap(parameters));
        Log.d(TAG, "Ret:" + ret.toString());
        return ret;
    }

    @SuppressWarnings("unchecked")
    private Multimap<String, Object> flattenParameters(
            final String keyPrefix,
            final Multimap<String, ? extends Object> parameters) {

        // This method converts nested maps into a flat list
        //   Input:  { "here": { "lat": 10, "lng": 20 }
        //   Output: { "here[lat]": 10, "here[lng]": 20 }

        Multimap<String, Object> result = ArrayListMultimap.create();

        for (Map.Entry<String, ? extends Object> entry
                : parameters.entries()) {

            String key = keyPrefix != null
                    ? keyPrefix + "[" + entry.getKey() + "]"
                    : entry.getKey();

            Object value = entry.getValue();

            if (value instanceof Multimap) {
                result.putAll(flattenParameters(key, (Multimap) value));
            } else if (value instanceof Map) {
                result.putAll(flattenParameters(key, Multimaps.forMap((Map) value)));
            } else if(value instanceof List) {
                List tmp  = (List) value;
                for(int i=0; i<tmp.size(); i++) {
                    Object obj = tmp.get(i);
                    String newKey = key + "[" + i + "]";

                    if(obj instanceof Map) {
                        result.putAll(flattenParameters(newKey, Multimaps.forMap((Map) obj)));
                    }
                    else if(obj instanceof Multimap) {
                        result.putAll(flattenParameters(newKey, (Multimap) obj));
                    }
                    else {
                        result.put(newKey, obj);
                    }
                }
            }
            else {
                result.put(key, value);
            }
        }

        return result;
    }
}
