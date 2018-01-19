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
import android.net.Uri;
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
import com.yalantis.ucrop.UCrop;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.model.Registration;
import net.wrappy.im.model.T;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.ui.ConversationDetailActivity;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.PopupUtils;
import net.wrappy.im.util.SecureMediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ben on 14/11/2017.
 */

public class AppFuncs {

    public static DisplayMetrics displayMetrics = new DisplayMetrics();

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
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(activity.getString(R.string.waiting_dialog));
        if (!activity.isFinishing()) {
            dialog.show();
        }
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

    public static void alert(Context context, CharSequence s, boolean isLong) {
        Toast.makeText(context, s, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public static void alert(Context context, int s, boolean isLong) {
        Toast.makeText(context, s, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public static void alert(Context context, String s, boolean isLong) {
        Toast.makeText(context, s, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public static void log(String string) {
        try {
            Log.i("wrappy_log", string);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        final CharSequence[] options = {activity.getString(R.string.popup_take_photo), activity.getString(R.string.choose_photos), activity.getString(R.string.cancel)};

        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            PopupUtils.getSelectionDialog(activity, activity.getString(R.string.add_photo), options, new DialogInterface.OnClickListener() {

                @Override

                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0:
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            activity.startActivityForResult(cameraIntent, requestCode);
                            break;
                        case 1:
                            Intent intent = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            activity.startActivityForResult(intent, requestCode);
                            break;
                        default:
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

    public static void openCamera(Activity activity, int requestCode) {
        if ((ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            activity.startActivityForResult(cameraIntent, requestCode);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                    199);
        }
    }

    public static void openGallery(Activity activity, int requestCode) {
        if ((ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            intent.setType("image/*");
            activity.startActivityForResult(intent, requestCode);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    199);
        }
    }

    public static void openPickFolder(Activity activity, int requestCode) {
        openPickFolder(activity, requestCode, null);
    }

    public static void openPickFolder(Activity activity, int requestCode, Uri uri) {
        if ((ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            activity.startActivityForResult(intent, requestCode);
            if (uri != null && activity instanceof ConversationDetailActivity)
                ((ConversationDetailActivity) activity).setSelectedUri(uri);

        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    199);
        }
    }

    public static void cropImage(Activity activity, Intent intent, boolean isAvarta) {
        Uri source = null;
        if (intent.getData() != null) {
            source = intent.getData();
        } else {
            if (intent.getExtras() != null) {
                Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
                source = getImageUri(activity, bitmap);
            }
        }
        if (source == null) {
            return;
        }
        Uri destination = Uri.fromFile(new File(activity.getCacheDir(), UUID.randomUUID().toString()));

        if (isAvarta) {
            UCrop.of(source, destination)
                    .withAspectRatio(1, 1)
                    .start(activity);
        } else {
            UCrop.of(source, destination)
                    .withAspectRatio(16, 9)
                    .start(activity);
        }

    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, UUID.randomUUID().toString(), null);
        return Uri.parse(path);
    }

    public static void cropImage(Activity activity, Uri source, int requestCode, boolean isAvarta) {
        Uri destination = Uri.fromFile(new File(activity.getCacheDir(), UUID.randomUUID().toString()));
        if (isAvarta) {
            UCrop.of(source, destination)
                    .withAspectRatio(1, 1)
                    .start(activity, requestCode);
        } else {
            UCrop.of(source, destination)
                    .withAspectRatio(16, 9)
                    .start(activity, requestCode);
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
            String fileName = UUID.randomUUID().toString();
            f = new File(context.getCacheDir(), fileName);
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

    public static float getPixelsInCM(float cm, boolean isX) {
        return (cm / 2.54f) * (isX ? displayMetrics.xdpi : displayMetrics.ydpi);
    }

    public static String convertTimestamp(long timestamp) {
        String pattern = "MMM dd, yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(timestamp);
        return date;
    }

    public static void shareApp(Activity activity, String content) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.app_name));
            i.putExtra(Intent.EXTRA_TEXT, content);
            activity.startActivity(Intent.createChooser(i, activity.getString(R.string.share_title)));
        } catch (Exception e) {
            //e.toString();
        }
    }

    public static void getSyncUserInfo(final long accountId) {
        RestAPI.GetDataWrappy(ImApp.sImApp, RestAPI.GET_MEMBER_INFO, new RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                Debug.d(s);
                try {
                    Registration registration = new Gson().fromJson(s, Registration.class);
                    Imps.Account.updateAccountFromDataServer(ImApp.sImApp.getContentResolver(), registration, accountId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
