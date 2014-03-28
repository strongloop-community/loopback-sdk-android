package com.strongloop.android.loopback.test;

import android.os.Environment;
import android.test.MoreAsserts;
import android.util.Log;

import com.google.common.io.Files;
import com.strongloop.android.loopback.Container;
import com.strongloop.android.loopback.ContainerRepository;
import com.strongloop.android.loopback.File;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.callbacks.ObjectCallback;
import com.strongloop.android.remoting.adapters.Adapter;
import com.strongloop.android.remoting.adapters.RestContractItem;

import junit.framework.AssertionFailedError;

import org.apache.http.client.HttpResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileTest extends AsyncTestCase {
    static final private String TAG = "FileTest";
    private final byte[] binaryData = new byte[]{1, 2, 3};

    private RestAdapter adapter;
    private ContainerRepository containerRepo;
    private final java.io.File localDir = new java.io.File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "test-data");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        adapter = createRestAdapter();
        containerRepo = adapter.createRepository(ContainerRepository.class);

        try {
            destroyAllContainers();
        } catch (Exception e) {
            throw e;
        } catch (Throwable t) {
            throw new Exception(t);
        }

        setupLocalDir();
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
                });
            }
        });
    }

    public void testFileUpload() throws Throwable {
        final Container container = givenContainer(containerRepo);
        final String fileName = "a-file-name";

        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                container.upload(fileName, binaryData, null,
                        new ObjectTestCallback<File>() {
                            @Override
                            public void onSuccess(File file) {
                                assertEquals(fileName, file.getName());
                                notifyFinished();
                            }
                        });
            }
        });
    }

    public void testFileDownload() throws Throwable {
        final File file = givenFile(containerRepo, binaryData);
        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                file.download(new File.DownloadCallback() {
                    @Override
                    public void onSuccess(byte[] content, String contentType) {
                        MoreAsserts.assertEquals(binaryData, content);
                        notifyFinished();
                    }

                    @Override
                    public void onError(Throwable error) {
                        notifyFailed(error);
                    }
                });
            }
        });
    }

    public void testFileGet() throws Throwable {
        final File file = givenFile(containerRepo, "a-file.txt");
        final Container container = file.getContainerRef();

        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                container.getFile("a-file.txt", new ObjectTestCallback<File>() {
                    @Override
                    public void onSuccess(File file) {
                        assertEquals("a-file.txt", file.getName());
                        assertEquals(container.getName(), file.getContainer());
                        notifyFinished();
                    }
                });
            }
        });
    }

    public void testFileDelete() throws Throwable {
        final File file = givenFile(containerRepo, "a-file.txt");
        final Container container = file.getContainerRef();

        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                file.delete(new VoidTestCallback() {
                    @Override
                    public void onSuccess() {
                        container.getFile("a-file.txt", new ObjectCallback<File>() {
                            @Override
                            public void onSuccess(File object) {
                                notifyFailed(new AssertionFailedError(
                                        "Get of deleted file should have failed"));
                            }

                            @Override
                            public void onError(Throwable t) {
                                Log.i(TAG, "Get deleted file result: " + t);
                                HttpResponseException hre = (HttpResponseException) t;
                                assertEquals(500, hre.getStatusCode());
                                notifyFinished();
                            }
                        });
                    }
                });
            }
        });
    }

    public void testGetAllFiles() throws Throwable {
        final File file =  givenFile(containerRepo, "a-file.txt");
        final Container container = file.getContainerRef();

        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
               container.getAllFiles(new ListTestCallback<File>() {
                   @Override
                   public void onSuccess(List<File> files) {
                       String[] expectedNames = new String[]{"a-file.txt"};

                       List<String> actualNames = new ArrayList<String>();
                       for (File f : files) {
                           actualNames.add(f.getName());
                       }

                       MoreAsserts.assertEquals(expectedNames, actualNames.toArray());
                       notifyFinished();
                   }
               });
            }
        });
    }

    public void testFileUploadFromLocalFile() throws Throwable {
        final Container container = givenContainer(containerRepo);
        final java.io.File local = givenLocalFile(binaryData);

        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                container.upload(local, new ObjectTestCallback<File>() {
                    @Override
                    public void onSuccess(File file) {
                        assertEquals(local.getName(), file.getName());
                        notifyFinished();
                    }
                });
            }
        });

        Log.i(TAG, "Downloading the uploaded file");
        byte[] content = download(container, local.getName());
        MoreAsserts.assertEquals(binaryData, content);
    }

    public void testFileDownloadToLocalFile() throws Throwable {
        final File file = givenFile(containerRepo, binaryData);
        final java.io.File local = new java.io.File(localDir, "outfile");

        doAsyncTest(new AsyncTest() {
            @Override
            public void run() {
                file.download(local, new VoidTestCallback() {
                    @Override
                    public void onSuccess() {
                        try {
                            byte[] content = Files.toByteArray(local);
                            MoreAsserts.assertEquals(binaryData, content);
                            notifyFinished();
                        } catch (IOException ex) {
                            notifyFailed(ex);
                        }
                    }
                });
            }
        });
    }

    private byte[] download(final Container container, final String fileName)
            throws Throwable {
        final List<byte[]> ref = new ArrayList<byte[]>(1);
        ref.add(null);

        await(new AsyncTask() {
            @Override
            public void run() {
                container.createFileObject(fileName)
                        .download(new File.DownloadCallback() {
                            @Override
                            public void onSuccess(byte[] content, String contentType) {
                                ref.set(0, content);
                                notifyFinished();
                            }

                            @Override
                            public void onError(Throwable error) {
                                notifyFailed(error);
                            }
                        });
            }
        });

        return ref.get(0);
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

    private void setupLocalDir() throws Exception {
        String storageState = Environment.getExternalStorageState();
        assertEquals("External Storage must be mounted",
                Environment.MEDIA_MOUNTED, storageState);

        if (localDir.exists()) {
            cleanDir(localDir);
            return;
        }

        if (!localDir.mkdirs())
            throw new Error("Cannot create '" + localDir + "'");
    }

    private void cleanDir(java.io.File dir) {
        for (java.io.File f: dir.listFiles()) {
            if (f.isDirectory())
                cleanDir(f);
            else
                f.delete();
        }
    }

    public java.io.File givenLocalFile(byte[] content) throws IOException {
        if (!localDir.exists()) throw new Error(localDir + " does not exist!");
        java.io.File file = new java.io.File(localDir, "a-file.txt");
        Files.write(content, file);
        return file;
    }
}
