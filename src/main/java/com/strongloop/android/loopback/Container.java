package com.strongloop.android.loopback;

import com.google.common.collect.ImmutableMap;
import com.strongloop.android.loopback.callbacks.ListCallback;
import com.strongloop.android.loopback.callbacks.ObjectCallback;
import com.strongloop.android.remoting.VirtualObject;


public class Container extends VirtualObject {

    private String name;
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    /**
     * Upload a new file
     * @param file Content of the file.
     * @param callback The callback to be executed when finished.
     */
    public void upload(java.io.File file, ObjectCallback<File> callback) {
        getFileRepository().upload(file, callback);
    }

    /**
     * Upload a new file
     * @param fileName The file name, must be unique within the container.
     * @param content Content of the file.
     * @param contentType Content type (optional).
     * @param callback The callback to be executed when finished.
     */
    public void upload(String fileName, byte[] content, String contentType,
                       ObjectCallback<File> callback) {
        getFileRepository().upload(fileName, content, contentType, callback);
    }

    /**
     * Create a new File object associated with this container.
     * @param name The name of the file.
     * @return the object created
     */
    public File createFileObject(String name) {
        return getFileRepository().createObject(ImmutableMap.of("name", name));
    }

    /**
     * Get data of a File object.
     * @param fileName The name of the file.
     * @param callback The callback to be executed when finished.
     */
    public void getFile(String fileName, ObjectCallback<File> callback) {
        getFileRepository().get(fileName, callback);
    }

    /**
     * List all files in the container.
     * @param callback The callback to be executed when finished.
     */
    public void getAllFiles(ListCallback<File> callback) {
        getFileRepository().getAll(callback);
    }

    public FileRepository getFileRepository() {
        RestAdapter adapter = ((RestAdapter)getRepository().getAdapter());
        FileRepository repo = adapter.createRepository(FileRepository.class);
        repo.setContainer(this);
        return repo;
    }
}
