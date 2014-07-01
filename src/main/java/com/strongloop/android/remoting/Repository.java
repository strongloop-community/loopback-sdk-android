// Copyright (c) 2013 StrongLoop. All rights reserved.

package com.strongloop.android.remoting;

import java.util.Map;

import com.strongloop.android.remoting.adapters.Adapter;

/**
 * A local representative of remote model repository, it provides
 * access to static methods like <pre>User.findById()</pre>.
 */
public class Repository<T extends VirtualObject> {

    private final Class<T> objectClass;
    private String className;
    private Adapter adapter;

    /**
     * Creates a new Repository, associating it with the named remote class.
     * @param className The remote class name.
     */
    public Repository(String className) {
        this(className, null);
    }

    /**
     * Creates a new Repository, associating it with the named remote class.
     * @param className The remote class name.
     */
    @SuppressWarnings("unchecked")
    public Repository(String className, Class<T> objectClass) {
        if (className == null || className.length() == 0) {
            throw new IllegalArgumentException(
                    "Class name cannot be null or empty.");
        }
        this.className = className;

        if (objectClass != null)
            this.objectClass = objectClass;
        else
            this.objectClass = (Class<T>)VirtualObject.class;
    }

    /**
     * Gets the name given to this prototype on the server.
     * @return the class name.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets the {@link Adapter} that should be used for invoking methods, both
     * for static methods on this prototype and all methods on all instances of
     * this prototype.
     * @return the adapter.
     */
    public Adapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the {@link Adapter} that should be used for invoking methods, both
     * for static methods on this prototype and all methods on all instances of
     * this prototype.
     * @param adapter The adapter.
     */
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Creates a new {@link VirtualObject} as a virtual instance of this remote
     * class.
     * @param creationParameters The creation parameters of the new object.
     * @return A new {@link VirtualObject} based on this prototype.
     */
    public T createObject(
            Map<String, ? extends Object> creationParameters) {
        T object = null;
        try {
            object = objectClass.newInstance();
        }
        catch (Exception e) {
            IllegalArgumentException ex = new IllegalArgumentException();
            ex.initCause(e);
            throw ex;
        }
        object.setRepository(this);
        if (creationParameters != null) {
            object.setCreationParameters(creationParameters);
            BeanUtil.setProperties(object, creationParameters, true);
        }
        return object;
    }

    /**
     * Invokes a remotable method exposed statically within this class on the
     * server.
     * @see Adapter#invokeStaticMethod(String, Map,
     * com.strongloop.android.remoting.adapters.Adapter.Callback)
     * @param method The method to invoke (without the class name), e.g.
     * <code>"doSomething"</code>.
     * @param parameters The parameters to invoke with.
     * @param callback The callback to invoke when the execution finishes.
     */
    public void invokeStaticMethod(String method,
            Map<String, ? extends Object> parameters,
            Adapter.Callback callback) {
        if (adapter == null) {
            throw new IllegalArgumentException("No adapter set");
        }
        String path = className + "." + method;
        adapter.invokeStaticMethod(path, parameters, callback);
    }

    /**
     * Invokes a remotable method exposed statically within this class on the
     * server,
     * parses the response as binary data.
     * @see Adapter#invokeStaticMethod(String, Map,
     * com.strongloop.android.remoting.adapters.Adapter.Callback)
     * @param method The method to invoke (without the class name), e.g.
     * <code>"doSomething"</code>.
     * @param parameters The parameters to invoke with.
     * @param callback The callback to invoke when the execution finishes.
     */
    public void invokeStaticMethod(String method,
                                   Map<String, ? extends Object> parameters,
                                   Adapter.BinaryCallback callback) {
        if (adapter == null) {
            throw new IllegalArgumentException("No adapter set");
        }
        String path = className + "." + method;
        adapter.invokeStaticMethod(path, parameters, callback);
    }
}
