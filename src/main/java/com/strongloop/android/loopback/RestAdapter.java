// Copyright (c) 2013 StrongLoop. All rights reserved.

package com.strongloop.android.loopback;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * An extension to the vanilla
 * {@link com.strongloop.android.remoting.adapters.RestAdapter}
 * to make working with
 * {@link Model}s easier.
 */
public class RestAdapter
        extends com.strongloop.android.remoting.adapters.RestAdapter {

    public static final String SHARED_PREFERENCES_NAME =
            RestAdapter.class.getCanonicalName();
    public static final String PROPERTY_ACCESS_TOKEN = "accessToken";

    private final Context context;

    public RestAdapter(Context context, String url) {
        super(context, url);
        if (context == null) throw new NullPointerException("context must be not null");
        this.context = context;
        setAccessToken(loadAccessToken());
    }

    public void setAccessToken(String accessToken) {
        saveAccessToken(accessToken);
        getClient().addHeader("Authorization", accessToken);
    }

    public void clearAccessToken() {
        getClient().addHeader("Authorization", null);
    }

    public Context getApplicationContext() {
        return context;
    }

    /**
     * Creates a new {@link ModelRepository} representing the named model type.
     * @param name The model name.
     * @return A new repository instance.
     */
    public ModelRepository<Model> createRepository(String name) {
        return createRepository(name, null, null);
    }

    /**
     * Creates a new {@link ModelRepository} representing the named model type.
     * @param name The model name.
     * @param nameForRestUrl The model name to use in REST URL, usually the plural form of `name`.
     * @return A new repository instance.
     */
    public ModelRepository<Model> createRepository(String name, String nameForRestUrl) {
        return createRepository(name, nameForRestUrl, null);
    }

    /**
     * Creates a new {@link ModelRepository} representing the named model type.
     * @param name The model name.
     * @param nameForRestUrl The model name to use in REST URL, usually the plural form of `name`.
     * @param modelClass The model class. The class must have a public
     * no-argument constructor.
     * @return A new repository instance.
     */
    public <T extends Model> ModelRepository<T> createRepository(String name,
                                               String nameForRestUrl,
                                               Class<T> modelClass) {
        ModelRepository<T> repository = new ModelRepository<T>(name, nameForRestUrl, modelClass);
        attachModelRepository(repository);
        return repository;
    }

    /**
     * Creates a new {@link ModelRepository} from the given subclass.
     * @param repositoryClass A subclass of {@link ModelRepository} to use.
     * The class must have a public no-argument constructor.
     * @return A new repository instance.
     */
    public <U extends RestRepository> U createRepository(
            Class<U> repositoryClass) {
        U repository = null;
        try {
            repository = repositoryClass.newInstance();
            repository.setAdapter(this);
        }
        catch (Exception e) {
            IllegalArgumentException ex = new IllegalArgumentException();
            ex.initCause(e);
            throw ex;
        }
        attachModelRepository(repository);
        return repository;
    }


    private void attachModelRepository(RestRepository repository) {
        getContract().addItemsFromContract(repository.createContract());
        repository.setAdapter(this);
    }

    private void saveAccessToken(String accessToken) {
        final SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PROPERTY_ACCESS_TOKEN, accessToken);
        editor.commit();
    }

    private String loadAccessToken() {
        return getSharedPreferences().getString(PROPERTY_ACCESS_TOKEN, null);
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(
                SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }
}
