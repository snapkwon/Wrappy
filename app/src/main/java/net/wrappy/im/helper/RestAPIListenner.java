package net.wrappy.im.helper;

import android.content.Context;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.util.PopupUtils;

import java.lang.reflect.Field;

/**
 * Created by hp on 1/17/2018.
 */
public abstract class RestAPIListenner {
    protected abstract void OnComplete(int httpCode, String error, String s);

    protected void onError(ErrorCode errorCode) {
        onError(errorCode.getErrorCode());
    }

    protected void onError(int errorCode) {
        Context context = ImApp.sImApp;
        int resId = getResId("error_" + errorCode);
        if (context != null && resId > 0) {
            PopupUtils.showOKDialog(context, context.getString(R.string.error), context.getString(resId), null);
        }
    }

    public int getResId(String resName) {

        try {
            Field idField = String.class.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
