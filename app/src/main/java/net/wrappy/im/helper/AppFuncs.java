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
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import com.google.gson.reflect.TypeToken;
import com.yalantis.ucrop.UCrop;

import net.wrappy.im.BuildConfig;
import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.model.PromotionSetting;
import net.wrappy.im.model.Registration;
import net.wrappy.im.model.T;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.provider.Store;
import net.wrappy.im.ui.BaseActivity;
import net.wrappy.im.ui.ConversationDetailActivity;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.PopupUtils;
import net.wrappy.im.util.SecureMediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by ben on 14/11/2017.
 */

public class AppFuncs {

    public static DisplayMetrics displayMetrics = new DisplayMetrics();

    public static int TIMECOUNTDOWN = 49000;

    private static AppFuncs _ins;

    public static AppFuncs getInstance() {
        if (_ins == null) {
            _ins = new AppFuncs();
        }
        return _ins;
    }

    private static ProgressDialog dialog;

    public static void showProgressWaiting(Activity activity) {
        dialog = new ProgressDialog(activity);
        dialog.setCancelable(false);
        dialog.setMessage(activity.getString(R.string.waiting_dialog));
        if (!activity.isFinishing()) {
            dialog.show();
        }
    }

    public static void dismissProgressWaiting() {
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
        if (BuildConfig.DEBUG) {
            Log.d("wrappy_log", string);
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

    private static String mCurrentPhotoPath;

    private static File createImageFile(Activity activity) throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_" + UUID.randomUUID().toString();
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static void openCamera(Activity activity, boolean isAvatar) {
        if ((ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile(activity);
                } catch (IOException ex) {

                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(activity,
                            activity.getPackageName()+".provider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    if (isAvatar) {
                        activity.startActivityForResult(takePictureIntent, BaseActivity.RESULT_AVATAR);
                    } else {
                        activity.startActivityForResult(takePictureIntent, BaseActivity.RESULT_BANNER);
                    }

                }
            }
        } else {
            if (isAvatar) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                        BaseActivity.REQUEST_PERMISSION_CAMERA_AVATAR);
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                        BaseActivity.REQUEST_PERMISSION_CAMERA_BANNER);
            }

        }
    }

    public static void openGallery(Activity activity, boolean isAvatar) {
        if ((ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            intent.setType("image/*");
            if (isAvatar) {
                activity.startActivityForResult(intent, BaseActivity.RESULT_AVATAR);
            } else {
                activity.startActivityForResult(intent, BaseActivity.RESULT_BANNER);
            }
        } else {
            if (isAvatar) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        BaseActivity.REQUEST_PERMISSION_PICKER_AVATAR);
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        BaseActivity.REQUEST_PERMISSION_PICKER_BANNER);
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void openPickFolder(Activity activity, int requestCode) {
        openPickFolder(activity, requestCode, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
        cropImage(activity, intent, isAvarta, UCrop.REQUEST_CROP);
    }

    public static void cropImage(Activity activity, Intent intent, boolean isAvarta, int requestCode) {
        Uri source = null;
        if (intent==null) {
            File path = new File(mCurrentPhotoPath);
            source = Uri.fromFile(path);
        } else {
            if (intent.getData() != null) {
                source = intent.getData();
            } else {
                File path = new File(mCurrentPhotoPath);
                source = Uri.fromFile(path);
            }
        }
        if (source == null) {
            return;
        }
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
                if (imm.isAcceptingText())
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
        String pattern = ImApp.sImApp.getString(R.string.local_time_format);
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
        RestAPI.GetDataWrappy(ImApp.sImApp, RestAPI.GET_MEMBER_INFO, new RestAPIListener() {
            @Override
            public void OnComplete(String s) {
                Debug.d(s);
                try {
                    Registration registration = new Gson().fromJson(s, Registration.class);
                    if (registration!=null && registration.getWpKMemberDto()!=null) {
                        Imps.Account.updateAccountFromDataServer(ImApp.sImApp.getContentResolver(), registration.getWpKMemberDto(), accountId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void sendRequestInviteFriend(final Activity activity) {
        String countryCode = activity.getResources().getConfiguration().locale.toString().toLowerCase();
        RestAPI.GetDataWrappy(activity, RestAPI.getContentPromotionUrl(countryCode), new RestAPIListener(activity) {
            @Override
            public void OnComplete(String s) {
                try {
                    Gson gson = new Gson();
                    PromotionSetting promotionSetting = gson.fromJson(s, new TypeToken<PromotionSetting>() {
                    }.getType());
                    AppFuncs.shareApp(activity, promotionSetting.getContent());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void plusNumberMsgOnBadger() {
        Context context = ImApp.sImApp.getApplicationContext();
        int oldCount = Store.getIntData(context,Store.NUM_UNREAD_MESSAGE);
        int newCount = oldCount+1;
        Store.putIntData(context,Store.NUM_UNREAD_MESSAGE,newCount);
        ShortcutBadger.applyCount(context, newCount);
    }

    public static void minusNumberMsgOnBadger() {
        Context context = ImApp.sImApp.getApplicationContext();
        int oldCount = Store.getIntData(context,Store.NUM_UNREAD_MESSAGE);
        if (oldCount > 0) {
            int newCount = oldCount-1;
            Store.putIntData(context,Store.NUM_UNREAD_MESSAGE,newCount);
            ShortcutBadger.applyCount(context, newCount);
        }
    }

}
