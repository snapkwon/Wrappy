package net.wrappy.im.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Created by ben on 14/11/2017.
 */

public class AppFuncs {

    public static void alert(Context context,String s,boolean isLong) {
        Toast.makeText(context,s,isLong? Toast.LENGTH_LONG: Toast.LENGTH_SHORT).show();
    }

    public static void log(String string) {
        Log.i("AppFuncs",string);
    }

}
