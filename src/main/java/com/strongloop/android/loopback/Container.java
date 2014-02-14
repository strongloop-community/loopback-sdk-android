package com.strongloop.android.loopback;

import com.strongloop.android.remoting.VirtualObject;


public class Container extends VirtualObject {

    private String name;
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public File createFile(String name, String url) {
        return getFileRepository().createFile(getName(), name, url);
    }

    public void getFile( String fileName, final FileRepository.FileCallback callback) {
        getFileRepository().get(getName(), fileName, callback);
    }

    private FileRepository getFileRepository() {
        return new FileRepository(getName());
    }
}
