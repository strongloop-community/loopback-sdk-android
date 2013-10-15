// Copyright (c) 2013 StrongLoop. All rights reserved.

package com.strongloop.android.loopback;

import android.content.Context;

import com.strongloop.android.remoting.adapters.RestAdapter;

/**
 * An extension to the vanilla {@link RestAdapter} to make working with
 * {@link Model}s easier.
 */
public class ModelAdapter<T extends Model> extends RestAdapter {

    public ModelAdapter(Context context, String url) {
        super(context, url);
    }

    /**
     * Creates a new {@link ModelPrototype} representing the named model type.
     * @param name The model name.
     * @return A new prototype instance.
     */
    public ModelPrototype<T> createPrototype(String name) {
        return createPrototype(name, null, null);
    }

    /**
     * Creates a new {@link ModelPrototype} representing the named model type.
     * @param name The model name.
     * @param nameForRestUrl The model name to use in REST URL, usually the plural form of `name`.
     * @return A new prototype instance.
     */
    public ModelPrototype<T> createPrototype(String name, String nameForRestUrl) {
        return createPrototype(name, nameForRestUrl, null);
    }

    /**
     * Creates a new {@link ModelPrototype} representing the named model type.
     * @param name The model name.
     * @param nameForRestUrl The model name to use in REST URL, usually the plural form of `name`.
     * @param modelClass The model class. The class must have a public
     * no-argument constructor.
     * @return A new prototype instance.
     */
    public ModelPrototype<T> createPrototype(String name,
                                             String nameForRestUrl,
                                             Class<T> modelClass) {
        ModelPrototype<T> prototype = new ModelPrototype<T>(name, nameForRestUrl, modelClass);
        attachPrototype(prototype);
        return prototype;
    }

    /**
     * Creates a new {@link ModelPrototype} from the given subclass.
     * @param prototypeClass A subclass of {@link ModelPrototype} to use.
     * The class must have a public no-argument constructor.
     * @return A new prototype instance.
     */
    public <U extends ModelPrototype<T>> U createPrototype(
            Class<U> prototypeClass) {
        U prototype = null;
        try {
            prototype = prototypeClass.newInstance();
        }
        catch (Exception e) {
            IllegalArgumentException ex = new IllegalArgumentException();
            ex.initCause(e);
            throw ex;
        }
        attachPrototype(prototype);
        return prototype;
    }

    private void attachPrototype(ModelPrototype<T> prototype) {
        getContract().addItemsFromContract(prototype.createContract());
        prototype.setAdapter(this);
    }
}
