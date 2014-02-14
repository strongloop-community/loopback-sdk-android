package com.strongloop.android.loopback.callbacks;

import com.strongloop.android.remoting.adapters.Adapter;

public class EmptyResponseParser implements Adapter.Callback {
    private final VoidCallback callback;

    public EmptyResponseParser(VoidCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onSuccess(String response) {
        callback.onSuccess();
    }

    @Override
    public void onError(Throwable t) {
        callback.onError(t);
    }
}
