package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import net.wrappy.im.model.Contact;
import net.wrappy.im.model.MemberGroupDisplay;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.ui.adapters.MemberGroupAdapter;
import net.wrappy.im.ui.background.BackgroundGridAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class SettingConversationActivity extends AppCompatActivity {
    @BindView(R.id.layout_search_setting)
    LinearLayout mSearchLayout;
    @BindView(R.id.switch_notification)
    Switch switch_notification;
    @BindView(R.id.layout_member_groups)
    LinearLayout mMemberGroupsLayout;
    @BindView(R.id.member_group_recycler_view)
    RecyclerView mGroupRecycleView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_conversation);

        ButterKnife.bind(this);

        // back button at action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getResources().getString(R.string.setting_screen));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_arrow_back);
        }
        Intent intent = getIntent();
        if (intent != null) {
//            mName = getIntent().getStringExtra("nickname");
            mAddress = getIntent().getStringExtra("address");
            mProviderId = getIntent().getLongExtra("provider", -1);
            mAccountId = getIntent().getLongExtra("account", -1);
            mLastChatId = getIntent().getLongExtra("chatId", -1);
            mContactType = getIntent().getIntExtra("isGroupChat", -1);
        }

        Cursor cursor = getContentResolver().query(Imps.ProviderSettings.CONTENT_URI, new String[]{Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE}, Imps.ProviderSettings.PROVIDER + "=?", new String[]{Long.toString(mProviderId)}, null);

        if (cursor == null)
            return; //not going to work
        Imps.ProviderSettings.QueryMap providerSettings = new Imps.ProviderSettings.QueryMap(
                cursor, getContentResolver(), mProviderId, false, null);
        mConn = ((ImApp) getApplication()).getConnection(mProviderId, mAccountId);
        mLocalAddress = Imps.Account.getUserName(getContentResolver(), mAccountId) + '@' + providerSettings.getDomain();

        try {
            mSession = mConn.getChatSessionManager().getChatSession(mAddress);
            if (getSession() != null) {
                mGroupOwner = getSession().getGroupChatOwner();
                if (mGroupOwner != null)
                    mIsOwner = mGroupOwner.getAddress().getBareAddress().equals(mLocalAddress);
            }
        } catch (RemoteException e) {
        }

        switch_notification.setChecked(!isMuted());

        // showing member group chat
        if (mContactType == Imps.Contacts.TYPE_GROUP) {
            mMemberGroupsLayout.setVisibility(View.VISIBLE);

            memberGroupDisplays = new ArrayList<>();

            mGroupRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            memberGroupAdapter = new MemberGroupAdapter(memberGroupDisplays, this);
            mGroupRecycleView.setAdapter(memberGroupAdapter);
        }
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

    @OnCheckedChanged(R.id.switch_notification)
    public void onCheckChangedNotification(CompoundButton p0, boolean p1) {
        setMuted(!p1);
    }

    @OnClick({R.id.layout_change_background_setting, R.id.layout_clean_setting, R.id.layout_leave_setting})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_change_background_setting:
                mBackgroundFragment = BackgroundBottomSheetFragment.getInstance();
                mBackgroundFragment.show(getSupportFragmentManager(), "Dialog");
                break;
            case R.id.layout_leave_setting:
                clearHistory();
                leaveGroup();
                break;
            case R.id.layout_clean_setting:
                int result = clearHistory();
                if (result > 0) {
                    finish();
                }
                break;
        }
    }

    private int clearHistory() {
        return Imps.Messages.deleteOtrMessagesByThreadId(getContentResolver(), mLastChatId);
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
            if (getSession() != null)
                getSession().setMuted(muted);
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }

    public IChatSession getSession() {
        net.wrappy.im.util.Debug.d("mSession " + mSession);
        return mSession;
    }

    private void leaveGroup() {
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
            }

        } catch (Exception e) {
            Log.e(ImApp.LOG_TAG, "error leaving group", e);
        }
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
                "backgrounds/page_1/chat_bg_thumb_1.png",
                "backgrounds/page_1/chat_bg_thumb_2.png",
                "backgrounds/page_1/chat_bg_thumb_3.png",
                "backgrounds/page_1/chat_bg_thumb_4.png",
                "backgrounds/page_1/chat_bg_thumb_5.png",
                "backgrounds/page_1/chat_bg_thumb_6.png",
                "backgrounds/page_1/chat_bg_thumb_7.png",
                "backgrounds/page_1/chat_bg_thumb_8.png",
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
