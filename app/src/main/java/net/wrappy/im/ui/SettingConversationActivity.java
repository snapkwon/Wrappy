package net.wrappy.im.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Switch;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.model.Contact;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.model.MemberGroupDisplay;
import net.wrappy.im.model.WpKChatGroupDto;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.tasks.GroupChatSessionTask;
import net.wrappy.im.ui.adapters.MemberGroupAdapter;
import net.wrappy.im.ui.background.BackgroundGridAdapter;
import net.wrappy.im.util.BundleKeyConstant;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class SettingConversationActivity extends BaseActivity {
    @BindView(R.id.layout_search_setting)
    LinearLayout mSearchLayout;
    @BindView(R.id.switch_notification)
    Switch switch_notification;
    @BindView(R.id.layout_member_groups)
    LinearLayout mMemberGroupsLayout;
    @BindView(R.id.layout_leave_setting)
    LinearLayout layout_leave_setting;
    @BindView(R.id.layout_add_member)
    LinearLayout mAddMemberLayout;
    @BindView(R.id.member_group_recycler_view)
    RecyclerView mGroupRecycleView;
    @BindView(R.id.view_divider)
    View mViewDivider;

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

    private BackgroundBottomSheetFragment mBackgroundFragment;

    private MemberGroupAdapter memberGroupAdapter;
    private ArrayList<MemberGroupDisplay> memberGroupDisplays;
    private Thread mThreadUpdate;

    private final static int REQUEST_PICK_CONTACT = 100;

    public final static int PICKER_ADD_MEMBER = 1;

    private WpKChatGroupDto groupid;
    ImApp imApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting_conversation);
        super.onCreate(savedInstanceState);

        imApp = (ImApp) getApplication();
        // back button at action bar
        getSupportActionBar().setTitle(getResources().getString(R.string.setting_screen));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        getSupportActionBar().setTitle("Change Security Question");
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

        if (cursor == null)
            return; //not going to work
        Imps.ProviderSettings.QueryMap providerSettings = new Imps.ProviderSettings.QueryMap(
                cursor, getContentResolver(), mProviderId, false, null);
        try {
            mConn = ImApp.getConnection(mProviderId, mAccountId);
            mLocalAddress = Imps.Account.getUserName(getContentResolver(), mAccountId) + '@' + providerSettings.getDomain();


            mSession = mConn.getChatSessionManager().getChatSession(mAddress);

            if (mSession != null) {
                mGroupOwner = mSession.getGroupChatOwner();
                if (mGroupOwner != null)
                    mIsOwner = mGroupOwner.getAddress().getBareAddress().equals(mLocalAddress);
                net.wrappy.im.util.Debug.e("mIsOwner: " + mIsOwner);
            }
        } catch (RemoteException e) {
        }

        switch_notification.setChecked(!isMuted());

        // showing member group chat
        if (mContactType == Imps.Contacts.TYPE_GROUP) {
            mMemberGroupsLayout.setVisibility(View.VISIBLE);
            layout_leave_setting.setVisibility(View.GONE);

            if (mIsOwner) {
                mAddMemberLayout.setVisibility(View.VISIBLE);
                mViewDivider.setVisibility(View.VISIBLE);
            }

            memberGroupDisplays = new ArrayList<>();

            mGroupRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            memberGroupAdapter = new MemberGroupAdapter(this, memberGroupDisplays);
            mGroupRecycleView.setAdapter(memberGroupAdapter);

            updateMembers();
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

                        if (member.getAffiliation() != null) {
                            if (member.getAffiliation().contentEquals("owner") ||
                                    member.getAffiliation().contentEquals("admin")) {
                                    if (member.getUsername().equals(mLocalAddress)) {
                                        mIsOwner = true;
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
                            mGroupRecycleView.getAdapter().notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        mThreadUpdate.start();
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

    @OnClick({R.id.layout_search_setting, R.id.layout_change_background_setting, R.id.layout_clean_setting, R.id.layout_leave_setting, R.id.layout_add_member})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_search_setting:
                searchActive();
                break;
            case R.id.layout_change_background_setting:
                mBackgroundFragment = BackgroundBottomSheetFragment.getInstance();
                mBackgroundFragment.show(getSupportFragmentManager(), "Dialog");
                break;
            case R.id.layout_leave_setting:
                if (leaveGroup())
                    clearHistory();
                else AppFuncs.alert(this, "Could not leave group", true);
                break;
            case R.id.layout_clean_setting:
                clearHistory();
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
                intent.putExtra(BundleKeyConstant.EXTRA_EXCLUDED_CONTACTS ,true );

                startActivityForResult(intent, REQUEST_PICK_CONTACT);
                break;
        }
    }

    private void startGroupChat(WpKChatGroupDto group, ArrayList<String> invitees, IImConnection conn) {
        String chatServer = ""; //use the default
        String nickname = imApp.getDefaultUsername().split("@")[0];
        new GroupChatSessionTask(this, group, invitees, conn).executeOnExecutor(ImApp.sThreadPoolExecutor, chatServer, nickname);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

            }
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
                mSession = mConn.getChatSessionManager().getChatSession(mAddress);
                if (mSession == null)
                    mSession = mConn.getChatSessionManager().createChatSession(mAddress, true);
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

    private boolean leaveGroup() {
        try {
            IChatSessionManager manager = mConn.getChatSessionManager();
            IChatSession session = manager.getChatSession(mAddress);

            if (session == null)
                session = manager.createChatSession(mAddress, true);

            if (session != null) {
                session.leave();

                //clear the stack and go back to the main activity
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }

        } catch (Exception e) {
            Log.e(ImApp.LOG_TAG, "error leaving group", e);
        }
        return false;
    }

    /**
     * Showing bottom sheet to change background
     */
    public static class BackgroundBottomSheetFragment extends BottomSheetDialogFragment {
        @BindView(R.id.backgroundGridView)
        GridView mGridView;

        public int[] mThumbIds = {
                R.drawable.chat_bg_thumb_1,
                R.drawable.chat_bg_thumb_2,
                R.drawable.chat_bg_thumb_3,
                R.drawable.chat_bg_thumb_4,
                R.drawable.chat_bg_thumb_5,
                R.drawable.chat_bg_thumb_6,
                R.drawable.chat_bg_thumb_7,
                R.drawable.chat_bg_thumb_8
        };

        public String[] mImagePath = {
                "backgrounds/page_1/chat_bg_1.png",
                "backgrounds/page_1/chat_bg_2.png",
                "backgrounds/page_1/chat_bg_3.png",
                "backgrounds/page_1/chat_bg_4.png",
                "backgrounds/page_1/chat_bg_5.png",
                "backgrounds/page_1/chat_bg_6.png",
                "backgrounds/page_1/chat_bg_7.png",
                "backgrounds/page_1/chat_bg_8.png",
        };

        public static final BackgroundBottomSheetFragment getInstance() {

            BackgroundBottomSheetFragment backgroundFragment = new BackgroundBottomSheetFragment();

            return backgroundFragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.background_grid, container, false);

            ButterKnife.bind(this, view);

            mGridView.setAdapter(new BackgroundGridAdapter(getContext(), mThumbIds));

            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    Bundle bundle = new Bundle();
                    bundle.putInt("type", ConversationDetailActivity.TYPE_REQUEST_CHANGE_BACKGROUND);
                    bundle.putString("imagePath", mImagePath[i]);

                    Intent intent = new Intent();
                    intent.putExtras(bundle);

                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                }
            });

            return view;
        }
    }
}
