package net.wrappy.im.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.yalantis.ucrop.UCrop;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListenner;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.helper.layout.CircleImageView;
import net.wrappy.im.model.BottomSheetCell;
import net.wrappy.im.model.BottomSheetListener;
import net.wrappy.im.model.Contact;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.model.MemberGroupDisplay;
import net.wrappy.im.model.WpKAuthDto;
import net.wrappy.im.model.WpKChatGroupDto;
import net.wrappy.im.model.WpKIcon;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.tasks.GroupChatSessionTask;
import net.wrappy.im.ui.adapters.MemberGroupAdapter;
import net.wrappy.im.ui.conference.ConferenceConstant;
import net.wrappy.im.ui.conversation.BackgroundBottomSheetFragment;
import net.wrappy.im.util.BundleKeyConstant;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.PopupUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class SettingConversationActivity extends BaseActivity {
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
    @BindView(R.id.imgGroupPhoto)
    CircleImageView imgGroupPhoto;

    private String mAddress = null;
    private long mProviderId = -1;
    private long mAccountId = -1;
    private long mLastChatId = -1;
    private String mLocalAddress = null;
    private int mContactType = -1;

    private IImConnection mConn;
    private IChatSession mSession;
    private Contact mGroupOwner;
    private boolean mIsOwner = false;
    WpKChatGroupDto wpKChatGroup;

    private BackgroundBottomSheetFragment mBackgroundFragment;

    private MemberGroupAdapter memberGroupAdapter;
    private ArrayList<MemberGroupDisplay> memberGroupDisplays;
    private Thread mThreadUpdate;
    private String mAdminGroup;

    private final static int REQUEST_PICK_CONTACT = 100;

    public final static int PICKER_ADD_MEMBER = 1;

    private WpKChatGroupDto groupid;
    ImApp imApp;

    private List<WpKMemberDto> identifiers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting_conversation);
        super.onCreate(savedInstanceState);

        imApp = (ImApp) getApplication();
        // back button at action bar
        getSupportActionBar().setTitle(getResources().getString(R.string.setting_screen));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        if (intent != null) {
//            mName = getIntent().getStringExtra("nickname");
            mAddress = getIntent().getStringExtra("address");
            mProviderId = getIntent().getLongExtra("provider", -1);
            mAccountId = getIntent().getLongExtra("account", -1);
            mLastChatId = getIntent().getLongExtra("chatId", -1);
            mContactType = getIntent().getIntExtra("isGroupChat", -1);
            groupid = getIntent().getParcelableExtra("groupid");
        }

        Cursor cursor = getContentResolver().query(Imps.ProviderSettings.CONTENT_URI, new String[]{Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE}, Imps.ProviderSettings.PROVIDER + "=?", new String[]{Long.toString(mProviderId)}, null);
//        AppFuncs.log(DatabaseUtils.dumpCursorToString(cursor));
        if (cursor == null)
            return; //not going to work
        try {
            mConn = ImApp.getConnection(mProviderId, mAccountId);
            if (mConn.getState() == ImConnection.LOGGED_IN) {
                mLocalAddress = Imps.Account.getUserName(getContentResolver(), mAccountId);


                mSession = mConn.getChatSessionManager().getChatSession(mAddress);

                if (mSession != null) {
                    mGroupOwner = mSession.getGroupChatOwner();
                    if (mGroupOwner != null) {
                        mIsOwner = mGroupOwner.getAddress().getBareAddress().equals(mLocalAddress);
                        mAdminGroup = mGroupOwner.getName();
                        if (!mIsOwner) {
                            btnEditGroupName.setVisibility(View.GONE);
                            imgGroupPhoto.setVisibility(View.GONE);
                            btnGroupPhoto.setEnabled(false);
                        }
                    } else {
                        btnEditGroupName.setVisibility(View.GONE);
                        imgGroupPhoto.setVisibility(View.GONE);
                        btnGroupPhoto.setEnabled(false);
                    }
                }
            } else {
                finish();
                return;
            }
        } catch (RemoteException e) {
            AppFuncs.log(e.getLocalizedMessage());
        }

        switch_notification.setChecked(!isMuted());

        // showing member group chat
        if (mContactType == Imps.Contacts.TYPE_GROUP) {
            String groupXmppId = mAddress;
            if (mAddress.contains("@")) {
                groupXmppId = mAddress.split("@")[0];
            }
            RestAPI.GetDataWrappy(getApplicationContext(), RestAPI.getGroupByXmppId(groupXmppId), new RestAPIListenner() {
                @Override
                public void OnComplete(int httpCode, String error, String s) {
                    try {
                        Gson gson = new Gson();
                        wpKChatGroup = gson.fromJson(s, new TypeToken<WpKChatGroupDto>() {
                        }.getType());
                        memberGroupAdapter.setmWpKChatGroupDto(wpKChatGroup);
                        identifiers = wpKChatGroup.getParticipators();

                        edGroupName.setText(wpKChatGroup.getName());
                        if (wpKChatGroup.getIcon() != null) {
                            GlideHelper.loadBitmapToCircleImage(getApplicationContext(), btnGroupPhoto, RestAPI.getAvatarUrl(wpKChatGroup.getIcon().getReference()));
                            updateAvatar();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            edGroupName.setText(Imps.Contacts.getNicknameFromAddress(getContentResolver(), mAddress));
            String avatar = Imps.Avatars.getAvatar(getContentResolver(), mAddress);
            GlideHelper.loadBitmapToImageView(getApplicationContext(), btnGroupPhoto, RestAPI.getAvatarUrl(avatar));
            edGroupName.setText(Imps.Contacts.getNicknameFromAddress(getContentResolver(), mAddress));
            mMemberGroupsLayout.setVisibility(View.VISIBLE);

            if (mIsOwner) {
                mAddMemberLayout.setVisibility(View.VISIBLE);
                mViewDivider.setVisibility(View.VISIBLE);
                mAdminDeleteGroup.setVisibility(View.VISIBLE);
                mMemberLeaveGroup.setVisibility(View.GONE);
            }

            memberGroupDisplays = new ArrayList<>();

            String currentUser = Imps.Account.getUserName(getContentResolver(), mAccountId);

            mGroupRecycleView.setNestedScrollingEnabled(false);
            mGroupRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            memberGroupAdapter = new MemberGroupAdapter(this, memberGroupDisplays, currentUser, mAdminGroup, mLastChatId, mSession);
            mGroupRecycleView.setAdapter(memberGroupAdapter);

            updateMembers();
        } else {
            lnAvatarOfGroup.setVisibility(View.GONE);
        }
    }

    private void updateMembers() {
        if (mThreadUpdate != null) {
            mThreadUpdate.interrupt();
            mThreadUpdate = null;
        }
        mThreadUpdate = new Thread(new Runnable() {
            @Override
            public void run() {

                ArrayList<MemberGroupDisplay> members = new ArrayList<>();

                String[] projection = {Imps.GroupMembers.USERNAME, Imps.GroupMembers.NICKNAME,
                        Imps.GroupMembers.ROLE, Imps.GroupMembers.AFFILIATION};
                Uri memberUri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, mLastChatId);
                ContentResolver cr = getContentResolver();
                Cursor c = cr.query(memberUri, projection, null, null, null);

                if (c != null) {
                    int colUsername = c.getColumnIndex(Imps.GroupMembers.USERNAME);
                    int colNickname = c.getColumnIndex(Imps.GroupMembers.NICKNAME);
                    int colRole = c.getColumnIndex(Imps.GroupMembers.ROLE);
                    int colAffiliation = c.getColumnIndex(Imps.GroupMembers.AFFILIATION);

                    while (c.moveToNext()) {
                        MemberGroupDisplay member = new MemberGroupDisplay();
                        member.setUsername(new XmppAddress(c.getString(colUsername)).getBareAddress());
                        member.setNickname(c.getString(colNickname));
                        member.setRole(c.getString(colRole));
                        member.setEmail(ImApp.getEmail(member.getUsername()));
                        member.setAffiliation(c.getString(colAffiliation));

                        Debug.e("username: " + member.getUsername());

                        if (member.getAffiliation() != null) {
                            if (member.getAffiliation().contentEquals("owner") ||
                                    member.getAffiliation().contentEquals("admin")) {
                                if (member.getUsername().equals(mLocalAddress)) {
                                    mIsOwner = true;
                                }
                            }
                        }

                        if (member.getNickname() == null || member.getUsername().contains(member.getNickname())) {
                            for (WpKMemberDto memberDto : identifiers) {
                                String account = memberDto.getXMPPAuthDto().getAccount();
                                if (member.getUsername().contains(account)) {
                                    member.setNickname(memberDto.getIdentifier());
                                    Imps.GroupMembers.updateNicknameFromGroup(getContentResolver(), member.getUsername(), memberDto.getIdentifier());
                                    break;
                                }
                            }
                        }
                        members.add(member);

                    }
                    c.close();
                }
                memberGroupDisplays.clear();
                memberGroupDisplays.addAll(members);

                if (!Thread.currentThread().isInterrupted()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            memberGroupAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        mThreadUpdate.start();
    }

    private void updateUnknownFriendInfoInGroup(final MemberGroupDisplay member) {
        RestAPI.GetDataWrappy(ImApp.sImApp, String.format(RestAPI.GET_MEMBER_INFO_BY_JID, new XmppAddress(member.getUsername()).getUser()), new RestAPIListenner() {
            @Override
            protected void OnComplete(int httpCode, String error, String s) {
                try {
                    Debug.e("s: " + s);
                    if (s != null) {
                        WpKMemberDto wpKMemberDto = new Gson().fromJson(s, new TypeToken<WpKMemberDto>() {
                        }.getType());
                        if (wpKMemberDto != null) {
                            Imps.GroupMembers.updateNicknameFromGroup(getContentResolver(), member.getUsername(), wpKMemberDto.getIdentifier());
                            updateMembers();
                        }
                    }
                } catch (IllegalStateException | JsonSyntaxException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnCheckedChanged({R.id.switch_notification})
    public void onCheckChanged(CompoundButton buttonView, boolean isChecked) {
        setMuted(!isChecked);
    }

    @OnClick({R.id.btnGroupPhoto, R.id.btnGroupNameClose, R.id.btnGroupNameCheck, R.id.btnEditGroupName, R.id.layout_search_setting, R.id.layout_change_background_setting, R.id.layout_clean_setting, R.id.layout_admin_delete_group, R.id.layout_add_member,
            R.id.layout_member_leave_group})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_search_setting:
                searchActive();
                break;
            case R.id.layout_change_background_setting:
                mBackgroundFragment = BackgroundBottomSheetFragment.getInstance();
                mBackgroundFragment.show(getSupportFragmentManager(), "Dialog");
                break;
            case R.id.layout_admin_delete_group:
                confirmDeleteGroup();
                break;
            case R.id.layout_member_leave_group:
                confirmLeaveGroup();
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
                            AppFuncs.openCamera(SettingConversationActivity.this, 100);
                        } else {
                            AppFuncs.openGallery(SettingConversationActivity.this, 100);
                        }
                    }
                }).show();
                break;
            case R.id.btnEditGroupName:
                edGroupName.setEnabled(true);
                edGroupName.setFocusable(true);
                lnChangeGroupNameController.setVisibility(View.VISIBLE);
                btnEditGroupName.setVisibility(View.GONE);
                break;
            case R.id.btnGroupNameCheck:
                String name = edGroupName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    return;
                }
                edGroupName.setText(name);
                wpKChatGroup.setName(name);
                updateData();
                edGroupName.setFocusable(false);
                edGroupName.setEnabled(false);
                lnChangeGroupNameController.setVisibility(View.GONE);
                btnEditGroupName.setVisibility(View.VISIBLE);
                break;
            case R.id.btnGroupNameClose:
                edGroupName.setText(wpKChatGroup.getName());
                edGroupName.setFocusable(false);
                edGroupName.setEnabled(false);
                lnChangeGroupNameController.setVisibility(View.GONE);
                btnEditGroupName.setVisibility(View.VISIBLE);
                break;
            case R.id.layout_add_member:
                Intent intent = new Intent(SettingConversationActivity.this, ContactsPickerActivity.class);
                ArrayList<String> usernames = new ArrayList<>(memberGroupDisplays.size());
                for (MemberGroupDisplay member : memberGroupDisplays) {
                    usernames.add(member.getNickname());
                }
                intent.putExtra(BundleKeyConstant.EXTRA_LIST_MEMBER, usernames);
                intent.putExtra(BundleKeyConstant.EXTRA_GROUP_ID, groupid);
                intent.putExtra("type", PICKER_ADD_MEMBER);
                intent.putExtra(BundleKeyConstant.EXTRA_EXCLUDED_CONTACTS, true);

                startActivityForResult(intent, REQUEST_PICK_CONTACT);
                break;
        }
    }

    private void startGroupChat(WpKChatGroupDto group, ArrayList<String> invitees, IImConnection conn) {
        String chatServer = ""; //use the default
        String nickname = imApp.getDefaultUsername().split("@")[0];
        new GroupChatSessionTask(this, group, invitees, conn).executeOnExecutor(ImApp.sThreadPoolExecutor, chatServer, nickname);
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
                    try {
                        IImConnection conn = imApp.getConnection(imApp.getDefaultProviderId(), imApp.getDefaultAccountId());
                        if (conn.getState() == ImConnection.LOGGED_IN) {
                            startGroupChat(group, users, conn);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }

            } else if (requestCode == 100) {
                AppFuncs.cropImage(this, data, true);
            } else if (requestCode == UCrop.REQUEST_CROP) {
                Uri uri = UCrop.getOutput(data);
                btnGroupPhoto.setImageURI(uri);
                RestAPI.uploadFile(getApplicationContext(), new File(uri.getPath()), RestAPI.PHOTO_AVATAR).setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        try {
                            AppFuncs.log(result.getResult());
                            final String reference = RestAPI.getPhotoReference(result.getResult());
                            WpKIcon wpKIcon = new WpKIcon();
                            wpKIcon.setReference(reference);
                            wpKChatGroup.setIcon(wpKIcon);
                            updateData();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

            }
        }

    }

    private void updateData() {
        JsonObject jsonObject = AppFuncs.convertClassToJsonObject(wpKChatGroup);
        RestAPI.PutDataWrappy(getApplicationContext(), jsonObject, RestAPI.CHAT_GROUP, new RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                if (!TextUtils.isEmpty(s)) {
                    AppFuncs.log(s);
                    if (RestAPI.checkHttpCode(httpCode)) {
                        updateAvatarAndNotify(true);
                        AppFuncs.alert(getApplicationContext(), "Update Success", false);
                    }
                }
            }
        });
    }

    private void updateAvatar() {
        updateAvatarAndNotify(false);
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
            if (getSession() != null)
                return getSession().isMuted();
            else
                return false;
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
                RestAPI.DeleteDataWrappy(getApplicationContext(), jsonObject, RestAPI.CHAT_GROUP, new RestAPIListenner() {
                    @Override
                    public void OnComplete(int httpCode, String error, String s) {
                        if (RestAPI.checkHttpCode(httpCode)) {
                            AppFuncs.alert(getApplicationContext(), "Delete and leave group", true);
                            deleteGroupByAdmin();
                        }
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
                    Imps.Account.getAccountName(getContentResolver(), ImApp.sImApp.getDefaultAccountId())), new RestAPIListenner() {
                @Override
                public void OnComplete(int httpCode, String error, String s) {
                    if (RestAPI.checkHttpCode(httpCode)) {
                        AppFuncs.log(s != null ? s : "");
                        leaveXmppGroup();
                    }
                }
            });
        }
    }

    private boolean leaveXmppGroup() {
        try {
            getSession().leave();
            //clear the stack and go back to the main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean deleteGroupByAdmin() {
        try {

            String groupName = wpKChatGroup.getName();

            StringBuffer deleteCode = new StringBuffer();
            deleteCode.append(ConferenceConstant.DELETE_GROUP_BY_ADMIN);
            deleteCode.append(":");
            deleteCode.append(groupName);
            getSession().sendMessage(deleteCode.toString(), false);

            getSession().delete();

            //clear the stack and go back to the main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } catch (Exception e) {
            Log.e(ImApp.LOG_TAG, "error deleting group", e);
        }
        return false;
    }

}
