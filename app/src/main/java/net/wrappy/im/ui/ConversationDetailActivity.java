/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wrappy.im.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.wrappy.im.BuildConfig;
import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.ErrorCode;
import net.wrappy.im.helper.FileUtil;
import net.wrappy.im.helper.NotificationCenter;
import net.wrappy.im.helper.NotificationCenter.NotificationCenterDelegate;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.helper.layout.LayoutHelper;
import net.wrappy.im.model.Presence;
import net.wrappy.im.model.WpKChatGroupDto;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.tasks.AddContactAsyncTask;
import net.wrappy.im.ui.conference.ConferenceConstant;
import net.wrappy.im.util.BundleKeyConstant;
import net.wrappy.im.util.ConferenceUtils;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.PopupUtils;
import net.wrappy.im.util.PreferenceUtils;
import net.wrappy.im.util.SecureMediaStore;
import net.wrappy.im.util.SystemServices;
import net.wrappy.im.util.Utils;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import butterknife.OnClick;
import eu.siacs.conversations.Downloader;
import info.guardianproject.iocipher.FileInputStream;

import static net.wrappy.im.helper.RestAPI.getAvatarUrl;

//import com.bumptech.glide.Glide;

public class ConversationDetailActivity extends BaseActivity implements OnHandleMessage, NotificationCenterDelegate {

    private AddContactAsyncTask task;

    // private WpKChatGroupDto chatGroupDto;

    boolean isRegisterNotificationCenter;

    public static Intent getStartIntent(Context context, long chatId, String nickname, String reference) {
        Intent intent = getStartIntent(context, chatId);
        intent.putExtra(BundleKeyConstant.CONTACT_ID_KEY, chatId);
        intent.putExtra(BundleKeyConstant.NICK_NAME_KEY, nickname);
        intent.putExtra(BundleKeyConstant.REFERENCE_KEY, reference);
        intent.putExtra(BundleKeyConstant.ADDRESS_KEY, nickname + Constant.EMAIL_DOMAIN);
        return intent;
    }

    public static Intent getStartIntent(Context context, long chatId, String nickname, String reference, String maddress) {
        Intent intent = getStartIntent(context, chatId);
        intent.putExtra(BundleKeyConstant.CONTACT_ID_KEY, chatId);
        intent.putExtra(BundleKeyConstant.NICK_NAME_KEY, nickname);
        intent.putExtra(BundleKeyConstant.REFERENCE_KEY, reference);
        intent.putExtra(BundleKeyConstant.ADDRESS_KEY, maddress);
        return intent;
    }

    // public WpKChatGroupDto getGroupDto() {
    //     return chatGroupDto;
    //}


    public static Intent getStartIntent(Context context, long chatId) {
        Intent intent = getStartIntent(context);
        intent.putExtra(BundleKeyConstant.CONTACT_ID_KEY, chatId);
        return intent;
    }

    public static Intent getStartIntent(Context context) {
        return new Intent(context, ConversationDetailActivity.class);
    }

    private long mChatId = -1;
    private String mAddress = null;
    private String mNickname = null;
    private String mReference = null;
    private Uri mSelectedUri;// selected file to download

    public void setSelectedUri(Uri mSelectedUri) {
        this.mSelectedUri = mSelectedUri;
    }

    private Menu menuitem;
    private ConversationView mConvoView = null;

    MediaRecorder mMediaRecorder = null;
    File mAudioFilePath = null;

    private ImApp mApp;

    //private AppBarLayout appBarLayout;
//    private Toolbar mToolbar;

    FrameLayout popupWindow;

    private PrettyTime mPrettyTime;

    private MyHandler mHandler;

    private TextView txtStatus;
    private TextView txtTitleActionBar;

    WpKChatGroupDto wpKChatGroupDto;

    @Override
    public void onHandle(Message msg) {
        handleMessage();
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.addSearchBarInDetailConverasation) {
            addSearchViewInActionBar();
        }
        if (id == NotificationCenter.updateConversationDetail) {
            JsonObject jsonObject = (JsonObject) args[0];
            wpKChatGroupDto = new Gson().fromJson(jsonObject, new TypeToken<WpKChatGroupDto>() {
            }.getType());
            AppFuncs.log(jsonObject.toString());
            setCustomActionBar(mConvoView.isGroupChat());
        }
    }

    @Override
    public void onBackPressed() {
        if (mConvoView.getSearchMode()) {
            mConvoView.unActiveSearchmode();
            searchView.setVisibility(View.GONE);
            addIconActionBar(R.drawable.ic_tran);
            addIconActionBar(R.drawable.ic_info_outline_white_24dp);
            setCustomActionBar(mConvoView.isGroupChat());
        } else {
            finish();
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<Activity> weakReference;
        private OnHandleMessage onHandleMessage;

        MyHandler(Activity activity) {
            weakReference = new WeakReference<>(activity);
        }

        void setOnHandleMessage(OnHandleMessage onHandleMessage) {
            this.onHandleMessage = onHandleMessage;
        }

        @Override
        public void handleMessage(Message msg) {
            if (weakReference != null && weakReference.get() != null) {
                if (msg.what == 1 && onHandleMessage != null) {
                    onHandleMessage.onHandle(msg);
                }
            }
        }
    }

    private void handleMessage() {
        /*if (mConvoView.getLastSeen() != null) {
            getSupportActionBar().setSubtitle(mPrettyTime.format(mConvoView.getLastSeen()));
        } else {
            if (mConvoView.getRemotePresence() == Presence.AWAY)
                getSupportActionBar().setSubtitle(getString(R.string.presence_away));
            else if (mConvoView.getRemotePresence() == Presence.OFFLINE)
                getSupportActionBar().setSubtitle(getString(R.string.presence_offline));
            else if (mConvoView.getRemotePresence() == Presence.DO_NOT_DISTURB)
                getSupportActionBar().setSubtitle(getString(R.string.presence_busy));

        }*/
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(final Context context, final Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                mConvoView.setSelected(false);
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                mConvoView.setSelected(true);
            } else if (ConferenceConstant.SEND_BACKGROUND_CHAT_PREFIX.equals(intent.getAction())) {
                loadBitmapPreferences();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.awesome_activity_detail);
        super.onCreate(savedInstanceState);
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        initActionBarDefault(true, 0);
        if (Constant.CONFERENCE_ENABLED) {
            addIconActionBar(R.drawable.ic_camera);
            addIconActionBar(R.drawable.ic_phone);
        }
        addIconActionBar(R.drawable.ic_tran);
        addIconActionBar(R.drawable.ic_info_outline_white_24dp);
        mApp = (ImApp) getApplication();

        mConvoView = new ConversationView(this);
        mHandler = new MyHandler(this);
        mHandler.setOnHandleMessage(this);
//        mToolbar = (Toolbar) findViewById(R.id.toolbar);

//        mToolbar.setPadding(0, 0, 0, 0);//for tab otherwise give space in tab
//        mToolbar.setContentInsetsAbsolute(0, 0);
        //  appBarLayout = (AppBarLayout)findViewById(R.id.appbar);


        popupWindow = mConvoView.popupDisplay(ConversationDetailActivity.this);

        popupWindow.setVisibility(View.GONE);

        popupWindow.setBackgroundColor(0xfff);

        txtStatus = (TextView) findViewById(R.id.txtStatus);
        FrameLayout main = (FrameLayout) findViewById(R.id.container);
        main.addView(popupWindow, new FrameLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.TOP));

        mPrettyTime = new PrettyTime(getCurrentLocale());


        mConvoView.getHistoryView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (popupWindow.getVisibility() == View.VISIBLE) {
                    popupWindow.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (popupWindow.getVisibility() == View.VISIBLE) {
                    popupWindow.setVisibility(View.GONE);
                }
            }
        });

        setCustomActionBar(mConvoView.isGroupChat());
        processIntent(getIntent());

        // set background for this screen
        loadBitmapPreferences();
    }

    @Override
    public void onClickActionBar(int resId) {
        super.onClickActionBar(resId);
        switch (resId) {
            case R.drawable.ic_tran:
                if (popupWindow.getVisibility() == View.GONE) {
                    popupWindow.setVisibility(View.VISIBLE);
                } else {
                    popupWindow.setVisibility(View.GONE);
                }
                break;
            case R.drawable.ic_phone:
                mConvoView.startAudioConference();
                break;
            case R.drawable.ic_camera:
                PopupUtils.showCustomDialog(this, getString(R.string.comming_soon), getString(R.string.comming_soon), R.string.ok, null);
                break;
            case R.drawable.ic_info_outline_white_24dp:
                mConvoView.startSettingScreen();
                break;
        }
    }

    private void setCustomActionBar(boolean isGroupChat) {
        clearViewLeftInActionBar();
        View view = LayoutInflater.from(this).inflate(R.layout.actionbar_chat_room, null);

        ImageView avatar = (ImageView) view.findViewById(R.id.chat_room_avatar);
        ImageView status = (ImageView) view.findViewById(R.id.chat_room_status);
        txtTitleActionBar = (TextView) view.findViewById(R.id.chat_room_nickname);

        String avarImg = Imps.Avatars.getAvatar(getContentResolver(), getIntent().getStringExtra(BundleKeyConstant.ADDRESS_KEY));

        if (!TextUtils.isEmpty(avarImg)) {
            mReference = avarImg;
        }
        if (isGroupChat) {
            if (TextUtils.isEmpty(mReference)) {
                avatar.setImageResource(R.drawable.chat_group);
            } else {
                GlideHelper.loadBitmapToCircleImage(this, avatar, getAvatarUrl(mReference));
            }
            status.setVisibility(View.GONE);
            if (wpKChatGroupDto != null) {
                txtTitleActionBar.setText(wpKChatGroupDto.getName());
            } else {
                txtTitleActionBar.setText(mNickname);
            }
        } else {
            if (TextUtils.isEmpty(mReference)) {
                GlideHelper.loadAvatarFromNickname(this, avatar, mNickname);
            } else {
                GlideHelper.loadBitmapToCircleImageDefault(this, avatar, getAvatarUrl(mReference), mNickname);
            }
            setAvatarStatus(status);
            String fName = Imps.Contacts.getNicknameFromAddress(getContentResolver(), mAddress);
            if (!Utils.setTextForView(txtTitleActionBar,fName))
                Utils.setTextForView(txtTitleActionBar,mNickname);
        }

        addCustomViewToActionBar(view);
    }

    private void setAvatarStatus(ImageView imageView) {
        if (mConvoView.getLastSeen() != null) {
            imageView.setImageResource(R.drawable.status_active);
        } else {
            if (mConvoView.getRemotePresence() == Presence.AWAY)
                imageView.setImageResource(R.drawable.status_aw);
            else if (mConvoView.getRemotePresence() == Presence.OFFLINE)
                imageView.setImageResource(R.drawable.status_disable);
        }
    }

    SearchView searchView;

    private void addSearchViewInActionBar() {
        clearViewInActionBar();
        View view = LayoutInflater.from(this).inflate(R.layout.actionbar_chat_room, null);
        view.findViewById(R.id.relAvatar).setVisibility(View.GONE);
        view.findViewById(R.id.chat_room_nickname).setVisibility(View.GONE);
        searchView = (SearchView) view.findViewById(R.id.searchtext);

        searchView.setVisibility(View.VISIBLE);


        int id = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) searchView.findViewById(id);

        searchEditText.setTextColor(getResources().getColor(R.color.message_background_light));
        searchEditText.setHintTextColor(getResources().getColor(R.color.message_background_light));

        mConvoView.activeSearchmode();
        mConvoView.searchText(searchView);

        addCustomViewToActionBar(view);
    }

    public void updateLastSeen(Date lastSeen) {
        mHandler.sendEmptyMessage(1);
    }

    MyLoaderCallbacks loaderCallbacks;

    private void processIntent(Intent intent) {

        mApp = (ImApp) getApplication();

        if (mChatId == -1)
            mChatId = intent.getLongExtra(BundleKeyConstant.CONTACT_ID_KEY, -1);
        mAddress = intent.getStringExtra(BundleKeyConstant.ADDRESS_KEY);
        mNickname = intent.getStringExtra(BundleKeyConstant.NICK_NAME_KEY);
        Utils.setTextForView(txtTitleActionBar,mNickname);
        if (TextUtils.isEmpty(mNickname)) {
            mNickname = mAddress.split("@")[0];
        }
        mReference = intent.getStringExtra(BundleKeyConstant.REFERENCE_KEY);

        if (mChatId == -1) {
            android.app.LoaderManager loaderManager = getLoaderManager();
            loaderCallbacks = new MyLoaderCallbacks();
            loaderManager.initLoader(1, null, loaderCallbacks);
        } else {
//            finish();
            startChatting();
        }

    }

    @OnClick({R.id.btnAddFriend})
    protected void onClickAddFriend(View v) {
        RestAPI.PostDataWrappy(this, null, String.format(RestAPI.POST_ADD_CONTACT, mNickname), new RestAPIListener(this) {
            @Override
            public void OnComplete(String s) {
                addContactXmpp();
            }

            @Override
            protected void onError(int errorCode) {
                if (errorCode == ErrorCode.WP_K_MEMBER_ALREADY_EXISTS.getErrorCode())
                    addContactXmpp();
                else
                    super.onError(errorCode);
            }
        });
    }

    private void addContactXmpp() {
        mConvoView.updateStatusAddContact();
        task = new AddContactAsyncTask(mApp.getDefaultProviderId(), mApp.getDefaultAccountId()).setCallback(new AddContactAsyncTask.AddContactCallback() {
            @Override
            public void onFinished(Integer code) {
                startChat();
            }

            @Override
            public void onError() {

            }

        });
        task.execute(mAddress + Constant.EMAIL_DOMAIN, null, mNickname);
    }

    public void startChat() {
        if (loaderCallbacks != null) {
            getLoaderManager().destroyLoader(1);
            getLoaderManager().initLoader(2, null, loaderCallbacks);
        }
    }

    private void startChatting() {
        mConvoView.bindChat(mChatId, mNickname, mReference);
        mConvoView.startListening();
        setCustomActionBar(mConvoView.isGroupChat());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isRegisterNotificationCenter) {
            isRegisterNotificationCenter = true;
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.changeAvatarGroupFromSetting);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.addSearchBarInDetailConverasation);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateConversationDetail);
        }

        mConvoView.setSelected(true);

        IntentFilter regFilter = new IntentFilter();
        regFilter.addAction(Intent.ACTION_SCREEN_OFF);
        regFilter.addAction(Intent.ACTION_SCREEN_ON);
        regFilter.addAction(ConferenceConstant.SEND_BACKGROUND_CHAT_PREFIX);
        registerReceiver(receiver, regFilter);

        mConvoView.focusSearchmode(searchView);

        /**
         if (mConvoView.getOtrSessionStatus() == SessionStatus.ENCRYPTED
         && (!mConvoView.isOtrSessionVerified())
         )
         {

         Snackbar sb = Snackbar.make(mConvoView.getHistoryView(), R.string.not_verified, Snackbar.LENGTH_INDEFINITE);

         sb.setAction(R.string.ok, new View.OnClickListener() {
        @Override public void onClick(View v) {

        mConvoView.showVerifyDialog();
        }
        });

         sb.show();;

         }**/


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mConvoView.setSelected(false);

        unregisterReceiver(receiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        processIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mConvoView.getSearchMode()) {
                    mConvoView.unActiveSearchmode();
                    for (int i = 0; i < 4; i++) {
                        menuitem.getItem(i).setVisible(true);
                    }
                } else {
                    finish();
                }
                return true;
            /*case R.id.menu_end_conversation:
                mConvoView.closeChatSession(true);
                finish();
                return true;*/
            case R.id.menu_verify_or_view:
            case R.id.menu_group_info:
                mConvoView.startSettingScreen();
                return true;
            case R.id.menu_video_call:
                mConvoView.startVideoConference();
                return true;
            case R.id.menu_voice_call:
                mConvoView.startAudioConference();
                return true;
//            case R.id.menu_settings_language:
            //   final FrameLayout popupWindow = mConvoView.popupDisplay(ConversationDetailActivity.this);
              /*  new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        popupWindow.showAtLocation(mRootLayout, Gravity.NO_GRAVITY, OFFSET_X, OFFSET_Y);
                    }
                });*/
//                if (popupWindow.getVisibility() == View.GONE) {
//                    popupWindow.setVisibility(View.VISIBLE);
//                } else {
//                    popupWindow.setVisibility(View.GONE);
//                }

//                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Add Conference menu item
        menuitem = menu;
//        getMenuInflater().inflate(R.menu.menu_conference, menu);
        if (mConvoView.isGroupChat()) {
            //getMenuInflater().inflate(R.menu.menu_conversation_detail_group, menu);
        } else {
            //getMenuInflater().inflate(R.menu.menu_conversation_detail, menu);
        }
        return true;
    }

    void startImagePicker() {
        startActivityForResult(getPickImageChooserIntent(), REQUEST_SEND_IMAGE);
    }

    /**
     * Create a chooser intent to select the source to get image from.<br/>
     * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the intent chooser.
     */
    public Intent getPickImageChooserIntent() {


        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

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

    Uri mLastPhoto = null;
    private final static int MY_PERMISSIONS_REQUEST_AUDIO = 1;
    private final static int MY_PERMISSIONS_REQUEST_CAMERA = 2;
    private final static int MY_PERMISSIONS_REQUEST_FILE = 3;


    void startPhotoTaker() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {


                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(mConvoView.getHistoryView(), R.string.grant_perms_camera, Snackbar.LENGTH_LONG).show();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {

            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "cs_" + new Date().getTime() + ".jpg");

            mLastPhoto = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photo);

            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    mLastPhoto);

            // start the image capture Intent
            startActivityForResult(intent, ConversationDetailActivity.REQUEST_TAKE_PICTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    void startFilePicker() {

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {


                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(mConvoView.getHistoryView(), R.string.grant_perms_pick_file, Snackbar.LENGTH_LONG).show();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_FILE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {

            // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
            // browser.
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            // Filter to show only images, using the image MIME data type.
            // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
            // To search for all documents available via installed storage providers,
            // it would be "*/*".
            intent.setType("*/*");

            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file_to_send)), REQUEST_SEND_FILE);
        }
    }

    void startLocationMessage() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            AppFuncs.alert(this, R.string.google_play_services_not_available,
                    true);
        }
    }

    public void handleSendDelete(Uri contentUri, String defaultType, boolean delete, boolean resizeImage, boolean importContent) {

        handleSendDelete(mConvoView.getChatSession(), contentUri, defaultType, delete, resizeImage, importContent);
    }

    public void handleSendDelete(IChatSession session, Uri contentUri, String defaultType, boolean delete, boolean resizeImage, boolean importContent) {
        try {

            // import
            SystemServices.FileInfo info = SystemServices.getFileInfoFromURI(this, contentUri);

            if (info.type == null)
                info.type = defaultType;

            String sessionId = mConvoView.getChatId() + "";

            String fileName = contentUri.getLastPathSegment();

            Uri vfsUri;
            /*if (resizeImage)
                vfsUri = SecureMediaStore.resizeAndImportImage(this, sessionId, contentUri, info.type);
            else*/
            if (importContent) {

                if (contentUri.getScheme() == null || contentUri.getScheme().equals("assets"))
                    vfsUri = SecureMediaStore.importContent(sessionId, fileName, info.stream);
                else if (contentUri.getScheme().startsWith("http")) {
                    vfsUri = SecureMediaStore.importContent(sessionId, fileName, new URL(contentUri.toString()).openConnection().getInputStream());
                } else
                    vfsUri = SecureMediaStore.importContent(sessionId, fileName, info.stream);
            } else {
                vfsUri = contentUri;
            }

            // send
            boolean sent = handleSendData(session, vfsUri, info.type);
            if (!sent) {
                // not deleting if not sent
                return;
            }
            // auto delete
            if (delete) {
                boolean deleted = delete(contentUri);
                if (!deleted) {
                    throw new IOException("Error deleting " + contentUri);
                }
            }
        } catch (Exception e) {
            //  AppFuncs.alert(this, "Error sending file", true); // TODO i18n
            Log.e(ImApp.LOG_TAG, "error sending file", e);
        }
    }

    private boolean delete(Uri uri) {
        if (uri.getScheme().equals("content")) {
            int deleted = getContentResolver().delete(uri, null, null);
            return deleted == 1;
        }
        if (uri.getScheme().equals("file")) {
            java.io.File file = new java.io.File(uri.toString().substring(5));
            return file.delete();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (resultCode == RESULT_OK) {
            Uri uri;
            switch (requestCode) {
                case REQUEST_PICK_CONTACTS:
                    ArrayList<String> invitees = new ArrayList<>();
                    String username = resultIntent.getStringExtra(BundleKeyConstant.RESULT_KEY);
                    if (username != null)
                        invitees.add(username);
                    else
                        invitees = resultIntent.getStringArrayListExtra(BundleKeyConstant.EXTRA_RESULT_USERNAMES);
                    mConvoView.inviteContacts(invitees);
                    break;
                case REQUEST_SEND_IMAGE:
                    uri = resultIntent.getData();
                    if (uri != null) {
                        handleSendDelete(uri, "image/jpeg", false, true, true);
                    }
                    break;
                case REQUEST_SEND_FILE:
                case REQUEST_SEND_AUDIO:
                    uri = resultIntent.getData();
                    if (uri != null) {
                        String defaultType = resultIntent.getType();
                        handleSendDelete(uri, defaultType, false, false, true);
                    }
                    break;
                case REQUEST_TAKE_PICTURE:
                    if (mLastPhoto != null) {
                        handleSendDelete(mLastPhoto, "image/jpeg", true, true, true);
                        mLastPhoto = null;
                    }
                    break;
                case REQUEST_FROM_SETTING:
                    Bundle extras = resultIntent.getExtras();
                    int type = extras.getInt("type");
                    if (type == TYPE_REQUEST_CHANGE_BACKGROUND) {
                        String imagePath = extras.getString("imagePath");

                        ConferenceUtils.saveBitmapPreferences(imagePath, new XmppAddress(mConvoView.mRemoteAddress).getUser(), this);

                        loadBitmapPreferences();

                        mConvoView.sendMessageAsync(ConferenceConstant.SEND_BACKGROUND_CHAT_PREFIX + imagePath);
                    }
                    break;
                case REQUEST_PLACE_PICKER:
                    Place place = PlacePicker.getPlace(resultIntent, this);
                    mConvoView.sendMessageAsync(ConferenceConstant.SEND_LOCATION_FREFIX + place.getLatLng().latitude + ":" + place.getLatLng().longitude);
                    break;
                case REQUEST_PICK_FOLDER:
                    uri = resultIntent.getData();
                    if (mSelectedUri != null) {
                        try {
                            info.guardianproject.iocipher.File fileDownload = new info.guardianproject.iocipher.File(mSelectedUri.getPath());
                            FileInputStream inputStream = new FileInputStream(fileDownload);
                            String fileName = Downloader.getFilenameFromUrl(mSelectedUri.getPath());
                            File file = new File(FileUtil.getFullPathFromTreeUri(uri, this), fileName);
                            if (!file.exists())
                                file.createNewFile();
                            SecureMediaStore.copyToVfs(inputStream, file.getPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mSelectedUri = null;
                    }
                    break;
            }
        }
    }

    /**
     * loading bitmap to set background this screen
     */

    private void loadBitmapPreferences() {
        String imagePath = PreferenceUtils.getString(new XmppAddress(mConvoView.mRemoteAddress).getUser(), "", getApplicationContext());
        if (!TextUtils.isEmpty(imagePath)) {
            Glide.with(this)
                    .load(Uri.parse("file:///android_asset/" + imagePath))
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            //  mRootLayout.setBackground(resource.getCurrent());
                            getWindow().setBackgroundDrawable(resource.getCurrent());
                        }
                    });
        }
    }

    public boolean handleSendData(IChatSession session, Uri uri, String mimeType) {
        try {
            if (session != null) {

                String offerId = UUID.randomUUID().toString();
                return session.offerData(offerId, uri.toString(), mimeType);
            }
        } catch (RemoteException e) {
            Log.e(ImApp.LOG_TAG, "error sending file", e);
        }
        return false; // was not sent
    }

    boolean mIsAudioRecording = false;

    public boolean isAudioRecording() {
        return mIsAudioRecording;
    }

    public void startAudioRecording() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {


                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(mConvoView.getHistoryView(), R.string.grant_perms_audio, Snackbar.LENGTH_LONG).show();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_AUDIO);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            mMediaRecorder = new MediaRecorder();

            String fileName = UUID.randomUUID().toString().substring(0, 8) + ".m4a";
            mAudioFilePath = new File(getFilesDir(), fileName);

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            //maybe we can modify these in the future, or allow people to tweak them
            mMediaRecorder.setAudioChannels(1);
            mMediaRecorder.setAudioEncodingBitRate(22050);
            mMediaRecorder.setAudioSamplingRate(64000);
            mMediaRecorder.setOutputFile(mAudioFilePath.getAbsolutePath());

            try {
                mIsAudioRecording = true;
                mMediaRecorder.prepare();
                mMediaRecorder.start();
            } catch (Exception e) {
                Log.e(ImApp.LOG_TAG, "couldn't start audio", e);
            }
        }
    }

    public void stopAudioRecording(boolean send) {
        if (mMediaRecorder != null && mAudioFilePath != null && mIsAudioRecording) {

            try {

                mMediaRecorder.stop();

                mMediaRecorder.reset();
                mMediaRecorder.release();

                if (send) {
                    Uri uriAudio = Uri.fromFile(mAudioFilePath);
                    boolean deleteFile = true;
                    boolean resizeImage = false;
                    boolean importContent = true;
                    handleSendDelete(uriAudio, "audio/mp4", deleteFile, resizeImage, importContent);
                } else {
                    mAudioFilePath.delete();
                }
            } catch (IllegalStateException ise) {
                Log.w(ImApp.LOG_TAG, "error stopping audio recording: " + ise);
            } catch (RuntimeException re) //stop can fail so we should catch this here
            {
                Log.w(ImApp.LOG_TAG, "error stopping audio recording: " + re);
            }

            mIsAudioRecording = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (mLastPhoto != null)
            savedInstanceState.putString(BundleKeyConstant.LAST_PHOTO_KEY, mLastPhoto.toString());

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        String lastPhotoPath = savedInstanceState.getString(BundleKeyConstant.LAST_PHOTO_KEY);
        if (lastPhotoPath != null)
            mLastPhoto = Uri.parse(lastPhotoPath);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public Locale getCurrentLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return getResources().getConfiguration().locale;
        }
    }

    public static final int REQUEST_PICK_CONTACTS = RESULT_FIRST_USER + 1;
    public static final int REQUEST_SEND_IMAGE = REQUEST_PICK_CONTACTS + 1;
    public static final int REQUEST_SEND_FILE = REQUEST_SEND_IMAGE + 1;
    public static final int REQUEST_SEND_AUDIO = REQUEST_SEND_FILE + 1;
    public static final int REQUEST_TAKE_PICTURE = REQUEST_SEND_AUDIO + 1;
    public static final int REQUEST_SETTINGS = REQUEST_TAKE_PICTURE + 1;
    public static final int REQUEST_TAKE_PICTURE_SECURE = REQUEST_SETTINGS + 1;
    public static final int REQUEST_ADD_CONTACT = REQUEST_TAKE_PICTURE_SECURE + 1;
    public static final int REQUEST_FROM_SETTING = REQUEST_ADD_CONTACT + 1;
    private static final int REQUEST_PLACE_PICKER = REQUEST_FROM_SETTING + 1;
    public static final int REQUEST_PICK_FOLDER = REQUEST_PLACE_PICKER + 1;


    public static final int TYPE_SEARCH = 0;
    public static final int TYPE_REQUEST_CHANGE_BACKGROUND = 1;

    class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
            StringBuilder buf = new StringBuilder();
            buf.append(Imps.Contacts.TYPE).append('=').append(Imps.Contacts.TYPE_NORMAL);
            buf.append(" and ");
            buf.append(Imps.Contacts.NICKNAME).append(" = '").append(mNickname).append("'");

            Uri baseUri = Imps.Contacts.CONTENT_URI;
            Uri.Builder builder = baseUri.buildUpon();
            CursorLoader loader = new CursorLoader(ConversationDetailActivity.this, builder.build(), CHAT_PROJECTION,
                    buf.toString(), null, Imps.Contacts.SUB_AND_ALPHA_SORT_ORDER);

            return loader;
        }

        @Override
        public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
            if (data == null || data.getCount() == 0) {
                findViewById(R.id.btnAddFriend).performClick();
                mConvoView.setViewType(ConversationView.VIEW_TYPE_INVITATION);
            } else {
                if (mChatId == -1 && data.moveToFirst())
                    mChatId = data.getLong(0);
                startChatting();
            }
        }

        @Override
        public void onLoaderReset(android.content.Loader<Cursor> loader) {

        }

        public final String[] CHAT_PROJECTION = {Imps.Contacts._ID, Imps.Contacts.PROVIDER,
                Imps.Contacts.ACCOUNT, Imps.Contacts.USERNAME,
                Imps.Contacts.NICKNAME, Imps.Contacts.TYPE,
                Imps.Contacts.SUBSCRIPTION_TYPE,
                Imps.Contacts.SUBSCRIPTION_STATUS,
                Imps.Presence.PRESENCE_STATUS,
                Imps.Presence.PRESENCE_CUSTOM_STATUS,
                Imps.Chats.LAST_MESSAGE_DATE,
                Imps.Chats.LAST_UNREAD_MESSAGE
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegisterNotificationCenter) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.changeAvatarGroupFromSetting);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.addSearchBarInDetailConverasation);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateConversationDetail);
        }
        mConvoView.stopListening();
        if (task != null)
            task.setCallback(null);

        if (MessageListItem.mAudioPlayer != null) {
            MessageListItem.mAudioPlayer.stop();
            MessageListItem.mAudioPlayer = null;
        }
    }
}
