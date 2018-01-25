package net.wrappy.im.util;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;

/**
 * Created by hp on 12/21/2017.
 */

public class Utils {

    public static int getContrastColor(int colorIn) {
        double y = (299 * Color.red(colorIn) + 587 * Color.green(colorIn) + 114 * Color.blue(colorIn)) / 1000;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }

    public static String formatDurationMedia(long duration) {
        return String.valueOf(Math.round(duration * 2 / 2000.0) + "secs");// round 3/4
    }

    public static int getWithScreenDP(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        int screenWidth = Math.round(displayMetrics.widthPixels / displayMetrics.density);

        return screenWidth;
    }

    public static int convertSpToPixels(float sp, Context context) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
        return px;
    }

    public static String isValidEmail(Context context, String email) {
        String error = null;
        if (!TextUtils.isEmpty(email) && !AppFuncs.isEmailValid(email)) {
            error = context.getString(R.string.error_invalid_email);
        } else if (TextUtils.isEmpty(email)) {
            error = context.getString(R.string.error_empty_email);
        }
        return error;
    }
}
