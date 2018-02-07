package net.wrappy.im.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ben on 30/11/2017.
 */

public class Store {

    public final static String USERNAME = "wr_username";
    public final static String REFERRAL = "wr_referral";
    public final static String NUM_UNREAD_MESSAGE = "wr_number_unread_message";

    public static void putStringData(Context context, String key, String data) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, data); // value to store
        editor.commit();
    }

    public static String getStringData(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, "");
    }

    public static void putIntData(Context context, String key, int data) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, data); // value to store
        editor.commit();
    }

    public static int getIntData(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, 0);
    }

    public static void putLongData(Context context, String key, long data) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, data); // value to store
        editor.commit();
    }

    public static long getLongData(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(key,0);
    }

    public static void putBooleanData(Context context, String key, boolean data) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, data); // value to store
        editor.commit();
    }

    public static boolean getBooleanData(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.contains(key)) {
            return preferences.getBoolean(key, false);
        }
        return false;
    }

    public static void clear(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().clear().apply();
    }

}
