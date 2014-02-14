package com.strongloop.android.loopback.callbacks;

import com.strongloop.android.remoting.VirtualObject;

public interface ObjectCallback<T extends VirtualObject> {
    public void onSuccess(T object);
    public void onError(Throwable t);
}

