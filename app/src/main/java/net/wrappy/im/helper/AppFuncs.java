package net.wrappy.im.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.wrappy.im.model.T;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ben on 14/11/2017.
 */

public class AppFuncs {

    private static AppFuncs _ins;

    public static AppFuncs getInstance() {
        if (_ins==null) {
            _ins = new AppFuncs();
        }
        return _ins;
    }

    ProgressDialog dialog;
    public void showProgressWaiting(Activity activity) {
        dialog = new ProgressDialog(activity);
        dialog.setTitle("Waiting...");
        dialog.show();
    }

    public void dismissProgressWaiting() {
        if (dialog!=null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

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

    public static void getImageFromDevice(final Activity activity, final int requestCode) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };



        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Add Photo!");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo"))

                {

                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    //pic = f;

                    activity.startActivityForResult(cameraIntent, requestCode);


                }

                else if (options[item].equals("Choose from Gallery"))

                {

                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    activity.startActivityForResult(intent, requestCode);



                }

                else if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }

    public static String bitmapToBase64String(Bitmap bitmap) {
        String base64String = "";
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return base64String;
    }

    public static File ConvertBitmapToFile(Context context, Bitmap bitmap) {
        File f = null;
        try {
            f = new File(context.getCacheDir(), "file");
            f.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return f;
    }

    public static JsonObject convertClassToJsonObject(T aClass) {
        try {
            Gson gson = new Gson();
            String jsonObject = gson.toJson(aClass);
            JsonObject object = (new JsonParser()).parse(jsonObject).getAsJsonObject();
            return object;
        }catch (Exception ex) {
            ex.printStackTrace();
            return new JsonObject();
        }
    }

}
