package net.wrappy.im.tasks.sync;

/**
 * Created by khoa.nguyen on 12/21/2017.
 */

public interface SyncDataListener<T> {
    void sync(T[] data);

    void processing(T[] data);
}
