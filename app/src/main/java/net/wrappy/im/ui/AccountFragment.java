package net.wrappy.im.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImageView;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.ui.legacy.DatabaseUtils;
import net.wrappy.im.ui.legacy.SignInHelper;
import net.wrappy.im.ui.onboarding.OnboardingManager;
import net.wrappy.im.ui.qr.QrDisplayActivity;
import net.wrappy.im.ui.qr.QrShareAsyncTask;
import net.wrappy.im.util.PopupUtils;
import net.wrappy.im.util.SecureMediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class AccountFragment extends Fragment {

    ImageView mIvAvatar;
    CropImageView mCropImageView;
    TextView mTvPassword, mTvNickname;
    ImApp mApp;
    Handler mHandler = new Handler();
    ImageView ivScan;
    View mView;

    long mProviderId;
    long mAccountId;
    String mUserAddress;
    String mNickname;
    String mUserKey;
    String mAccountName;

    private final static String DEFAULT_PASSWORD_TEXT = "*************";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GalleryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mApp = ((ImApp) getActivity().getApplication());
        mProviderId = mApp.getDefaultProviderId();
        mAccountId = mApp.getDefaultAccountId();
        mUserAddress = mApp.getDefaultUsername();
        mUserKey = mApp.getDefaultOtrKey();
        mNickname = Imps.Account.getNickname(getContext().getContentResolver(), mAccountId);
        mAccountName = Imps.Account.getAccountName(getContext().getContentResolver(), mAccountId);
        String email = Imps.Account.getString(getContext().getContentResolver(), Imps.Account.ACCOUNT_EMAIL, mAccountId);

        mView = inflater.inflate(R.layout.awesome_fragment_account, container, false);

        if (!TextUtils.isEmpty(mUserAddress)) {

            mUserAddress = mUserAddress.trim(); //make sure any whitespace is removed

            mTvNickname = (TextView) mView.findViewById(R.id.tvNickname);

            TextView tvUsername = (TextView) mView.findViewById(R.id.edtName);
            mTvPassword = (TextView) mView.findViewById(R.id.edtPass);
            mTvPassword.setText(DEFAULT_PASSWORD_TEXT);

            View btnShowPassword = mView.findViewById(R.id.btnShowPass);
            btnShowPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mTvPassword.getText().toString().equals(DEFAULT_PASSWORD_TEXT))
                        mTvPassword.setText(getAccountPassword(mProviderId));
                    else
                        mTvPassword.setText(DEFAULT_PASSWORD_TEXT);
                }
            });

            View btnEditAccountNickname = mView.findViewById(R.id.edit_account_nickname);
            btnEditAccountNickname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showChangeNickname();
                }
            });

            View btnEditAccountPassword = mView.findViewById(R.id.edit_account_password);
            btnEditAccountPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showChangePassword();
                }
            });


            mIvAvatar = (ImageView) mView.findViewById(R.id.imageAvatar);
            mIvAvatar.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    startAvatarTaker();

                }
            });

            try {

                Drawable avatar = DatabaseUtils.getAvatarFromAddress(mApp.getContentResolver(), mUserAddress, ImApp.DEFAULT_AVATAR_WIDTH, ImApp.DEFAULT_AVATAR_HEIGHT, false);

                if (avatar != null)
                    mIvAvatar.setImageDrawable(avatar);
            } catch (Exception e) {
                Log.w(ImApp.LOG_TAG, "error getting avatar", e);
            }
            if (!TextUtils.isEmpty(email)) {
                tvUsername.setVisibility(View.VISIBLE);
                tvUsername.setText(email);
            }
            mTvNickname.setText(mAccountName);

            IImConnection conn = mApp.getConnection(mProviderId, mAccountId);

            try {
                final List<String> remoteOmemoFingerprints = conn.getFingerprints(mUserAddress);

                if (remoteOmemoFingerprints != null && remoteOmemoFingerprints.size() > 0) {

                    ImageView btnQrDisplay = (ImageView) mView.findViewById(R.id.omemoqrcode);
                    btnQrDisplay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                String xmppLink = OnboardingManager.generateXmppLink(mUserAddress, remoteOmemoFingerprints.get(0));
                                Intent intent = new Intent(getActivity(), QrDisplayActivity.class);
                                intent.putExtra(Intent.EXTRA_TEXT, xmppLink);
                                getActivity().startActivity(intent);
                            } catch (IOException ioe) {
                                Log.e(ImApp.LOG_TAG, "couldn't generate QR code", ioe);
                            }
                        }
                    });
                    TextView tvFingerprint = (TextView) mView.findViewById(R.id.omemoFingerprint);
                    tvFingerprint.setText(prettyPrintFingerprint(remoteOmemoFingerprints.get(0)));

                    ImageView btnQrShare = (ImageView) mView.findViewById(R.id.omemoqrshare);
                    btnQrShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            try {
                                String inviteLink = OnboardingManager.generateInviteLink(getActivity(), mUserAddress, remoteOmemoFingerprints.get(0), mNickname);
                                new QrShareAsyncTask(getActivity()).execute(inviteLink, mNickname);
                            } catch (IOException ioe) {
                                Log.e(ImApp.LOG_TAG, "couldn't generate QR code", ioe);
                            }
                        }
                    });
                } else {
                    mView.findViewById(R.id.omemodisplay).setVisibility(View.GONE);
                }
            } catch (RemoteException re) {

            }


        }

        return mView;
    }

    private void showChangeNickname() {
        PopupUtils.showCustomEditDialog(getContext(), "", mAccountName, R.string.yes, R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newNickname = String.valueOf(v.getTag());

                //update just the nickname
                ImApp.insertOrUpdateAccount(getContext().getContentResolver(), mProviderId, mAccountId, mNickname, "", null, newNickname);

                mTvNickname.setText(newNickname);
            }
        }, null);
    }

    private void showChangePassword() {
        PopupUtils.showCustomEditDialog(getContext(), getContext().getString(R.string.lock_screen_create_passphrase),
                "", getAccountPassword(mProviderId), R.string.yes, R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newPassword = String.valueOf(v.getTag());

                        if (!TextUtils.isEmpty(newPassword)) {
                            new ChangePasswordTask().execute(getAccountPassword(mProviderId), newPassword);
                        }
                    }
                }, null, false);
    }

    private String getAccountPassword(long providerId) {

        String result = "";

        Cursor c = getActivity().getContentResolver().query(Imps.Provider.CONTENT_URI_WITH_ACCOUNT,
                new String[]{Imps.Provider.ACTIVE_ACCOUNT_PW}, Imps.Provider.CATEGORY + "=? AND providers." + Imps.Provider._ID + "=?" /* selection */,
                new String[]{ImApp.IMPS_CATEGORY, providerId + ""} /* selection args */,
                Imps.Provider.DEFAULT_SORT_ORDER);

        if (c != null) {
            c.moveToFirst();
            result = c.getString(0);
            c.close();
        }

        return result;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 200) {

            Uri imageUri = getPickImageResultUri(data);

            if (imageUri == null)
                return;

            mCropImageView = new CropImageView(getActivity());// (CropImageView)view.findViewById(R.id.CropImageView);
            mCropImageView.setAspectRatio(1, 1);
            mCropImageView.setFixedAspectRatio(true);
            mCropImageView.setCropShape(CropImageView.CropShape.OVAL);

            //  mCropImageView.setGuidelines(1);

            try {
                Bitmap bmpThumbnail = SecureMediaStore.getThumbnailFile(getActivity(), imageUri, 512);
                mCropImageView.setImageBitmap(bmpThumbnail);
//                PopupUtils.showCustomViewDialog(getActivity(), mCropImageView, R.string.yes, R.string.cancel, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        setAvatar(mCropImageView.getCroppedImage());
//                    }
//                }, null);
            } catch (IOException ioe) {
                Log.e(ImApp.LOG_TAG, "couldn't load avatar", ioe);
            }

        }


    }


    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    /**
     * Create a chooser intent to select the source to get image from.<br/>
     * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the intent chooser.
     */
    public Intent getPickImageChooserIntent() {

        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getActivity().getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, getString(R.string.choose_photos));

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    /**
     * Get URI to image received from capture by camera.
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getActivity().getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpg"));
        }
        return outputFileUri;
    }


    /**
     * Get the URI of the selected image from {@link #getPickImageChooserIntent()}.<br/>
     * Will return the correct URI for camera and gallery image.
     *
     * @param data the returned data of the activity result
     */
    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {

            if (data.getData() == null)
                return getCaptureImageOutputUri();
            else {
                String action = data.getAction();
                isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                return isCamera ? getCaptureImageOutputUri() : data.getData();
            }

        } else
            return getCaptureImageOutputUri();
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private String prettyPrintFingerprint(String fingerprint) {
        StringBuffer spacedFingerprint = new StringBuffer();

        for (int i = 0; i + 8 <= fingerprint.length(); i += 8) {
            spacedFingerprint.append(fingerprint.subSequence(i, i + 8));
            spacedFingerprint.append(' ');
        }

        return spacedFingerprint.toString();
    }

    void signIn() {
        // The toggle is enabled
        SignInHelper helper = new SignInHelper(getActivity(), mHandler, new SignInHelper.SignInListener() {
            @Override
            public void connectedToService() {

            }

            @Override
            public void stateChanged(int state, long accountId) {

            }
        });

        helper.signIn(getAccountPassword(mProviderId), mProviderId, mAccountId, true);

        //keep signed in please!
        ContentValues values = new ContentValues();
        values.put(Imps.AccountColumns.KEEP_SIGNED_IN, 1);
        getActivity().getContentResolver().update(ContentUris.withAppendedId(Imps.Account.CONTENT_URI, mAccountId), values, null, null);
    }

    void signOut() {
        //if you are signing out, then we will deactive "auto" sign in
        ContentValues values = new ContentValues();
        values.put(Imps.AccountColumns.KEEP_SIGNED_IN, 0);
        getActivity().getContentResolver().update(ContentUris.withAppendedId(Imps.Account.CONTENT_URI, mAccountId), values, null, null);
        signOut(mProviderId, mAccountId);
        ;
    }

    void signOut(long providerId, long accountId) {

        try {

            IImConnection conn = mApp.getConnection(mProviderId, mAccountId);
            if (conn != null) {
                conn.logout();
            } else {
                // Normally, we can always get the connection when user chose to
                // sign out. However, if the application crash unexpectedly, the
                // status will never be updated. Clear the status in this case
                // to make it recoverable from the crash.
                ContentValues values = new ContentValues(2);
                values.put(Imps.AccountStatusColumns.PRESENCE_STATUS, Imps.CommonPresenceColumns.OFFLINE);
                values.put(Imps.AccountStatusColumns.CONNECTION_STATUS, Imps.ConnectionStatus.OFFLINE);
                String where = Imps.AccountStatusColumns.ACCOUNT + "=?";
                getActivity().getContentResolver().update(Imps.AccountStatus.CONTENT_URI, values, where,
                        new String[]{Long.toString(accountId)});
            }
        } catch (RemoteException ex) {
            Log.e(ImApp.LOG_TAG, "signout: caught ", ex);
        } finally {

        }

    }

    private final static int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    void startAvatarTaker() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(mView, R.string.grant_perms_camera, Snackbar.LENGTH_LONG).show();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {

            startActivityForResult(getPickImageChooserIntent(), 200);
        }
    }

    private class ChangePasswordTask extends AsyncTask<String, Void, Boolean> {

        String newPassword = null;

        @Override
        protected Boolean doInBackground(String... setupValues) {
            try {

                String oldPassword = setupValues[0];
                newPassword = setupValues[1];

                if (!oldPassword.equals(newPassword)) {
                    boolean result = OnboardingManager.changePassword(getActivity(), mProviderId, mAccountId, oldPassword, newPassword);
                    return result;
                } else
                    return false;
            } catch (Exception e) {
                Log.e(ImApp.LOG_TAG, "auto onboarding fail", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean passwordChanged) {

            if (passwordChanged) {
                //update just the nickname
                ImApp.insertOrUpdateAccount(getContext().getContentResolver(), mProviderId, mAccountId, "", "", newPassword);
                mTvPassword.setText(DEFAULT_PASSWORD_TEXT);
            }

        }
    }

}
