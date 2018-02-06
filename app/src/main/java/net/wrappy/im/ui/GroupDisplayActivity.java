package net.wrappy.im.ui;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.model.Contact;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatListener;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IContactListManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.ui.legacy.DatabaseUtils;
import net.wrappy.im.ui.legacy.adapter.ChatListenerAdapter;
import net.wrappy.im.ui.onboarding.OnboardingManager;
import net.wrappy.im.ui.qr.QrShareAsyncTask;
import net.wrappy.im.ui.widgets.GroupAvatar;
import net.wrappy.im.ui.widgets.LetterAvatar;
import net.wrappy.im.util.BundleKeyConstant;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.PopupUtils;

import org.apache.commons.codec.DecoderException;

import java.util.ArrayList;

public class GroupDisplayActivity extends BaseActivity {

    private String mName = null;
    private String mAddress = null;
    private long mProviderId = -1;
    private long mAccountId = -1;
    private long mLastChatId = -1;
    private String mLocalAddress = null;

    private IImConnection mConn;
    private IChatSession mSession;
    private Contact mGroupOwner;
    private boolean mIsOwner = false;
    private Thread mThreadUpdate;

    private class GroupMemberDisplay {
        public String username;
        public String nickname;
        public String email;
        public String role;
        public String affiliation;
        public Drawable avatar;
        public boolean online = false;
    }

    private RecyclerView mRecyclerView;
    private ArrayList<GroupMemberDisplay> mMembers;
    private View mActionAddFriends = null;

    private final static int REQUEST_PICK_CONTACTS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.awesome_activity_group);

        mName = getIntent().getStringExtra("nickname");
        mAddress = getIntent().getStringExtra("address");
        mProviderId = getIntent().getLongExtra("provider", -1);
        mAccountId = getIntent().getLongExtra("account", -1);
        mLastChatId = getIntent().getLongExtra("chat", -1);

        Cursor cursor = getContentResolver().query(Imps.ProviderSettings.CONTENT_URI, new String[]{Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE}, Imps.ProviderSettings.PROVIDER + "=?", new String[]{Long.toString(mProviderId)}, null);

        if (cursor == null)
            return; //not going to work

        Imps.ProviderSettings.QueryMap providerSettings = new Imps.ProviderSettings.QueryMap(
                cursor, getContentResolver(), mProviderId, false, null);

        mMembers = new ArrayList<>();
        mConn = ((ImApp) getApplication()).getConnection(mProviderId, mAccountId);
        mLocalAddress = Imps.Account.getUserName(getContentResolver(), mAccountId) + '@' + providerSettings.getDomain();

        providerSettings.close();

        try {
            mSession = mConn.getChatSessionManager().getChatSession(mAddress);
            if (mSession != null) {
                mGroupOwner = mSession.getGroupChatOwner();
                if (mGroupOwner != null)
                    mIsOwner = mGroupOwner.getAddress().getBareAddress().equals(mLocalAddress);
            }
        } catch (RemoteException e) {
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.rvRoot);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new RecyclerView.Adapter() {

            private static final int VIEW_TYPE_MEMBER = 0;
            private static final int VIEW_TYPE_HEADER = 1;
            private static final int VIEW_TYPE_FOOTER = 2;

            private int colorTextPrimary = 0xff000000;

            public RecyclerView.Adapter init() {
                TypedValue out = new TypedValue();
                getTheme().resolveAttribute(R.attr.contactTextPrimary, out, true);
                colorTextPrimary = out.data;
                return this;
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                switch (viewType) {
                    case VIEW_TYPE_HEADER:
                        return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.awesome_activity_group_header, parent, false));
                    case VIEW_TYPE_FOOTER:
                        return new FooterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.awesome_activity_group_footer, parent, false));
                }
                return new MemberViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.group_member_view, parent, false));
            }

            @Override
            public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof HeaderViewHolder) {
                    HeaderViewHolder h = (HeaderViewHolder) holder;
                    GroupAvatar avatar = new GroupAvatar(mAddress.split("@")[0]);
                    avatar.setRounded(false);
                    h.avatar.setImageDrawable(avatar);

                    h.qr.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String inviteString;
                            try {
                                inviteString = OnboardingManager.generateInviteLink(GroupDisplayActivity.this, mAddress, "", mName);
                                OnboardingManager.inviteScan(GroupDisplayActivity.this, inviteString);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    h.groupName.setText(mName);
                    h.groupAddress.setText(mAddress);

                    h.actionShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                String inviteLink = OnboardingManager.generateInviteLink(GroupDisplayActivity.this, mAddress, "", mName);
                                new QrShareAsyncTask(GroupDisplayActivity.this).execute(inviteLink, mName);
                            } catch (Exception e) {
                                Log.e(ImApp.LOG_TAG, "couldn't generate QR code", e);
                            }
                        }
                    });

                    mActionAddFriends = h.actionAddFriends;
                    showAddFriends();

                    h.actionMute.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setMuted(!isMuted());
                            mRecyclerView.getAdapter().notifyItemChanged(holder.getAdapterPosition());
                        }
                    });
                    boolean muted = isMuted();
                    h.actionMute.setText(muted ? R.string.turn_notifications_on : R.string.turn_notifications_off);
                    //       h.actionMute.setText(muted ? "turn on" : " t")
                    TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(h.actionMute,
                            muted ? R.drawable.ic_notifications_active_black_24dp : R.drawable.ic_notifications_off_black_24dp,
                            0, 0, 0);

                    if (!mIsOwner)
                        h.editGroupName.setVisibility(View.GONE);
                    else {
                        h.editGroupName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                editGroupSubject();
                            }
                        });
                    }
                } else if (holder instanceof FooterViewHolder) {
                    FooterViewHolder h = (FooterViewHolder) holder;

                    // Tint the "leave" text and drawable(s)
                    int colorAccent = ResourcesCompat.getColor(getResources(), R.color.holo_orange_light, getTheme());
                    for (Drawable d : h.actionLeave.getCompoundDrawables()) {
                        if (d != null) {
                            DrawableCompat.setTint(d, colorAccent);
                        }
                    }
                    h.actionLeave.setTextColor(colorAccent);
                    h.actionLeave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmLeaveGroup();
                        }
                    });
                } else if (holder instanceof MemberViewHolder) {
                    MemberViewHolder h = (MemberViewHolder) holder;

                    // Reset the padding to match other views in this hierarchy
                    //
                    int padding = getResources().getDimensionPixelOffset(R.dimen.detail_view_padding);
                    h.itemView.setPadding(padding, h.itemView.getPaddingTop(), padding, h.itemView.getPaddingBottom());

                    int idxMember = position - 1;
                    final GroupMemberDisplay member = mMembers.get(idxMember);

                    String nickname = member.nickname;
                    h.line1.setText(nickname);

                    boolean hasRoleNone = TextUtils.isEmpty(member.role) || "none".equalsIgnoreCase(member.role);
                    h.line1.setTextColor(hasRoleNone ? Color.GRAY : colorTextPrimary);
                    h.line2.setText(member.email);
                    if (member.affiliation != null && (member.affiliation.contentEquals("owner") || member.affiliation.contentEquals("admin"))) {
                        h.avatarCrown.setVisibility(View.VISIBLE);
                    } else {
                        h.avatarCrown.setVisibility(View.GONE);
                    }

                    /**
                     if (!member.online)
                     {
                     h.line1.setEnabled(false);
                     h.line2.setEnabled(false);
                     h.avatar.setBackgroundColor(getResources().getColor(R.color.holo_grey_light));
                     }**/

                    //h.line2.setText(member.username);
                    if (member.avatar == null && !TextUtils.isEmpty(nickname)) {
                        padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
                        member.avatar = new LetterAvatar(holder.itemView.getContext(), nickname, padding);
                    }
                    h.avatar.setImageDrawable(member.avatar);
                    h.avatar.setVisibility(View.VISIBLE);
                    h.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showMemberInfo(member);
                        }
                    });
                }
            }

            @Override
            public int getItemCount() {
                return 2 + mMembers.size();
            }

            @Override
            public int getItemViewType(int position) {
                if (position == 0)
                    return VIEW_TYPE_HEADER;
                else if (position == getItemCount() - 1)
                    return VIEW_TYPE_FOOTER;
                return VIEW_TYPE_MEMBER;
            }
        }.init());
        updateMembers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSession != null) {
            try {
                mSession.registerChatListener(mChatListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        updateMembers();
    }

    @Override
    protected void onPause() {
        if (mSession != null) {
            try {
                mSession.unregisterChatListener(mChatListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        super.onPause();
    }

    private void updateMembers() {
        if (mThreadUpdate != null) {
            mThreadUpdate.interrupt();
            mThreadUpdate = null;
        }
        mThreadUpdate = new Thread(new Runnable() {
            @Override
            public void run() {

                final ArrayList<GroupMemberDisplay> members = new ArrayList<>();

                IContactListManager contactManager = null;

                try {
                    if (mConn != null) {
                        contactManager = mConn.getContactListManager();
                    }
                } catch (RemoteException re) {
                }

                String[] projection = {Imps.GroupMembers.USERNAME, Imps.GroupMembers.NICKNAME, Imps.GroupMembers.ROLE, Imps.GroupMembers.AFFILIATION};
                Uri memberUri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, mLastChatId);
                ContentResolver cr = getContentResolver();
                Cursor c = cr.query(memberUri, projection, null, null, null);
                if (c != null) {
                    int colUsername = c.getColumnIndex(Imps.GroupMembers.USERNAME);
                    int colNickname = c.getColumnIndex(Imps.GroupMembers.NICKNAME);
                    int colRole = c.getColumnIndex(Imps.GroupMembers.ROLE);
                    int colAffiliation = c.getColumnIndex(Imps.GroupMembers.AFFILIATION);

                    while (c.moveToNext()) {
                        GroupMemberDisplay member = new GroupMemberDisplay();
                        member.username = new XmppAddress(c.getString(colUsername)).getBareAddress();
                        member.nickname = c.getString(colNickname);
                        member.role = c.getString(colRole);
                        member.affiliation = c.getString(colAffiliation);
                        member.email = ImApp.getEmail(member.username);
                        try {
                            member.avatar = DatabaseUtils.getAvatarFromAddress(cr, member.username, ImApp.SMALL_AVATAR_WIDTH, ImApp.SMALL_AVATAR_HEIGHT);
                        } catch (DecoderException e) {
                            e.printStackTrace();
                        }

                        if (member.affiliation != null) {
                            if (member.affiliation.contentEquals("owner") || member.affiliation.contentEquals("admin")) {
                                if (member.username.equals(mLocalAddress))
                                    mIsOwner = true;
                            }
                        }


                        /**
                         try {
                         if (contactManager != null) {
                         Contact contact = contactManager.getContactByAddress(member.username);
                         if (contact != null)
                         member.online = contact.getPresence().isOnline();
                         }
                         }
                         catch (RemoteException re){}**/


                        if (member.nickname == null || member.username.contains(member.nickname)) {
                            updateUnknownFriendInfoInGroup(member);
                        } else {
                            members.add(member);
                        }
                    }
                    c.close();
                }
                mMembers.clear();
                mMembers.addAll(members);
                if (!Thread.currentThread().isInterrupted()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.getAdapter().notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        mThreadUpdate.start();
    }

    private void updateUnknownFriendInfoInGroup(final GroupMemberDisplay member) {
        RestAPI.GetDataWrappy(ImApp.sImApp, RestAPI.getMemberByIdUrl(member.nickname), new RestAPIListener() {
            @Override
            public void OnComplete(String s) {
                Debug.d(s);
                try {
                    WpKMemberDto wpKMemberDtos = new Gson().fromJson(s, new TypeToken<WpKMemberDto>() {
                    }.getType());
                    Imps.GroupMembers.updateNicknameFromGroup(getContentResolver(), member.username, wpKMemberDtos.getIdentifier());
                    updateMembers();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void inviteContacts(ArrayList<String> invitees) {
        if (mConn == null)
            return;

        try {
            IChatSessionManager manager = mConn.getChatSessionManager();
            IChatSession session = manager.getChatSession(mAddress);

            for (String invitee : invitees) {
                session.inviteContact(invitee);
                GroupMemberDisplay member = new GroupMemberDisplay();
                XmppAddress address = new XmppAddress(invitee);
                member.username = address.getBareAddress();
                member.nickname = address.getUser();
                try {
                    member.avatar = DatabaseUtils.getAvatarFromAddress(getContentResolver(), member.username, ImApp.SMALL_AVATAR_WIDTH, ImApp.SMALL_AVATAR_HEIGHT);
                } catch (DecoderException e) {
                    e.printStackTrace();
                }
                mMembers.add(member);
            }

            mRecyclerView.getAdapter().notifyDataSetChanged();

        } catch (Exception e) {
            Log.e(ImApp.LOG_TAG, "error inviting contacts to group", e);
        }

    }

    public void showMemberInfo(GroupMemberDisplay member) {
        Intent intent = new Intent(this, ContactDisplayActivity.class);
        intent.putExtra("address", member.username);
        intent.putExtra("nickname", member.nickname);
        intent.putExtra("provider", mProviderId);
        intent.putExtra("account", mAccountId);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {

        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_PICK_CONTACTS) {

                ArrayList<String> invitees = new ArrayList<String>();

                String username = resultIntent.getStringExtra(BundleKeyConstant.RESULT_KEY);

                if (username != null)
                    invitees.add(username);
                else
                    invitees = resultIntent.getStringArrayListExtra(BundleKeyConstant.EXTRA_RESULT_USERNAMES);

                inviteContacts(invitees);

            }
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        final ImageView avatar;
        final ImageView qr;
        final TextView groupName;
        final View editGroupName;
        final TextView groupAddress;
        final TextView actionShare;
        final TextView actionAddFriends;
        final TextView actionMute;

        HeaderViewHolder(View view) {
            super(view);
            avatar = (ImageView) view.findViewById(R.id.ivAvatar);
            qr = (ImageView) view.findViewById(R.id.qrcode);
            groupName = (TextView) view.findViewById(R.id.tvGroupName);
            editGroupName = view.findViewById(R.id.edit_group_subject);
            groupAddress = (TextView) view.findViewById(R.id.tvGroupAddress);
            actionShare = (TextView) view.findViewById(R.id.tvActionShare);
            actionAddFriends = (TextView) view.findViewById(R.id.tvActionAddFriends);
            actionMute = (TextView) view.findViewById(R.id.tvActionMute);
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {
        final TextView actionLeave;

        FooterViewHolder(View view) {
            super(view);
            actionLeave = (TextView) view.findViewById(R.id.tvActionLeave);
        }
    }

    private class MemberViewHolder extends RecyclerView.ViewHolder {
        final TextView line1;
        final TextView line2;
        final ImageView avatar;
        final ImageView avatarCrown;

        MemberViewHolder(View view) {
            super(view);
            line1 = (TextView) view.findViewById(R.id.line1);
            line2 = (TextView) view.findViewById(R.id.line2);
            avatar = (ImageView) view.findViewById(R.id.avatar);
            avatarCrown = (ImageView) view.findViewById(R.id.avatarCrown);
        }
    }

    private void editGroupSubject() {
        PopupUtils.showCustomEditDialog(GroupDisplayActivity.this, "", mName, R.string.yes, R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newSubject = String.valueOf(v.getTag());
                changeGroupSubject(newSubject);

                // Update the UI
                mName = newSubject;
                mRecyclerView.getAdapter().notifyItemChanged(0);
            }
        }, null);
    }

    private void changeGroupSubject(String subject) {
        try {
            IChatSession session = mConn.getChatSessionManager().getChatSession(mAddress);
            session.setGroupChatSubject(subject);
        } catch (Exception e) {
        }
    }

    boolean isMuted() {
        try {
            if (mSession != null)
                return mSession.isMuted();
            else
                return false;
        } catch (RemoteException re) {
            return false;
        }
    }

    public void setMuted(boolean muted) {
        try {
            if (mSession != null)
                mSession.setMuted(muted);
        } catch (RemoteException re) {

        }
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

    private final IChatListener mChatListener = new ChatListenerAdapter() {
        @Override
        public void onContactJoined(IChatSession ses, Contact contact) {
            super.onContactJoined(ses, contact);
            updateMembers();
        }

        @Override
        public void onContactLeft(IChatSession ses, Contact contact) {
            super.onContactLeft(ses, contact);
            updateMembers();
        }

        @Override
        public void onContactRoleChanged(IChatSession ses, Contact contact) {
            super.onContactRoleChanged(ses, contact);
            updateMembers();
        }
    };

    private void showAddFriends() {
        if (mActionAddFriends != null) {
            if (!mIsOwner)
                mActionAddFriends.setVisibility(View.GONE);
            else {
                mActionAddFriends.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GroupDisplayActivity.this, ContactsPickerActivity.class);
                        ArrayList<String> usernames = new ArrayList<>(mMembers.size());
                        for (GroupMemberDisplay member : mMembers) {
                            usernames.add(member.username);
                        }
                        intent.putExtra(BundleKeyConstant.EXTRA_EXCLUDED_CONTACTS, usernames);
                        startActivityForResult(intent, REQUEST_PICK_CONTACTS);
                    }
                });
            }
        }
    }
}
