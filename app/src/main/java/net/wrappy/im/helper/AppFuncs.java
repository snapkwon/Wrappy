package net.wrappy.im.helper;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.wrappy.im.model.T;
import net.wrappy.im.util.PopupUtils;
import net.wrappy.im.util.SecureMediaStore;

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
        if (_ins == null) {
            _ins = new AppFuncs();
        }
        return _ins;
    }

    ProgressDialog dialog;

    public void showProgressWaiting(Activity activity) {
        dialog = new ProgressDialog(activity);
        dialog.setMessage("Waiting...");
        dialog.show();
    }

    public void dismissProgressWaiting() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static void alert(Context context, String s, boolean isLong) {
        Toast.makeText(context, s, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public static void log(String string) {
        Log.i("AppFuncs", string);
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
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            PopupUtils.getSelectionDialog(activity, "Add Photo!", options, new DialogInterface.OnClickListener() {

                @Override

                public void onClick(DialogInterface dialog, int item) {

                    if (options[item].equals("Take Photo"))

                    {

                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        //pic = f;

                        activity.startActivityForResult(cameraIntent, requestCode);


                    } else if (options[item].equals("Choose from Gallery"))

                    {

                        Intent intent = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        activity.startActivityForResult(intent, requestCode);


                    } else if (options[item].equals("Cancel")) {

                        dialog.dismiss();

                    }

                }

            });
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    199);
        }


    }

    public static String bitmapToBase64String(Bitmap bitmap) {
        String base64String = "";
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return base64String;
    }

    public static File getFileFromBitmap(Context context) {
        File f;
        try {
            f = new File(context.getCacheDir(), "file");
            if (f.exists())
                return f;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static File convertBitmapToFile(Context context, Bitmap bitmap) {
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
            ex.printStackTrace();
            return new JsonObject();
        }
    }

    public static void dismissKeyboard(Activity activity) {
        try {
            View view = activity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static Bitmap getBitmapFromIntentResult(Context context, Intent data) {
        Bitmap photo = null;
        try {
            if (data.getData() != null) {
                photo = SecureMediaStore.getThumbnailFile(context, data.getData(), 512);
            } else {
                photo = (Bitmap) data.getExtras().get("data");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return photo;
    }

    public static JsonElement convertToJson(String s) throws Exception {
        JsonElement jsonElement = (new JsonParser()).parse(s);
        return jsonElement;
    }

    public static JsonElement convertToJson(Object ob) throws Exception {
        Gson gson = new Gson();
        return gson.toJsonTree(ob);
    }

}
