package net.wrappy.im.util;

import android.content.Context;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import net.wrappy.im.BuildConfig;

import org.apache.commons.io.output.StringBuilderWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

public class Debug {

    public static boolean DEBUG_ENABLED = false;
    public static final boolean DEBUGGER_ATTACH_ENABLED = false;
    public static final boolean DEBUG_INJECT_ERRORS = false;
    private static int injectCount = 0;

    public static void recordTrail(Context context, String key, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        recordTrail(context, key, sdf.format(date));
    }

    public static String getTrail(Context context) {
        File trail = new File(context.getFilesDir(), "trail.properties");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(trail));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
            reader.close();
            return builder.toString();
        } catch (IOException e) {
            return "#notrail";
        }
    }

    public static void recordTrail(Context context, String key, String value) {
        File trail = new File(context.getFilesDir(), "trail.properties");
        Properties props = new Properties();
        try {
            FileReader reader = new FileReader(trail);
            props.load(reader);
            reader.close();
        } catch (IOException e) {
            // ignore
        }

        try {
            FileWriter writer = new FileWriter(trail);
            props.put(key, value);
            props.store(writer, "ChatSecure debug trail file");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getTrail(Context context, String key) {
        File trail = new File(context.getFilesDir(), "trail.properties");
        Properties props = new Properties();
        try {
            FileReader reader = new FileReader(trail);
            props.load(reader);
            reader.close();
            return props.getProperty(key);
        } catch (IOException e) {
            return null;
        }
    }

    public static void onConnectionStart() {
        if (DEBUG_ENABLED) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build());
        }
    }

    public static void onAppStart() {
        // Same StrictMode policy
        onConnectionStart();
    }

    public static void onServiceStart() {
        if (DEBUGGER_ATTACH_ENABLED)
            android.os.Debug.waitForDebugger();
    }

    public static void onHeartbeat() {
        if (DEBUG_ENABLED)
            System.gc();
    }

    public static String injectErrors(String body) {
        if (!DEBUG_INJECT_ERRORS)
            return body;
        // Inject an error every few blocks
        if (++injectCount % 5 == 0 && body.length() > 5)
            body = body.substring(0, 5) + 'X' + body.substring(6);
        return body;
    }

    static public void wrapExceptions(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            StringBuilderWriter writer = new StringBuilderWriter();
            PrintWriter pw = new PrintWriter(writer, true);
            t.printStackTrace(pw);
            writer.flush();
            throw new IllegalStateException("service throwable: " + writer.getBuilder().toString());
        }
    }

    private static final String TAG = "Wrappy";
    public static boolean DEBUGGABLE = BuildConfig.DEBUG;
    public static final String[] FORMAT_STRING = {
            "%d", "%b", "%f", "%l"};


    public static void d(String msg, Object... objects) {
        if (!DEBUGGABLE)
            return;
        d(String.format(wrapSMS(msg), wrapParams(objects)));
    }


    public static void i(String msg, Object... objects) {
        if (!DEBUGGABLE)
            return;
        i(String.format(wrapSMS(msg), wrapParams(objects)));
    }

    public static void e(String msg, Object... objects) {
        if (!DEBUGGABLE)
            return;
        e(String.format(wrapSMS(msg), wrapParams(objects)));
    }

    private static Object[] wrapParams(Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            objects[i] = String.valueOf(objects[i]);
        }
        return objects;
    }

    public static final void d(String msg) {
        if (!DEBUGGABLE)
            return;
        Log.d(TAG, formatSMS(msg));
    }

    public static final void i(String msg) {
        if (!DEBUGGABLE)
            return;

        Log.i(TAG, formatSMS(msg));
    }

    public static final void e(String msg) {
        if (!DEBUGGABLE)
            return;

        Log.e(TAG, formatSMS(msg));
    }

    private static String wrapSMS(String msg) {
        for (String temp : FORMAT_STRING)
            msg = msg.replaceAll(temp, "%s");
        return msg;
    }

    private static String formatSMS(String sms) {
        String className = null;
        int lineNumber = 0;
//        int index = 0;
//        try {
//            className = Thread.currentThread().getStackTrace()[4].getFileName();
//        } catch (Exception e) {
//        }
//        try {
//            lineNumber = Thread.currentThread().getStackTrace()[4].getLineNumber();
//        } catch (Exception e) {
//
//        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        try {
            for (int i = 0; i < stackTrace.length; i++) {
                if (TextUtils.equals(Debug.class.getName(), stackTrace[i].getClassName())) {
                    className = stackTrace[i + 1].getFileName();
                    lineNumber = stackTrace[i + 1].getLineNumber();
                }
            }
        } catch (Exception e) {
        }

        return sms + " " + className + ":" + lineNumber;
    }
}
