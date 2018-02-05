package net.wrappy.im.helper;

import android.content.Context;
import android.view.View;

import net.wrappy.im.R;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.PopupUtils;

/**
 * Created by hp on 1/17/2018.
 */
public abstract class RestAPIListener {

    /**
     * empty constructor do not show error message
     */
    public RestAPIListener() {

    }

    /**
     * Constructor use activity to show error message
     */
    public RestAPIListener(Context context) {
        this.mContext = context;
    }

    protected abstract void OnComplete(String s);

    protected void onError(ErrorCode errorCode) {
        onError(errorCode.getErrorCode());
    }

    protected void onError(int errorCode, String url) {
        Debug.d(url);
        onError(errorCode);
    }

    protected void onError(int errorCode) {
        AppFuncs.dismissProgressWaiting();
        if (mContext != null) {
            int resId = getResId("error_" + errorCode);
            if (resId > 0) {
                PopupUtils.showOKDialog(mContext, mContext.getString(R.string.error), mContext.getString(resId), onOkListener);
            } else
                PopupUtils.showOKDialog(mContext, mContext.getString(R.string.error), mContext.getString(R.string.error_unknown), onOkListener);
        }
    }

    public int getResId(String resName) {
        try {
            return mContext.getResources().getIdentifier(resName, "string", mContext.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    private View.OnClickListener onOkListener;
    private Context mContext;

    public void setOnListener(View.OnClickListener onOkListener) {
        this.onOkListener = onOkListener;
    }
}
