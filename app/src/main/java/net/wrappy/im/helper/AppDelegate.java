package net.wrappy.im.helper;

/**
 * Created by ben on 07/12/2017.
 */

public interface AppDelegate {
    public static final int ACTION_FROM_QUESTION = 1;
    public static final int ACTION_FROM_CREATE_NEW = 2;
    public static final int ACTION_FROM_RESET_EMAIL = 3;
    public static final int ACTION_FROM_CHECK_EMAIL = 4;

    public void onChangeInApp(int id, String data);
}
