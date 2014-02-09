package com.strongloop.android.loopback.test;

import android.os.Environment;
import android.util.Log;

import com.strongloop.android.loopback.Container;
import com.strongloop.android.loopback.ContainerRepository;
import com.strongloop.android.loopback.File;
import com.strongloop.android.loopback.FileRepository;
import com.strongloop.android.loopback.RestAdapter;

import java.util.List;

/**
 * Created by gmxtian on 2/7/14.
 */
public class FileTest extends AsyncTestCase {

    private RestAdapter adapter;
    private ContainerRepository containerRepo;
    private FileRepository fileRepo;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        adapter = new RestAdapter(getActivity(), REST_SERVER_URL);
        containerRepo = adapter.createRepository(ContainerRepository.class);
        fileRepo = adapter.createRepository(FileRepository.class);
    }

    // create container
    public void testCreateDelete() throws Throwable {

        final Container container = containerRepo.createContainer("containertest45");

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                container.save( new ModelCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i("FileTest", "create container");
                        notifyFinished();
                    }
                });
            }
        });

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                container.delete(new ModelCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i("FileTest", "delete container");
                        notifyFinished();
                    }
                });
            }
        });

    }

    public void testGetContainerAndFile() throws Throwable {

        final Object[] containers = new Object[1];

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {

                containerRepo.get("container1",
                        new ContainerRepository.ContainerCallback() {

                    @Override
                    public void onSuccess(Container newContainer) {
                        assertNotNull("Null container", newContainer);
                        containers[0] = newContainer;
                        Log.i("FileTest", "get container");
                        notifyFinished();
                    }

                    @Override
                    public void onError(Throwable t) {
                        notifyFailed(t);
                    }

                });
            }
        });

        assertNotNull(containers[0]);

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                Container container = (Container)containers[0];
                container.getFile("f1.txt",
                        new FileRepository.FileCallback() {

                            @Override
                            public void onSuccess(File newFile) {
                                assertNotNull("Null file", newFile);
                                Log.i("FileTest", "get file");
                                notifyFinished();
                            }

                            @Override
                            public void onError(Throwable t) {
                                notifyFailed(t);
                            }
                        });
            }
        });
    }


    // download / upload a file
    public void testDownloadUpload() throws Throwable {

        final java.io.File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {

                fileRepo.download(storageDir.toString(), "container1", "nope.jpg",
                        new FileRepository.FileCallback() {

                    @Override
                    public void onSuccess(File file) {
                        assertNotNull("File is null", file);
                        notifyFinished();
                    }

                    @Override
                    public void onError(Throwable t) {
                        notifyFailed(t);
                    }

                });
            }
        });
    }

    // create container
    public void testGetAllContainers() throws Throwable {


        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                containerRepo.getAll(new ContainerRepository.AllContainersCallback() {
                    @Override
                    public void onSuccess(List<Container> containerList) {
                        assertNotNull("No container list", containerList);
                        assertTrue("Empty container list", containerList.size() > 0);
                        Log.i("FileTest", "get all containers");
                        notifyFinished();
                    }
                    @Override
                    public void onError(Throwable t) {
                        notifyFailed(t);
                    }

                });
            }
        });
    }
    
}
