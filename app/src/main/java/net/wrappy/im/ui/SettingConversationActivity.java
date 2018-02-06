package net.wrappy.im.ui;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.yalantis.ucrop.UCrop;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.NotificationCenter;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.helper.layout.CircleImageView;
import net.wrappy.im.model.BottomSheetCell;
import net.wrappy.im.model.BottomSheetListener;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.model.MemberGroupDisplay;
import net.wrappy.im.model.WpKChatGroupDto;
import net.wrappy.im.model.WpKIcon;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.tasks.ChatSessionTask;
import net.wrappy.im.tasks.GroupChatSessionTask;
import net.wrappy.im.ui.adapters.MemberGroupAdapter;
import net.wrappy.im.ui.conference.ConferenceConstant;
import net.wrappy.im.ui.conversation.BackgroundBottomSheetFragment;
import net.wrappy.im.util.BundleKeyConstant;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.PopupUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class SettingConversationActivity extends BaseActivity {

    final String IS_OWNER = "owner";

    @BindView(R.id.layout_search_setting)
    LinearLayout mSearchLayout;
    @BindView(R.id.switch_notification)
    Switch switch_notification;
    @BindView(R.id.layout_member_groups)
    LinearLayout mMemberGroupsLayout;
    @BindView(R.id.layout_admin_delete_group)
    LinearLayout mAdminDeleteGroup;
    @BindView(R.id.layout_member_leave_group)
    LinearLayout mMemberLeaveGroup;
    @BindView(R.id.layout_add_member)
    LinearLayout mAddMemberLayout;
    @BindView(R.id.member_group_recycler_view)
    RecyclerView mGroupRecycleView;
    @BindView(R.id.edGroupName)
    AppEditTextView edGroupName;
    @BindView(R.id.btnGroupPhoto)
    CircleImageView btnGroupPhoto;
    @BindView(R.id.lnChangeGroupNameController)
    LinearLayout lnChangeGroupNameController;
    @BindView(R.id.btnEditGroupName)
    ImageButton btnEditGroupName;
    @BindView(R.id.view_divider)
    View mViewDivider;
    @BindView(R.id.text_admin_delete_setting)
    TextView mTxtDelete;
    @BindView(R.id.lnAvatarOfGroup)
    LinearLayout lnAvatarOfGroup;
    @BindView(R.id.edGroupSubText)
    AppTextView edGroupSubText;
    @BindView(R.id.text_member_leave_group)
    TextView textLeaveGroup;

    private String mAddress = null;
    private long mProviderId = -1;
    private long mAccountId = -1;
    private long mLastChatId = -1;
    private int mContactType = -1;

    private IImConnection mConn;
    private IChatSession mSession;
    private boolean mIsOwner = false;
    WpKChatGroupDto wpKChatGroup;
    WpKChatGroupDto wpKChatGroupTemp;

    private BackgroundBottomSheetFragment mBackgroundFragment;

    private MemberGroupAdapter memberGroupAdapter;
    private ArrayList<MemberGroupDisplay> memberGroupDisplays;
    private String mOwnerGroup = "";

    private final static int REQUEST_PICK_CONTACT = 100;

    public final static int PICKER_ADD_MEMBER = 1;

    ImApp imApp;

    private List<WpKMemberDto> identifiers = new ArrayList<>();
    long idMemberOwner = -1;

    private String oldGroupName = null;
    boolean isLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting_conversation);
        super.onCreate(savedInstanceState);

        imApp = (ImApp) getApplication();
        // back button at action bar
        initActionBarDefault(true,R.string.setting_screen);
        Intent intent = getIntent();
        if (intent != null) {
            mAddress = getIntent().getStringExtra("address");
            mProviderId = getIntent().getLongExtra("provider", -1);
            mAccountId = getIntent().getLongExtra("account", -1);
            mLastChatId = getIntent().getLongExtra("chatId", -1);
            mContactType = getIntent().getIntExtra("isGroupChat", -1);

        }

        try {
            mConn = ImApp.getConnection(mProviderId, mAccountId);
            if (mConn==null) {
                PopupUtils.showCustomDialog(this, getString(R.string.error), getString(R.string.error_0), R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
            }
        } catch (Exception e) {
            AppFuncs.log(e.getLocalizedMessage());
        }

        switch_notification.setChecked(!isMuted());

        final boolean isGroup = mContactType == Imps.Contacts.TYPE_GROUP;
        // showing member group chat

        mAddMemberLayout.setVisibility(isGroup ? View.VISIBLE : View.GONE);
        mMemberGroupsLayout.setVisibility(isGroup ? View.VISIBLE : View.GONE);
        if (isGroup) {
            textLeaveGroup.setText(getString(R.string.setting_delete_and_leave_group));

            setAvatarGroup();

            initRecycleView();

            getWpkGroupFromServer();
        } else {
            isLoaded = true;
            lnAvatarOfGroup.setVisibility(View.GONE);
        }
    }


    private void setAvatarGroup() {
        String avatar = Imps.Avatars.getAvatar(getContentResolver(), mAddress);
        GlideHelper.loadBitmapToImageView(getApplicationContext(), btnGroupPhoto, RestAPI.getAvatarUrl(avatar));
    }

    private void initRecycleView() {
        memberGroupDisplays = new ArrayList<>();
        String currentUser = new XmppAddress(ImApp.sImApp.getDefaultUsername()).getUser();
        mGroupRecycleView.setNestedScrollingEnabled(false);
        mGroupRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        memberGroupAdapter = new MemberGroupAdapter(this, memberGroupDisplays, currentUser, mOwnerGroup, mLastChatId, mSession);
        mGroupRecycleView.setAdapter(memberGroupAdapter);
    }

    private void getWpkGroupFromServer() {
        String groupXmppId = mAddress;
        if (mAddress.contains("@")) {
            groupXmppId = mAddress.split("@")[0];
        }

        RestAPI.GetDataWrappy(getApplicationContext(), RestAPI.getGroupByXmppId(groupXmppId), new RestAPIListener(SettingConversationActivity.this) {
            @Override
            public void OnComplete(String s) {
                try {
                    Gson gson = new Gson();
                    wpKChatGroup = gson.fromJson(s, new TypeToken<WpKChatGroupDto>() {
                    }.getType());
                    isLoaded = true;
                    memberGroupAdapter.setmWpKChatGroupDto(wpKChatGroup);

                    idMemberOwner = wpKChatGroup.getIdentifier();

                    identifiers = wpKChatGroup.getParticipators();

                    edGroupName.setText(wpKChatGroup.getName());
                    if (wpKChatGroup.getIcon() != null) {
                        GlideHelper.loadBitmapToCircleImage(getApplicationContext(), btnGroupPhoto, RestAPI.getAvatarUrl(wpKChatGroup.getIcon().getReference()));
                        updateAvatar();
                    }

                    if (identifiers != null) {
                        updateMembers();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void updateMembers() {
        ArrayList<MemberGroupDisplay> members = new ArrayList<>();

        for (WpKMemberDto memberDto : identifiers) {

            MemberGroupDisplay member = new MemberGroupDisplay();
            member.setNickname(memberDto.getGiven());
            member.setUsername(memberDto.getIdentifier());
            if (memberDto.getAvatar() != null) {
                member.setReferenceAvatar(memberDto.getAvatar().getReference());
            }
            if (memberDto.getId() == idMemberOwner) {
                member.setAffiliation(IS_OWNER);
                edGroupSubText.setText(String.format(getString(R.string.create_by), TextUtils.isEmpty(member.getNickname())?member.getUsername():member.getNickname()));
                if (member.getUsername().equals(new XmppAddress(ImApp.sImApp.getDefaultUsername()).getUser())) {
                    mIsOwner = true;
                    memberGroupAdapter.setOwner(member.getUsername());
                }
            } else {
                member.setAffiliation("member");
            }
            members.add(member);
        }

        memberGroupDisplays.clear();
        memberGroupDisplays.addAll(members);
        memberGroupAdapter.setData(memberGroupDisplays);
        if (mIsOwner) {
            mMemberLeaveGroup.setVisibility(View.GONE);
            mAdminDeleteGroup.setVisibility(View.VISIBLE);
            mAddMemberLayout.setVisibility(View.VISIBLE);
        } else {
            mMemberLeaveGroup.setVisibility(View.VISIBLE);
            mAdminDeleteGroup.setVisibility(View.GONE);
            mAddMemberLayout.setVisibility(View.GONE);
        }
    }

    @OnCheckedChanged({R.id.switch_notification})
    public void onCheckChanged(CompoundButton buttonView, boolean isChecked) {
        setMuted(!isChecked);
    }

    @OnClick({R.id.btnGroupPhoto, R.id.btnGroupNameClose, R.id.btnGroupNameCheck, R.id.btnEditGroupName, R.id.layout_search_setting, R.id.layout_change_background_setting, R.id.layout_clean_setting, R.id.layout_admin_delete_group, R.id.layout_add_member,
            R.id.layout_member_leave_group})
    public void onClick(View view) {
        if (isLoaded) {
            switch (view.getId()) {
                case R.id.layout_search_setting:
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.addSearchBarInDetailConverasation, "");
                    finish();
                    break;
                case R.id.layout_change_background_setting:
                    mBackgroundFragment = BackgroundBottomSheetFragment.getInstance();
                    mBackgroundFragment.show(getSupportFragmentManager(), "Dialog");
                    break;
                case R.id.layout_admin_delete_group:
                    confirmDeleteGroup();
                    break;
                case R.id.layout_member_leave_group:
                    if (mContactType == Imps.Contacts.TYPE_GROUP) {
                        confirmLeaveGroup();
                    } else {
                        clearHistory();
                    }
                    break;
                case R.id.layout_clean_setting:
                    clearHistory();
                    break;
                case R.id.btnGroupPhoto:
                    ArrayList<BottomSheetCell> sheetCells = new ArrayList<>();
                    BottomSheetCell sheetCell = new BottomSheetCell(1, R.drawable.ic_choose_camera, getString(R.string.popup_take_photo));
                    sheetCells.add(sheetCell);
                    sheetCell = new BottomSheetCell(2, R.drawable.ic_choose_gallery, getString(R.string.popup_choose_gallery));
                    sheetCells.add(sheetCell);
                    PopupUtils.createBottomSheet(this, sheetCells, new BottomSheetListener() {
                        @Override
                        public void onSelectBottomSheetCell(int index) {
                            if (index == 1) {
                                AppFuncs.openCamera(SettingConversationActivity.this, true);
                            } else {
                                AppFuncs.openGallery(SettingConversationActivity.this, true);
                            }
                        }
                    }).show();
                    break;
                case R.id.btnEditGroupName:
                    edGroupName.setEnabled(true);
                    edGroupName.setFocusable(true);
                    edGroupName.setSelection(edGroupName.getText().length());
                    lnChangeGroupNameController.setVisibility(View.VISIBLE);
                    btnEditGroupName.setVisibility(View.GONE);
                    break;
                case R.id.btnGroupNameCheck:
                    String name = edGroupName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        return;
                    }
                    wpKChatGroupTemp = wpKChatGroup;
                    wpKChatGroupTemp.setName(name);
                    updateData();
                    edGroupName.setEnabled(false);
                    lnChangeGroupNameController.setVisibility(View.GONE);
                    btnEditGroupName.setVisibility(View.VISIBLE);
                    break;
                case R.id.btnGroupNameClose:
                    edGroupName.setText(wpKChatGroup.getName());
                    edGroupName.setEnabled(false);
                    lnChangeGroupNameController.setVisibility(View.GONE);
                    btnEditGroupName.setVisibility(View.VISIBLE);
                    break;
                case R.id.layout_add_member:
                    Intent intent = new Intent(SettingConversationActivity.this, ContactsPickerActivity.class);
                    ArrayList<String> usernames = new ArrayList<>(memberGroupDisplays.size());
                    for (MemberGroupDisplay member : memberGroupDisplays) {
                        usernames.add(member.getUsername());
                    }
                    intent.putExtra(BundleKeyConstant.EXTRA_LIST_MEMBER, usernames);
                    intent.putExtra(BundleKeyConstant.EXTRA_GROUP_ID, wpKChatGroup);
                    intent.putExtra("type", PICKER_ADD_MEMBER);
                    intent.putExtra(BundleKeyConstant.EXTRA_EXCLUDED_CONTACTS, true);
                    intent.putExtra(BundleKeyConstant.EXTRA_CHAT_ID, mLastChatId);

                    startActivityForResult(intent, REQUEST_PICK_CONTACT);
                    break;
            }
        }
    }

    private void startGroupChat(WpKChatGroupDto group, ArrayList<String> invitees, IImConnection conn) {
        String chatServer = ""; //use the default
        String nickname = imApp.getDefaultUsername().split("@")[0];
        new GroupChatSessionTask(this, group, invitees, conn, false).executeOnExecutor(ImApp.sThreadPoolExecutor, chatServer, nickname);
    }

    @Override
    public void onResultPickerImage(boolean isAvatar, Intent data, boolean isSuccess) {
        super.onResultPickerImage(isAvatar, data, isSuccess);
        try {
            Uri uri = UCrop.getOutput(data);
            btnGroupPhoto.setImageURI(uri);
            RestAPI.uploadFile(getApplicationContext(), new File(uri.getPath()), RestAPI.PHOTO_AVATAR).setCallback(new FutureCallback<Response<String>>() {
                @Override
                public void onCompleted(Exception e, Response<String> result) {
                    try {
                        final String reference = RestAPI.getPhotoReference(result.getResult());
                        WpKIcon wpKIcon = new WpKIcon();
                        wpKIcon.setReference(reference);
                        wpKChatGroupTemp = wpKChatGroup;
                        wpKChatGroupTemp.setIcon(wpKIcon);
                        updateData();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_CONTACT) {
                WpKChatGroupDto group = data.getParcelableExtra(BundleKeyConstant.EXTRA_RESULT_GROUP_NAME);
                ArrayList<String> users = data.getStringArrayListExtra(BundleKeyConstant.EXTRA_RESULT_USERNAMES);

                if (users != null) {
                    //start group and do invite hereartGrou
                    //finish();
                    try {
                        IImConnection conn = ImApp.getConnection(imApp.getDefaultProviderId(), imApp.getDefaultAccountId());
                        if (conn != null && conn.getState() == ImConnection.LOGGED_IN) {
                            startGroupChat(group, users, conn);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }

            }
        }
    }

    private void updateData() {
        AppFuncs.log("updateData");
        final JsonObject jsonObject = AppFuncs.convertClassToJsonObject(wpKChatGroupTemp);
        RestAPI.PutDataWrappy(SettingConversationActivity.this, jsonObject, RestAPI.CHAT_GROUP, new RestAPIListener(SettingConversationActivity.this) {
            @Override
            public void OnComplete(String s) {
                if (!TextUtils.isEmpty(s)) {
                    AppFuncs.log(s);
                    wpKChatGroup = wpKChatGroupTemp;
                    if (wpKChatGroup.getIcon() != null) {
                        updateAvatarAndNotify(true);
                    }
                    updateGroupNameInDB(wpKChatGroup.getName(), wpKChatGroup.getXmppGroup() + "@" + Constant.DEFAULT_CONFERENCE_SERVER);
                    changeGroupNameXmpp();
                    PopupUtils.showCustomDialog(SettingConversationActivity.this,getString(R.string.success),getString(R.string.update_profile_success),R.string.ok,null);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateConversationDetail, jsonObject);
                }
            }

            @Override
            protected void onError(int errorCode) {
                super.onError(errorCode);
                if (wpKChatGroup.getIcon() != null) {
                    GlideHelper.loadBitmapToCircleImage(SettingConversationActivity.this, btnGroupPhoto, RestAPI.getAvatarUrl(wpKChatGroup.getIcon().getReference()));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edGroupName.setText(wpKChatGroup.getName());
                    }
                });
            }
        });
    }

    private void changeGroupNameXmpp() {
        new ChatSessionTask().modifyGroupName().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, wpKChatGroup.getXmppGroup() + "@" + Constant.DEFAULT_CONFERENCE_SERVER, wpKChatGroup.getName());
    }

    private int updateGroupNameInDB(String newGroupName, String oldGroupName) {
        Uri uri = Imps.Contacts.CONTENT_URI;
//        Imps.Chats.CONTENT_URI

        String selection = Imps.Contacts.USERNAME + "='" + oldGroupName + "'";

        ContentValues values = new ContentValues();
        values.put(Imps.Contacts.NICKNAME, newGroupName);

        int ret = getContentResolver().update(uri, values, selection, null);
        return ret;
    }

    private void updateAvatar() {
        updateAvatarAndNotify(true);
    }

    private void updateAvatarAndNotify(boolean broadcast) {
        String avatarReference = wpKChatGroup.getIcon().getReference();
        String hash = net.wrappy.im.ui.legacy.DatabaseUtils.generateHashFromAvatar(avatarReference);
        String address = wpKChatGroup.getXmppGroup() + "@" + Constant.DEFAULT_CONFERENCE_SERVER;
        net.wrappy.im.ui.legacy.DatabaseUtils.insertAvatarHash(ImApp.sImApp.getContentResolver(), Imps.Avatars.CONTENT_URI, ImApp.sImApp.getDefaultProviderId(), ImApp.sImApp.getDefaultAccountId(), avatarReference, hash, address);
        if (broadcast) {
            ImApp.broadcastIdentity(avatarReference + ":" + hash + ":" + address);
        }
    }

    private void clearHistory() {
        Imps.Messages.deleteOtrMessagesByThreadId(getContentResolver(), mLastChatId);
        Uri chatURI = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, mLastChatId);
        Imps.Chats.insertOrUpdateChat(getContentResolver(), chatURI, "", false);
        finish();
    }

    boolean isMuted() {
        try {
            return getSession() != null && getSession().isMuted();
        } catch (RemoteException re) {
            re.printStackTrace();
            return false;
        }
    }

    public void setMuted(boolean muted) {
        try {
            if (getSession() != null) {
                getSession().setMuted(muted);
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }

    public IChatSession getSession() {
        net.wrappy.im.util.Debug.d("mSession " + mSession);
        if (mSession == null)
            try {
                if (mConn.getState() == ImConnection.LOGGED_IN) {
                    mSession = mConn.getChatSessionManager().getChatSession(mAddress);
                    if (mSession == null)
                        mSession = mConn.getChatSessionManager().createChatSession(mAddress, true);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        return mSession;
    }

    private void searchActive() {
        Bundle bundle = new Bundle();
        bundle.putInt("type", ConversationDetailActivity.TYPE_SEARCH);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        this.setResult(Activity.RESULT_OK, intent);
        this.finish();
    }

    private void confirmDeleteGroup() {
        PopupUtils.showCustomDialog(this, getString(R.string.action_delete_group), getString(R.string.confirm_delete_and_leave_group), R.string.yes, R.string.no, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JsonObject jsonObject = AppFuncs.convertClassToJsonObject(wpKChatGroup);
                RestAPI.DeleteDataWrappy(getApplicationContext(), jsonObject, RestAPI.CHAT_GROUP, new RestAPIListener(SettingConversationActivity.this) {
                    @Override
                    public void OnComplete(String s) {
                        AppFuncs.alert(getApplicationContext(), R.string.setting_delete_and_leave_group, true);
                        deleteGroupByAdmin();
                    }
                });

            }
        }, null, false);
    }

    private void confirmLeaveGroup() {
        PopupUtils.showCustomDialog(this, getString(R.string.action_leave), getString(R.string.confirm_leave_group), R.string.yes, R.string.no, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveGroup();
            }
        }, null, false);
    }

    private void leaveGroup() {
        if (wpKChatGroup != null) {
            RestAPI.DeleteDataWrappy(this, new JsonObject(), String.format(RestAPI.DELETE_MEMBER_GROUP, wpKChatGroup.getId(),
                    Imps.Account.getUserName(getContentResolver(), mAccountId)), new RestAPIListener(SettingConversationActivity.this) {
                @Override
                public void OnComplete(String s) {
                    AppFuncs.log(s != null ? s : "");
                    leaveXmppGroup();
                }
            });
        }
    }

    private void leaveXmppGroup() {
        try {
            getSession().leave();
            //clear the stack and go back to the main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void deleteGroupByAdmin() {
        try {
            String groupName = wpKChatGroup.getName();

            String deleteCode = ConferenceConstant.DELETE_GROUP_BY_ADMIN + ":" + groupName;
            getSession().sendMessage(deleteCode, false);

            getSession().delete();

            //clear the stack and go back to the main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(ImApp.LOG_TAG, "error deleting group", e);
        }
    }
}
