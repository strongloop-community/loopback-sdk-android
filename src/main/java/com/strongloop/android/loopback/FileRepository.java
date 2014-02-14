package com.strongloop.android.loopback;

import com.google.common.collect.ImmutableMap;
import com.strongloop.android.loopback.callbacks.JsonArrayParser;
import com.strongloop.android.loopback.callbacks.JsonObjectParser;
import com.strongloop.android.loopback.callbacks.ListCallback;
import com.strongloop.android.loopback.callbacks.ObjectCallback;
import com.strongloop.android.remoting.JsonUtil;
import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.RestContract;
import com.strongloop.android.remoting.adapters.RestContractItem;
import com.strongloop.android.remoting.adapters.StreamParam;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FileRepository extends RestRepository<File> {
    private final static String TAG = "FileRepository";

    private Container container;

    public Container getContainer() {
        return container;
    }
    public String getContainerName() {
        return getContainer().getName();
    }

    public void setContainer(Container value) {
        container = value;
    }

    public FileRepository() {
        super("file", File.class);
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

        String basePath = "/containers/:container";
        String className = getClassName();

        contract.addItem(new RestContractItem(basePath + "/files/:name", "GET"),
                className + ".get");

        contract.addItem(new RestContractItem(basePath + "/files", "GET"),
                className + ".getAll");

        contract.addItem(
                RestContractItem.createMultipart(basePath + "/upload", "POST"),
                className + ".upload");

        contract.addItem(new RestContractItem(basePath +  "/download/:name", "GET"),
                className + ".prototype.download");

        contract.addItem(new RestContractItem(basePath + "/files/:name", "DELETE"),
                className + ".prototype.delete");

        return contract;
    }

    @Override
    public File createObject(Map<String, ? extends Object> parameters) {
        File file = super.createObject(parameters);
        file.setContainerRef(container);
        return file;
    }

    /**
     * Upload a new file
     * @param name The file name, must be unique within the container.
     * @param content Content of the file.
     * @param contentType Content type (optional).
     * @param callback The callback to be executed when finished.
     */
    public void upload(String name, byte[] content, String contentType,
                       ObjectCallback<File> callback) {
        upload(name, new ByteArrayInputStream(content), contentType, callback);
    }

    /**
     * Upload a new file
     * @param name The file name, must be unique within the container.
     * @param content Content of the file.
     * @param contentType Content type (optional).
     * @param callback The callback to be executed when finished.
     */
    public void upload(String name, InputStream content, String contentType,
                       final ObjectCallback<File> callback) {

        StreamParam param = new StreamParam(content, name, contentType);
        invokeStaticMethod("upload",
                ImmutableMap.of("container", getContainerName(), "file", param),
                new UploadResponseParser(this, callback));
    }

    /**
     * Upload a new file
     * @param localFile The local file to upload.
     * @param callback The callback to be executed when finished.
     */
    public void upload(java.io.File localFile, final ObjectCallback<File> callback) {
        invokeStaticMethod("upload",
                ImmutableMap.of("container", getContainerName(), "file", localFile),
                new UploadResponseParser(this, callback));
    }

    /**
     * Get file by name
     * @param name The name of the file to get.
     * @param callback The callback to be executed when finished.
     */
    public void get(String name, final ObjectCallback<File> callback) {
        final HashMap<String, Object> params = new HashMap<String, Object>();

        params.put("container", getContainerName());
        params.put("name", name);
        invokeStaticMethod("get", params,
                new JsonObjectParser<File>(this, callback));
    }

    /**
     * List all files in the container.
     * @param callback The callback to be executed when finished.
     */
    public void getAll(ListCallback<File> callback) {
        invokeStaticMethod("getAll",
                ImmutableMap.of("container", getContainerName()),
                new JsonArrayParser<File>(this, callback));
    }

    private class UploadResponseParser extends Adapter.JsonObjectCallback {
        private final FileRepository repository;
        private final ObjectCallback<File> callback;

        private UploadResponseParser(FileRepository repository, ObjectCallback<File> callback) {
            this.repository = repository;
            this.callback = callback;
        }

        @Override
        public void onSuccess(JSONObject response) {
            try {
                JSONObject data = response.getJSONObject("result")
                        .getJSONObject("files")
                        .getJSONArray("file")
                        .getJSONObject(0);
                callback.onSuccess(
                        repository.createObject(JsonUtil.fromJson(data)));
            } catch (JSONException e) {
                callback.onError(e);
            }
        }

        @Override
        public void onError(Throwable t) {
            callback.onError(t);
        }
    }
}
