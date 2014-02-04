package com.strongloop.android.loopback;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.json.JSONObject;

import com.strongloop.android.remoting.adapters.Adapter;

public class File extends Model {

    private String name;
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    
    private String url;
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
    
    private String container;
    public void setContainer(String container) {
        this.container = container;
    }
    public String getContainer()
    {
        return container;
    }
    
    /*** TODO - Restore when Container is in
    private java.io.File file;
    public void setFile(java.io.File file) {
        this.file = file;
    }
    public java.io.File getFile() {
        return file;
    }
    ***/
    
    // TODO - Move to Container
    private java.io.File uploadFiles;
    public void setUploadFiles(java.io.File file) {
        uploadFiles = file;
    }
    public java.io.File getUploadFiles() {
        return uploadFiles;
    }
    
     /**
     * Saves the File to the server.
     * <p>
     * This method calls {@link #toMap()} to determine which fields should be
     * saved.
     * @param callback The callback to be executed when finished.
     */
    public void upload(final Callback callback) {
        
        // TODO - Change to file when container is in
        uploadFiles = new java.io.File(url + '/' + name);
        
        invokeMethod("upload", toMap(),
                new Adapter.JsonObjectCallback() {

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }

            @Override
            public void onSuccess(JSONObject response) {
                Object id = response.opt("id");
                if (id != null) {
                    setId(id);
                }
                callback.onSuccess();
            }
        });
    }

    public boolean save(ByteBuffer byteBuffer) throws IOException
    {
        boolean saved = false;

        // TODO - Change to file when Container isin.
        uploadFiles = new java.io.File( url + "/" + name);
        
        BufferedOutputStream bos = null;
        
        bos = new BufferedOutputStream( new FileOutputStream(uploadFiles) );
            bos.write(byteBuffer.array());
            bos.flush();
            bos.close();
            saved = true;
        return saved;
    }    
    
}
