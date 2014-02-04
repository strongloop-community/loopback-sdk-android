package com.strongloop.android.loopback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.strongloop.android.remoting.JsonUtil;
import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.RestContract;
import com.strongloop.android.remoting.adapters.RestContractItem;

public class ContainerRepository extends ModelRepository<Container> {

    public ContainerRepository() {
        super("container", Container.class);
    }

    public interface ContainerCallback {
        public void onSuccess(Container container);
        public void onError(Throwable t);
    }

    public interface AllContainersCallback {
        public void onSuccess(List<Container> containerList);
        public void onError(Throwable t);
    }
    
    public Container createContainer(String name) {
        Map<String, Object>map = new HashMap<String, Object>();
        map.put("name", name);
        Container container = createModel(map);
        return container;
    }
    
    /**
     * Creates a {@link RestContract} representing the user type's custom
     * routes. Used to extend an {@link Adapter} to support user. Calls
     * super {@link ModelRepository) createContract first. 
     *
     * @return A {@link RestContract} for this model type.
     */
     
    public RestContract createContract() {
        RestContract contract = super.createContract();
        
        String className = getClassName();
        
        contract.addItem(new RestContractItem("/" + getNameForRestUrl(), "GET"),
                className + ".getAll");
        return contract;
    }
    
    public void getAllContainers( final AllContainersCallback callback) {
        
        invokeStaticMethod("getAll", null, new Adapter.JsonArrayCallback() {

            @Override
            public void onError(Throwable t) {
                callback.onError(t);                
            }

            @Override
            public void onSuccess(JSONArray response) {
                // create containers
                List<Container> containerList;
                try {
                    containerList = createContainers(response);
                    callback.onSuccess(containerList);
                } catch (JSONException e) {
                    callback.onError(e);
                }
            }

        });
    }
    
    public List<Container> createContainers(JSONArray jarray) throws JSONException {
        ArrayList<Container> containerList = new ArrayList<Container>(jarray.length());
        for( int i=0; i<jarray.length(); i++ ) {
            JSONObject jobj = jarray.getJSONObject(i);
            Container container = createModel(JsonUtil.fromJson(jobj));
            containerList.add(container);
        }
        return containerList;        
    }
}
