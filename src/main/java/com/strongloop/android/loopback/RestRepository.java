package com.strongloop.android.loopback;

import com.strongloop.android.remoting.Repository;
import com.strongloop.android.remoting.VirtualObject;
import com.strongloop.android.remoting.adapters.RestContract;

public class RestRepository<T extends VirtualObject> extends Repository<T>{
    public RestRepository(String className) {
        super(className);
    }

    public RestRepository(String className, Class<T> objectClass) {
        super(className, objectClass);
    }

    public RestContract createContract() {
        return new RestContract();
    }
}
