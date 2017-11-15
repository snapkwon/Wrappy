package net.wrappy.im.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static boolean detectSpecialCharacters(String s) {
        Pattern p = Pattern.compile("[^A-Za-z0-9]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(s);
        return m.find();
    }

    public static boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static void getImageFromDevice(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

}
