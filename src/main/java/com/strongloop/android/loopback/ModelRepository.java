// Copyright (c) 2013 StrongLoop. All rights reserved.

package com.strongloop.android.loopback;

import com.strongloop.android.loopback.callbacks.JsonArrayParser;
import com.strongloop.android.loopback.callbacks.JsonObjectParser;
import com.strongloop.android.loopback.callbacks.ListCallback;
import com.strongloop.android.loopback.callbacks.ObjectCallback;
import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.RestContract;
import com.strongloop.android.remoting.adapters.RestContractItem;

import org.atteo.evo.inflector.English;

import java.util.HashMap;
import java.util.Map;

/**
 * A local representative of a single model type on the server, encapsulating
 * the name of the model type for easy {@link Model} creation, discovery, and
 * management.
 */
public class ModelRepository<T extends Model> extends RestRepository<T> {

    /**
     * @deprecated Use {link ObjectCallback} instead.
     */
    public interface FindCallback<T extends Model> extends ObjectCallback<T> {
    }

    /**
     * @deprecated Use {link ListCallback} instead.
     */
    public interface FindAllCallback<T extends Model> extends ListCallback<T> {
    }

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
     *                       Use {@code null} for the default value, which is the plural
     *                       form of className.
     * @param modelClass The Model class. It must have a public no-argument
     * constructor.
     */
    @SuppressWarnings("unchecked")
    public ModelRepository(String className, String nameForRestUrl, Class<T> modelClass) {
        super(className, modelClass != null ? modelClass : (Class<T>)Model.class);

        this.nameForRestUrl = nameForRestUrl != null
                ? nameForRestUrl
                : English.plural(className);
    }

   /**
    * Returns the name of the REST url
    * @return nameForRestUrl
    */
    public String getNameForRestUrl() {
        return nameForRestUrl;
    }

    /**
     * Creates a {@link RestContract} representing this model type's custom
     * routes. Used to extend an {@link Adapter} to support this model type.
     *
     * @return A {@link RestContract} for this model type.
     */
    public RestContract createContract() {
        RestContract contract = super.createContract();

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
        contract.addItem(new RestContractItem("/" + nameForRestUrl + "/findOne", "GET"),
                className + ".findOne");
        contract.addItem(new RestContractItem("/" + nameForRestUrl, "GET"),
                className + ".all");

        return contract;
    }

    /**
     * @deprecated Use {link ModelRepository#createObject} instead.
     */
    public T createModel(Map<String, ? extends Object> parameters) {
        return createObject(parameters);
    }

    /**
     * Creates a new {@link Model} of this type with the parameters described.
     * @param  parameters The parameters.
     * @return A new {@link Model}.
     */
    @Override
    public T createObject(Map<String, ? extends Object> parameters) {
        T model = super.createObject(parameters);
        model.putAll(parameters);

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
    public void findById(Object id, final ObjectCallback<T> callback) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        invokeStaticMethod("findById", params,
                new JsonObjectParser<T>(this, callback));
    }

    /**
     * Finds and downloads all models of this type on and from the server.
     * @param callback The callback to be executed when finished.
     */
    public void findAll(final ListCallback<T> callback) {
        find(null, callback);
    }

    /**
     * Finds and downloads all models of this type on and from the server.
     * that match the specified filter
     * @param parameters filter.
     * @param callback The callback to be executed when finished.
     */
    public void find(Map<String, ? extends Object> parameters, final ListCallback<T> callback) {
        invokeStaticMethod("all",
                parameters,
                new JsonArrayParser<T>(this, callback));
    }


    /**
     * Finds and downloads the first model of this type on and from the server.
     * that match the specified filter
     * @param parameters filter.
     * @param callback The callback to be executed when finished.
     */
    public void findOne(Map<String, ? extends Object> parameters, final ObjectCallback<T> callback) {
        invokeStaticMethod("findOne",
                parameters,
                new JsonObjectParser<T>(this, callback));
    }
}
