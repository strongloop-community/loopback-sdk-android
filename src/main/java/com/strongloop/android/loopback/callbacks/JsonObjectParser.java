package com.strongloop.android.loopback.callbacks;

import com.strongloop.android.remoting.JsonUtil;
import com.strongloop.android.remoting.Repository;
import com.strongloop.android.remoting.VirtualObject;
import com.strongloop.android.remoting.adapters.Adapter;

import org.json.JSONObject;

public class JsonObjectParser<T extends VirtualObject>
        extends Adapter.JsonObjectCallback {

    private final Repository<T> repository;
    private final ObjectCallback<T> callback;

    public JsonObjectParser(Repository<T> repository, ObjectCallback<T> callback) {
        this.repository = repository;
        this.callback = callback;
    }

    @Override
    public void onSuccess(JSONObject response) {
        if (response == null) {
            // Not found
            callback.onSuccess(null);
            return;
        }

        callback.onSuccess(
                repository.createObject(JsonUtil.fromJson(response)));
    }

    @Override
    public void onError(Throwable throwable) {
        callback.onError(throwable);
    }
}
