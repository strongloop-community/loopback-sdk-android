package com.strongloop.android.loopback;

import org.json.JSONObject;

import com.strongloop.android.remoting.adapters.Adapter;


public class Container extends Model {

    private String name;
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }    

    public void delete(final Callback callback) {
        invokeMethod("remove", toMap(), 
            new Adapter.JsonObjectCallback() {
                
            @Override
            public void onError(Throwable t) {
                callback.onError(t);                    
            }
            
            @Override
            public void onSuccess(JSONObject response) {
                callback.onSuccess();
            }
        });
    }
    
    public void getFile( String fileName, final FileRepository.FileCallback callback) {
        ContainerRepository containerRepo = (ContainerRepository) getRepository();
        FileRepository fileRepo = containerRepo.getFileRepository();
        fileRepo.get(getName(), fileName, callback);
    }
}
