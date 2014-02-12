// Copyright (c) 2013 StrongLoop. All rights reserved.

package com.strongloop.android.loopback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.atteo.evo.inflector.English;
import org.json.JSONArray;
import org.json.JSONObject;

import com.strongloop.android.remoting.JsonUtil;
import com.strongloop.android.remoting.Repository;
import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.RestContract;
import com.strongloop.android.remoting.adapters.RestContractItem;

/**
 * A local representative of a single model type on the server, encapsulating
 * the name of the model type for easy {@link Model} creation, discovery, and
 * management.
 */
public class ModelRepository<T extends Model> extends Repository {

    public interface FindCallback<T extends Model> {
        public void onSuccess(T model);
        public void onError(Throwable t);
    }

    public interface FindAllCallback<T extends Model> {
        public void onSuccess(List<T> models);
        public void onError(Throwable t);
    }

    private Class<T> modelClass;
    private String nameForRestUrl;

    public ModelRepository(String className) {
        this(className, null);
    }

    /**
     * Creates a new Repository, associating it with the named remote class.
     * @param className The remote class name.
     * @param modelClass The Model class. It must have a public no-argument
     * constructor.
     */
    public ModelRepository(String className, Class<T> modelClass) {
      this(className, null, modelClass);
    }

    /**
     * Creates a new Repository, associating it with the named remote class.
     * @param className The remote class name.
     * @param nameForRestUrl The pluralized class name to use in REST transport.
     * @param modelClass The Model class. It must have a public no-argument
     * constructor.
     */
    @SuppressWarnings("unchecked")
    public ModelRepository(String className, String nameForRestUrl, Class<T> modelClass) {
        super(className);

        this.nameForRestUrl = nameForRestUrl != null
                ? nameForRestUrl
                : English.plural(className);

        if (modelClass == null) {
            this.modelClass = (Class<T>)Model.class;
        }
        else {
            this.modelClass = modelClass;
        }
    }

   /**
    * Returns the name of the REST url
    * @return nameForRestUrl
    */
    public String getNameForRestUrl() {
        return nameForRestUrl;
    }

    /**
     * Sets the REST url
     * @param nameForRestUrl
     */
    public void setNameForRestUrl(String nameForRestUrl) {
        this.nameForRestUrl = nameForRestUrl;
    }

    /**
     * Creates a {@link RestContract} representing this model type's custom
     * routes. Used to extend an {@link Adapter} to support this model type.
     *
     * @return A {@link RestContract} for this model type.
     */
    public RestContract createContract() {
        RestContract contract = new RestContract();

        String className = getClassName();

        contract.addItem(new RestContractItem("/" + nameForRestUrl, "POST"),
                className + ".prototype.create");
        contract.addItem(new RestContractItem("/" + nameForRestUrl + "/:id", "PUT"),
                className + ".prototype.save");
        contract.addItem(
                new RestContractItem("/" + nameForRestUrl + "/:id", "DELETE"),
                className + ".prototype.remove");
        contract.addItem(new RestContractItem("/" + nameForRestUrl + "/:id", "GET"),
                className + ".findById");
        contract.addItem(new RestContractItem("/" + nameForRestUrl, "GET"),
                className + ".all");

        return contract;
    }

    /**
     * Creates a new {@link Model} of this type with the parameters described.
     * @param  parameters The parameters.
     * @return A new {@link Model}.
     */
    public T createModel(Map<String, ? extends Object> parameters) {
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }
        T model = null;
        try {
            model = modelClass.newInstance();
        }
        catch (Exception e) {
            IllegalArgumentException ex = new IllegalArgumentException();
            ex.initCause(e);
            throw ex;
        }
        model.setRepository(this);
        model.setCreationParameters(parameters);
        model.putAll(parameters);
        BeanUtil.setProperties(model, parameters, true);
        Object id = parameters.get("id");
        if (id != null) {
            model.setId(id);
        }
        return model;
    }

    /**
     * Finds and downloads a single instance of this model type on and from the
     * server with the given id.
     * @param id The id to search for.
     * @param callback The callback to be executed when finished.
     */
    public void findById(Object id, final FindCallback<T> callback) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        invokeStaticMethod("findById", params,
                new Adapter.JsonObjectCallback() {

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }

            @Override
            public void onSuccess(JSONObject response) {
                if (response == null) {
                    // Not found
                    callback.onSuccess(null);
                }
                else {
                    callback.onSuccess(
                            createModel(JsonUtil.fromJson(response)));
                }
            }
        });
    }

    /**
     * Finds and downloads all models of this type on and from the server.
     * @param callback The callback to be executed when finished.
     */
    public void findAll(final FindAllCallback<T> callback) {
        invokeStaticMethod("all", null, new Adapter.JsonArrayCallback() {

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }

            @Override
            public void onSuccess(JSONArray response) {
                List<T> list = new ArrayList<T>();
                if (response != null) {
                    for (int i = 0; i < response.length(); i++) {
                        list.add(createModel(JsonUtil.fromJson(
                                response.optJSONObject(i))));
                    }
                }
                callback.onSuccess(list);
            }
        });
    }
}
