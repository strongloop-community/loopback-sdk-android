package com.strongloop.android.loopback.test;

import com.strongloop.android.loopback.callbacks.ListCallback;
import com.strongloop.android.loopback.callbacks.ObjectCallback;
import com.strongloop.android.loopback.callbacks.VoidCallback;
import com.strongloop.android.remoting.VirtualObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AsyncTask implements Runnable {
    private CountDownLatch signal;
    private Throwable failException;

    private Throwable getFailException() {
        return failException;
    }

    private void setSignal(final CountDownLatch signal) {
        this.signal = signal;
    }

    protected void notifyFailed(Throwable reason) {
        failException = reason;
        notifyFinished();
    }

    protected void notifyFinished() {
        signal.countDown();
    }

    /**
     * VoidCallback that reports error as test failures.
     */
    public class VoidTestCallback implements VoidCallback {
        @Override
        public void onSuccess() {
            notifyFinished();
        }

        @Override
        public void onError(Throwable t) {
            notifyFailed(t);
        }
    }

    /**
     * ObjectCallback<T> that reports errors as test failures.
     * @param <T> The Model type.
     */
    public abstract class ObjectTestCallback<T extends VirtualObject>
            implements ObjectCallback<T> {

        @Override
        public void onError(Throwable t) {
            notifyFailed(t);
        }
    }

    /**
     * ListCallback<T> that reports errors as test failures.
     * @param <T> The Model type.
     */
    public abstract class ListTestCallback<T extends VirtualObject>
            implements ListCallback<T> {

        @Override
        public void onError(Throwable t) {
            notifyFailed(t);
        }
    }


    public static class Runner implements Runnable {

        private final AsyncTask asyncTask;
        private final CountDownLatch signal = new CountDownLatch(1);
        private Throwable uncaughtException;

        public Runner(AsyncTask asyncTask) {
            this.asyncTask = asyncTask;
            this.asyncTask.setSignal(signal);
        }

        @Override
        public void run() {
            try {
                asyncTask.run();
            }
            catch (Throwable t) {
                uncaughtException = t;
                signal.countDown();
            }
        }

        public void await() throws Throwable {
            if (!signal.await(30, TimeUnit.SECONDS))
                uncaughtException = new TimeoutException("The task timed out.");

            if (uncaughtException == null)
                uncaughtException = asyncTask.getFailException();

            if (uncaughtException != null)
                throw uncaughtException;

            if (signal.getCount() != 0)
                throw new AssertionError("Unexpected result: the task has neither finished nor failed.");
        }
    }
}
