package com.strongloop.android.loopback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.RestContract;
import com.strongloop.android.remoting.adapters.RestContractItem;

public class FileRepository extends ModelRepository<File> {

    public FileRepository() {
        super("file", File.class);
    }
    
    public FileRepository(String nameForRestUrl) {
   
        super("file", nameForRestUrl, File.class);
    }
    
    public interface FileCallback {
        public void onSuccess(File file);
        public void onError(Throwable t);
    }
    
    public File createFile(String containerName, String fileName, String url) {
        Map<String, Object>map = new HashMap<String, Object>();
        map.put("name", fileName);
        map.put("url", url);
        map.put("container", containerName);
        File file = createModel(map);
        return file;
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
        
        contract.addItem(new RestContractItem("/" + getNameForRestUrl() + 
                "/:container/upload", "POST", true),
                className + ".prototype.upload");
        contract.addItem(new RestContractItem("/" + getNameForRestUrl() + 
                "/:container/download/:name", "GET", true),
                className + ".download");
        contract.addItem(new RestContractItem("/" + getNameForRestUrl() + 
                "/:container/files/:name", "GET"),
                className + ".prototype.get");
        contract.addItem(new RestContractItem("/" + getNameForRestUrl() + 
                "/:container/:name", "DELETE"),
                className + ".prototype.delete");
       
        return contract;
    }
   
    public void get(String containerName, String name, final FileCallback callback) {
        
        File fileModel = createModel(null);
        fileModel.setName(name);;
        fileModel.setContainer(containerName);
        fileModel.get(callback);
    }
    
    public void download(final String downloadPath, final String serverContainer, final String fileName,
            final FileCallback callback) {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("container", serverContainer);
        params.put("name", fileName);
                
        invokeStaticMethod("download", params,
                new Adapter.Callback() {

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }

            @Override
            public void onSuccess(String response, Object...data) {
                if ( data.length > 0 && data[0] instanceof ByteBuffer) {
                    ByteBuffer byteBuffer = (ByteBuffer)data[0];
                    File newFile;
                    try {
                        newFile = createFile(downloadPath, serverContainer, fileName, byteBuffer);
                        callback.onSuccess(newFile);
                    } catch (IOException e) {
                        callback.onError(e);
                    }
                }
            }
        });
        
    }
    
    protected File createFile(String downloadPath, String serverContainer, String fileName, ByteBuffer byteBuffer) throws IOException {
        File newFile = createFile(serverContainer, fileName, downloadPath);
        newFile.save(byteBuffer);
        return newFile;
    }
}
