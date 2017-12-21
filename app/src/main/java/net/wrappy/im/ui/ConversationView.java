/*
 * Copyright (C) 2008 Esmertec AG. Copyright (C) 2008 The Android Open Source
 * Project
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Browser;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import net.ironrabbit.type.CustomTypefaceSpan;
import net.java.otr4j.OtrPolicy;
import net.java.otr4j.session.SessionStatus;
import net.wrappy.im.ImApp;
import net.wrappy.im.Preferences;
import net.wrappy.im.R;
import net.wrappy.im.TranslateAPI.InAppTranslation;
import net.wrappy.im.bho.DictionarySearch;
import net.wrappy.im.crypto.IOtrChatSession;
import net.wrappy.im.crypto.otr.OtrAndroidKeyManagerImpl;
import net.wrappy.im.crypto.otr.OtrChatManager;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.model.Address;
import net.wrappy.im.model.ConferenceMessage;
import net.wrappy.im.model.Contact;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.model.ImErrorInfo;
import net.wrappy.im.model.Presence;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatListener;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IContactList;
import net.wrappy.im.service.IContactListListener;
import net.wrappy.im.service.IContactListManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.service.ISubscriptionListener;
import net.wrappy.im.service.ImServiceConstants;
import net.wrappy.im.tasks.AddContactAsyncTask;
import net.wrappy.im.tasks.ChatSessionInitTask;
import net.wrappy.im.ui.MessageListItem.DeliveryState;
import net.wrappy.im.ui.MessageListItem.EncryptionState;
import net.wrappy.im.ui.conference.ConferenceConstant;
import net.wrappy.im.ui.legacy.DatabaseUtils;
import net.wrappy.im.ui.legacy.Markup;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.ui.legacy.adapter.ChatListenerAdapter;
import net.wrappy.im.ui.stickers.Sticker;
import net.wrappy.im.ui.stickers.StickerGroup;
import net.wrappy.im.ui.stickers.StickerManager;
import net.wrappy.im.ui.stickers.StickerPagerAdapter;
import net.wrappy.im.ui.stickers.StickerSelectListener;
import net.wrappy.im.ui.widgets.MessageViewHolder;
import net.wrappy.im.ui.widgets.RoundedAvatarDrawable;
import net.wrappy.im.util.ConferenceUtils;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.GiphyAPI;
import net.wrappy.im.util.LogCleaner;
import net.wrappy.im.util.PopupUtils;
import net.wrappy.im.util.PreferenceUtils;
import net.wrappy.im.util.SystemServices;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConversationView {
    // This projection and index are set for the query of active chats
    public static final String[] CHAT_PROJECTION = {Imps.Contacts._ID, Imps.Contacts.ACCOUNT,
            Imps.Contacts.PROVIDER, Imps.Contacts.USERNAME,
            Imps.Contacts.NICKNAME, Imps.Contacts.TYPE,
            Imps.Presence.PRESENCE_STATUS,
            Imps.Chats.LAST_UNREAD_MESSAGE,
            Imps.Chats._ID,
            Imps.Contacts.SUBSCRIPTION_TYPE,
            Imps.Contacts.SUBSCRIPTION_STATUS,
            Imps.Contacts.AVATAR_DATA

    };

    public static final int CONTACT_ID_COLUMN = 0;
    public static final int ACCOUNT_COLUMN = 1;
    public static final int PROVIDER_COLUMN = 2;
    public static final int USERNAME_COLUMN = 3;
    public static final int NICKNAME_COLUMN = 4;
    public static final int TYPE_COLUMN = 5;
    public static final int PRESENCE_STATUS_COLUMN = 6;
    public static final int LAST_UNREAD_MESSAGE_COLUMN = 7;
    public static final int CHAT_ID_COLUMN = 8;
    public static final int SUBSCRIPTION_TYPE_COLUMN = 9;
    public static final int SUBSCRIPTION_STATUS_COLUMN = 10;
    public static final int AVATAR_COLUMN = 11;

    @BindView(R.id.txtStatus)
    TextView txtInviteStatus;
    @BindView(R.id.imgStatus)
    ImageView imgStatus;
    @BindView(R.id.btnAddFriend)
    Button btnAddContact;

    //static final int MIME_TYPE_COLUMN = 9;

    static final String[] INVITATION_PROJECT = {Imps.Invitation._ID, Imps.Invitation.PROVIDER,
            Imps.Invitation.SENDER,};
    static final int INVITATION_ID_COLUMN = 0;
    static final int INVITATION_PROVIDER_COLUMN = 1;
    static final int INVITATION_SENDER_COLUMN = 2;

    static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    static final StyleSpan STYLE_NORMAL = new StyleSpan(Typeface.NORMAL);

    Markup mMarkup;

    ConversationDetailActivity mActivity;
    ImApp mApp;
    private SimpleAlertHandler mHandler;
    IImConnection mConn;

    //private ImageView mStatusIcon;
    // private TextView mTitle;
    /*package*/ RecyclerView mHistory;
    EditText mComposeMessage;
    private ImageButton mSendButton, mMicButton;
    private TextView mButtonTalk;
    private ImageButton mButtonAttach;
    private View mViewAttach;

    private ImageView mButtonDeleteVoice;
    private View mViewDeleteVoice;


    private ImageView mDeliveryIcon;
    private boolean mExpectingDelivery;

    private boolean mIsSelected = false;

    private SessionStatus mLastSessionStatus = null;
    private boolean mIsStartingOtr = false;
    private boolean mIsVerified = false;

    private ConversationRecyclerViewAdapter mMessageAdapter;
    //   private boolean isServiceUp;
    private IChatSession mCurrentChatSession;

    long mLastChatId = -1;
    String mRemoteNickname;
    String mRemoteAddress;
    String mRemoteReference;
    RoundedAvatarDrawable mRemoteAvatar = null;
    Drawable mRemoteHeader = null;
    int mSubscriptionType;
    int mSubscriptionStatus;

    long mProviderId = -1;
    long mAccountId = -1;
    long mInvitationId;
    private Context mContext; // TODO
    private int mPresenceStatus;
    private Date mLastSeen;

    private int mViewType;

    private boolean istranslate;

    private static final int VIEW_TYPE_CHAT = 1;
    public static final int VIEW_TYPE_INVITATION = 2;
    private static final int VIEW_TYPE_SUBSCRIPTION = 3;

    //    private static final long SHOW_TIME_STAMP_INTERVAL = 30 * 1000; // 15 seconds
    private static final long SHOW_DELIVERY_INTERVAL = 10 * 1000; // 5 seconds
    private static final long SHOW_MEDIA_DELIVERY_INTERVAL = 120 * 1000; // 2 minutes
    private static final long DEFAULT_QUERY_INTERVAL = 2000;
    private static final long FAST_QUERY_INTERVAL = 200;

    public SpamBottomSheet mSpamBottomSheet;

    private RequeryCallback mRequeryCallback = null;

    // array data of spinner language
    private String[] arraySpinner = null;
    private ChatSessionInitTask task;

    public SimpleAlertHandler getHandler() {
        return mHandler;
    }

    public int getType() {
        return mViewType;
    }

    private class RequeryCallback implements Runnable {
        public void run() {
            if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                Debug.d("RequeryCallback");
            }
            requeryCursor();

        }
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;

        if (mViewType != VIEW_TYPE_CHAT)
            return;

        if (mIsSelected) {
            //  bindChat(mLastChatId);
            startListening();

            updateWarningView();
            mComposeMessage.requestFocus();
            mMessageAdapter.setNeedRequeryCursor(false);
            userActionDetected();
            updateGroupTitle();

            try {
                mApp.dismissChatNotification(mProviderId, XmppAddress.stripResource(mRemoteAddress));
                mCurrentChatSession.markAsRead();
            } catch (Exception e) {
            }

            try {

                if (mConn == null)
                    if (!checkConnection())
                        return;

                IContactListManager manager = mConn.getContactListManager();

                Contact contact = manager.getContactByAddress(mRemoteAddress);

                if (contact != null) {

                    if (contact.getPresence() != null) {
                        mLastSeen = contact.getPresence().getLastSeen();
                        if (mLastSeen != null)
                            mActivity.updateLastSeen(mLastSeen);
                    }

                    if (!TextUtils.isEmpty(contact.getForwardingAddress())) {
                        showContactMoved(contact);
                    }

                }

                if ((mLastSessionStatus == null || mLastSessionStatus == SessionStatus.PLAINTEXT)) {

                    boolean otrPolicyAuto = getOtrPolicy() == OtrPolicy.OPPORTUNISTIC
                            || getOtrPolicy() == OtrPolicy.OTRL_POLICY_ALWAYS;

                    if (mCurrentChatSession == null)
                        mCurrentChatSession = getChatSession();
                    if (mCurrentChatSession == null)
                        return;


                    IOtrChatSession otrChatSession = mCurrentChatSession.getDefaultOtrChatSession();

                    if (otrChatSession != null && (!isGroupChat())) {
                        String remoteJID = otrChatSession.getRemoteUserId();

                        boolean doOtr = (remoteJID != null && (remoteJID.toLowerCase().contains("chatsecure") || remoteJID.toLowerCase().contains("zom")));

                        if (!doOtr)
                            doOtr = OtrAndroidKeyManagerImpl.getInstance(mActivity).hasRemoteFingerprint(remoteJID);

                        if (otrPolicyAuto && doOtr) //if set to auto, and is chatsecure, then start encryption
                        {
                            //automatically attempt to turn on OTR after 1 second
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    setOTRState(true);
                                    scheduleRequery(DEFAULT_QUERY_INTERVAL);

                                }
                            }, 100);
                        }
                    }

                }


            } catch (Exception re) {
                re.printStackTrace();
            }

        } else {
            stopListening();
            sendTypingStatus(false);
        }

    }

    public void updateStatusAddContact() {
        btnAddContact.setVisibility(View.GONE);
        txtInviteStatus.setVisibility(View.VISIBLE);
    }

    private int getOtrPolicy() {

        int otrPolicy = OtrPolicy.OPPORTUNISTIC;

        String otrModeSelect = Preferences.getOtrMode();

        if (otrModeSelect.equals("auto")) {
            otrPolicy = OtrPolicy.OPPORTUNISTIC;
        } else if (otrModeSelect.equals("disabled")) {
            otrPolicy = OtrPolicy.NEVER;

        } else if (otrModeSelect.equals("force")) {
            otrPolicy = OtrPolicy.OTRL_POLICY_ALWAYS;

        } else if (otrModeSelect.equals("requested")) {
            otrPolicy = OtrPolicy.OTRL_POLICY_MANUAL;
        }

        return otrPolicy;
    }

    public void inviteContacts(ArrayList<String> invitees) {
        if (mConn == null)
            return;

        try {
            IChatSessionManager manager = mConn.getChatSessionManager();
            IChatSession session = manager.getChatSession(mRemoteAddress);

            for (String invitee : invitees)
                session.inviteContact(invitee);
        } catch (Exception e) {
            Log.e(ImApp.LOG_TAG, "error inviting contacts to group", e);
        }

    }
    //    collapsingToolbar.setCollapsedTitleTypeface(typeface);
    //   collapsingToolbar.setExpandedTitleTypeface(typeface);


    private boolean checkConnection() {
        if (mConn == null && mProviderId != -1) {
            mConn = mApp.getConnection(mProviderId, mAccountId);

            if (mConn == null)
                return false;

        }

        return true;


    }

    public void setOTRState(boolean otrEnabled) {

        try {

            if (mCurrentChatSession == null)
                mCurrentChatSession = getChatSession();

            if (mCurrentChatSession != null) {
                IOtrChatSession otrChatSession = mCurrentChatSession.getDefaultOtrChatSession();

                if (otrChatSession != null) {
                    if (otrEnabled && (otrChatSession.getChatStatus() != SessionStatus.ENCRYPTED.ordinal())) {

                        otrChatSession.startChatEncryption();
                        mIsStartingOtr = true;

                    }
                    // else if ((!otrEnabled) && otrChatSession.getChatStatus() == SessionStatus.ENCRYPTED.ordinal())
                    else {
                        otrChatSession.stopChatEncryption();

                    }


                }
            }


            updateWarningView();

        } catch (RemoteException e) {
            Log.d(ImApp.LOG_TAG, "error getting remote activity", e);
        }
    }

    public void sendDeleteChat(String msgId) {
        sendMessageAsync(ConferenceConstant.DELETE_CHAT_FREFIX + msgId);
    }

    public void sendEditChat(String msgId, String newMsg) {
        StringBuffer buffer = new StringBuffer(ConferenceConstant.EDIT_CHAT_FREFIX);
        buffer.append(msgId.length()).append(':').append(msgId).append(':').append(newMsg);
        sendMessageAsync(buffer.toString());
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!(view instanceof MessageListItem)) {
                return;
            }

            URLSpan[] links = ((MessageListItem) view).getMessageLinks();
            if (links.length > 0) {

                final ArrayList<String> linkUrls = new ArrayList<String>(links.length);
                for (URLSpan u : links) {
                    linkUrls.add(u.getURL());
                }
                ArrayAdapter<String> a = new ArrayAdapter<String>(mActivity,
                        android.R.layout.select_dialog_item, linkUrls);
                PopupUtils.getSelectionDialog(mActivity, mActivity.getString(R.string.select_link_title), a, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse(linkUrls.get(which));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.putExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, mProviderId);
                        intent.putExtra(ImServiceConstants.EXTRA_INTENT_ACCOUNT_ID, mAccountId);
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, mActivity.getPackageName());
                        mActivity.startActivity(intent);
                    }
                });
            }
        }
    };

    private final static int PROMPT_FOR_DATA_TRANSFER = 9999;
    private final static int SHOW_DATA_PROGRESS = 9998;
    private final static int SHOW_DATA_ERROR = 9997;
    private final static int SHOW_TYPING = 9996;


    private IChatListener mChatListener = new ChatListenerAdapter() {
        @Override
        public boolean onIncomingMessage(IChatSession ses,
                                         net.wrappy.im.model.Message msg) {
            return mIsSelected;
        }

        @Override
        public void onContactJoined(IChatSession ses, Contact contact) {
        }

        @Override
        public void onContactLeft(IChatSession ses, Contact contact) {
        }

        @Override
        public void onSendMessageError(IChatSession ses,
                                       net.wrappy.im.model.Message msg, ImErrorInfo error) {
        }

        @Override
        public void onIncomingReceipt(IChatSession ses, String packetId) throws RemoteException {
            scheduleRequery(DEFAULT_QUERY_INTERVAL);
        }

        @Override
        public void onStatusChanged(IChatSession ses) throws RemoteException {
            scheduleRequery(DEFAULT_QUERY_INTERVAL);

        }


        @Override
        public void onIncomingFileTransfer(String transferFrom, String transferUrl) throws RemoteException {

            String[] path = transferUrl.split("/");
            String sanitizedPath = SystemServices.sanitize(path[path.length - 1]);

            android.os.Message message = android.os.Message.obtain(null, PROMPT_FOR_DATA_TRANSFER, (int) (mProviderId >> 32),
                    (int) mProviderId, -1);
            message.getData().putString("from", transferFrom);
            message.getData().putString("file", sanitizedPath);
            mHandler.sendMessage(message);

            Debug.d("onIncomingFileTransfer: " + transferFrom + " @ " + transferUrl);

        }

        @Override
        public void onIncomingFileTransferProgress(String file, int percent)
                throws RemoteException {

            /**
             android.os.Message message = android.os.Message.obtain(null, SHOW_DATA_PROGRESS, (int) (mProviderId >> 32),
             (int) mProviderId, -1);
             message.getData().putString("file", file);
             message.getData().putInt("progress", percent);

             mHandler.sendMessage(message);*/

            Debug.d("onIncomingFileTransferProgress: " + file + " " + percent + "%");

        }

        @Override
        public void onIncomingFileTransferError(String file, String err) throws RemoteException {


            android.os.Message message = android.os.Message.obtain(null, SHOW_DATA_ERROR, (int) (mProviderId >> 32),
                    (int) mProviderId, -1);
            message.getData().putString("file", file);
            message.getData().putString("err", err);

            mHandler.sendMessage(message);

            Debug.d("onIncomingFileTransferProgress: " + file + " err: " + err);

        }

        @Override
        public void onContactTyping(IChatSession ses, Contact contact, boolean isTyping) throws RemoteException {
            super.onContactTyping(ses, contact, isTyping);

            if (contact.getPresence() != null) {
                mPresenceStatus = contact.getPresence().getStatus();
                mLastSeen = contact.getPresence().getLastSeen();
            } else {
                mLastSeen = new Date();
            }

            mActivity.updateLastSeen(mLastSeen);


            android.os.Message message = android.os.Message.obtain(null, SHOW_TYPING, (int) (mProviderId >> 32),
                    (int) mProviderId, -1);

            message.getData().putBoolean("typing", isTyping);

            mHandler.sendMessage(message);
        }

        @Override
        public void onGroupSubjectChanged(IChatSession ses) throws RemoteException {
            super.onGroupSubjectChanged(ses);
            if (getChatSession().getId() == ses.getId()) {
                updateGroupTitle();
            }
        }
    };

    private void showPromptForData(final String transferFrom, String filePath) {
        String message = transferFrom + ' ' + mActivity.getString(R.string.wants_to_send_you_the_file)
                + " '" + filePath + "'. " + mActivity.getString(R.string.accept_transfer_);
        PopupUtils.showCustomDialog(mActivity, mContext.getString(R.string.file_transfer), message, R.string.yes, R.string.no, new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    mCurrentChatSession.setIncomingFileResponse(transferFrom, true, false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }, new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    mCurrentChatSession.setIncomingFileResponse(transferFrom, false, false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Runnable mUpdateChatCallback = new Runnable() {
        public void run() {
            // if (mCursor != null && mCursor.requery() && mCursor.moveToFirst()) {
            updateChat();
            // }
        }
    };

    private ISubscriptionListener mSubscriptionListener = new ISubscriptionListener.Stub() {
        @Override
        public void onSubScriptionChanged(Contact from, long providerId, long accountId, int subType, int subStatus) throws RemoteException {
            mSubscriptionType = subType;
            mSubscriptionStatus = subStatus;
            showSubscriptionUI();
        }

        @Override
        public void onSubScriptionRequest(Contact from, long providerId, long accountId) throws RemoteException {

        }

        @Override
        public void onSubscriptionApproved(Contact from, long providerId, long accountId) throws RemoteException {

        }

        @Override
        public void onSubscriptionDeclined(Contact from, long providerId, long accountId) throws RemoteException {

        }
    };

    private IContactListListener mContactListListener = new IContactListListener.Stub() {
        public void onAllContactListsLoaded() {
        }

        public void onContactChange(int type, IContactList list, Contact contact) {

            if (contact != null && contact.getPresence() != null) {
                mPresenceStatus = contact.getPresence().getStatus();

            }
            mLastSeen = new Date();

            mActivity.updateLastSeen(mLastSeen);


        }

        public void onContactError(int errorType, ImErrorInfo error, String listName,
                                   Contact contact) {
        }

        public void onContactsPresenceUpdate(Contact[] contacts) {

            Debug.d("onContactsPresenceUpdate()");

            for (Contact c : contacts) {
                if (c.getAddress().getBareAddress().equals(Address.stripResource(mRemoteAddress))) {

                    if (c != null && c.getPresence() != null) {
                        mPresenceStatus = c.getPresence().getStatus();

                        if (mPresenceStatus != Presence.OFFLINE) {
                            mLastSeen = c.getPresence().getLastSeen();
                            mActivity.updateLastSeen(mLastSeen);
                        }

                    }

                    mHandler.post(mUpdateChatCallback);
                    scheduleRequery(DEFAULT_QUERY_INTERVAL);
                    break;
                }
            }

        }
    };

    private boolean mIsListening;

    public ConversationView(ConversationDetailActivity activity) {

        mActivity = activity;
        mContext = activity;
        istranslate = PreferenceUtils.getBoolean("istranslate",
                istranslate, mActivity);
        ButterKnife.bind(this, mActivity);

        mApp = (ImApp) mActivity.getApplication();
        mHandler = new ChatViewHandler(mActivity);

        initViews();
    }

    void registerForConnEvents() {
        mApp.registerForConnEvents(mHandler);
    }

    void unregisterForConnEvents() {
        mApp.unregisterForConnEvents(mHandler);
    }

    protected void initViews() {
        //  mStatusIcon = (ImageView) mActivity.findViewById(R.id.statusIcon);
        //   mDeliveryIcon = (ImageView) mActivity.findViewById(R.id.deliveryIcon);
        // mTitle = (TextView) mActivity.findViewById(R.id.title);
        mHistory = (RecyclerView) mActivity.findViewById(R.id.history);
        LinearLayoutManager llm = new LinearLayoutManager(mHistory.getContext());
        llm.setStackFromEnd(true);
        mHistory.setLayoutManager(llm);

        mComposeMessage = (EditText) mActivity.findViewById(R.id.composeMessage);
        mSendButton = (ImageButton) mActivity.findViewById(R.id.btnSend);
        mMicButton = (ImageButton) mActivity.findViewById(R.id.btnMic);
        mButtonTalk = (TextView) mActivity.findViewById(R.id.buttonHoldToTalk);

        mButtonDeleteVoice = (ImageView) mActivity.findViewById(R.id.btnDeleteVoice);
        mViewDeleteVoice = mActivity.findViewById(R.id.viewDeleteVoice);

        mButtonDeleteVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    int resolvedColor = mHistory.getResources().getColor(android.R.color.holo_red_light);
                    mButtonDeleteVoice.setBackgroundColor(resolvedColor);
                }

                return false;
            }
        });

        mButtonAttach = (ImageButton) mActivity.findViewById(R.id.btnAttach);
        mViewAttach = mActivity.findViewById(R.id.attachPanel);

        mButtonAttach.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toggleAttachMenu();
            }

        });

        mActivity.findViewById(R.id.btnAttachPicture).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mActivity.startImagePicker();
            }

        });

        mActivity.findViewById(R.id.btnTakePicture).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mActivity.startPhotoTaker();
            }

        });


        mActivity.findViewById(R.id.btnAttachFile).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mActivity.startFilePicker();
            }

        });

        mActivity.findViewById(R.id.btnAttachSticker).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toggleAttachMenu();
                showStickers();
            }

        });

        mActivity.findViewById(R.id.btnAttachLocation).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mActivity.startLocationMessage();
            }
        });

        mMicButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //this is the tap to change to hold to talk mode
                if (mMicButton.getVisibility() == View.VISIBLE) {
                    mComposeMessage.setVisibility(View.GONE);
                    mMicButton.setVisibility(View.GONE);

                    // Check if no view has focus:
                    View view = mActivity.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    mSendButton.setImageResource(R.drawable.ic_keyboard_black_36dp);
                    mSendButton.setVisibility(View.VISIBLE);
                    mButtonTalk.setVisibility(View.VISIBLE);

                }
            }

        });


        final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                //this is for recording audio directly from one press
                mActivity.startAudioRecording();
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mActivity.isAudioRecording()) {
                    boolean send = true;//inViewInBounds(mMicButton, (int) motionEvent.getX(), (int) motionEvent.getY());
                    mActivity.stopAudioRecording(send);
                }
                return super.onSingleTapUp(e);
            }
        });

        mMicButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);

            }
        });

        mButtonTalk.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View btnTalk, MotionEvent theMotion) {
                switch (theMotion.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mActivity.startAudioRecording();
                        mButtonTalk.setText(mActivity.getString(R.string.recording_release));
                        mViewDeleteVoice.setVisibility(View.VISIBLE);

                        break;
                    case MotionEvent.ACTION_MOVE:
                        boolean inBounds = inViewInBounds(btnTalk, (int) theMotion.getX(), (int) theMotion.getY());
                        if (!inBounds)
                            mButtonTalk.setText(mActivity.getString(R.string.recording_delete));
                        else {
                            mButtonTalk.setText(mActivity.getString(R.string.recording_release));
                            mViewDeleteVoice.setVisibility(View.VISIBLE);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mButtonTalk.setText(mActivity.getString(R.string.push_to_talk));
                        boolean send = inViewInBounds(btnTalk, (int) theMotion.getX(), (int) theMotion.getY());
                        mActivity.stopAudioRecording(send);
                        mViewDeleteVoice.setVisibility(View.GONE);

                        break;
                }
                return true;
            }
        });
        /**
         mHistory.setOnItemLongClickListener(new OnItemLongClickListener ()
         {

         @TargetApi(Build.VERSION_CODES.HONEYCOMB)
         @Override public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {


         if (arg1 instanceof MessageView)
         {

         String textToCopy = ((MessageView)arg1).getLastMessage();

         int sdk = android.os.Build.VERSION.SDK_INT;
         if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
         android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
         clipboard.setText(textToCopy); //
         } else {
         android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
         android.content.ClipData clip = android.content.ClipData.newPlainText("chat",textToCopy);
         clipboard.setPrimaryClip(clip); //
         }

         Toast.makeText(mActivity, mContext.getString(R.string.toast_chat_copied_to_clipboard), Toast.LENGTH_SHORT).show();

         return true;

         }

         return false;
         }

         });**/

        mComposeMessage.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                sendTypingStatus(true);

                return false;
            }
        });

        mComposeMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                sendTypingStatus(hasFocus);

            }
        });

        mComposeMessage.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                            sendMessage();
                            return true;

                        case KeyEvent.KEYCODE_ENTER:
                            if (event.isAltPressed()) {
                                mComposeMessage.append("\n");
                                return true;
                            }
                    }

                }


                return false;
            }
        });

        mComposeMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    if (event.isAltPressed()) {
                        return false;
                    }
                }

                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && imm.isActive(v)) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                sendMessage();
                return true;
            }
        });

        // TODO: this is a hack to implement BUG #1611278, when dispatchKeyEvent() works with
        // the soft keyboard, we should remove this hack.
        mComposeMessage.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int before, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int after) {

            }

            public void afterTextChanged(Editable s) {
                doWordSearch();
                userActionDetected();
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (mComposeMessage.getVisibility() == View.VISIBLE) {
                    checkBeforeSubmit();
                } else {
                    mSendButton.setImageResource(R.drawable.ic_send_holo_light);

                    if (mLastSessionStatus == SessionStatus.ENCRYPTED)
                        mSendButton.setImageResource(R.drawable.ic_send_secure);

                    mSendButton.setVisibility(View.GONE);
                    mButtonTalk.setVisibility(View.GONE);
                    mComposeMessage.setVisibility(View.VISIBLE);
                    mMicButton.setVisibility(View.VISIBLE);


                }
            }
        });

        mMessageAdapter = new ConversationRecyclerViewAdapter(mActivity, null);
        mHistory.setAdapter(mMessageAdapter);
    }

    private void checkBeforeSubmit() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", mComposeMessage.getText().toString());

        RestAPI.PostDataWrappy(mContext, jsonObject, RestAPI.POST_CHECK_OBJECTIONABLE, new RestAPI.RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                Debug.e(s);
                try {
                    JsonObject jObject = (new JsonParser()).parse(s).getAsJsonObject();

                    if (jObject.get("status").toString() != null) {
                        boolean status = Boolean.parseBoolean(jObject.get("status").toString());
                        if (status) {

                        } else {
                            sendMessage();
                        }
                    }
                } catch (Exception e) {
                    sendMessage();
                }
            }
        });
    }

    private boolean mLastIsTyping = false;

    private void sendTypingStatus(boolean isTyping) {

        if (mLastIsTyping != isTyping) {
            try {
                if (mConn != null)
                    mConn.sendTypingStatus(mRemoteAddress, isTyping);
            } catch (Exception ie) {
                Log.e(ImApp.LOG_TAG, "error sending typing status", ie);
            }

            mLastIsTyping = isTyping;
        }

    }

    DictionarySearch ds = null;
    PopupMenu mPopupWords = null;
    SearchWordTask taskSearch = null;

    private class SearchWordTask extends AsyncTask<String, Long, ArrayList<String>> {

        private String mLastSearchTerm = null;

        protected ArrayList<String> doInBackground(String... searchTerm) {

            String[] searchTerms = searchTerm[0].split("་");

            if (searchTerms.length > 0) {
                mLastSearchTerm = searchTerms[searchTerms.length - 1];

                ArrayList<String> result = ds.getMatchingWords(mLastSearchTerm);

                return result;
            } else
                return null;
        }

        protected void onProgressUpdate(Long... progress) {

        }

        protected void onPostExecute(ArrayList<String> result) {
            if (result != null && result.size() > 0) {

                if (mPopupWords == null) {

                    mPopupWords = new PopupMenu(mActivity, mComposeMessage);

                    mPopupWords.setOnMenuItemClickListener(new
                                                                   PopupMenu.OnMenuItemClickListener() {
                                                                       @Override
                                                                       public boolean onMenuItemClick(MenuItem item) {

                                                                           String[] currentText = mComposeMessage.getText().toString().split("་");

                                                                           currentText[currentText.length - 1] = item.toString();

                                                                           mComposeMessage.setText("");

                                                                           for (int i = 0; i < currentText.length; i++) {
                                                                               mComposeMessage.append(currentText[i]);

                                                                               if ((i + 1) != currentText.length)
                                                                                   mComposeMessage.append("་");
                                                                           }

                                                                           mComposeMessage.setSelection(mComposeMessage.getText().length());

                                                                           return true;
                                                                       }
                                                                   });

                }

                mPopupWords.getMenu().clear();

                for (String item : result) {
                    if (!TextUtils.isEmpty(item)) {
                        SpannableStringBuilder sb = new SpannableStringBuilder(item);
                        sb.setSpan(new CustomTypefaceSpan("", mActivity), 0, item.length(), 0);
                        mPopupWords.getMenu().addSubMenu(sb);

                    }


                }

                mPopupWords.show();

            }
        }
    }

    private void doWordSearch() {

        if (Preferences.getUseTibetanDictionary()) {
            if (ds == null)
                ds = new DictionarySearch(mActivity);

            String searchText = mComposeMessage.getText().toString();

            if (!TextUtils.isEmpty(searchText)) {
                if (taskSearch == null || taskSearch.getStatus() == AsyncTask.Status.FINISHED) {
                    taskSearch = new SearchWordTask();
                    taskSearch.execute(mComposeMessage.getText().toString());

                }
            }
        }

    }

    private boolean inViewInBounds(View view, int x, int y) {
        Rect outRect = new Rect();
        int[] location = new int[2];

        view.getHitRect(outRect);

        return outRect.contains(x, y);
    }

    public void startListening() {

        mIsListening = true;

        registerChatListener();
        registerForConnEvents();

        updateWarningView();
    }

    public void stopListening() {
        //Cursor cursor = getMessageCursor();
        //if (cursor != null && (!cursor.isClosed())) {
        //   cursor.close();
        // }

        cancelRequery();
        unregisterChatListener();
        unregisterForConnEvents();
        mIsListening = false;
    }

    void updateChat() {
        setViewType(VIEW_TYPE_CHAT);

        //mHistory.invalidate();
        checkConnection();

        startQuery(getChatId());
        // This is not needed, now that there is a ChatView per fragment.  It also causes a spurious detection of user action
        // on fragments adjacent to the current one, when they get initialized.
        //mComposeMessage.setText("");

        updateWarningView();

        mActivity.findViewById(R.id.btnAttachPicture).setEnabled(true);
        mActivity.findViewById(R.id.btnTakePicture).setEnabled(true);
        //mActivity.findViewById(R.id.btnAttachFile).setEnabled(true);
        mMicButton.setEnabled(true);

    }

    int mContactType = -1;

    private void updateSessionInfo(Cursor c) {

        if (c != null && (!c.isClosed())) {
            mProviderId = c.getLong(PROVIDER_COLUMN);
            mAccountId = c.getLong(ACCOUNT_COLUMN);
            mPresenceStatus = c.getInt(PRESENCE_STATUS_COLUMN);
            mContactType = c.getInt(TYPE_COLUMN);

            mRemoteNickname = c.getString(NICKNAME_COLUMN);
            mRemoteAddress = c.getString(USERNAME_COLUMN);

            mSubscriptionType = c.getInt(SUBSCRIPTION_TYPE_COLUMN);

            mSubscriptionStatus = c.getInt(SUBSCRIPTION_STATUS_COLUMN);

            showSubscriptionUI();


        }

    }

    private void showSubscriptionUI() {

        if (isGroupChat())
            return;

        mHandler.post(new Runnable() {

            public void run() {

                if ((mSubscriptionType == Imps.Contacts.SUBSCRIPTION_TYPE_FROM
                        && mSubscriptionStatus == Imps.Contacts.SUBSCRIPTION_STATUS_SUBSCRIBE_PENDING)) {
                    updateStatusAddContact();
//                    mActivity.findViewById(R.id.waiting_view).setVisibility(View.VISIBLE);
//                    mActivity.findViewById(R.id.waiting_refresh).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            resendFriendRequest();
//                        }
//                    });
                } else if (mSubscriptionStatus == Imps.Contacts.SUBSCRIPTION_STATUS_SUBSCRIBE_PENDING) {
                    Snackbar sb = Snackbar.make(mHistory, mContext.getString(R.string.subscription_prompt, mRemoteNickname), Snackbar.LENGTH_INDEFINITE);
                    sb.setAction(mActivity.getString(R.string.approve_subscription), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            approveSubscription();
                        }
                    });
                    sb.show();
                }
            }
        });

    }

    private void resendFriendRequest() {
        //if not group chat, then send the contact another friend request
        if (!isGroupChat())
            new AddContactAsyncTask(mApp.getDefaultProviderId(), mApp.getDefaultAccountId(), mApp).execute(mRemoteAddress, null, null);

    }

    public String getTitle() {
        return mRemoteNickname;

    }

    public String getSubtitle() {
        return mRemoteAddress;
    }

    public Date getLastSeen() {
        return mLastSeen;
    }

    public RoundedAvatarDrawable getIcon() {
        return mRemoteAvatar;
    }

    public Drawable getHeader() {
        return mRemoteHeader;
    }


    private void updateGroupTitle() {
        if (isGroupChat()) {

            // Update title
            final String[] projection = {Imps.GroupMembers.NICKNAME};
            Uri contactUri = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, mLastChatId);
            ContentResolver cr = mActivity.getContentResolver();
            Cursor c = cr.query(contactUri, projection, null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    int col = c.getColumnIndex(projection[0]);
                    mRemoteNickname = c.getString(col);
                }
                c.close();
            }

            if (mRemoteNickname == null) {
                StringBuilder buf = new StringBuilder();

                int count = -1;

                try {
                    buf.append(mCurrentChatSession.getName());
                    count = mCurrentChatSession.getParticipants().length;
                } catch (Exception e) {
                }

                if (count > 0) {
                    buf.append(" (");
                    buf.append(count);
                    buf.append(")");
                }
                mRemoteNickname = buf.toString();
            }
            mActivity.getSupportActionBar().setTitle(mRemoteNickname);
        }
    }

    private void deleteChat() {
        Uri chatUri = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, mLastChatId);
        mActivity.getContentResolver().delete(chatUri, null, null);

    }

    public void bindChat(long chatId, String name, String reference) {
        //log("bind " + this + " " + chatId);
        this.mRemoteReference = reference;
        mLastChatId = chatId;

        setViewType(VIEW_TYPE_CHAT);

        Uri contactUri = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, chatId);
        Cursor c = mActivity.getContentResolver().query(contactUri, CHAT_PROJECTION, null, null, null);

        if (c == null)
            return;

        if (!c.moveToFirst()) {
            if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                Debug.d("Failed to query chat: " + chatId);
            }
            mLastChatId = -1;

            c.close();

        } else {

            updateSessionInfo(c);

            if (mRemoteAvatar == null) {
                try {
                    mRemoteAvatar = DatabaseUtils.getAvatarFromCursor(c, AVATAR_COLUMN, ImApp.DEFAULT_AVATAR_WIDTH, ImApp.DEFAULT_AVATAR_HEIGHT);
                } catch (Exception e) {
                }

                if (mRemoteAvatar == null) {
                    mRemoteAvatar = new RoundedAvatarDrawable(BitmapFactory.decodeResource(mActivity.getResources(),
                            R.drawable.avatar_unknown));

                }


            }

            if (mRemoteHeader == null) {
                try {
                    mRemoteHeader = DatabaseUtils.getHeaderImageFromCursor(c, AVATAR_COLUMN, ImApp.DEFAULT_AVATAR_WIDTH, ImApp.DEFAULT_AVATAR_HEIGHT);
                } catch (Exception e) {
                }
            }


            c.close();

            initSession();

            if (isGroupChat()) {
                updateGroupTitle();
            } else {
                if (mRemoteNickname == null)
                    mRemoteNickname = name;
                try {
                    mRemoteNickname = mRemoteNickname.split("@")[0].split("\\.")[0];
                } catch (Exception e) {
                    //handle glitches in unicode nicknames
                }
            }
        }

    }

    private void initSession() {
        mHandler.post(mUpdateChatCallback);

        new Thread() {
            public void run() {

                mCurrentChatSession = getChatSession();

                if (mCurrentChatSession == null)
                    mCurrentChatSession = createChatSession();

                mHandler.post(mUpdateChatCallback);

            }
        }.start();
    }

    /**
     * public void bindSubscription(long providerId, String from) {
     * mProviderId = providerId;
     * <p>
     * //  mRemoteAddressString = from;
     * <p>
     * setViewType(VIEW_TYPE_SUBSCRIPTION);
     * <p>
     * TextView text = (TextView) mActivity.findViewById(R.id.txtSubscription);
     * String displayableAddr = ImpsAddressUtils.getDisplayableAddress(from);
     * text.setText(mContext.getString(R.string.subscription_prompt, displayableAddr));
     * //.displayableAdd    mNewChatActivity.setTitle(mContext.getString(R.string.chat_with, displayableAddr));
     * <p>
     * mApp.dismissChatNotification(providerId, from);
     * }
     */


    public void setViewType(int type) {
        mViewType = type;
        if (type == VIEW_TYPE_CHAT) {
            mActivity.findViewById(R.id.invitationPanel).setVisibility(View.GONE);
            //     mActivity.findViewById(R.id.subscription).setVisibility(View.GONE);
            setChatViewEnabled(true);
        } else if (type == VIEW_TYPE_INVITATION) {
            //setChatViewEnabled(false);
            mActivity.findViewById(R.id.invitationPanel).setVisibility(View.VISIBLE);
        } else if (type == VIEW_TYPE_SUBSCRIPTION) {
            //setChatViewEnabled(false);
            //   mActivity.findViewById(R.id.subscription).setVisibility(View.VISIBLE);

            // mActivity.findViewById(R.id.btnApproveSubscription).requestFocus();
        }
    }

    private void setChatViewEnabled(boolean enabled) {
        mComposeMessage.setEnabled(enabled);
        mSendButton.setEnabled(enabled);
        if (enabled) {
            // This can steal focus from the fragment that's i n front of the user
            //mComposeMessage.requestFocus();
        } else {
            mHistory.setAdapter(null);
        }

    }

    RecyclerView getHistoryView() {
        return mHistory;
    }

    private Uri mUri;
    private LoaderManager mLoaderManager;
    private int loaderId = 100001;

    private synchronized void startQuery(long chatId) {

        mUri = Imps.Messages.getContentUriByThreadId(chatId);

        mLoaderManager = mActivity.getSupportLoaderManager();

        if (mLoaderManager == null)
            mLoaderManager.initLoader(loaderId++, null, new MyLoaderCallbacks());
        else
            mLoaderManager.restartLoader(loaderId++, null, new MyLoaderCallbacks());

    }

    class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            CursorLoader loader = new CursorLoader(mActivity, mUri, null, Imps.MessageColumns.STATUS + " = " + Imps.MessageColumns.VISIBLE, null, Imps.Messages.DEFAULT_SORT_ORDER);

            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {

            if (newCursor != null) {
                //Log.i("Cursor", android.database.DatabaseUtils.dumpCursorToString(newCursor));
                newCursor.setNotificationUri(mActivity.getApplicationContext().getContentResolver(), mUri);
                mMessageAdapter.swapCursor(new DeltaCursor(newCursor));

                if (!mMessageAdapter.isScrolling()) {

                    mHandler.post(new Runnable() {

                        public void run() {
                            if (mMessageAdapter.getItemCount() > 0) {
                                mHistory.getLayoutManager().scrollToPosition(mMessageAdapter.getItemCount() - 1);
                            }
                        }
                    });
                }

            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

            mMessageAdapter.swapCursor(null);

        }
    }

    void scheduleRequery(long interval) {


        if (mRequeryCallback == null) {
            mRequeryCallback = new RequeryCallback();
        } else {
            mHandler.removeCallbacks(mRequeryCallback);
        }

        if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
            Debug.d("scheduleRequery");
        }
        mHandler.postDelayed(mRequeryCallback, interval);


    }

    void cancelRequery() {

        if (mRequeryCallback != null) {
            if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                Debug.d("cancelRequery");
            }
            mHandler.removeCallbacks(mRequeryCallback);
            mRequeryCallback = null;
        }
    }


    void requeryCursor() {
        Debug.d("requeryCursor");
        mLoaderManager.restartLoader(loaderId++, null, new MyLoaderCallbacks());
        updateWarningView();

        /**
         if (mMessageAdapter.isScrolling()) {
         mMessageAdapter.setNeedRequeryCursor(true);
         return;
         }

         // This is redundant if there are messages in view, because the cursor requery will update everything.
         // However, if there are no messages, no update will trigger below, and we still want this to update.


         // TODO: async query?
         Cursor cursor = getMessageCursor();
         if (cursor != null) {
         cursor.requery();
         }*/
    }

    private Cursor getMessageCursor() {
        return mMessageAdapter == null ? null : mMessageAdapter.getCursor();
    }

    public void closeChatSession(boolean doDelete) {
        if (getChatSession() != null) {
            try {

                setOTRState(false);
                updateWarningView();
                getChatSession().leave();

            } catch (RemoteException e) {

                mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                LogCleaner.error(ImApp.LOG_TAG, "send message error", e);
            }
        }

        if (doDelete)
            deleteChat();

    }

    /**
     * public void verifyScannedFingerprint (String scannedFingerprint)
     * {
     * try
     * {
     * IOtrChatSession otrChatSession = mCurrentChatSession.getDefaultOtrChatSession();
     * <p>
     * if (scannedFingerprint != null && scannedFingerprint.equalsIgnoreCase(otrChatSession.getRemoteFingerprint())) {
     * verifyRemoteFingerprint();
     * }
     * }
     * catch (RemoteException e)
     * {
     * LogCleaner.error(ImApp.LOG_TAG, "unable to perform manual key verification", e);
     * }
     * }
     */

    public void showVerifyDialog() {

        Intent intent = new Intent(mContext, ContactDisplayActivity.class);
        intent.putExtra("nickname", mRemoteNickname);
        intent.putExtra("address", mRemoteAddress);
        intent.putExtra("provider", mProviderId);
        intent.putExtra("account", mAccountId);
        intent.putExtra("contactId", mLastChatId);

        if (mCurrentChatSession != null) {
            try {
                IOtrChatSession otrChatSession = mCurrentChatSession.getDefaultOtrChatSession();
                if (otrChatSession != null)
                    intent.putExtra("fingerprint", otrChatSession.getRemoteFingerprint());
            } catch (RemoteException re) {
            }
        }

        mContext.startActivity(intent);

    }

    public void showGroupInfo() {

        Intent intent = new Intent(mContext, GroupDisplayActivity.class);
        intent.putExtra("nickname", mRemoteNickname);
        intent.putExtra("address", mRemoteAddress);
        intent.putExtra("provider", mProviderId);
        intent.putExtra("account", mAccountId);
        intent.putExtra("chat", mLastChatId);

        mContext.startActivity(intent);
    }


    public void blockContact() {
        // TODO: unify with codes in ContactListView
        View.OnClickListener confirmListener = new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    checkConnection();
                    mConn = mApp.getConnection(mProviderId, mAccountId);
                    IContactListManager manager = mConn.getContactListManager();
                    manager.blockContact(Address.stripResource(mRemoteAddress));
                    //  mNewChatActivity.finish();
                } catch (Exception e) {

                    mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                    LogCleaner.error(ImApp.LOG_TAG, "send message error", e);
                }
            }
        };

        Resources r = mActivity.getResources();

        // The positive button is deliberately set as no so that
        // the no is the default value
        PopupUtils.showCustomDialog(mContext, mContext.getString(R.string.confirm), r.getString(R.string.confirm_block_contact, mRemoteNickname), R.string.yes,
                R.string.no, confirmListener, null, false);
    }

    public long getProviderId() {
        return mProviderId;
    }

    public long getAccountId() {
        return mAccountId;
    }

    public long getChatId() {
        return mLastChatId;
    }

    private IChatSession createChatSession() {

        try {
            if (mConn != null) {
                IChatSessionManager sessionMgr = mConn.getChatSessionManager();
                if (sessionMgr != null) {

                    String remoteAddress = mRemoteAddress;
                    IChatSession session = null;

                    if (mContactType == Imps.Contacts.TYPE_GROUP) {
                        //Contact contactGroup = new Contact(new XmppAddress(mRemoteAddress),mRemoteNickname,Imps.Contacts.TYPE_GROUP);
                        session = sessionMgr.createMultiUserChatSession(mRemoteAddress, mRemoteNickname, null, false);

                        //new ChatSessionInitTask(((ImApp)mActivity.getApplication()),mProviderId, mAccountId, Imps.Contacts.TYPE_GROUP)
                        //      .executeOnExecutor(ImApp.sThreadPoolExecutor,contactGroup);

                    } else {
                        remoteAddress = Address.stripResource(mRemoteAddress);

                        session = sessionMgr.createChatSession(remoteAddress, false);
                    }

                    return session;

                }
            }

        } catch (Exception e) {

            //mHandler.showServiceErrorAlert(e.getLocalizedMessage());
            LogCleaner.error(ImApp.LOG_TAG, "issue getting chat session", e);
        }

        return null;
    }

    public IChatSession getChatSession() {

        try {

            if (mConn != null) {
                IChatSessionManager sessionMgr = mConn.getChatSessionManager();
                if (sessionMgr != null) {

                    IChatSession session = sessionMgr.getChatSession(mRemoteAddress);

                    return session;

                }
            }
        } catch (Exception e) {
            //mHandler.showServiceErrorAlert(e.getLocalizedMessage());
            LogCleaner.error(ImApp.LOG_TAG, "error getting chat session", e);
        }

        return null;
    }

    public boolean isGroupChat() {
        return this.mContactType == Imps.Contacts.TYPE_GROUP;
    }

    void sendMessage() {

        String msg = mComposeMessage.getText().toString();
        //new SendMessageAsyncTask().execute(msg);
        sendMessageAsync(msg);
        sendTypingStatus(false);
        /**
         if (ds != null)
         {
         ds.close();
         }*/
    }

    void sendMessageAsync(final String msg) {

        //new SendMessageAsyncTask().execute(msg);

        new Thread() {
            public void run() {
                sendMessage(msg, false);
            }
        }.start();

        mComposeMessage.setText("");
        mComposeMessage.requestFocus();
    }

    boolean sendMessage(String msg, boolean isResend) {
        //don't send empty messages
        if (TextUtils.isEmpty(msg.trim())) {
            return false;
        }

        //if the message starts with a command (just /giphy for now) do something else
        if (msg.startsWith("/giphy ")) {
            return doGiphy(msg);
        }

        //otherwise get the session, create if necessary, and then send
        IChatSession session = getChatSession();

        if (session == null)
            session = createChatSession();

        if (session != null) {
            try {
                // delete own message
                if (msg.startsWith(ConferenceConstant.DELETE_CHAT_FREFIX)) {
                    String packet_id = msg.replace(ConferenceConstant.DELETE_CHAT_FREFIX, "");
                    session.deleteMessageInDb(packet_id);
                } else if (msg.startsWith(ConferenceConstant.EDIT_CHAT_FREFIX)) {// update own message
                    String[] message_edit = ConferenceUtils.getEditedMessage(msg);
                    session.updateMessageInDb(message_edit[0], message_edit[1]);
//                    session.deleteMessageInDb(message_edit[0]);
                }

                session.sendMessage(msg, isResend);
                return true;
                //requeryCursor();
            } catch (RemoteException e) {

                //  mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                LogCleaner.error(ImApp.LOG_TAG, "send message error", e);
            } catch (Exception e) {

                //  mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                LogCleaner.error(ImApp.LOG_TAG, "send message error", e);
            }
        }

        return false;
    }

    private static GiphyAPI.Monitor mMonitor = null;

    private synchronized boolean doGiphy(String search) {
        mMonitor = new GiphyAPI.Monitor() {

            public void onSearchComplete(GiphyAPI.SearchResult result) {

                if (result.data != null && result.data.length > 0) {

                    final GiphyAPI.GifImage gifResult = result.data[0].images.original;

                    new Thread() {
                        public void run() {

                            mActivity.handleSendDelete(getChatSession(), Uri.parse(gifResult.url), "image/gif", false, false, true);
                        }
                    }.start();


                } else {
                    Toast.makeText(mActivity, "No giphy stickers available for your search", Toast.LENGTH_SHORT).show();
                }

                GiphyAPI.get().removeMonitor(mMonitor);
                mMonitor = null;

            }
        };

        GiphyAPI.get().addMonitor(mMonitor);


        try {
            GiphyAPI.get().search(URLEncoder.encode(search.substring(7).trim(), "UTF-8"));

        } catch (Exception e) {
        }

        return true;
    }

    void registerChatListener() {
        if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
            Debug.d("registerChatListener " + mLastChatId);
        }
        try {

            checkConnection();

            if (getChatSession() != null) {
                getChatSession().registerChatListener(mChatListener);
            }

            if (mConn != null) {
                IContactListManager listMgr = mConn.getContactListManager();
                listMgr.registerContactListListener(mContactListListener);
                listMgr.registerSubscriptionListener(mSubscriptionListener);

            }

        } catch (Exception e) {
            Log.w(ImApp.LOG_TAG, "<ChatView> registerChatListener fail:" + e.getMessage());
        }
    }

    void unregisterChatListener() {
        if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
            Debug.d("unregisterChatListener " + mLastChatId);
        }
        try {
            if (getChatSession() != null) {
                getChatSession().unregisterChatListener(mChatListener);
            }
            checkConnection();

            if (mConn != null) {
                IContactListManager listMgr = mConn.getContactListManager();
                listMgr.unregisterContactListListener(mContactListListener);
                listMgr.unregisterSubscriptionListener(mSubscriptionListener);

            }
        } catch (Exception e) {
            Log.w(ImApp.LOG_TAG, "<ChatView> unregisterChatListener fail:" + e.getMessage());
        }
    }

    void updateWarningView() {
        try {
            checkConnection();
        } catch (Exception e) {
        }

        if (this.isGroupChat()) {
            mComposeMessage.setHint(R.string.this_is_a_group_chat);
        } else if (mCurrentChatSession != null) {
            IOtrChatSession otrChatSession = null;

            try {
                otrChatSession = mCurrentChatSession.getDefaultOtrChatSession();

                //check if the chat is otr or not
                if (otrChatSession != null) {
                    try {
                        mLastSessionStatus = SessionStatus.values()[otrChatSession.getChatStatus()];
                    } catch (RemoteException e) {
                        Log.w("Gibber", "Unable to call remote OtrChatSession from ChatView", e);
                    }
                }
            } catch (RemoteException e) {
                LogCleaner.error(ImApp.LOG_TAG, "error getting OTR session in ChatView", e);
            }


            boolean isSessionEncrypted = false;

            try {
                isSessionEncrypted = mCurrentChatSession.isEncrypted() || mLastSessionStatus == SessionStatus.ENCRYPTED;
            } catch (RemoteException re) {
            }

            if (isSessionEncrypted) {

                mIsStartingOtr = false; //it's started!

                if (mSendButton.getVisibility() == View.GONE) {
                    mComposeMessage.setHint(R.string.compose_hint_secure);
                    mSendButton.setImageResource(R.drawable.ic_send_secure);
                }

                if (otrChatSession != null) {
                    try {
                        String rFingerprint = otrChatSession.getRemoteFingerprint();
                        mIsVerified = (OtrChatManager.getInstance().isRemoteKeyVerified(mRemoteAddress, rFingerprint));
                    } catch (RemoteException re) {
                    }
                }

            } else if (mIsStartingOtr) {

            } else if (mLastSessionStatus == SessionStatus.PLAINTEXT) {

                mSendButton.setImageResource(R.drawable.ic_send_holo_light);
                mComposeMessage.setHint(R.string.compose_hint);


            } else if (mLastSessionStatus == SessionStatus.FINISHED) {

                mSendButton.setImageResource(R.drawable.ic_send_holo_light);
                mComposeMessage.setHint(R.string.compose_hint);
            }
        }
    }

    public SessionStatus getOtrSessionStatus() {
        return mLastSessionStatus;
    }

    public boolean isOtrSessionVerified() {
        return mIsVerified;
    }

    public int getRemotePresence() {
        return mPresenceStatus;
    }

    public String getmRemoteAddress() {
        return mRemoteAddress;
    }

    /*
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        userActionDetected();
        return mActivity.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            userActionDetected();
            return mActivity.dispatchTouchEvent(ev);
        } catch (ActivityNotFoundException e) {
           // if the user clicked a link, e.g. geo:60.17,24.829, and there is
            //  no app to handle that kind of link, catch the exception
            Toast.makeText(mActivity, R.string.error_no_app_to_handle_url, Toast.LENGTH_SHORT)
                    .show();
            return true;
        }
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        userActionDetected();
        return mActivity.dispatchTrackballEvent(ev);
    }*/

    private void userActionDetected() {
        // Check that we have a chat session and that our fragment is resumed
        // The latter filters out bogus TextWatcher events on restore from saved
        if (getChatSession() != null && mIsListening) {
            try {
                getChatSession().markAsRead();
                //updateWarningView();

            } catch (RemoteException e) {

                mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                LogCleaner.error(ImApp.LOG_TAG, "send message error", e);
            }
        }

        toggleInputMode();

    }

    private void toggleInputMode() {
        if (mButtonTalk.getVisibility() == View.GONE) {
            if (mComposeMessage.getText().length() > 0 && mSendButton.getVisibility() == View.GONE) {
                mMicButton.setVisibility(View.GONE);
                mSendButton.setVisibility(View.VISIBLE);
                mSendButton.setImageResource(R.drawable.ic_send_holo_light);

                if (mLastSessionStatus == SessionStatus.ENCRYPTED)
                    mSendButton.setImageResource(R.drawable.ic_send_secure);


            } else if (mComposeMessage.getText().length() == 0) {
                mMicButton.setVisibility(View.VISIBLE);
                mSendButton.setVisibility(View.GONE);

            }
        }
    }

    private final class ChatViewHandler extends SimpleAlertHandler {


        public ChatViewHandler(Activity activity) {
            super(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            long providerId = ((long) msg.arg1 << 32) | msg.arg2;
            if (providerId != mProviderId) {
                return;
            }

            switch (msg.what) {

                case ImApp.EVENT_CONNECTION_DISCONNECTED:
                    Debug.d("Handle event connection disconnected.");
                    updateWarningView();
                    promptDisconnectedEvent(msg);
                    return;
                case PROMPT_FOR_DATA_TRANSFER:
                    showPromptForData(msg.getData().getString("from"), msg.getData().getString("file"));
                    break;
                case SHOW_DATA_ERROR:

                    String fileName = msg.getData().getString("file");
                    String error = msg.getData().getString("err");

                    Toast.makeText(mContext, "Error transferring file: " + error, Toast.LENGTH_LONG).show();
                    break;
                case SHOW_DATA_PROGRESS:

                    int percent = msg.getData().getInt("progress");


                    break;
                case SHOW_TYPING:

                    boolean isTyping = msg.getData().getBoolean("typing");
                    mActivity.findViewById(R.id.tvTyping).setVisibility(isTyping ? View.VISIBLE : View.GONE);

                default:
                    updateWarningView();
            }

            super.handleMessage(msg);
        }
    }

    public static class DeltaCursor implements Cursor {
        static final String DELTA_COLUMN_NAME = "delta";

        private Cursor mInnerCursor;
        private String[] mColumnNames;
        private int mDateColumn = -1;
        private int mDeltaColumn = -1;

        DeltaCursor(Cursor cursor) {
            mInnerCursor = cursor;

            String[] columnNames = cursor.getColumnNames();
            int len = columnNames.length;

            mColumnNames = new String[len + 1];

            for (int i = 0; i < len; i++) {
                mColumnNames[i] = columnNames[i];
                if (mColumnNames[i].equals(Imps.Messages.DATE)) {
                    mDateColumn = i;
                }
            }

            mDeltaColumn = len;
            mColumnNames[mDeltaColumn] = DELTA_COLUMN_NAME;
        }

        public int getCount() {
            return mInnerCursor.getCount();
        }

        public int getPosition() {
            return mInnerCursor.getPosition();
        }

        public boolean move(int offset) {
            return mInnerCursor.move(offset);
        }

        public boolean moveToPosition(int position) {
            return mInnerCursor.moveToPosition(position);
        }

        public boolean moveToFirst() {
            return mInnerCursor.moveToFirst();
        }

        public boolean moveToLast() {
            return mInnerCursor.moveToLast();
        }

        public boolean moveToNext() {
            return mInnerCursor.moveToNext();
        }

        public boolean moveToPrevious() {
            return mInnerCursor.moveToPrevious();
        }

        public boolean isFirst() {
            return mInnerCursor.isFirst();
        }

        public boolean isLast() {
            return mInnerCursor.isLast();
        }

        public boolean isBeforeFirst() {
            return mInnerCursor.isBeforeFirst();
        }

        public boolean isAfterLast() {
            return mInnerCursor.isAfterLast();
        }

        public int getColumnIndex(String columnName) {
            if (DELTA_COLUMN_NAME.equals(columnName)) {
                return mDeltaColumn;
            }

            int columnIndex = mInnerCursor.getColumnIndex(columnName);
            return columnIndex;
        }

        public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
            if (DELTA_COLUMN_NAME.equals(columnName)) {
                return mDeltaColumn;
            }

            return mInnerCursor.getColumnIndexOrThrow(columnName);
        }

        public String getColumnName(int columnIndex) {
            if (columnIndex == mDeltaColumn) {
                return DELTA_COLUMN_NAME;
            }

            return mInnerCursor.getColumnName(columnIndex);
        }

        public int getColumnCount() {
            return mInnerCursor.getColumnCount() + 1;
        }

        public void deactivate() {
            mInnerCursor.deactivate();
        }

        public boolean requery() {
            return mInnerCursor.requery();
        }

        public void close() {
            mInnerCursor.close();
        }

        public boolean isClosed() {
            return mInnerCursor.isClosed();
        }

        public void registerContentObserver(ContentObserver observer) {
            mInnerCursor.registerContentObserver(observer);
        }

        public void unregisterContentObserver(ContentObserver observer) {
            mInnerCursor.unregisterContentObserver(observer);
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            mInnerCursor.registerDataSetObserver(observer);
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            mInnerCursor.unregisterDataSetObserver(observer);
        }

        public void setNotificationUri(ContentResolver cr, Uri uri) {
            mInnerCursor.setNotificationUri(cr, uri);
        }

        public boolean getWantsAllOnMoveCalls() {
            return mInnerCursor.getWantsAllOnMoveCalls();
        }

        @Override
        public void setExtras(Bundle bundle) {

        }

        public Bundle getExtras() {
            return mInnerCursor.getExtras();
        }

        public Bundle respond(Bundle extras) {
            return mInnerCursor.respond(extras);
        }

        public String[] getColumnNames() {
            return mColumnNames;
        }

        private void checkPosition() {
            int pos = mInnerCursor.getPosition();
            int count = mInnerCursor.getCount();

            if (-1 == pos || count == pos) {
                throw new CursorIndexOutOfBoundsException(pos, count);
            }
        }

        public byte[] getBlob(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return null;
            }

            return mInnerCursor.getBlob(column);
        }

        public String getString(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                long value = getDeltaValue();
                return Long.toString(value);
            }

            return mInnerCursor.getString(column);
        }

        public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
            checkPosition();

            if (columnIndex == mDeltaColumn) {
                long value = getDeltaValue();
                String strValue = Long.toString(value);
                int len = strValue.length();
                char[] data = buffer.data;
                if (data == null || data.length < len) {
                    buffer.data = strValue.toCharArray();
                } else {
                    strValue.getChars(0, len, data, 0);
                }
                buffer.sizeCopied = strValue.length();
            } else {
                mInnerCursor.copyStringToBuffer(columnIndex, buffer);
            }
        }

        public short getShort(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return (short) getDeltaValue();
            }

            return mInnerCursor.getShort(column);
        }

        public int getInt(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return (int) getDeltaValue();
            }

            return mInnerCursor.getInt(column);
        }

        public long getLong(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return getDeltaValue();
            }

            return mInnerCursor.getLong(column);
        }

        public float getFloat(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return getDeltaValue();
            }

            return mInnerCursor.getFloat(column);
        }

        public double getDouble(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return getDeltaValue();
            }

            return mInnerCursor.getDouble(column);
        }

        public boolean isNull(int column) {
            checkPosition();

            if (column == mDeltaColumn) {
                return false;
            }

            return mInnerCursor.isNull(column);
        }

        private long getDeltaValue() {
            int pos = mInnerCursor.getPosition();
            //Log.i(LOG_TAG, "getDeltaValue: mPos=" + mPos);

            long t2, t1;

            if (pos == getCount() - 1) {
                t1 = mInnerCursor.getLong(mDateColumn);
                t2 = System.currentTimeMillis();
            } else {
                mInnerCursor.moveToPosition(pos + 1);
                t2 = mInnerCursor.getLong(mDateColumn);
                mInnerCursor.moveToPosition(pos);
                t1 = mInnerCursor.getLong(mDateColumn);
            }

            return t2 - t1;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public int getType(int arg0) {
            return mInnerCursor.getType(arg0);
        }

        @TargetApi(19)
        @Override
        public Uri getNotificationUri() {
            return mInnerCursor.getNotificationUri();
        }

    }

    public class ConversationRecyclerViewAdapter
            extends CursorRecyclerViewAdapter<MessageViewHolder> {

        private int mScrollState;
        private boolean mNeedRequeryCursor;

        private int mNicknameColumn;
        private int mBodyColumn;
        private int mDateColumn;
        private int mTypeColumn;
        private int mErrCodeColumn;
        private int mDeltaColumn;
        private int mDeliveredColumn;
        private int mMimeTypeColumn;
        private int mIdColumn;

        class BodyTranslate {
            public boolean mIstranslate;
            public String mTexttranslate;

        }


        private List<BodyTranslate> bodytranslate = new ArrayList<>();
        private InAppTranslation iapptranslater;
        private String targetlanguage = "ja";
        // private String bodytranalate = "";


        private ActionMode mActionMode;
        private View mLastSelectedView;
        private String tempPacketIDSelect = "";
        private int tempMessageType = 0;
        private String tempNicknameSelect = "";
        private Bitmap mCaptureBitmap;

        public void setTargetLanguage(String target) {
            switch (target) {
                case "English":
                    targetlanguage = "en";
                    break;
                case "Japanese":
                    targetlanguage = "ja";
                    break;
                case "Vietnamese":
                    targetlanguage = "vi";
                    break;
            }
            resetTranslate();
            // bodytranslate.clear();
        }

        public void resetTranslate() {
            for (int i = 0; i < bodytranslate.size(); i++) {
                bodytranslate.get(i).mIstranslate = false;
            }
        }

        public ConversationRecyclerViewAdapter(Activity context, Cursor c) {
            super(context, c);
            if (c != null) {
                resolveColumnIndex(c);
            }

            setHasStableIds(true);
        }

        private void resolveColumnIndex(Cursor c) {
            mNicknameColumn = c.getColumnIndexOrThrow(Imps.Messages.NICKNAME);
            mBodyColumn = c.getColumnIndexOrThrow(Imps.Messages.BODY);
            mDateColumn = c.getColumnIndexOrThrow(Imps.Messages.DATE);
            mTypeColumn = c.getColumnIndexOrThrow(Imps.Messages.TYPE);
            mErrCodeColumn = c.getColumnIndexOrThrow(Imps.Messages.ERROR_CODE);
            mDeltaColumn = c.getColumnIndexOrThrow(DeltaCursor.DELTA_COLUMN_NAME);
            mDeliveredColumn = c.getColumnIndexOrThrow(Imps.Messages.IS_DELIVERED);
            mMimeTypeColumn = c.getColumnIndexOrThrow(Imps.Messages.MIME_TYPE);
            mIdColumn = c.getColumnIndexOrThrow(Imps.Messages._ID);
        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            if (newCursor != null) {
                resolveColumnIndex(newCursor);
                if (newCursor.moveToFirst()) {
                    do {
                        if (bodytranslate.size() < newCursor.getPosition() + 1) {
                            BodyTranslate data = new BodyTranslate();
                            data.mIstranslate = false;
                            data.mTexttranslate = "";
                            bodytranslate.add(data);
                        }
                    } while (newCursor.moveToNext());
                }
            }

            return super.swapCursor(newCursor);
        }

        @Override
        public long getItemId(int position) {

            Cursor c = getCursor();
            c.moveToPosition(position);
            long chatId = c.getLong(mIdColumn);
            return chatId;
        }

        @Override
        public int getItemViewType(int position) {

            Cursor c = getCursor();
            c.moveToPosition(position);
            int type = c.getInt(mTypeColumn);
            boolean isLeft = (type == Imps.MessageType.INCOMING_ENCRYPTED) || (type == Imps.MessageType.INCOMING) || (type == Imps.MessageType.INCOMING_ENCRYPTED_VERIFIED);

            if (isLeft)
                return 0;
            else
                return 1;

        }

        public Cursor getItem(int position) {
            Cursor c = getCursor();
            c.moveToPosition(position);
            return c;
        }


        void setLinkifyForMessageView(MessageListItem messageView) {
            try {

                if (messageView == null)
                    return;

                if (mConn != null)
                    messageView.setLinkify(!mConn.isUsingTor() || Preferences.getDoLinkify());

            } catch (RemoteException e) {
                e.printStackTrace();
                messageView.setLinkify(false);
            }
        }


        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MessageListItem view = null;
            MessageViewHolder mvh = null;

            if (viewType == 0)
                view = (MessageListItem) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_view_left, parent, false);
            else
                view = (MessageListItem) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_view_right, parent, false);

            iapptranslater = new InAppTranslation(mActivity, new InAppTranslation.CompleteTransaction() {
                @Override
                public void onTaskTranslateComplete(String result, int position) {
                    BodyTranslate data = new BodyTranslate();
                    data.mIstranslate = true;
                    data.mTexttranslate = result;
                    bodytranslate.set(position, data);
                    notifyItemChanged(position);
                }

                @Override
                public void onTaskDetectComplete(String result, String src, int position) {
                    if (!result.equals("")) {
                        iapptranslater.translate(src, result, targetlanguage, position);
                    }
                }

                @Override
                public void onTaskLListTranslateComplete(List<String> result) {

                }
            });


            mvh = new MessageViewHolder(view);
            view.applyStyleColors();
            return mvh;
        }


        @Override
        public void onBindViewHolder(final MessageViewHolder viewHolder, final Cursor cursor, final int position) {

            cursor.moveToPosition(position);

            viewHolder.btntranslate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cursor.moveToPosition(viewHolder.getPos());
                    if (bodytranslate.get(viewHolder.getPos()).mIstranslate == false) {
                        bodytranslate.get(viewHolder.getPos()).mIstranslate = true;
                        viewHolder.btntranslate.setText(mActivity.getResources().getString(R.string.waiting_dialog));
                        viewHolder.btntranslate.setEnabled(false);
                        iapptranslater.detectlanguage(cursor.getString(mBodyColumn), viewHolder.getPos());
                    } else {
                        bodytranslate.get(viewHolder.getPos()).mIstranslate = false;
                        notifyItemChanged(viewHolder.getPos());
                    }
                }
            });


            viewHolder.setPosition(position);

            MessageListItem messageView = (MessageListItem) viewHolder.itemView;
            setLinkifyForMessageView(messageView);
            messageView.applyStyleColors();

            int messageType = cursor.getInt(mTypeColumn);
            final String nickname = isGroupChat() ? cursor.getString(mNicknameColumn) : mRemoteNickname;
            ;
            final String address = isGroupChat() ? Imps.Contacts.getAddressFromNickname(mActivity.getContentResolver(), nickname) : mRemoteAddress;

            viewHolder.mAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    intent.putExtra("address", address);
                    intent.putExtra("nickname", nickname);
                    mContext.startActivity(intent);
                }
            });
            String mimeType = cursor.getString(mMimeTypeColumn);
            int id = cursor.getInt(mIdColumn);
            String body = cursor.getString(mBodyColumn);
            if (istranslate == false || cursor.getString(mMimeTypeColumn) != null
                    || cursor.getString(mBodyColumn).startsWith(ConferenceConstant.CONFERENCE_PREFIX)) {

                viewHolder.btntranslate.setVisibility(View.GONE);
                viewHolder.txttranslate.setVisibility(View.GONE);
                // body =cursor.getString(mBodyColumn);
            } else {
                viewHolder.btntranslate.setEnabled(true);
                viewHolder.btntranslate.setVisibility(View.VISIBLE);
                if (bodytranslate.get(viewHolder.getPos()).mIstranslate == true) {
                    if (bodytranslate.size() > viewHolder.getPos() && !bodytranslate.get(viewHolder.getPos()).mTexttranslate.isEmpty()) {
                        viewHolder.txttranslate.setVisibility(View.VISIBLE);
                        viewHolder.txttranslate.setText(bodytranslate.get(viewHolder.getPos()).mTexttranslate);
                    }
                    viewHolder.btntranslate.setText(mActivity.getResources().getString(R.string.closetranslate));
                } else {
                    viewHolder.txttranslate.setVisibility(View.GONE);
                    viewHolder.btntranslate.setText(mActivity.getResources().getString(R.string.seetranslate));
                }

            }

            long delta = cursor.getLong(mDeltaColumn);
            boolean showTimeStamp = true;//(delta > SHOW_TIME_STAMP_INTERVAL);
            long timestamp = cursor.getLong(mDateColumn);

            Date date = showTimeStamp ? new Date(timestamp) : null;
            boolean isDelivered = cursor.getLong(mDeliveredColumn) > 0;
            long showDeliveryInterval = (mimeType == null) ? SHOW_DELIVERY_INTERVAL : SHOW_MEDIA_DELIVERY_INTERVAL;
            boolean showDelivery = ((System.currentTimeMillis() - timestamp) > showDeliveryInterval);

            DeliveryState deliveryState = DeliveryState.NEUTRAL;

            if (showDelivery && !isDelivered && mExpectingDelivery) {
                deliveryState = DeliveryState.UNDELIVERED;
            } else if (isDelivered) {
                deliveryState = DeliveryState.DELIVERED;
            }


            if (!mExpectingDelivery && isDelivered) {
                mExpectingDelivery = true;
            } else if (cursor.getPosition() == cursor.getCount() - 1) {
                /*
                // if showTimeStamp is false for the latest message, then set a timer to query the
                // cursor again in a minute, so we can update the last message timestamp if no new
                // message is received
                if (Log.isLoggable(ImApp.LOG_TAG, Log.DEBUG)) {
                    log("delta = " + delta + ", showTs=" + showTimeStamp);
                }
                *//*
                if (!showDelivery) {
                    scheduleRequery(SHOW_DELIVERY_INTERVAL);
                } else if (!showTimeStamp) {
                    scheduleRequery(SHOW_TIME_STAMP_INTERVAL);
                } else {
                    cancelRequery();
                }*/
            }

            EncryptionState encState = EncryptionState.NONE;
            if (messageType == Imps.MessageType.INCOMING_ENCRYPTED) {
                messageType = Imps.MessageType.INCOMING;
                encState = EncryptionState.ENCRYPTED;
            } else if (messageType == Imps.MessageType.INCOMING_ENCRYPTED_VERIFIED) {
                messageType = Imps.MessageType.INCOMING;
                encState = EncryptionState.ENCRYPTED_AND_VERIFIED;
            } else if (messageType == Imps.MessageType.OUTGOING_ENCRYPTED) {
                messageType = Imps.MessageType.OUTGOING;
                encState = EncryptionState.ENCRYPTED;
            } else if (messageType == Imps.MessageType.OUTGOING_ENCRYPTED_VERIFIED) {
                messageType = Imps.MessageType.OUTGOING;
                encState = EncryptionState.ENCRYPTED_AND_VERIFIED;
            }
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (mActionMode != null) {
                        return false;
                    }

                    cursor.moveToPosition(viewHolder.getPos());

                    mLastSelectedView = view;
                    tempPacketIDSelect = cursor.getString(cursor.getColumnIndex(Imps.Messages.PACKET_ID));
                    tempMessageType = cursor.getInt(mTypeColumn);
                    tempNicknameSelect = nickname;
                    // Start the CAB using the ActionMode.Callback defined above
                    mActionMode = ((Activity) mContext).startActionMode(mActionModeCallback);

                    mCaptureBitmap = captureView(mLastSelectedView);
                    AppFuncs.convertBitmapToFile(mActivity, mCaptureBitmap);

                    return true;
                }
            });

            switch (messageType) {
                case Imps.MessageType.INCOMING:
                    messageView.bindIncomingMessage(viewHolder, id, messageType, address, nickname, mimeType, body, date, mMarkup, false, encState, isGroupChat(), mPresenceStatus, mRemoteReference);
                    break;

                case Imps.MessageType.OUTGOING:
                case Imps.MessageType.QUEUED:

                    int errCode = cursor.getInt(mErrCodeColumn);
                    if (errCode != 0) {
                        messageView.bindErrorMessage(errCode);
                    } else {
                        messageView.bindOutgoingMessage(viewHolder, id, messageType, null, mimeType, body, date, mMarkup, false,
                                deliveryState, encState);
                    }

                    break;

                default:
                    messageView.bindPresenceMessage(viewHolder, nickname, messageType, date, isGroupChat(), false);
            }


        }

        /**
         * Capture message text to report spam
         *
         * @param view
         */
        public Bitmap captureView(View view) {
            // create a bitmap with the same dimensions
            Bitmap image = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
            // draw the view inside the Bitmap
            view.draw(new Canvas(image));
            return image;
        }

        boolean isScrolling() {
            return mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING;
        }

        void setNeedRequeryCursor(boolean requeryCursor) {
            mNeedRequeryCursor = requeryCursor;
        }

        ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();

                boolean isLeft = (tempMessageType == Imps.MessageType.INCOMING_ENCRYPTED)
                        || (tempMessageType == Imps.MessageType.INCOMING)
                        || (tempMessageType == Imps.MessageType.INCOMING_ENCRYPTED_VERIFIED);

                if (isLeft) {
                    inflater.inflate(R.menu.menu_each_spam_message, menu);
                } else {
                    inflater.inflate(R.menu.menu_each_delete_message, menu);
                }
                return true;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // Return false if nothing is done
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.menu_message_delete:
                        if (!tempPacketIDSelect.equalsIgnoreCase("")) {
                            sendDeleteChat(tempPacketIDSelect);
                        }
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.menu_report_spam:
                        ImApp mApp = (ImApp) mActivity.getApplication();
                        long mAccountId = mApp.getDefaultAccountId();

                        mSpamBottomSheet = SpamBottomSheet.getInstance(Imps.Account.getAccountName(mActivity.getContentResolver(), mAccountId), tempNicknameSelect, tempPacketIDSelect);
                        mSpamBottomSheet.show(mActivity.getSupportFragmentManager(), "Dialog");

                        mode.finish();
                        return true;
//                    case R.id.menu_message_share:
//                        ((MessageListItem) mLastSelectedView).exportMediaFile();
//                        mode.finish(); // Action picked, so close the CAB
//                        return true;
//                    case R.id.menu_message_forward:
//                        ((MessageListItem) mLastSelectedView).forwardMediaFile();
//                        mode.finish(); // Action picked, so close the CAB
//                        return true;
//                    case R.id.menu_message_resend:
//                        sendMessageAsync(((MessageListItem) mLastSelectedView).getLastMessage());
//                        mode.finish(); // Action picked, so close the CAB
//                        return true;

                    default:
                        return false;
                }


            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mActionMode = null;


                if (mLastSelectedView != null)
                    tempPacketIDSelect = "";
                mLastSelectedView.setSelected(false);
            }
        };
    }

    public Cursor getMessageAtPosition(int position) {
        Object item = mMessageAdapter.getItem(position);

        return (Cursor) item;
    }

    public EditText getComposedMessage() {
        return mComposeMessage;
    }

    /**
     * public void onServiceConnected() {
     * bindChat(mLastChatId, null, null);
     * startListening();
     * }
     **/

    private void toggleAttachMenu() {
        if (mViewAttach.getVisibility() == View.INVISIBLE) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                // get the center for the clipping circle
                int cx = mViewAttach.getLeft();
                int cy = mViewAttach.getHeight();

                // get the final radius for the clipping circle
                float finalRadius = (float) Math.hypot(cx, cy);

                // create the animator for this view (the start radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(mViewAttach, cx, cy, 0, finalRadius);

                // make the view visible and start the animation

                mViewAttach.setVisibility(View.VISIBLE);
                anim.start();
            } else {
                mViewAttach.setVisibility(View.VISIBLE);

            }

            // Check if no view has focus:
            View view = mActivity.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                // get the center for the clipping circle
                int cx = mViewAttach.getLeft();
                int cy = mViewAttach.getHeight();

// get the initial radius for the clipping circle
                float initialRadius = (float) Math.hypot(cx, cy);

// create the animation (the final radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(mViewAttach, cx, cy, initialRadius, 0);

// make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mViewAttach.setVisibility(View.INVISIBLE);
                    }
                });

// start the animation
                anim.start();

            } else {
                mViewAttach.setVisibility(View.INVISIBLE);
            }

        }


        if (mStickerBox != null)
            mStickerBox.setVisibility(View.GONE);
    }

    private ViewPager mStickerPager = null;
    private View mStickerBox = null;

    private void showStickers() {
        if (mStickerPager == null) {

            initStickers();
            mStickerBox = mActivity.findViewById(R.id.stickerBox);
        }

        mStickerBox.setVisibility(mStickerBox.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }


    private synchronized void initStickers() {


        mStickerPager = (ViewPager) mActivity.findViewById(R.id.stickerPager);

        Collection<StickerGroup> emojiGroups = StickerManager.getInstance(mActivity).getEmojiGroups();

        if (emojiGroups.size() > 0) {
            StickerPagerAdapter emojiPagerAdapter = new StickerPagerAdapter(mActivity, new ArrayList<StickerGroup>(emojiGroups),
                    new StickerSelectListener() {
                        @Override
                        public void onStickerSelected(Sticker s) {

                            sendStickerCode(s.assetUri);
                            /**
                             if (isGroupChat())
                             {
                             sendStickerCode(s.assetUri);

                             }
                             else
                             {
                             mActivity.handleSendDelete(s.assetUri,"image/png", false, false, true);
                             }*/
                            //   mActivity.handleSendData(Uri.parse(s.assetPath),"image/png");

                            mViewAttach.setVisibility(View.INVISIBLE);
                            showStickers();
                        }
                    });

            mStickerPager.setAdapter(emojiPagerAdapter);

        }


    }

    //generate a :pack-sticker: code
    private void sendStickerCode(Uri assetUri) {
        StringBuffer stickerCode = new StringBuffer();
        stickerCode.append(":");

        stickerCode.append(assetUri.getPathSegments().get(assetUri.getPathSegments().size() - 2));
        stickerCode.append("-");
        stickerCode.append(assetUri.getLastPathSegment().split("\\.")[0]);

        stickerCode.append(":");

        sendMessageAsync(stickerCode.toString());
    }

    void approveSubscription() {

        if (mConn != null) {
            try {
                IContactListManager manager = mConn.getContactListManager();
                manager.approveSubscription(new Contact(new XmppAddress(mRemoteAddress), mRemoteNickname, Imps.Contacts.TYPE_NORMAL));
            } catch (RemoteException e) {

                // mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                LogCleaner.error(ImApp.LOG_TAG, "approve sub error", e);
            }
        }
    }

    void declineSubscription() {

        if (mConn != null) {
            try {
                IContactListManager manager = mConn.getContactListManager();
                manager.declineSubscription(new Contact(new XmppAddress(mRemoteAddress), mRemoteNickname, Imps.Contacts.TYPE_NORMAL));
            } catch (RemoteException e) {
                // mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                LogCleaner.error(ImApp.LOG_TAG, "decline sub error", e);
            }
        }
    }

    private void showContactMoved(final Contact contact) {
        final View viewNotify = mActivity.findViewById(R.id.upgrade_view);
        ImageView viewImage = (ImageView) mActivity.findViewById(R.id.upgrade_view_image);
        TextView viewDesc = (TextView) mActivity.findViewById(R.id.upgrade_view_text);
        Button buttonAction = (Button) mActivity.findViewById(R.id.upgrade_action);

        viewNotify.setVisibility(View.VISIBLE);

        viewDesc.setText(mActivity.getString(R.string.contact_migration_notice) + ' ' + contact.getForwardingAddress());

        buttonAction.setText(R.string.contact_migration_action);
        buttonAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewNotify.setVisibility(View.GONE);
                startChat(contact.getForwardingAddress());
            }
        });
    }

    private void startChat(String username) {

        if (username != null) {
            task = new ChatSessionInitTask(mActivity, mProviderId, mAccountId, Imps.Contacts.TYPE_NORMAL) {
                @Override
                protected void onPostExecute(Long chatId) {
                    if (task.isStable() && chatId != -1) {
                        Intent intent = ConversationDetailActivity.getStartIntent(mActivity);
                        intent.putExtra("id", chatId);
                        mActivity.startActivity(intent);
                    }
                }

            };
            task.executeOnExecutor(ImApp.sThreadPoolExecutor, new Contact(new XmppAddress(username)));

            mActivity.finish();
        }
    }

    /**
     * Showing popup menu item translate
     *
     * @return
     */
    public FrameLayout popupDisplay(final ConversationDetailActivity activity) {
        FrameLayout popupWindow = new FrameLayout(activity);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        popupWindow.setLayoutParams(layoutParams);

        // inflate menu item layout
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.menu_popup_translate, null);

        // setting up Spinner
        arraySpinner = new String[]{
                "English", "Japanese", "Vietnamese"
        };
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner_settings_language);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                R.layout.spinner_language_item, arraySpinner);
        spinner.setAdapter(adapter);

        spinner.setSelection(1);
        mMessageAdapter.setTargetLanguage(spinner.getSelectedItem().toString());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                mMessageAdapter.setTargetLanguage(arraySpinner[position]);
                mMessageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final TextView textTurnOnOff = (TextView) view.findViewById(R.id.text_turn_on_off_translate);

        // setting switch turn on/off translate
        Switch turnOnOff = (Switch) view.findViewById(R.id.switch_turn_on_off_translate);
        turnOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    textTurnOnOff.setText("On");
                    istranslate = true;
                    PreferenceUtils.putBoolean("istranslate",
                            istranslate, mActivity);
                    mMessageAdapter.notifyDataSetChanged();
                } else {
                    textTurnOnOff.setText("Off");
                    istranslate = false;
                    PreferenceUtils.putBoolean("istranslate",
                            istranslate, mActivity);
                    mMessageAdapter.notifyDataSetChanged();
                }
            }
        });

        popupWindow.setFocusable(true);
        // popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        //   popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.addView(view);

        return popupWindow;
    }

    public void startAudioConference() {
        String roomId = getRoomId(ConferenceMessage.ConferenceType.AUDIO);
        ConferenceActivity.startAudioCall(mContext, roomId);
    }

    public void startVideoConference() {
        String roomId = getRoomId(ConferenceMessage.ConferenceType.VIDEO);
        Debug.d("room Id " + roomId);
        ConferenceActivity.startVideoCall(mContext, roomId);
    }

    private String getRoomId(ConferenceMessage.ConferenceType type) {
        String roomId;
        ConferenceMessage message = new ConferenceMessage(String.valueOf(mAccountId), String.valueOf(mLastChatId), isGroupChat(), type, ConferenceMessage.ConferenceState.REQUEST);
        sendMessageAsync(message.toString());
        roomId = message.getRoomId();
        return roomId;
    }

    public void startSettingScreen() {
        Intent intent = new Intent(mActivity, SettingConversationActivity.class);
        intent.putExtra("chatId", mLastChatId);
        intent.putExtra("account", mAccountId);
        intent.putExtra("address", mRemoteAddress);
        intent.putExtra("provider", mProviderId);
        intent.putExtra("isGroupChat", mContactType);
        mActivity.startActivityForResult(intent, ConversationDetailActivity.REQUEST_CHANGE_BACKGROUND);
    }

    public static class SpamBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
        @BindView(R.id.layout_message_spam)
        LinearLayout mSpamLayout;
        @BindView(R.id.layout_message_violence)
        LinearLayout mViolenceLayout;
        @BindView(R.id.layout_message_cancel)
        LinearLayout mCancelLayout;

        public static String TYPE_SPAM = "SPAM";
        public static String TYPE_VIOLENCE = "OBJECTIONABLE";

        public static SpamBottomSheet getInstance(String reporter, String member, String messageId) {
            SpamBottomSheet spamBottomSheet = new SpamBottomSheet();

            Bundle args = new Bundle();
            args.putString("reporter", reporter);
            args.putString("member", member);
            args.putString("messageId", messageId);

            spamBottomSheet.setArguments(args);

            return spamBottomSheet;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.report_spam_bottom_sheet, container, false);

            ButterKnife.bind(this, view);

            mSpamLayout.setOnClickListener(this);
            mViolenceLayout.setOnClickListener(this);
            mCancelLayout.setOnClickListener(this);

            return view;
        }

        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.layout_message_spam:
                case R.id.layout_message_violence:

                    File mFileCapture = AppFuncs.getFileFromBitmap(getContext());
                    RestAPI.uploadFile(getContext(), mFileCapture, RestAPI.PHOTO_BRAND).setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            JsonObject jsonObject = (new JsonParser()).parse(result.getResult()).getAsJsonObject();
                            String mReference = jsonObject.get(RestAPI.PHOTO_REFERENCE).getAsString();

                            if (getArguments() != null) {

                                String reporter = getArguments().getString("reporter");
                                String member = getArguments().getString("member");
                                String messageId = getArguments().getString("messageId");

                                sendReportMessage(reporter, member, messageId, mReference, view.getId() == R.id.layout_message_spam ? TYPE_SPAM : TYPE_VIOLENCE);
                            }
                        }
                    });
                    dismiss();
                    break;
                case R.id.layout_message_cancel:
                    dismiss();
                    break;
                default:
                    break;
            }
        }

        public void sendReportMessage(String reporter, String member, String messageId,
                                      String reference, String type) {

            JsonObject reporterObject = new JsonObject();
            reporterObject.addProperty("identifier", reporter);

            JsonObject memberObject = new JsonObject();
            memberObject.addProperty("identifier", member);

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("reporter", reporterObject);
            jsonObject.add("member", memberObject);
            jsonObject.addProperty("messageId", messageId);
            jsonObject.addProperty("screenShot", reference);
            jsonObject.addProperty("type", type);

            RestAPI.PostDataWrappy(ImApp.sImApp, jsonObject, RestAPI.POST_REPORT_MESSAGE, new RestAPI.RestAPIListenner() {
                @Override
                public void OnComplete(int httpCode, String error, String s) {
                    Debug.e(s);
                }
            });

        }
    }
}
