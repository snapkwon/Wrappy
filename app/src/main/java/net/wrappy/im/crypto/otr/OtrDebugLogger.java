package net.wrappy.im.crypto.otr;

import net.wrappy.im.ImApp;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.LogCleaner;
import android.util.Log;

public class OtrDebugLogger {

    public static void log(String msg) {
        if (Debug.DEBUG_ENABLED)// && Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG))
            Log.d(ImApp.LOG_TAG, LogCleaner.clean(msg));
    }

    public static void log(String msg, Exception e) {
        Log.e(ImApp.LOG_TAG, LogCleaner.clean(msg), e);
    }
}
