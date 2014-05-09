package com.strongloop.android.loopback;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.strongloop.android.loopback.callbacks.EmptyResponseParser;
import com.strongloop.android.loopback.callbacks.VoidCallback;
import com.strongloop.android.remoting.Transient;
import com.strongloop.android.remoting.VirtualObject;
import com.strongloop.android.remoting.adapters.Adapter;

import java.io.IOException;
import java.util.Map;

public class File extends VirtualObject {

    private String name;
    public void setName(String name) { this.name = name; }

    /**
     * The name of the file, e.g. "image.gif"
     * @return the name
     */
    public String getName() { return name; }
    
    private String url;
    public void setUrl(String url) { this.url = url; }

    /**
     * The URL of the file.
     * @return the URL
     */
    public String getUrl() {
        return url;
    }
    
    private Container container;
    @Transient
    public void setContainerRef(Container container) { this.container = container; }
    @Transient
    public Container getContainerRef() { return container; }

    /**
     * Name of the container this file belongs to.
     * @return the container name
     */
    public String getContainer() { return getContainerRef().getName(); }

    public static interface DownloadCallback {
        public void onSuccess(byte[] content, String contentType);
        public void onError(Throwable error);
    }

    /**
     * Download content of this file.
     * @param callback The callback to be executed when finished.
     */
    public void download(final DownloadCallback callback) {
        invokeMethod("download", getCommonParams(), new Adapter.BinaryCallback() {
            @Override
            public void onSuccess(byte[] content, String contentType) {
                callback.onSuccess(content, contentType);
            }

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }
        });
    }

    /**
     * Download content of this file to a local file.
     * @param localFile Path to the local file.
     * @param callback The callback to be executed when finished.
     */
    public void download(final java.io.File localFile, final VoidCallback callback) {
        download(new DownloadCallback() {
            @Override
            public void onSuccess(byte[] content, String contentType) {
                try {
                    Files.write(content, localFile);
                    callback.onSuccess();
                } catch (IOException ex) {
                    callback.onError(ex);
                }
            }

            @Override
            public void onError(Throwable error) {
                callback.onError(error);

            }
        });
    }

    /**
     * Delete this file.
     * @param callback The callback to be executed when finished.
     */
    public void delete(final VoidCallback callback) {
        invokeMethod("delete", getCommonParams(), new EmptyResponseParser(callback));
    }

    private Map<String, String> getCommonParams() {
        return ImmutableMap.of(
                "container", getContainer(),
                "name", getName());
    }
}
