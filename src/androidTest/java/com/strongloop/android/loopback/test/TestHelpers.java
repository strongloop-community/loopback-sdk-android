package com.strongloop.android.loopback.test;

import android.util.Log;

import com.google.common.collect.ImmutableSet;
import com.strongloop.android.loopback.Container;
import com.strongloop.android.loopback.ContainerRepository;
import com.strongloop.android.loopback.RestAdapter;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

public class TestHelpers {
    public static void assertPropertyNames(JSONObject obj, String... names) {
        Set<String> actual = new HashSet<String>();
        Iterator iter = obj.keys();
        while (iter.hasNext())
            actual.add((String) iter.next());

        Set<String> expected = ImmutableSet.copyOf(names);

        assertEquals(expected, actual);
    }
}
