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

package net.wrappy.im;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yalantis.ucrop.UCrop;

import net.ironrabbit.type.CustomTypefaceManager;
import net.wrappy.im.comon.BaseFragmentV4;
import net.wrappy.im.helper.AppDelegate;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListenner;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.model.Contact;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.model.PopUpNotice;
import net.wrappy.im.model.WpKChatGroupDto;
import net.wrappy.im.model.WpKChatRoster;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.provider.Store;
import net.wrappy.im.service.IContactListManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.service.ImServiceConstants;
import net.wrappy.im.tasks.AddContactAsyncTask;
import net.wrappy.im.tasks.ChatSessionInitTask;
import net.wrappy.im.tasks.GroupChatSessionTask;
import net.wrappy.im.tasks.sync.SyncDataListener;
import net.wrappy.im.tasks.sync.SyncDataRunnable;
import net.wrappy.im.ui.AddContactNewActivity;
import net.wrappy.im.ui.BaseActivity;
import net.wrappy.im.ui.ContactsPickerActivity;
import net.wrappy.im.ui.ConversationDetailActivity;
import net.wrappy.im.ui.ConversationListFragment;
import net.wrappy.im.ui.LockScreenActivity;
import net.wrappy.im.ui.MainMenuFragment;
import net.wrappy.im.ui.ProfileFragment;
import net.wrappy.im.ui.onboarding.OnboardingManager;
import net.wrappy.im.ui.promotion.MainPromotionFragment;
import net.wrappy.im.util.AssetUtil;
import net.wrappy.im.util.BundleKeyConstant;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.PopupUtils;
import net.wrappy.im.util.PreferenceUtils;
import net.wrappy.im.util.SecureMediaStore;
import net.wrappy.im.util.SystemServices;
import net.wrappy.im.util.Utils;
import net.wrappy.im.util.XmppUriHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import info.guardianproject.iocipher.VirtualFileSystem;
import me.ydcool.lib.qrmodule.activity.QrScannerActivity;

import static net.wrappy.im.helper.RestAPI.GET_LIST_CONTACT;
import static net.wrappy.im.helper.RestAPI.POST_CREATE_GROUP;

/**
 */
public class MainActivity extends BaseActivity implements AppDelegate {

    private ImApp mApp;

    public final static int REQUEST_ADD_CONTACT = 300;
    public final static int REQUEST_CHOOSE_CONTACT = 301;
    public final static int REQUEST_CHANGE_SETTINGS = 302;
    public final static int UPDATE_PROFILE_COMPLETE = 303;

    public final static String IS_FROM_PATTERN_ACTIVITY = "isFromPatternScreen";

    Adapter adapter;
    FragmentManager fragmentManager;

    @BindView(R.id.btnHeaderEdit)
    ImageButton btnHeaderEdit;
    @BindView(R.id.btnHeaderSearch)
    ImageButton btnHeaderSearch;
    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.edSearchConversation)
    AppEditTextView edSearchConversation;
    @BindView(R.id.imgLogo)
    ImageView imgLogo;
    @BindView(R.id.fab)
    FloatingActionButton mFab;

    //Check to load old data from server
    Handler mLoadDataHandler = new Handler();
    Handler mLoadContactHandler = new Handler();
    SyncDataRunnable<WpKChatGroupDto> syncGroupChatRunnable;
    SyncDataRunnable<WpKChatRoster> syncContactRunnable;
    private ChatSessionInitTask task;
    private Stack<WpKChatGroupDto> sessionTasks = new Stack<>();
    private GroupChatSessionTask groupSessionTask;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.awesome_activity_main);
        super.onCreate(savedInstanceState);

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (settings.getBoolean("prefBlockScreenshots", false))
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);


        mApp = (ImApp) getApplication();
        btnHeaderSearch.setVisibility(View.GONE);
        initFloatButton();
        initViewPager();
        initTabLayout();

        //don't wnat this to happen to often
        checkForUpdates();

        installRingtones();

        applyStyle();
        Imps.deleteMessageInDbByTime(getContentResolver());

        showPopUpNotice();
    }

    @OnEditorAction(R.id.edSearchConversation)
    public boolean onEditorAction(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            try {
                AppFuncs.dismissKeyboard(MainActivity.this);
                ConversationListFragment page = (ConversationListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + mViewPager.getCurrentItem());
                page.doSearch(edSearchConversation.getText().toString().trim());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @OnClick({R.id.btnHeaderSearch, R.id.btnHeaderEdit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnHeaderSearch:
                edSearchConversation.setText("");
                if (edSearchConversation.getVisibility() == View.VISIBLE) {
                    AppFuncs.dismissKeyboard(MainActivity.this);
                    edSearchConversation.setVisibility(View.GONE);
                    imgLogo.setVisibility(View.VISIBLE);
                    try {
                        AppFuncs.dismissKeyboard(MainActivity.this);
                        ConversationListFragment page = (ConversationListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + mViewPager.getCurrentItem());
                        page.doSearch("");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    edSearchConversation.setVisibility(View.VISIBLE);
                    edSearchConversation.setFocusable(true);
                    edSearchConversation.setFocusableInTouchMode(true);
                    imgLogo.setVisibility(View.GONE);
                }
                break;
            case R.id.btnHeaderEdit:
                try {
                    AppFuncs.dismissKeyboard(MainActivity.this);
                    ProfileFragment page = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + mViewPager.getCurrentItem());
                    page.onDataChange();
                    btnHeaderEdit.setVisibility(View.GONE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
        }
    }

    private void showPopUpNotice() {
        RestAPI.GetDataWrappy(ImApp.sImApp, RestAPI.GET_POPUP_NOTICE, new RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                try {
                    Gson gson = new Gson();
                    ArrayList<PopUpNotice> popUpNotices = gson.fromJson(s, new TypeToken<ArrayList<PopUpNotice>>() {
                    }.getType());

                    Set<String> references = PreferenceUtils.getStringSet("reference", new HashSet<String>(), MainActivity.this);

                    if (!references.contains(popUpNotices.get(0).getReference())) {
                        references.add(popUpNotices.get(0).getReference());
                        PreferenceUtils.putStringSet("reference", references, MainActivity.this);
                        PopupUtils.showCustomDialog(MainActivity.this, popUpNotices.get(0).getTitle().getEnUS(), popUpNotices.get(0).getDetail().getEnUS(), R.string.ok, null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initViewPager() {
        fragmentManager = getSupportFragmentManager();
        adapter = new Adapter(fragmentManager);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setCurrentItem(1);
    }

    private void initTabLayout() {

        // main menu tab
        TabLayout.Tab tab;
        for (int i = 0; i < 4; i++) {
            tab = mTabLayout.newTab();
            mTabLayout.addTab(tab);
        }
        createTabIcons(0, R.drawable.ic_menu_normal, getString(R.string.tab_menu_menu));
        createTabIcons(1, R.drawable.ic_menu_conversation_normal, getString(R.string.tab_menu_conversation));
        createTabIcons(2, R.drawable.ic_promotion, getString(R.string.tab_menu_promotion));
        createTabIcons(3, R.drawable.ic_menu_info_normal, getString(R.string.tab_menu_my_page));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                try {
                    AppFuncs.dismissKeyboard(MainActivity.this);
                    AppTextView appTextView = (AppTextView) tab.getCustomView();
                    appTextView.setTextColor(getResources().getColor(R.color.menu_text_active));
                    edSearchConversation.setText("");
                    edSearchConversation.setVisibility(View.GONE);
                    btnHeaderEdit.setVisibility(View.GONE);
                    imgLogo.setVisibility(View.VISIBLE);
                    if (tab.getPosition() == 0) {
                        appTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_menu_active, 0, 0);
                    } else if (tab.getPosition() == 1) {
                        btnHeaderSearch.setVisibility(View.VISIBLE);
                        mFab.setVisibility(View.VISIBLE);
                        appTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_menu_conversation_active, 0, 0);
                    } else if (tab.getPosition() == 2) {
                        appTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_promotion_h, 0, 0);
                    } else {
                        btnHeaderEdit.setVisibility(View.VISIBLE);
                        appTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_menu_info_active, 0, 0);
                    }
                    mViewPager.setCurrentItem(tab.getPosition());
                    if (tab.getPosition() == 2) {
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                try {
                    AppTextView appTextView = (AppTextView) tab.getCustomView();
                    appTextView.setTextColor(getResources().getColor(R.color.menu_text_normal));
                    if (tab.getPosition() == 0) {
                        appTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_menu_normal, 0, 0);
                    } else if (tab.getPosition() == 1) {
                        btnHeaderSearch.setVisibility(View.GONE);
                        mFab.setVisibility(View.GONE);
                        appTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_menu_conversation_normal, 0, 0);
                    } else if (tab.getPosition() == 2) {
                        appTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_promotion, 0, 0);
                    } else {
                        appTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_menu_info_normal, 0, 0);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        mTabLayout.getTabAt(1).select();
    }

    private void createTabIcons(int index, int isResIcon, String title) {
        AppTextView appTextView = new AppTextView(getApplicationContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        appTextView.setLayoutParams(layoutParams);
        appTextView.setTextColor(getResources().getColor(R.color.menu_text_normal));
        appTextView.setText(title);
        appTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        appTextView.setCompoundDrawablesWithIntrinsicBounds(0, isResIcon, 0, 0);
        mTabLayout.getTabAt(index).setCustomView(appTextView);
    }

    private void initFloatButton() {
        mFab.setVisibility(View.GONE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tabIdx = mViewPager.getCurrentItem();

                if (tabIdx == 0 || tabIdx == 1) {

                    Intent intent = new Intent(MainActivity.this, ContactsPickerActivity.class);
                    startActivityForResult(intent, REQUEST_CHOOSE_CONTACT);
                }
            }
        });
    }

    private void installRingtones() {
        AssetUtil.installRingtone(getApplicationContext(), R.raw.bell, "Zom Bell");
        AssetUtil.installRingtone(getApplicationContext(), R.raw.chant, "Zom Chant");
        AssetUtil.installRingtone(getApplicationContext(), R.raw.yak, "Zom Yak");
        AssetUtil.installRingtone(getApplicationContext(), R.raw.dranyen, "Zom Dranyen");
    }

    public void inviteContact() {
        Intent i = new Intent(MainActivity.this, AddContactNewActivity.class);
        startActivityForResult(i, MainActivity.REQUEST_ADD_CONTACT);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mViewPager.getCurrentItem() == 3) {
//            if (!Wallet.isNewWallet(this.getFilesDir()) && adapter.getItem(3).equals(mwelcome_wallet_fragment)) {
//                adapter.mFragments.remove(mwelcome_wallet_fragment);
//                fragmentManager.beginTransaction().remove(mwelcome_wallet_fragment).commit();
//                mwalletFragment = new WalletFragment();
//                addWalletTab(mwalletFragment);
//            } else if (Wallet.isNewWallet(this.getFilesDir()) && adapter.getItem(3).equals(mwalletFragment)) {
//                adapter.mFragments.remove(mwalletFragment);
//                fragmentManager.beginTransaction().remove(mwalletFragment).commit();
//                mwelcome_wallet_fragment = new Welcome_Wallet_Fragment();
//                addWalletTab(mwelcome_wallet_fragment);
//            }
//        }
        //if VFS is not mounted, then send to WelcomeActivity
        if (!VirtualFileSystem.get().isMounted()) {
            finish();
            startActivity(new Intent(this, RouterActivity.class));

        } else {
            mApp.maybeInit(this);
            mApp.initAccountInfo();
        }

        handleIntent();
        checkConnection();
    }

    @Override
    protected void onDestroy() {
        if (mLoadDataHandler != null) {
            mLoadDataHandler.removeCallbacks(syncGroupChatRunnable);
            mLoadDataHandler = null;
            syncGroupChatRunnable = null;
        }
        if (mLoadContactHandler != null) {
            mLoadContactHandler.removeCallbacks(syncContactRunnable);
            mLoadContactHandler = null;
            syncContactRunnable = null;
        }
        super.onDestroy();
    }

    private void addWalletTab(Fragment fragment) {
        adapter.addFragment(fragment, "Wallet", R.drawable.ic_wallet);
        adapter.notifyDataSetChanged();
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(3);
    }

    private boolean checkConnection() {
        try {
            if (mApp.getDefaultProviderId() != -1) {
                IImConnection conn = mApp.getConnection(mApp.getDefaultProviderId(), mApp.getDefaultAccountId());

                if (conn.getState() == ImConnection.DISCONNECTED
                        || conn.getState() == ImConnection.SUSPENDED
                        || conn.getState() == ImConnection.SUSPENDING) {

                    Snackbar sb = Snackbar.make(mViewPager, R.string.error_suspended_connection, Snackbar.LENGTH_LONG);
//                    sb.setAction(getString(R.string.connect), new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            Intent i = new Intent(MainActivity.this, AccountsActivity.class);
//                            startActivity(i);
//                        }
//                    });
                    sb.show();

                    return false;
                } else if (conn.getState() == ImConnection.LOGGING_IN) {
                    Snackbar sb = Snackbar.make(mViewPager, R.string.signing_in_wait, Snackbar.LENGTH_LONG);
                    sb.show();
                } else if (conn.getState() == ImConnection.LOGGING_OUT) {
                    Snackbar sb = Snackbar.make(mViewPager, R.string.signing_out_wait, Snackbar.LENGTH_LONG);
                    sb.show();
                } else if (conn.getState() == ImConnection.LOGGED_IN) {
                    rejoinGroupChat();
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        handleIntent();
    }

    private void handleIntent() {

        Intent intent = getIntent();

        if (intent != null) {
            Uri data = intent.getData();
            String type = intent.getType();
            if (data != null && Imps.Chats.CONTENT_ITEM_TYPE.equals(type)) {
                long chatId = ContentUris.parseId(data);
                startActivity(ConversationDetailActivity.getStartIntent(this, chatId));
            } else if (Imps.Contacts.CONTENT_ITEM_TYPE.equals(type)) {
                long providerId = intent.getLongExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, mApp.getDefaultProviderId());
                long accountId = intent.getLongExtra(ImServiceConstants.EXTRA_INTENT_ACCOUNT_ID, mApp.getDefaultAccountId());
                String username = intent.getStringExtra(ImServiceConstants.EXTRA_INTENT_FROM_ADDRESS);
                startChat(providerId, accountId, username, true, true);
            } else if (intent.hasExtra("username")) {
                //launch a new chat based on the intent value
                startChat(mApp.getDefaultProviderId(), mApp.getDefaultAccountId(), intent.getStringExtra("username"), true, true);
            }
            checkToLoadDataServer();

            setIntent(null);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == ProfileFragment.AVATAR || requestCode == ProfileFragment.BANNER || requestCode == UCrop.REQUEST_CROP) {
                try {
                    ProfileFragment profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + mViewPager.getCurrentItem());
                    profileFragment.onActivityResult(requestCode, resultCode, data);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (requestCode == REQUEST_CHANGE_SETTINGS) {
                finish();
                startActivity(new Intent(this, MainActivity.class));
            } else if (requestCode == REQUEST_ADD_CONTACT) {

                String username = data.getStringExtra(BundleKeyConstant.RESULT_KEY);

                if (username != null) {
                    long providerId = data.getLongExtra(BundleKeyConstant.PROVIDER_KEY, -1);
                    long accountId = data.getLongExtra(BundleKeyConstant.ACCOUNT_KEY, -1);

                    startChat(providerId, accountId, username, true, true);
                }

            } else if (requestCode == REQUEST_CHOOSE_CONTACT) {
                String username = data.getStringExtra(BundleKeyConstant.RESULT_KEY);

                if (username != null) {
                    long providerId = data.getLongExtra(BundleKeyConstant.PROVIDER_KEY, -1);
                    long accountId = data.getLongExtra(BundleKeyConstant.ACCOUNT_KEY, -1);

                    startChat(providerId, accountId, username, true, true);
                } else {
                    WpKChatGroupDto group = data.getParcelableExtra(BundleKeyConstant.EXTRA_RESULT_GROUP_NAME);
//                    if (groupName == null) {
//                        groupName = "groupchat" + UUID.randomUUID().toString().substring(0, 8);
//                    }
                    ArrayList<String> users = data.getStringArrayListExtra(BundleKeyConstant.EXTRA_RESULT_USERNAMES);
                    if (users != null) {
                        //start group and do invite hereartGrou
                        try {
                            IImConnection conn = mApp.getConnection(mApp.getDefaultProviderId(), mApp.getDefaultAccountId());
                            if (conn.getState() == ImConnection.LOGGED_IN) {
                                startGroupChat(group, users, conn);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }

                }
            } else if (requestCode == ConversationDetailActivity.REQUEST_TAKE_PICTURE) {
                try {
                    if (mLastPhoto != null)
                        importPhoto();
                } catch (Exception e) {
                    Log.w(ImApp.LOG_TAG, "error importing photo", e);

                }
            } else if (requestCode == OnboardingManager.REQUEST_SCAN) {

                ArrayList<String> resultScans = data.getStringArrayListExtra("result");
                for (String resultScan : resultScans) {

                    try {

                        String address = null;

                        if (resultScan.startsWith("xmpp:")) {
                            address = XmppUriHelper.parse(Uri.parse(resultScan)).get(XmppUriHelper.KEY_ADDRESS);
                            String fingerprint = XmppUriHelper.getOtrFingerprint(resultScan);
                            new AddContactAsyncTask(mApp.getDefaultProviderId(), mApp.getDefaultAccountId(), mApp).execute(address, fingerprint);

                        } else {
                            //parse each string and if they are for a new user then add the user
                            OnboardingManager.DecodedInviteLink diLink = OnboardingManager.decodeInviteLink(resultScan);

                            new AddContactAsyncTask(mApp.getDefaultProviderId(), mApp.getDefaultAccountId(), mApp).execute(diLink.username, diLink.fingerprint, diLink.nickname);
                        }

                        if (address != null)
                            startChat(mApp.getDefaultProviderId(), mApp.getDefaultAccountId(), address, true, true);

                        //if they are for a group chat, then add the group
                    } catch (Exception e) {
                        Log.w(ImApp.LOG_TAG, "error parsing QR invite link", e);
                    }
                }
            } else if (resultCode == 1000 || resultCode == QrScannerActivity.QR_REQUEST_CODE) {
                //mwelcome_wallet_fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void startGroupChat(WpKChatGroupDto group, ArrayList<String> invitees, IImConnection conn) {
        String chatServer = ""; //use the default
        String nickname = mApp.getDefaultUsername().split("@")[0];
        new GroupChatSessionTask(this, group, invitees, conn).executeOnExecutor(ImApp.sThreadPoolExecutor, chatServer, nickname);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (mLastPhoto != null)
            savedInstanceState.putString("lastphoto", mLastPhoto.toString());

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        String lastPhotoPath = savedInstanceState.getString("lastphoto");
        if (lastPhotoPath != null)
            mLastPhoto = Uri.parse(lastPhotoPath);
    }

    private void importPhoto() throws FileNotFoundException {

        // import
        SystemServices.FileInfo info = SystemServices.getFileInfoFromURI(this, mLastPhoto);
        String sessionId = "self";
        String offerId = UUID.randomUUID().toString();

        try {
            Uri vfsUri = SecureMediaStore.resizeAndImportImage(this, sessionId, mLastPhoto, info.type);

            delete(mLastPhoto);

            //adds in an empty message, so it can exist in the gallery and be forwarded
            Imps.insertMessageInDb(
                    getContentResolver(), false, new Date().getTime(), true, null, vfsUri.toString(),
                    System.currentTimeMillis(), Imps.MessageType.OUTGOING_ENCRYPTED_VERIFIED,
                    0, offerId, info.type);

            mLastPhoto = null;
        } catch (IOException ioe) {
            Log.e(ImApp.LOG_TAG, "error importing photo", ioe);
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


    private SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
//
//        if (mSearchView != null) {
//            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//            mSearchView.setIconifiedByDefault(false);
//
//            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
//                public boolean onQueryTextChange(String query) {
//                    if (mTabLayout.getSelectedTabPosition() == 0)
//                        mConversationList.doSearch(query);
//                    else if (mTabLayout.getSelectedTabPosition() == 1)
//                        mContactList.doSearch(query);
//
//                    return true;
//                }
//
//                public boolean onQueryTextSubmit(String query) {
//                    if (mTabLayout.getSelectedTabPosition() == 0)
//                        mConversationList.doSearch(query);
//                    else if (mTabLayout.getSelectedTabPosition() == 1)
//                        mContactList.doSearch(query);
//
//                    return true;
//                }
//            };
//
//            mSearchView.setOnQueryTextListener(queryTextListener);
//
//            mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
//                @Override
//                public boolean onClose() {
//                    mConversationList.doSearch(null);
//                    return false;
//                }
//            });
//        }
//
//        MenuItem mItem = menu.findItem(R.id.menu_lock_reset);
//
//        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
//        if (!settings.contains(ImApp.PREFERENCE_KEY_TEMP_PASS))
//            mItem.setVisible(true);

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private void clearFilters() {
//
//        if (mTabLayout.getSelectedTabPosition() == 0)
//            mConversationList.setArchiveFilter(false);
//        else
//            mContactList.setArchiveFilter(false);
//
//
//
//    }

//    private void enableArchiveFilter() {
//
//        if (mTabLayout.getSelectedTabPosition() == 0)
//            mConversationList.setArchiveFilter(true);
//        else
//            mContactList.setArchiveFilter(true);
//
//
//
//    }

    public void resetPassphrase() {
        /**
         Intent intent = new Intent(this, LockScreenActivity.class);
         intent.setAction(LockScreenActivity.ACTION_RESET_PASSPHRASE);
         startActivity(intent);**/

        //need to setup new user passphrase
        Intent intent = new Intent(this, LockScreenActivity.class);
        intent.setAction(LockScreenActivity.ACTION_CHANGE_PASSPHRASE);
        startActivity(intent);
    }

    @Override
    public void onChangeInApp(int id, String data) {
        if (id == UPDATE_PROFILE_COMPLETE) {
            btnHeaderEdit.setVisibility(View.VISIBLE);
        }
    }

    public class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();
        private final List<Integer> mFragmentIcons = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title, int icon) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
            mFragmentIcons.add(icon);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new MainMenuFragment();
            } else if (position == 1) {
                return new ConversationListFragment();
            } else if (position == 2) {
                return new MainPromotionFragment();
            } else {
                return ProfileFragment.newInstance(0, Store.getStringData(getApplicationContext(), Store.USERNAME), "", "");
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof BaseFragmentV4) {
                ((BaseFragmentV4) object).reloadFragment();
            }
            return super.getItemPosition(object);
        }
    }


    public void startChat(long providerId, long accountId, String username, boolean startCrypto, final boolean openChat) {

        //startCrypto is not actually used anymore, as we move to OMEMO

        if (username != null) {
            task = new ChatSessionInitTask(this, providerId, accountId, Imps.Contacts.TYPE_NORMAL);
            task.setListener(new ChatSessionInitTask.OnFinishTask() {
                @Override
                public void onFinishTask(Long chatId) {
                    if (chatId != -1 && openChat) {
                        startActivity(ConversationDetailActivity.getStartIntent(MainActivity.this, chatId));
                    }
                }
            });
            task.executeOnExecutor(ImApp.sThreadPoolExecutor, new Contact(new XmppAddress(username)));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //    UpdateManager.unregister();
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        //   UpdateManager.register(this, ImApp.HOCKEY_APP_ID);

        //only check github for updates if there is no Google Play
        if (!hasGooglePlay()) {
            try {

                String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

                //if this is a full release, without -beta -rc etc, then check the appupdater!
                if (version.indexOf("-") == -1) {

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    long timeNow = new Date().getTime();
                    long timeSinceLastCheck = prefs.getLong("updatetime", -1);

                    //only check for updates once per day
                    if (timeSinceLastCheck == -1 || (timeNow - timeSinceLastCheck) > 86400) {

                        AppUpdater appUpdater = new AppUpdater(this);
                        appUpdater.setDisplay(Display.DIALOG);
                        appUpdater.setUpdateFrom(UpdateFrom.XML);
                        appUpdater.setUpdateXML(ImApp.URL_UPDATER);

                        //  appUpdater.showAppUpdated(true);
                        appUpdater.start();

                        prefs.edit().putLong("updatetime", timeNow).commit();
                    }
                }
            } catch (Exception e) {
                Log.d("AppUpdater", "error checking app updates", e);
            }
        }
    }

    boolean hasGooglePlay() {
        try {
            getApplication().getPackageManager().getPackageInfo("com.android.vending", 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;


    }


    Uri mLastPhoto = null;

    void startPhotoTaker() {

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "cs_" + new Date().getTime() + ".jpg");
        mLastPhoto = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                mLastPhoto);

        // start the image capture Intent
        startActivityForResult(intent, ConversationDetailActivity.REQUEST_TAKE_PICTURE);
    }

    /**
     * @Override public void onConfigurationChanged(Configuration newConfig) {
     * super.onConfigurationChanged(newConfig);
     * setContentView(R.layout.awesome_activity_main);
     * <p>
     * }
     */

    public void applyStyle() {

        //first set font
        checkCustomFont();


        applyStyleColors();
    }

    private void applyStyleColors() {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        //not set color

        int themeColorHeader = settings.getInt("themeColor", -1);
        int themeColorText = settings.getInt("themeColorText", -1);
        int themeColorBg = settings.getInt("themeColorBg", -1);

        if (themeColorHeader != -1) {

            if (themeColorText == -1)
                themeColorText = Utils.getContrastColor(themeColorHeader);

            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setNavigationBarColor(themeColorHeader);
                getWindow().setStatusBarColor(themeColorHeader);
                getWindow().setTitleColor(Utils.getContrastColor(themeColorHeader));
            }


            mTabLayout.setBackgroundColor(themeColorHeader);
            mTabLayout.setTabTextColors(themeColorText, themeColorText);

            mFab.setBackgroundColor(themeColorHeader);

        }

//        if (themeColorBg != -1) {
//            if (mConversationList != null && mConversationList.getView() != null)
//                mConversationList.getView().setBackgroundColor(themeColorBg);
//
//            if (mContactList != null && mContactList.getView() != null)
//                mContactList.getView().setBackgroundColor(themeColorBg);
//
//            if (mAccountFragment != null && mAccountFragment.getView() != null)
//                mAccountFragment.getView().setBackgroundColor(themeColorBg);
//
//            if (mwalletFragment != null && mwalletFragment.getView() != null)
//                mwalletFragment.getView().setBackgroundColor(themeColorBg);
//
//
//        }

    }

    private void checkCustomFont() {

        if (Preferences.isLanguageTibetan()) {
            CustomTypefaceManager.loadFromAssets(this, true);

        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();

            final int N = mInputMethodProperties.size();
            boolean loadTibetan = false;
            for (int i = 0; i < N; i++) {

                InputMethodInfo imi = mInputMethodProperties.get(i);

                //imi contains the information about the keyboard you are using
                if (imi.getPackageName().equals("org.ironrabbit.bhoboard")) {
                    //                    CustomTypefaceManager.loadFromKeyboard(this);
                    loadTibetan = true;

                    break;
                }

            }

            CustomTypefaceManager.loadFromAssets(this, loadTibetan);
        }

    }


    /*Start sync contacts and group chat from server*/
    private void checkToLoadDataServer() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra(IS_FROM_PATTERN_ACTIVITY, false)) {
            sessionTasks.clear();
            Imps.Chats.reset(getContentResolver());
            Imps.Contacts.reset(getContentResolver());
            RestAPI.GetDataWrappy(this, POST_CREATE_GROUP, new RestAPIListenner() {
                @Override
                public void OnComplete(int httpCode, String error, String s) {
                    try {
                        WpKChatGroupDto[] wpKMemberDtos = new Gson().fromJson(s, WpKChatGroupDto[].class);
                        syncData(mLoadDataHandler, wpKMemberDtos, syncGroupListener, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            RestAPI.GetDataWrappy(this, GET_LIST_CONTACT, new RestAPIListenner() {
                @Override
                public void OnComplete(int httpCode, String error, String s) {
                    try {
                        WpKChatRoster[] kChatRosters = new Gson().fromJson(s, WpKChatRoster[].class);
                        syncData(mLoadContactHandler, kChatRosters, syncContactsListener, 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    SyncDataListener<WpKChatGroupDto> syncGroupListener = new SyncDataListener<WpKChatGroupDto>() {
        @Override
        public void sync(WpKChatGroupDto[] data) {
            syncData(mLoadDataHandler, data, syncGroupListener, 0);
        }

        @Override
        public void processing(WpKChatGroupDto[] data) {
            for (WpKChatGroupDto groupDto : data) {
                if (!TextUtils.isEmpty(groupDto.getXmppGroup())) {
                    sessionTasks.push(groupDto);
                }
            }
            rejoinGroupChat();
        }
    };

    SyncDataListener<WpKChatRoster> syncContactsListener = new SyncDataListener<WpKChatRoster>() {
        @Override
        public void sync(WpKChatRoster[] data) {
            syncData(mLoadContactHandler, data, syncContactsListener, 1);
        }

        @Override
        public void processing(WpKChatRoster[] data) {
            for (WpKChatRoster roster : data) {
                approveSubscription(roster);
            }
        }
    };

    /*
   * Auto approved contact in list which were loaded from Xmpp server
   * */
    public static void approveSubscription(final WpKChatRoster roster) {
        try {
            ImApp mApp = ImApp.sImApp;
            IImConnection conn = mApp.getConnection(mApp.getDefaultProviderId(), mApp.getDefaultAccountId());
            if (conn.getState() == ImConnection.LOGGED_IN) {
                String address = roster.getContact().getXMPPAuthDto().getAccount() + Constant.EMAIL_DOMAIN;
                ImApp.updateContact(address, roster.getContact(), conn);
                IContactListManager manager = conn.getContactListManager();
                Contact contact = new Contact(new XmppAddress(address), roster.getContact().getIdentifier(), Imps.Contacts.TYPE_NORMAL);
                manager.approveSubscription(contact);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /*
   * Auto join group chat
   *
   */
    private void rejoinGroupChat() {
        if (!sessionTasks.isEmpty()) {
            try {
                IImConnection conn = mApp.getConnection(mApp.getDefaultProviderId(), mApp.getDefaultAccountId());
                if (conn.getState() == ImConnection.LOGGED_IN) {
                    String nickname = mApp.getDefaultNickname();
                    groupSessionTask = new GroupChatSessionTask(this, sessionTasks.pop(), conn);
                    groupSessionTask.setCallback(new GroupChatSessionTask.OnTaskFinish() {
                        @Override
                        public void onFinished() {
                            rejoinGroupChat();
                        }
                    });
                    groupSessionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "", nickname);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private <T> void syncData(Handler handler, T[] data, SyncDataListener<T> syncDataListener, int type) {

        try {
            if (mApp.getDefaultProviderId() != -1 && mApp.getDefaultAccountId() != -1) {
                IImConnection conn = mApp.getConnection(mApp.getDefaultProviderId(), mApp.getDefaultAccountId());
                SyncDataRunnable runable = type == 0 ? syncGroupChatRunnable : syncContactRunnable;
                if (handler != null) {
                    handler.removeCallbacks(runable);
                    if (conn != null && conn.getState() == ImConnection.LOGGED_IN) {
                        if (syncDataListener != null) {
                            syncDataListener.processing(data);

                        }
                    } else {
                        if (runable == null) {
                            runable = initRunnable(data, syncDataListener, type);
                        }
                        handler.postDelayed(runable, 2000);
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private <T> SyncDataRunnable initRunnable(T[] data, SyncDataListener<T> syncDataListener, int type) {
        if (type == 0) {
            syncGroupChatRunnable = (SyncDataRunnable<WpKChatGroupDto>) new SyncDataRunnable<T>(syncDataListener, data);
            return syncGroupChatRunnable;
        } else {
            syncContactRunnable = (SyncDataRunnable<WpKChatRoster>) new SyncDataRunnable<T>(syncDataListener, data);
            return syncContactRunnable;
        }
    }
}

