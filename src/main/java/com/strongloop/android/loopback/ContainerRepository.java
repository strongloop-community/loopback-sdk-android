package com.strongloop.android.loopback;

import com.google.common.collect.ImmutableMap;
import com.strongloop.android.loopback.callbacks.JsonArrayParser;
import com.strongloop.android.loopback.callbacks.JsonObjectParser;
import com.strongloop.android.loopback.callbacks.ListCallback;
import com.strongloop.android.loopback.callbacks.ObjectCallback;
import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.RestContract;
import com.strongloop.android.remoting.adapters.RestContractItem;

public class ContainerRepository extends RestRepository<Container> {

    private String getNameForRestUrl() {
        return "containers";
    }

    public ContainerRepository() {
        super("container", Container.class);
    }

    /**
     * Creates a {@link RestContract} representing the user type's custom
     * routes. Used to extend an {@link Adapter} to support user. Calls
     * super {@link ModelRepository} createContract first.
     *
     * @return A {@link RestContract} for this model type.
     */
     
    public RestContract createContract() {
        RestContract contract = super.createContract();

        String className = getClassName();

        contract.addItem(new RestContractItem("/" + getNameForRestUrl(), "POST"),
                className + ".create");

        contract.addItem(new RestContractItem("/" + getNameForRestUrl(), "GET"),
                className + ".getAll");

        contract.addItem(new RestContractItem("/" + getNameForRestUrl() + "/:name", "GET"),
                className + ".get");        
        
        contract.addItem(new RestContractItem("/" + getNameForRestUrl() + "/:name", "DELETE"),
                className + ".prototype.remove");
        
        return contract;
    }

    public void create(String name, ObjectCallback<Container> callback) {
        invokeStaticMethod("create", ImmutableMap.of("name", name),
                new JsonObjectParser<Container>(this, callback));
    }
    
    public void get(String containerName, ObjectCallback<Container> callback) {
        invokeStaticMethod("get", ImmutableMap.of("name", containerName),
                new JsonObjectParser<Container>(this, callback));
    }
    
    public void getAll(ListCallback<Container> callback) {
        invokeStaticMethod("getAll", null,
                new JsonArrayParser<Container>(this, callback));
    }
}