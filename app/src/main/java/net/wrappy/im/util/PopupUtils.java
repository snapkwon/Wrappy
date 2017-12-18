package net.wrappy.im.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;

/**
 * Created by hp on 12/18/2017.
 */

public class PopupUtils {
    public static void getSelectionDialog(Context context, String title, CharSequence[] options, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (!TextUtils.isEmpty(title))
            builder.setTitle(title);

        builder.setItems(options, listener);

        builder.show();
    }

    public static void getDialog(Context context, String title, String message, int positiveButton, DialogInterface.OnClickListener positiveListener) {
        getDialog(context, title, message, positiveButton, positiveListener, false);
    }

    public static void getDialog(Context context, String title, String message, int positiveButton, DialogInterface.OnClickListener positiveListener, boolean isCancelable) {
        getDialog(context, title, message, positiveButton, -1, -1, positiveListener, null, null, isCancelable);
    }

    public static void getDialog(Context context, String title, String message, int positiveButton, int negativeButton, DialogInterface.OnClickListener positiveListener,
                                 DialogInterface.OnClickListener negativeListener) {
        getDialog(context, title, message, positiveButton, negativeButton, positiveListener, negativeListener, false);
    }

    public static void getDialog(Context context, String title, String message, int positiveButton, int negativeButton, DialogInterface.OnClickListener positiveListener,
                                 DialogInterface.OnClickListener negativeListener, boolean isCancelable) {
        getDialog(context, title, message, positiveButton, negativeButton, -1, positiveListener, negativeListener, null, isCancelable);
    }

    public static void getDialog(Context context, String title, String message, int positiveButton, int negativeButton, int neutralButton, DialogInterface.OnClickListener positiveListener,
                                 DialogInterface.OnClickListener negativeListener, DialogInterface.OnClickListener neutralListener) {
        getDialog(context, title, message, positiveButton, negativeButton, neutralButton, positiveListener,
                negativeListener, neutralListener, false);
    }

    public static void getDialog(Context context, String title, String message, int positiveButton, int negativeButton, int neutralButton, DialogInterface.OnClickListener positiveListener,
                                 DialogInterface.OnClickListener negativeListener, DialogInterface.OnClickListener neutralListener, boolean isCancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(isCancelable);
        builder.setTitle(title);
        builder.setMessage(message);

        if (neutralButton > 0)
            builder.setNeutralButton(neutralButton, neutralListener);
        if (positiveButton > 0)
            builder.setPositiveButton(positiveButton, positiveListener);
        if (negativeButton > 0)
            builder.setNegativeButton(negativeButton, negativeListener);

        AlertDialog alert = builder.create();
        alert.show();
    }
}
