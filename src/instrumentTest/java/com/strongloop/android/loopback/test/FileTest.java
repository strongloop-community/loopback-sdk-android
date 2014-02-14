package com.strongloop.android.loopback.test;

import android.os.Environment;
import android.util.Log;

import com.strongloop.android.loopback.Container;
import com.strongloop.android.loopback.ContainerRepository;
import com.strongloop.android.loopback.File;
import com.strongloop.android.loopback.FileRepository;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.RestContractItem;

import java.util.List;

public class FileTest extends AsyncTestCase {
    static final private String TAG = "FileTest";

    private RestAdapter adapter;
    private ContainerRepository containerRepo;
    private FileRepository fileRepo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        adapter = createRestAdapter();
        containerRepo = adapter.createRepository(ContainerRepository.class);
        fileRepo = adapter.createRepository(FileRepository.class);

        try {
            destroyAllContainers();
        } catch (Exception e) {
            throw e;
        } catch (Throwable t) {
            throw new Exception(t);
        }
    }

    public void testContainerCreateAndGet() throws Throwable {
        final String name = "a-container-to-create";

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                containerRepo.create(name, new ObjectTestCallback<Container>() {

                    @Override
                    public void onSuccess(Container container) {
                        Log.i(TAG, "container created");
                        assertEquals(name, container.getName());
                        notifyFinished();
                    }
                });
            }
        });

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                containerRepo.get(name, new ObjectTestCallback<Container>() {
                    @Override
                    public void onSuccess(Container result) {
                        assertEquals(name, result.getName());
                        notifyFinished();
                    }
                });
            }
        });
    }

    public void testContainerGetAll() throws Throwable {
        final Container container = givenContainer(containerRepo);

        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                containerRepo.getAll(new ListTestCallback<Container>() {
                    @Override
                    public void onSuccess(List<Container> containerList) {
                        assertNotNull("Container list should be not null.", containerList);
                        assertEquals("Number of containers", 1, containerList.size());
                        assertEquals("Name of container 1",
                                container.getName(),
                                containerList.get(0).getName());
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

    public void testGetContainerAndFile() throws Throwable {
        final Container container = givenContainer(containerRepo);

        doAsyncTest(new AsyncTest() {

            @Override
            public void run() {
                container.getFile("f1.txt",
                        new FileRepository.FileCallback() {

                            @Override
                            public void onSuccess(File newFile) {
                                assertNotNull("Null file", newFile);
                                Log.i(TAG, "get file");
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

    private void destroyAllContainers() throws Throwable {
        adapter.getContract().addItem(
                new RestContractItem("/containers", "DELETE"),
                "containers.destroyAll");
        await(new AsyncTask() {
            @Override
            public void run() {
                adapter.invokeStaticMethod(
                        "containers.destroyAll",
                        null,
                        new Adapter.Callback() {
                            @Override
                            public void onSuccess(String s) {
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
