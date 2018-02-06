/*
 * Copyright (C) 2007-2008 Esmertec AG. Copyright (C) 2007-2008 The Android Open
 * Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.wrappy.im.ui;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.ListViewCompat;
import android.support.v4.widget.ResourceCursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import net.ironrabbit.type.CustomTypefaceManager;
import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.model.SelectedContact;
import net.wrappy.im.model.WpKChatGroup;
import net.wrappy.im.model.WpKChatGroupDto;
import net.wrappy.im.model.WpKIcon;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.provider.Store;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.ui.widgets.FlowLayout;
import net.wrappy.im.util.BundleKeyConstant;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity used to pick a contact.
 */
public class ContactsPickerActivity extends BaseActivity {

    private int REQUEST_CODE_ADD_CONTACT = 9999;

    private ContactAdapter mAdapter;

    private MyLoaderCallbacks mLoaderCallbacks;

    private ContactListListener mListener = null;
    private Uri mUri = Imps.Contacts.CONTENT_URI;

    private Handler mHandler = new Handler();

    private ArrayList<String> excludedContacts;
    private String mExcludeClause;
    Uri mData;
    private boolean mShowGroups = false;

    private String mSearchString;

    SearchView mSearchView = null;
    FlowLayout mSelectedContacts;
    View mLayoutContactSelect;
    View mLayoutGroupSelect;
    SwipeMenuListView mListView = null;
    private MenuItem mMenuStartGroupChat, mMenuContactsList, mMenuContactsAdd;
    private boolean isClickedMenu;
    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    public static final int LOADER_ID = 1;

    int type;
    WpKChatGroupDto groupDto;
    ArrayList<String> groupmember;

    boolean isGroupOnlyMode;

    //private AppBarLayout appBarLayout;
    private Toolbar mToolbar;

    private long lastchatid;

    // The callbacks through which we will interact with the LoaderManager.
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

    private LongSparseArray<SelectedContact> mSelection = new LongSparseArray<>();

    public LongSparseArray<SelectedContact> getSelection() {
        return mSelection;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);


        //    ((ImApp) getApplication()).setAppTheme(this);

        setContentView(R.layout.contacts_picker_activity);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mToolbar.setPadding(0, 0, 0, 0);//for tab otherwise give space in tab
        mToolbar.setContentInsetsAbsolute(0, 0);

        Typeface typeface = CustomTypefaceManager.getCurrentTypeface(this);

        if (typeface != null) {
            for (int i = 0; i < mToolbar.getChildCount(); i++) {
                View view = mToolbar.getChildAt(i);
                if (view instanceof TextView) {
                    TextView tv = (TextView) view;
                    tv.setTypeface(typeface);
                    break;
                }
            }
        }

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int themeColorHeader = settings.getInt("themeColor", -1);
        int themeColorText = settings.getInt("themeColorText", -1);
        int themeColorBg = settings.getInt("themeColorBg", -1);

        if (themeColorHeader != -1) {

            if (themeColorText == -1)
                themeColorText = Utils.getContrastColor(themeColorHeader);

            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setNavigationBarColor(themeColorHeader);
                getWindow().setStatusBarColor(themeColorHeader);
                getWindow().setTitleColor(themeColorText);
            }

            //      appBarLayout.setBackgroundColor(themeColorHeader);
            //   collapsingToolbar.setBackgroundColor(themeColorHeader);
            mToolbar.setBackgroundColor(themeColorHeader);
            mToolbar.setTitleTextColor(themeColorText);

        } else {
            mToolbar.setBackgroundColor(getResources().getColor(R.color.wrappy_primary));
            mToolbar.setTitleTextColor(getResources().getColor(R.color.message_background_light));
        }

        if (themeColorBg != -1) {
            getWindow().setBackgroundDrawable(new ColorDrawable(themeColorBg));

        }

        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getData() != null) {
            mUri = getIntent().getData();
        }
        groupmember = new ArrayList<>();

        type = getIntent().getIntExtra("type", -1);
        lastchatid = getIntent().getLongExtra(BundleKeyConstant.EXTRA_CHAT_ID, -1);
        if (type == SettingConversationActivity.PICKER_ADD_MEMBER) {
            groupDto = getIntent().getParcelableExtra(BundleKeyConstant.EXTRA_GROUP_ID);
            groupmember = getIntent().getStringArrayListExtra(BundleKeyConstant.EXTRA_LIST_MEMBER);
        }

        mLayoutContactSelect = findViewById(R.id.layoutContactSelect);
        mLayoutGroupSelect = findViewById(R.id.layoutGroupSelect);
        mSelectedContacts = (FlowLayout) findViewById(R.id.flSelectedContacts);
        mSelectedContacts.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // When the tag view grows we don't want the list to jump around, so
                // compensate for this by trying to scroll the list.
                final int diff = bottom - oldBottom;
                ListViewCompat.scrollListBy(mListView, diff);
            }
        });

        isGroupOnlyMode = isGroupOnlyMode();
        excludedContacts = getIntent().getStringArrayListExtra(BundleKeyConstant.EXTRA_EXCLUDED_CONTACTS);
        mShowGroups = getIntent().getBooleanExtra(BundleKeyConstant.EXTRA_SHOW_GROUPS, false);

        View btnCreateGroup = findViewById(R.id.btnCreateGroup);
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGroupMode(true);
            }
        });
        btnCreateGroup.setVisibility(isGroupOnlyMode ? View.GONE : View.VISIBLE);

        View btnAddContact = findViewById(R.id.btnAddFriend);
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ContactsPickerActivity.this, AddContactNewActivity.class);
                startActivityForResult(i, REQUEST_CODE_ADD_CONTACT);
            }
        });
        btnAddContact.setVisibility(isGroupOnlyMode ? View.GONE : View.VISIBLE);

        // Make sure the tag view can not be more than a third of the screen
        View root = findViewById(R.id.llRoot);
        if (root != null) {
            root.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if ((bottom - top) != (oldBottom - oldTop)) {
                        ViewGroup.LayoutParams lp = mSelectedContacts.getLayoutParams();
                        lp.height = (bottom - top) / 3;
                        mSelectedContacts.setLayoutParams(lp);
                    }
                }
            });
        }

        mListView = (SwipeMenuListView) findViewById(R.id.contactsList);
        setGroupMode(isGroupOnlyMode);

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                multiStart(i);
                //getSupportActionBar().startActionMode(mActionModeCallback);

                return true;
            }
        });

        // Uncomment this to set as list view header instead.
        //((ViewGroup)mSelectedContacts.getParent()).removeView(mSelectedContacts);
        //mSelectedContacts.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
        //mListView.addHeaderView(mSelectedContacts);

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                if (mListView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE) {
                    if (isSelected(id)) {
                        unselect(id);
                    } else {
                        select(position);
                    }
                } else {
                    Cursor cursor = (Cursor) mAdapter.getItem(position);
                    String address = cursor.getString(ContactListItem.COLUMN_CONTACT_USERNAME);
                    String nickName = address.split("@")[0];
                    long chatId = Imps.Contacts.getContactIdFromAddress(getContentResolver(),address);
                    String referenceAvatar = Imps.Avatars.getAvatar(getContentResolver(),address);
                    Intent intent = ConversationDetailActivity.getStartIntent(ContactsPickerActivity.this,chatId,nickName,referenceAvatar);
                    startActivity(intent);
                    finish();
                }
            }

        });

        if (getIntent() != null) {
            if (getIntent().getBooleanExtra(BundleKeyConstant.KEY_GROUP, false)) {
                setGroupMode(true);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void multiStart(int i) {
        setGroupMode(true);
        if (i != -1) {
            select(i);
        }
    }

    private boolean isGroupOnlyMode() {
        return getIntent().hasExtra(BundleKeyConstant.EXTRA_EXCLUDED_CONTACTS);
    }

    private boolean isGroupMode() {
        return mListView.getChoiceMode() != ListView.CHOICE_MODE_SINGLE;
    }

    private void setGroupMode(boolean groupMode) {
        setTitle(groupMode ? R.string.add_people : R.string.choose_friend);
        if (mAdapter != null) {

            mAdapter.enableAlphabet(groupMode);

        }
        mToolbar.setTitle(groupMode ? R.string.add_people : R.string.choose_friend);
        mLayoutContactSelect.setVisibility(groupMode ? View.GONE : View.VISIBLE);
        mLayoutGroupSelect.setVisibility(groupMode ? View.VISIBLE : View.GONE);
        int newChoiceMode = (groupMode ? ListView.CHOICE_MODE_MULTIPLE : ListView.CHOICE_MODE_SINGLE);
        if (mListView.getChoiceMode() != newChoiceMode) {
            mListView.setChoiceMode(newChoiceMode);
        }
        updateStartGroupChatMenu();
    }

    private void multiFinish() {
        if (mSelection.size() > 0) {
            ContactsPickerGroupFragment groupFragment = (ContactsPickerGroupFragment) getFragmentManager().findFragmentById(R.id.containerGroup);
            final String groupName = groupFragment.getGroupName();
            final ArrayList<String> members = groupFragment.getListUsername();
            Uri uri = groupFragment.getGroupUri();
            String error = "";
            if (groupName.isEmpty()) {
                error = getString(R.string.error_group_name_empty);
            } else if (members.size() == 0) {
                error = getString(R.string.error_group_empty);
            }

            if (error.isEmpty()) {
                AppFuncs.showProgressWaiting(this);
                if (uri != null) {
                    RestAPI.uploadFile(getApplicationContext(), new File(uri.getPath()), RestAPI.PHOTO_AVATAR).setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            String reference = "";
                            try {
                                JsonObject jsonObject = (new JsonParser()).parse(result.getResult()).getAsJsonObject();
                                reference = jsonObject.get(RestAPI.PHOTO_REFERENCE).getAsString();
                                WpKIcon icon = new WpKIcon();
                                icon.setReference(reference);
                                WpKChatGroupDto wpKChatGroupDto = createChatGroupDto(groupName);
                                wpKChatGroupDto.setIcon(icon);
                                WpKChatGroup wpKChatGroup = createChatGroup(wpKChatGroupDto, members);
                                JsonObject json = AppFuncs.convertClassToJsonObject(wpKChatGroup);
                                createGroupXMPP(groupName, reference, json);
                            } catch (Exception ex) {
                                AppFuncs.dismissProgressWaiting();
                                AppFuncs.alert(getApplicationContext(), getString(R.string.error_upload_photo), true);
                            }
                        }
                    });
                } else {
                    try {
                        WpKChatGroupDto wpKChatGroupDto = createChatGroupDto(groupName);
                        WpKChatGroup wpKChatGroup = createChatGroup(wpKChatGroupDto, members);
                        JsonObject jsonObject = AppFuncs.convertClassToJsonObject(wpKChatGroup);
                        createGroupXMPP(groupName, "", jsonObject);
                    } catch (Exception ex) {
                        AppFuncs.dismissProgressWaiting();
                        AppFuncs.alert(getApplicationContext(), getString(R.string.error_upload_photo), true);
                    }
                }
            } else {
                AppFuncs.alert(getApplicationContext(), error, true);
            }

        }
    }

    private WpKChatGroupDto createChatGroupDto(String groupName) {
        WpKChatGroupDto wpKChatGroupDto = new WpKChatGroupDto();
        wpKChatGroupDto.setName(groupName);
        wpKChatGroupDto.setDescription("");
        return wpKChatGroupDto;
    }

    private WpKChatGroup createChatGroup(WpKChatGroupDto wpKChatGroupDto, ArrayList<String> members) {
        WpKChatGroup wpKChatGroup = new WpKChatGroup();
        wpKChatGroup.setMemberIds(members);
        wpKChatGroup.setWpKChatGroupDto(wpKChatGroupDto);
        return wpKChatGroup;
    }

    private void createGroupXMPP(final String groupName, final String reference, JsonObject jsonObject) {
        Debug.d(jsonObject.toString());
        RestAPI.PostDataWrappy(getApplicationContext(), jsonObject, RestAPI.CHAT_GROUP, new RestAPIListener(this) {
            @Override
            public void OnComplete(String s) {
                AppFuncs.dismissProgressWaiting();
                ArrayList<String> users = new ArrayList<>();
                ArrayList<Integer> providers = new ArrayList<>();
                ArrayList<Integer> accounts = new ArrayList<>();
                WpKChatGroupDto chatGroupDto = new Gson().fromJson(s, WpKChatGroupDto.class);

                for (int i = 0; i < mSelection.size(); i++) {
                    SelectedContact contact = mSelection.valueAt(i);
                    users.add(contact.username);
                    providers.add(contact.provider);
                    accounts.add(contact.account);
                }
                if (chatGroupDto.getIcon()!=null) {
                    String avatar = chatGroupDto.getIcon().getReference();
                    String hash  = net.wrappy.im.ui.legacy.DatabaseUtils.generateHashFromAvatar(avatar);
                    net.wrappy.im.ui.legacy.DatabaseUtils.insertAvatarBlob(ImApp.sImApp.getContentResolver(), Imps.Avatars.CONTENT_URI, ImApp.sImApp.getDefaultProviderId(), ImApp.sImApp.getDefaultAccountId(), avatar, "", hash, chatGroupDto.getXmppGroup()+"@"+ Constant.DEFAULT_CONFERENCE_SERVER);
                }
                Store.putStringData(getApplicationContext(), groupName, reference);
                Intent data = new Intent();
                data.putExtra(BundleKeyConstant.EXTRA_RESULT_GROUP_NAME, chatGroupDto);
                data.putStringArrayListExtra(BundleKeyConstant.EXTRA_RESULT_USERNAMES, users);
                data.putIntegerArrayListExtra(BundleKeyConstant.PROVIDER_KEY, providers);
                data.putIntegerArrayListExtra(BundleKeyConstant.ACCOUNT_KEY, accounts);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    public void onResultPickerImage(boolean isAvatar, Intent data, boolean isSuccess) {
        super.onResultPickerImage(isAvatar, data, isSuccess);
        try {
            ContactsPickerGroupFragment groupFragment = (ContactsPickerGroupFragment) getFragmentManager().findFragmentById(R.id.containerGroup);
            groupFragment.updateAvatar(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);

        if (response == RESULT_OK) {
//            try {
//                ContactsPickerGroupFragment groupFragment = (ContactsPickerGroupFragment) getFragmentManager().findFragmentById(R.id.containerGroup);
//                groupFragment.onActivityResult(request, response, data);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
            if (request == REQUEST_CODE_ADD_CONTACT) {
                String newContact = data.getExtras().getString(BundleKeyConstant.RESULT_KEY);

                if (newContact != null) {
                    Intent dataNew = new Intent();

                    long providerId = data.getExtras().getLong(BundleKeyConstant.PROVIDER_KEY);

                    dataNew.putExtra(BundleKeyConstant.RESULT_KEY, newContact);
                    dataNew.putExtra(BundleKeyConstant.PROVIDER_KEY, providerId);
                    setResult(RESULT_OK, dataNew);

                    finish();

                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contact_list_menu, menu);

        mMenuStartGroupChat = menu.findItem(R.id.action_start_chat);
        mMenuContactsList = menu.findItem(R.id.action_contacts_list);
        mMenuContactsAdd = menu.findItem(R.id.action_contacts_add);
        mMenuContactsList.setVisible(false);
        updateStartGroupChatMenu();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));

        if (mSearchView != null) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            mSearchView.setBackground(getResources().getDrawable(R.drawable.background_search_view));
            EditText searchEditText = (EditText) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            searchEditText.setTextColor(getResources().getColor(R.color.message_background_light));
            searchEditText.setHintTextColor(getResources().getColor(R.color.message_background_light));
            mSearchView.setPadding(0, 0, 0, 0);

            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                public boolean onQueryTextChange(String newText) {
                    mSearchString = newText;
                    doFilter(mSearchString);
                    return true;
                }

                public boolean onQueryTextSubmit(String query) {
                    mSearchString = query;
                    doFilter(mSearchString);

                    return true;
                }
            };

            mSearchView.setOnQueryTextListener(queryTextListener);
        }


        return true;
    }

    private void updateStartGroupChatMenu() {
        if (mMenuStartGroupChat != null && mMenuContactsList != null && mMenuContactsAdd != null) {
            mMenuStartGroupChat.setVisible(isGroupMode());
            //mMenuContactsList.setVisible(!isGroupMode());
            mMenuContactsAdd.setVisible(!isGroupMode());
            mMenuStartGroupChat.setEnabled(mSelection.size() > 0);
        }
    }

    private void insertGroupMemberInDb(long chatid , SelectedContact contact) {

        Uri mChatURI = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, chatid);
        if (mChatURI != null) {
            String username = contact.getUsername();
            String nickname = contact.getNickName();

            ContentValues values = new ContentValues(4);
            values.put(Imps.GroupMembers.USERNAME, username);
            values.put(Imps.GroupMembers.NICKNAME, nickname);

            long groupId = ContentUris.parseId(mChatURI);
            Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, chatid);
            long databaseId = 0;
            Cursor c = getContentResolver().query(uri, new String[]{"_id"}, Imps.GroupMembers.NICKNAME + "=" + contact.getNickName(),null, null);
            if (c == null) {
                values.put(Imps.GroupMembers.ROLE, "none");
                values.put(Imps.GroupMembers.AFFILIATION, "none");
                getContentResolver().insert(uri, values);
                if (username.contains(nickname) || nickname == null) {
                    updateUnknownFriendInfoInGroup(uri, nickname);
                }
            }
        }
    }

    private void updateUnknownFriendInfoInGroup(final Uri uri, String jid) {
        RestAPI.GetDataWrappy(ImApp.sImApp, RestAPI.getMemberByIdUrl(jid), new RestAPIListener() {
            @Override
            public void OnComplete(String s) {
                Debug.d(s);
                try {
                    WpKMemberDto wpKMemberDtos = new Gson().fromJson(s, new TypeToken<WpKMemberDto>() {
                    }.getType());
                    Imps.GroupMembers.updateNicknameFromGroupUri(getContentResolver(), uri, wpKMemberDtos.getIdentifier());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isClickedMenu) {
            isClickedMenu = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isClickedMenu = false;
                }
            }, 1000);
            switch (item.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
                    return true;
                case R.id.action_start_chat:
                    if (getFragmentManager().getBackStackEntryCount() > 0)
                        multiFinish();
                    else {
                        if (type == 1) {
                           // String usersinvite = "";
                            ArrayList<String> users = new ArrayList();
                            for (int i = 0; i < mSelection.size(); i++) {
                                SelectedContact contact = mSelection.valueAt(i);
                                users.add(contact.getUsername());
                               /* if(usersinvite.isEmpty())
                                {
                                    usersinvite = contact.nickname;

                                }
                                else
                                {
                                    usersinvite = usersinvite + "-" + contact.nickname;
                                }*/
                            }
                            Gson gson = new Gson();
                            String jsonObject = gson.toJson(users);
                            JsonParser parser = new JsonParser();
                            JsonArray json = (JsonArray) parser.parse(jsonObject);


                            RestAPI.PostDataWrappyArray(this, json, String.format(RestAPI.ADD_MEMBER_TO_GROUP, groupDto.getId()), new RestAPIListener(this) {
                                @Override
                                public void OnComplete(String s) {
                                    ArrayList<String> users = new ArrayList<>();
                                    for (int i = 0; i < mSelection.size(); i++) {
                                        SelectedContact contact = mSelection.valueAt(i);
                                        users.add(contact.username);
                                       // insertGroupMemberInDb(lastchatid,contact);
                                    }
                                    Intent data = new Intent();
                                    data.putExtra(BundleKeyConstant.EXTRA_RESULT_GROUP_NAME, groupDto);
                                    data.putStringArrayListExtra(BundleKeyConstant.EXTRA_RESULT_USERNAMES, users);

                                    setResult(RESULT_OK, data);
                                    finish();
                                }
                            });
                        } else {
                            getFragmentManager().beginTransaction().add(R.id.containerGroup, ContactsPickerGroupFragment.newsIntance()).addToBackStack(null).commit();
                        }
                    }
                    //  getFragmentManager().beginTransaction().add(R.id.containerGroup, ContactsPickerGroupFragment.newsIntance()).addToBackStack(null).commit();
                    return true;
                case R.id.action_contacts_list:
                    ContactsPickerRosterActivity.start(this);
                    return true;
                case R.id.action_contacts_add:
                    Intent i = new Intent(ContactsPickerActivity.this, AddContactNewActivity.class);
                    startActivityForResult(i, REQUEST_CODE_ADD_CONTACT);
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
        else if (isGroupMode() && !isGroupOnlyMode()) {
            setGroupMode(false);
            unselectAll();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        doFilter(mSearchString);
    }

    boolean mAwaitingUpdate = false;

    public synchronized void doFilter(String filterString) {

        mSearchString = filterString;

        if (mAdapter == null) {

            mAdapter = new ContactAdapter(ContactsPickerActivity.this, R.layout.contact_view);
            mListView.setAdapter(mAdapter);
            setSwipeMenuCreator();

            mLoaderCallbacks = new MyLoaderCallbacks();
            getSupportLoaderManager().initLoader(LOADER_ID, null, mLoaderCallbacks);
        } else {

            if (!mAwaitingUpdate) {
                mAwaitingUpdate = true;
                mHandler.postDelayed(new Runnable() {

                    public void run() {

                        getSupportLoaderManager().restartLoader(LOADER_ID, null, mLoaderCallbacks);
                        mAwaitingUpdate = false;
                    }
                }, 1000);
            }

        }
    }

    private void setSwipeMenuCreator() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {

                int screenWidth = Utils.getWithScreenDP(getApplicationContext());
                int swipeLayoutWidth = (int) (screenWidth / 1.5);
                int titleSize = Utils.convertSpToPixels(swipeLayoutWidth / 40, getApplicationContext());

                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xff, 0x00,
                        0x00)));
                deleteItem.setWidth(swipeLayoutWidth);
                deleteItem.setTitle("Delete");
                deleteItem.setTitleSize(titleSize);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };
        mListView.setMenuCreator(creator);
        setSwipeMenuItemListener();
    }

    private void setSwipeMenuItemListener() {
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        Cursor cursor = (Cursor) mAdapter.getItem(position);

                        long providerId = cursor.getLong(ContactListItem.COLUMN_CONTACT_PROVIDER);
                        long accountId = cursor.getLong(ContactListItem.COLUMN_CONTACT_ACCOUNT);
                        final String address = cursor.getString(ContactListItem.COLUMN_CONTACT_USERNAME);
                        String jid = new XmppAddress(address).getUser();

                        ImApp app = (ImApp) getApplication();
                        final IImConnection conn = app.getConnection(providerId, accountId);

                        RestAPI.DeleteDataWrappy(ContactsPickerActivity.this, null, String.format(RestAPI.DELETE_CONTACT, jid), new RestAPIListener(ContactsPickerActivity.this) {
                            @Override
                            public void OnComplete(String s) {
                                ImApp.removeContact(getContentResolver(), address, conn);
                            }
                        });
                        break;
                }
                return false;
            }
        });
    }

    private Cursor mCursor;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCursor != null && (!mCursor.isClosed()))
            mCursor.close();


    }

    private void createTagView(int index, SelectedContact contact) {
        Cursor cursor = (Cursor) mAdapter.getItem(index);
        long itemId = mAdapter.getItemId(index);
        View view = LayoutInflater.from(mSelectedContacts.getContext()).inflate(R.layout.picked_contact_item, mSelectedContacts, false);
        view.setTag(contact);

        // TODO - Feel a little awkward to create a ContactListItem here just to use the binding code.
        // I guess we should move that somewhere else.
        ContactListItem cli = new ContactListItem(this, null);
        ContactViewHolder cvh = new ContactViewHolder(view);
        cli.bind(cvh, cursor, null, false);
        View btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            private long itemId;
            private View view;

            public View.OnClickListener init(long itemId, View view) {
                this.itemId = itemId;
                this.view = view;
                return this;
            }

            @Override
            public void onClick(View v) {
                unselect(this.itemId);
            }
        }.init(itemId, view));
        mSelectedContacts.addView(view);
    }

    private void removeTagView(SelectedContact contact) {
        View view = mSelectedContacts.findViewWithTag(contact);
        if (view != null) {
            mSelectedContacts.removeView(view);
        }
    }

    private void select(int index) {
        long id = mAdapter.getItemId(index);
        if (!isSelected(id)) {
            Cursor cursor = (Cursor) mAdapter.getItem(index);
            String userName = cursor.getString(ContactListItem.COLUMN_CONTACT_USERNAME);

            SelectedContact contact = new SelectedContact(id,
                    userName,
                    (int) cursor.getLong(ContactListItem.COLUMN_CONTACT_ACCOUNT),
                    (int) cursor.getLong(ContactListItem.COLUMN_CONTACT_PROVIDER), cursor.getString(ContactListItem.COLUMN_CONTACT_NICKNAME));
            mSelection.put(id, contact);
            createTagView(index, contact);
            mAdapter.notifyDataSetChanged();
            updateStartGroupChatMenu();
        }
    }

    private boolean isSelected(long id) {
        return mSelection.indexOfKey(id) >= 0;
    }

    private void unselect(long id) {
        if (isSelected(id)) {
            removeTagView(mSelection.get(id));
            mSelection.remove((Long) id);
            mAdapter.notifyDataSetChanged();
            updateStartGroupChatMenu();
        }
    }

    private void unselectAll() {
        while (mSelection.size() > 0) {
            unselect(mSelection.keyAt(0));
        }
    }

    public class ContactAdapter extends ResourceCursorAdapter {

        private Context mContext;
        private ArrayList<String> groupmember;
        private List<String> charSection = new ArrayList<>();
        private boolean isenablealphabet = false;

        public ContactAdapter(Context context, int view) {
            super(context, view, null, 0);
            mContext = context;
        }

        public void updateGroupmember(ArrayList<String> groupmember) {
            this.groupmember = groupmember;
        }

        public void enableAlphabet(boolean check) {
            isenablealphabet = check;
        }

        private void updateHeaders(Cursor cursor) {
            charSection.clear();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(ContactListItem.COLUMN_CONTACT_NICKNAME);
                    if (TextUtils.isEmpty(name)) {
                        String address = cursor.getString(ContactListItem.COLUMN_CONTACT_USERNAME);
                        name = new XmppAddress(address).getUser();
                    }
                    if (!TextUtils.isEmpty(name)) {
                        charSection.add(String.valueOf(name.charAt(0)).toUpperCase());
                    } else {
                        charSection.add("");
                    }
                } while (cursor.moveToNext());
            }
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public void changeCursor(Cursor cursor) {
            updateHeaders(cursor);
            super.changeCursor(cursor);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = super.getView(position, convertView, parent);//let the adapter handle setting up the row views
            v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            ContactListItem v = (ContactListItem) view;

            ContactViewHolder holder = v.getViewHolder();
            if (holder == null) {
                holder = new ContactViewHolder(v);
                v.setViewHolder(holder);
            }
            v.bind(holder, cursor, mSearchString, false);
            int index = cursor.getPosition();
            long itemId = getItemId(index);
            holder.mAvatarCheck.setVisibility(isSelected(itemId) ? View.VISIBLE : View.GONE);
            String userName = cursor.getString(ContactListItem.COLUMN_CONTACT_USERNAME);
            int status = cursor.getInt(ContactListItem.COLUMN_CONTACT_PRESENCE_STATUS);
            if (excludedContacts != null && excludedContacts.contains(userName)) {
                holder.mLine1.setTextColor((holder.mLine1.getCurrentTextColor() & 0x00ffffff) | 0x80000000);
                holder.mLine1.setText(getString(R.string.is_already_in_your_group, holder.mLine1.getText()));
                //   holder.mLine2.setText(status);
            } else {
                holder.mLine1.setTextColor(holder.mLine1.getCurrentTextColor() | 0xff000000);
            }
            if (isenablealphabet == false) {
                if (index > 0 && charSection.get(index).equalsIgnoreCase(charSection.get(index-1))) {

                    holder.linesection.setVisibility(View.INVISIBLE);
                    holder.textsection.setVisibility(View.INVISIBLE);


                } else {
                    holder.textsection.setVisibility(View.VISIBLE);
                    if (index > 0) {
                        holder.linesection.setVisibility(View.VISIBLE);
                    } else {
                        holder.linesection.setVisibility(View.GONE);
                    }
                    holder.textsection.setText(charSection.get(index));
                }
            } else {
                holder.textsection.setVisibility(View.GONE);
            }

            if (Imps.Contacts.TYPE_GROUP != cursor.getInt(ContactListItem.COLUMN_CONTACT_TYPE)) {
                holder.mAvatarCheck.setVisibility(View.VISIBLE);

                switch (status) {
                    case Imps.Presence.AVAILABLE:
                        holder.mAvatarCheck.setImageResource(R.drawable.status_active);
                        break;
                    case Imps.Presence.AWAY:
                    case Imps.Presence.IDLE:
                        holder.mAvatarCheck.setImageResource(R.drawable.status_aw);
                        break;
                    case Imps.Presence.OFFLINE:
                    case Imps.Presence.INVISIBLE:
                        holder.mAvatarCheck.setImageResource(R.drawable.status_disable);
                        break;
                    default:
                        break;
                }
            }

        }
    }

    class MyLoaderCallbacks implements LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            StringBuilder buf = new StringBuilder();

            if (!TextUtils.isEmpty(mSearchString)) {
                buf.append('(');
                buf.append(Imps.Contacts.NICKNAME);
                buf.append(" LIKE ");
                DatabaseUtils.appendValueToSql(buf, "%" + mSearchString + "%");
                buf.append(" OR ");
                buf.append(Imps.Contacts.USERNAME);
                buf.append(" LIKE ");
                DatabaseUtils.appendValueToSql(buf, "%" + mSearchString + "%");
                buf.append(')');
                buf.append(" AND ");
            }

            buf.append('(');
            buf.append(Imps.Contacts.TYPE).append('=').append(Imps.Contacts.TYPE_NORMAL);

            if (mShowGroups) {
                buf.append(" OR ");
                buf.append(Imps.Contacts.TYPE).append('=').append(Imps.Contacts.TYPE_GROUP);
            }

            buf.append(')');

            buf.append(" AND ");
            buf.append('(');
            buf.append(Imps.Contacts.SUBSCRIPTION_TYPE).append("==").append(Imps.Contacts.SUBSCRIPTION_TYPE_BOTH);
            buf.append(" OR ");
            buf.append(Imps.Contacts.SUBSCRIPTION_TYPE).append("==").append(Imps.Contacts.SUBSCRIPTION_TYPE_TO);
            buf.append(')');

            if (groupmember != null && groupmember.size() > 0) {
                buf.append(" AND ");
                buf.append('(');

                // buf.append(" OR ")
                buf.append(Imps.Contacts.NICKNAME);
                buf.append(" NOT IN (");
                for (int i = 0; i < groupmember.size(); i++) {
                    DatabaseUtils.appendValueToSql(buf, groupmember.get(i));

                    if (i != groupmember.size() - 1) {
                        buf.append(" , ");
                    } else {
                        buf.append(')');
                    }
                }
                buf.append(')');
            }

            CursorLoader loader = new CursorLoader(ContactsPickerActivity.this, mUri, ContactListItem.CONTACT_PROJECTION,
                    buf == null ? null : buf.toString(), null, Imps.Contacts.DEFAULT_SORT_NICKNAME_ORDER);
            //    loader.setUpdateThrottle(50L);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
            mAdapter.changeCursor(newCursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }

    }


    public interface ContactListListener {

        public void openChat(Cursor c);

        public void showProfile(Cursor c);
    }
}
