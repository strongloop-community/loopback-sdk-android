package com.strongloop.android.loopback.callbacks;

import com.strongloop.android.remoting.JsonUtil;
import com.strongloop.android.remoting.Repository;
import com.strongloop.android.remoting.VirtualObject;
import com.strongloop.android.remoting.adapters.Adapter;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class JsonArrayParser<T extends VirtualObject> extends Adapter.JsonArrayCallback {
    private final Repository<T> repository;
    private final ListCallback<T> callback;

    public JsonArrayParser(Repository<T> repository, ListCallback<T> callback) {
        this.repository = repository;
        this.callback = callback;
    }

    @Override
    public void onSuccess(JSONArray response) {
        List<T> list = new ArrayList<T>();
        if (response != null) {
            for (int i = 0; i < response.length(); i++) {
                list.add(repository.createObject(JsonUtil.fromJson(
                        response.optJSONObject(i))));
            }
        }
        callback.onSuccess(list);
    }

    @Override
    public void onError(Throwable throwable) {
        callback.onError(throwable);
    }
}
