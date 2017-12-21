package net.wrappy.im.tasks.sync;

import java.lang.ref.WeakReference;

/**
 * Created by Khoa.Nguyen on 12/21/2017.
 */

public class SyncDataRunnable<T> implements Runnable {
    T[] data;
    WeakReference<SyncDataListener<T>> weakReference;

    public SyncDataRunnable(SyncDataListener<T> dataListener, T[] data) {
        this.data = data;
        this.weakReference = new WeakReference<>(dataListener);
    }

    @Override
    public void run() {
        if (data != null && weakReference != null && weakReference.get() != null) {
            weakReference.get().sync(data);
        }
    }
}
