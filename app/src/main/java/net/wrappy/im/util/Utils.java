package net.wrappy.im.util;

import android.graphics.Color;

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
}
