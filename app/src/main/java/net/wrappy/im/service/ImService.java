package net.wrappy.im.service;

import android.content.Context;

public interface ImService {
    public void showToast(CharSequence text, int duration);
    public Context getApplicationContext();
}
