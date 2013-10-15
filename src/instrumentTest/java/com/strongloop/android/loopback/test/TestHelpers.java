package com.strongloop.android.loopback.test;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

public class TestHelpers {
    public static <T> Set<T> createSet(T... objs) {
        Set<T> set = new HashSet<T>();
        for (T o : objs) {
            set.add(o);
        }
        return set;
    }

    public static void assertPropertyNames(JSONObject obj, String... names) {
        Set<String> actual = new HashSet<String>();
        Iterator iter = obj.keys();
        while (iter.hasNext())
            actual.add((String) iter.next());

        Set<String> expected = createSet(names);

        assertEquals(expected, actual);
    }
}
