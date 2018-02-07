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

package net.wrappy.im.service.adapters;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.java.otr4j.OtrEngineListener;
import net.java.otr4j.session.SessionID;
import net.java.otr4j.session.SessionStatus;
import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.crypto.IOtrChatSession;
import net.wrappy.im.crypto.otr.OtrChatListener;
import net.wrappy.im.crypto.otr.OtrChatManager;
import net.wrappy.im.crypto.otr.OtrChatSessionAdapter;
import net.wrappy.im.crypto.otr.OtrDataHandler;
import net.wrappy.im.crypto.otr.OtrDebugLogger;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.model.Address;
import net.wrappy.im.model.ChatGroup;
import net.wrappy.im.model.ChatGroupManager;
import net.wrappy.im.model.ChatSession;
import net.wrappy.im.model.ConferenceCall;
import net.wrappy.im.model.ConferenceMessage;
import net.wrappy.im.model.Contact;
import net.wrappy.im.model.GroupListener;
import net.wrappy.im.model.GroupMemberListener;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.model.ImEntity;
import net.wrappy.im.model.ImErrorInfo;
import net.wrappy.im.model.Message;
import net.wrappy.im.model.MessageListener;
import net.wrappy.im.model.Presence;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatListener;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IDataListener;
import net.wrappy.im.service.RemoteImService;
import net.wrappy.im.service.StatusBarNotifier;
import net.wrappy.im.ui.ConferenceActivity;
import net.wrappy.im.ui.conference.ConferenceConstant;
import net.wrappy.im.ui.conference.ConferencePopupActivity;
import net.wrappy.im.util.ConferenceUtils;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.SecureMediaStore;
import net.wrappy.im.util.SystemServices;

import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.httpfileupload.UploadProgressListener;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.siacs.conversations.Downloader;
import info.guardianproject.iocipher.File;

import static cz.msebera.android.httpclient.conn.ssl.SSLConnectionSocketFactory.TAG;

public class ChatSessionAdapter extends IChatSession.Stub {

    private static final String NON_CHAT_MESSAGE_SELECTION = Imps.Messages.TYPE + "!="
            + Imps.MessageType.INCOMING + " AND "
            + Imps.Messages.TYPE + "!="
            + Imps.MessageType.OUTGOING;

    /**
     * The registered remote listeners.
     */
    final RemoteCallbackList<IChatListener> mRemoteListeners = new RemoteCallbackList<IChatListener>();

    ImConnectionAdapter mConnection;
    ChatSessionManagerAdapter mChatSessionManager;


    ChatSession mChatSession;
    ListenerAdapter mListenerAdapter;
    boolean mIsGroupChat;
    StatusBarNotifier mStatusBarNotifier;

    private ContentResolver mContentResolver;
    /*package*/ Uri mChatURI;

    private Uri mMessageURI;

    private boolean mConvertingToGroupChat;

    private static final int MAX_HISTORY_COPY_COUNT = 10;

    private HashMap<String, Integer> mContactStatusMap = new HashMap<String, Integer>();

    private boolean mHasUnreadMessages;

    private RemoteImService service = null;

    private HashMap<String, OtrChatSessionAdapter> mOtrChatSessions;
    private SessionStatus mLastSessionStatus = null;
    private OtrDataHandler mDataHandler;

    private IDataListener mDataListener;
    private DataHandlerListenerImpl mDataHandlerListener;

    private boolean mAcceptTransfer = false;
    private boolean mWaitingForResponse = false;
    private boolean mAcceptAllTransfer = true;//TODO set this via preference, but default true
    private String mLastFileUrl = null;


    private long mContactId;
    private boolean mIsMuted = false;

    public ChatSessionAdapter(ChatSession chatSession, ImConnectionAdapter connection, boolean isNewSession) {

        mChatSession = chatSession;
        mConnection = connection;

        service = connection.getContext();
        mContentResolver = service.getContentResolver();
        mStatusBarNotifier = service.getStatusBarNotifier();
        mChatSessionManager = (ChatSessionManagerAdapter) connection.getChatSessionManager();

        mListenerAdapter = new ListenerAdapter();

        mOtrChatSessions = new HashMap<String, OtrChatSessionAdapter>();

        mDataHandlerListener = new DataHandlerListenerImpl();

        ImEntity participant = mChatSession.getParticipant();

        if (participant instanceof ChatGroup) {
            init((ChatGroup) participant, isNewSession);
            mChatSessionManager.getChatGroupManager().loadMembers((ChatGroup) participant);
        } else {
            init((Contact) participant, isNewSession);
            initOtrChatSession(participant);
        }

        initMuted();

    }

    private void initOtrChatSession(ImEntity participant) {
        try {
            if (mConnection != null) {
                mDataHandler = new OtrDataHandler(mChatSession);
                mDataHandler.setDataListener(mDataHandlerListener);

                OtrChatManager cm = service.getOtrChatManager();
                cm.addOtrEngineListener(mListenerAdapter);
                mChatSession.setMessageListener(new OtrChatListener(cm, mListenerAdapter));

                if (participant instanceof Contact) {
                    String key = participant.getAddress().getAddress();
                    if (!mOtrChatSessions.containsKey(key)) {
                        OtrChatSessionAdapter adapter = new OtrChatSessionAdapter(mConnection.getLoginUser().getAddress().getAddress(), participant, cm);
                        mOtrChatSessions.put(key, adapter);
                    }
                } else if (participant instanceof ChatGroup) {


                }

                mDataHandler.setChatId(getId());

            }
        } catch (NullPointerException npe) {
            Log.e(ImApp.LOG_TAG, "error init OTR session", npe);
        }
    }

    public synchronized IOtrChatSession getDefaultOtrChatSession() {

        if (mOtrChatSessions.size() > 0)
            return mOtrChatSessions.entrySet().iterator().next().getValue();
        else
            return null;
    }

    private int lastPresence = -1;

    public void presenceChanged(int newPresence) {

        if (mChatSession.getParticipant() instanceof Contact) {
            ((Contact) mChatSession.getParticipant()).getPresence().setStatus(newPresence);

            if (lastPresence != newPresence && newPresence == Presence.AVAILABLE)
                sendPostponedMessages();

            lastPresence = newPresence;
        }

    }

    private void init(ChatGroup group, boolean isNewSession) {

        mIsGroupChat = true;

        mContactId = insertOrUpdateGroupContactInDb(group);
        group.addMemberListener(mListenerAdapter);
        mChatSession.setMessageListener(mListenerAdapter);

        try {
            mChatURI = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, mContactId);
            mChatSessionManager.getChatGroupManager().joinChatGroupAsync(group.getAddress(), group.getName());

            mMessageURI = Imps.Messages.getContentUriByThreadId(mContactId);
            if (isNewSession)
                insertOrUpdateChat("");

            for (Contact c : group.getMembers()) {
                mContactStatusMap.put(c.getName(), c.getPresence().getStatus());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void init(Contact contact, boolean isNewSession) {
        mIsGroupChat = false;
        ContactListManagerAdapter listManager = (ContactListManagerAdapter) mConnection.getContactListManager();

        mContactId = listManager.queryOrInsertContact(contact);

        mChatURI = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, mContactId);

        if (isNewSession)
            insertOrUpdateChat(null);

        mMessageURI = Imps.Messages.getContentUriByThreadId(mContactId);

        mContactStatusMap.put(contact.getName(), contact.getPresence().getStatus());

        sendPostponedMessages();

    }

    public void reInit() {
        // insertOrUpdateChat(null);

    }

    private ChatGroupManager getGroupManager() {
        return mConnection.getAdaptee().getChatGroupManager();
    }

    public ChatSession getAdaptee() {
        return mChatSession;
    }

    public Uri getChatUri() {
        return mChatURI;
    }

    public String[] getParticipants() {
        if (mIsGroupChat) {
            Contact self = mConnection.getLoginUser();
            ChatGroup group = (ChatGroup) mChatSession.getParticipant();
            List<Contact> members = group.getMembers();
            String[] result = new String[members.size() - 1];
            int index = 0;
            for (Contact c : members) {
                if (!c.equals(self)) {
                    result[index++] = c.getAddress().getAddress();
                }
            }
            return result;
        } else {

            return new String[]{mChatSession.getParticipant().getAddress().getAddress()};
        }
    }

    /**
     * Convert this chat session to a group chat. If it's already a group chat,
     * nothing will happen. The method works in async mode and the registered
     * listener will be notified when it's converted to group chat successfully.
     * <p>
     * Note that the method is not thread-safe since it's always called from the
     * UI and Android uses single thread mode for UI.
     */
    public void convertToGroupChat(String nickname) {
        if (mIsGroupChat || mConvertingToGroupChat) {
            return;
        }

        mConvertingToGroupChat = true;
        new ChatConvertor().convertToGroupChat(nickname);
    }

    public boolean isGroupChatSession() {
        return mIsGroupChat;
    }

    public String getName() {

        if (isGroupChatSession())
            return ((ChatGroup) mChatSession.getParticipant()).getName();
        else
            return ((Contact) mChatSession.getParticipant()).getName();

    }

    public String getAddress() {
        return mChatSession.getParticipant().getAddress().getAddress();
    }

    public long getId() {
        return ContentUris.parseId(mChatURI);
    }

    public void inviteContact(String contact) {
        if (!mIsGroupChat) {
            return;
        }
        ContactListManagerAdapter listManager = (ContactListManagerAdapter) mConnection
                .getContactListManager();
        Contact invitee = new Contact(new XmppAddress(contact), contact, Imps.Contacts.TYPE_NORMAL);
        getGroupManager().inviteUserAsync((ChatGroup) mChatSession.getParticipant(), invitee);

    }

    public void leave() {
        if (mIsGroupChat) {
            getGroupManager().leaveChatGroupAsync((ChatGroup) mChatSession.getParticipant());
        }

        mContentResolver.delete(mMessageURI, null, null);
        mContentResolver.delete(mChatURI, null, null);
        mStatusBarNotifier.dismissChatNotification(mConnection.getProviderId(), getAddress());
        mChatSessionManager.closeChatSession(this);
    }

    public void delete() {
        if (mIsGroupChat) {
            getGroupManager().deleteChatGroupAsync((ChatGroup) mChatSession.getParticipant());
        }

        mContentResolver.delete(mMessageURI, null, null);
        mContentResolver.delete(mChatURI, null, null);
        mStatusBarNotifier.dismissChatNotification(mConnection.getProviderId(), getAddress());
        mChatSessionManager.closeChatSession(this);
    }

    @Override
    public void removeMemberGroup(String username) throws RemoteException {
        if (mIsGroupChat) {
            ChatGroup group = (ChatGroup) mChatSession.getParticipant();
            Contact member = group.getMember(username);
            if (member != null) {
                getGroupManager().removeGroupMemberAsync(group, member);
            } else {
                AppFuncs.log("Cannot remove this member");
            }
        } else {
            AppFuncs.log("This is not group chat");
        }
    }

    public void leaveIfInactive() {
        //    if (mChatSession.getHistoryMessages().isEmpty()) {
        leave();
        //  }
    }

    public void sendMessage(String text, boolean isResend) {

        if (mConnection.getState() != ImConnection.LOGGED_IN) {
            // connection has been suspended, save the message without send it
            long now = System.currentTimeMillis();
            insertMessageInDb(null, text, now, Imps.MessageType.QUEUED, null);
            return;
        }

        Message msg = new Message(text);
        msg.setID(nextID());

        Debug.d("message Id " + msg.getID());
        msg.setFrom(mConnection.getLoginUser().getAddress());
        msg.setType(Imps.MessageType.QUEUED);

        long sendTime = System.currentTimeMillis();

        if (!isResend) {
            if (text.contains(ConferenceConstant.CONFERENCE_PREFIX)) {
                ConferenceMessage message = new ConferenceMessage(text);
                message.outgoing();
                text = message.toString();
            }
            insertMessageInDb(null, text, sendTime, msg.getType(), 0, msg.getID(), null);
            insertOrUpdateChat(text, sendTime);
        }

        int newType = mChatSession.sendMessageAsync(msg);

        if (msg.getDateTime() != null)
            sendTime = msg.getDateTime().getTime();

        // clear deleted message
        if (ConferenceUtils.isInvisibleMessage(msg.getBody()) && newType != Imps.MessageType.QUEUED) {
            deleteMessageInDb(msg.getID());
        } else
            updateMessageInDb(msg.getID(), newType, sendTime, null);
    }

    private Message storeMediaMessage(String localUrl, String mimeType) {

        String mediaPath = localUrl;

        Message msg = new Message(localUrl);
        msg.setID(nextID());

        msg.setFrom(mConnection.getLoginUser().getAddress());
        msg.setType(Imps.MessageType.QUEUED);

        long sendTime = System.currentTimeMillis();

        insertMessageInDb(null, mediaPath, sendTime, msg.getType(), 0, msg.getID(), mimeType);
        insertOrUpdateChat(mediaPath, sendTime);

        return msg;
    }

    private void sendMediaMessage(String localUrl, String publishUrl, Message msg) {

        String mediaPath = localUrl + ' ' + publishUrl;

        long sendTime = System.currentTimeMillis();

        msg.setBody(publishUrl);
        //insertOrUpdateChat(mediaPath);

        int newType = mChatSession.sendMessageAsync(msg);

        if (msg.getDateTime() != null)
            sendTime = msg.getDateTime().getTime();

        updateMessageInDb(msg.getID(), newType, sendTime, mediaPath);


    }

    private void sendUnstoredMessage(String text) {

        if (mConnection.getState() != ImConnection.LOGGED_IN) {
            return;
        }

        Message msg = new Message(text);
        msg.setID(nextID());

        msg.setFrom(mConnection.getLoginUser().getAddress());
        msg.setType(Imps.MessageType.QUEUED);

        long sendTime = System.currentTimeMillis();

        int newType = mChatSession.sendMessageAsync(msg);


    }

    @Override
    public boolean offerData(String offerId, final String mediaUri, final String mimeType) {

        if (TextUtils.isEmpty(mimeType))
            return false;

        Uri uri = Uri.parse(mediaUri);
        if (uri == null || uri.getPath() == null)
            return false;

        final java.io.File fileLocal;
        final java.io.InputStream fis;

        if (uri.getScheme() != null &&
                uri.getScheme().equals("vfs")) {
            fileLocal = new info.guardianproject.iocipher.File(uri.getPath());
            if (fileLocal.exists()) {
                try {
                    fis = new info.guardianproject.iocipher.FileInputStream((info.guardianproject.iocipher.File) fileLocal);
                } catch (FileNotFoundException fe) {
                    Log.w(TAG, "encrypted file not found on import: " + mediaUri);
                    return false;
                }
            } else {
                Log.w(TAG, "encrypted file not found on import: " + mediaUri);
                return false;
            }
        } else {
            fileLocal = new java.io.File(uri.getPath());
            if (fileLocal.exists()) {
                try {
                    fis = new java.io.FileInputStream(fileLocal);
                } catch (FileNotFoundException fe) {
                    Log.w(TAG, "file system file not found on import: " + mediaUri);
                    return false;
                }
            } else {
                Log.w(TAG, "file system file not found on import: " + mediaUri);
                return false;
            }
        }

        //TODO do HTTP Upload XEP 363
        //this is ugly... we need a nice async task!
        new Thread() {

            public void run() {


                final Message msgMedia = storeMediaMessage(mediaUri, mimeType);


                String fileName = fileLocal.getName();
                if (!fileName.contains(".")) {
                    fileName += "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                }

                //boolean doEncryption = !isGroupChatSession();
                /* turn-off for now */
                boolean doEncryption = false;

                UploadProgressListener listener = new UploadProgressListener() {
                    @Override
                    public void onUploadProgress(long sent, long total) {
                        //debug(TAG, "upload complete: " + l + "," + l1);
                        //once this is done, send the message
                        float percentF = ((float) sent) / ((float) total) * 100f;

                        if (mDataHandlerListener != null)
                            mDataHandlerListener.onTransferProgress(true, "", "", mediaUri.toString(), percentF);
                    }
                };


                String resultUrl = mConnection.publishFile(fileName, mimeType, fileLocal.length(), fis, doEncryption, listener);

                if (!TextUtils.isEmpty(resultUrl))
                    sendMediaMessage(mediaUri, resultUrl, msgMedia);
                else {
                    //didn't upload so lets queue it
                    updateMessageInDb(msgMedia.getID(), Imps.MessageType.QUEUED, new java.util.Date().getTime(), mediaUri);
                }
            }
        }.start();

        return true;


    }

    /**
     * Sends a message to other participant(s) in this session without adding it
     * to the history.
     *
     *
     */
    /*
    public void sendMessageWithoutHistory(String text) {

     Message msg = new Message(text);
     // TODO OTRCHAT use a lower level method
     mChatSession.sendMessageAsync(msg);
    }*/

    /**
     * boolean hasPostponedMessages() {
     * String[] projection = new String[] { BaseColumns._ID, Imps.Messages.BODY,
     * Imps.Messages.PACKET_ID,
     * Imps.Messages.DATE, Imps.Messages.TYPE, Imps.Messages.IS_DELIVERED };
     * String selection = Imps.Messages.TYPE + "=?";
     * <p>
     * boolean result = false;
     * <p>
     * Cursor c = mContentResolver.query(mMessageURI, projection, selection,
     * new String[] { Integer.toString(Imps.MessageType.QUEUED) }, null);
     * if (c == null) {
     * RemoteImService.debug("Query error while querying postponed messages");
     * return false;
     * }
     * else if (c.getCount() > 0)
     * {
     * result = true;
     * }
     * <p>
     * c.close();
     * return true;
     * <p>
     * }
     **/

    private boolean sendingPostponed = false;

    public void sendPostponedMessages() {

        if (!sendingPostponed) {
            sendingPostponed = true;

            String[] projection = new String[]{BaseColumns._ID, Imps.Messages.BODY,
                    Imps.Messages.PACKET_ID,
                    Imps.Messages.DATE, Imps.Messages.TYPE, Imps.Messages.IS_DELIVERED};
            String selection = Imps.Messages.TYPE + "=?";

            Cursor c = mContentResolver.query(mMessageURI, projection, selection,
                    new String[]{Integer.toString(Imps.MessageType.QUEUED)}, null);
            if (c == null) {
                return;
            }

            if (c.getCount() > 0) {
                ArrayList<String> messages = new ArrayList<String>();

                while (c.moveToNext())
                    messages.add(c.getString(1));

                removeMessageInDb(Imps.MessageType.QUEUED);

                for (String body : messages) {

                    if (body.startsWith("vfs:/") && (body.split(" ").length == 1)) {
                        String offerId = UUID.randomUUID().toString();
                        String mimeType = URLConnection.guessContentTypeFromName(body);
                        if (mimeType != null) {
                            offerData(offerId, body, mimeType);
                        }
                    } else {
                        sendMessage(body, false);
                    }
                }
            }

            c.close();

            sendingPostponed = false;
        }
    }

    public void registerChatListener(IChatListener listener) {
        if (listener != null) {
            mRemoteListeners.register(listener);

            if (mDataHandlerListener != null)
                mDataHandlerListener.checkLastTransferRequest();
        }
    }

    public void unregisterChatListener(IChatListener listener) {
        if (listener != null) {
            mRemoteListeners.unregister(listener);
        }
    }

    public void markAsRead() {
        if (mHasUnreadMessages) {

            /**
             * we want to keep the last message now
             ContentValues values = new ContentValues(1);
             values.put(Imps.Chats.LAST_UNREAD_MESSAGE, (String) null);
             mConnection.getContext().getContentResolver().update(mChatURI, values, null, null);
             */
            String baseUsername = mChatSession.getParticipant().getAddress().getBareAddress();
            mStatusBarNotifier.dismissChatNotification(mConnection.getProviderId(), baseUsername);

            mHasUnreadMessages = false;
        }
    }

    public void notifycationMemberLeft(String username) {
        ImEntity participant = mChatSession.getParticipant();
        if (mIsGroupChat) {

            ChatGroup group = (ChatGroup) participant;
            /**
             List<Contact> members = group.getMembers();
             for (Contact c : members) {
             if (username.equals(c.getAddress().getAddress())) {

             return c.getAddress().getResource();

             }
             }**/
            Contact groupMember = group.getMember(username);
            if (groupMember != null) {
                group.notifyMemberLeft(groupMember);
            }
        }
    }

    private String getNickName(String username) {
        ImEntity participant = mChatSession.getParticipant();
        if (mIsGroupChat) {

            ChatGroup group = (ChatGroup) participant;
            /**
             List<Contact> members = group.getMembers();
             for (Contact c : members) {
             if (username.equals(c.getAddress().getAddress())) {

             return c.getAddress().getResource();

             }
             }**/
            Contact groupMember = group.getMember(username);
            if (groupMember != null) {
                return groupMember.getAddress().getAddress();
            } else {
                // not found, impossible
                String[] parts = username.split("/");
                return parts[parts.length - 1];
            }
        } else {
            return ((Contact) participant).getName();
        }
    }

    private void onConvertToGroupChatSuccess(ChatGroup group) {
        Contact oldParticipant = (Contact) mChatSession.getParticipant();
        String oldAddress = getAddress();
        //    mChatSession.setParticipant(group);
        mChatSessionManager.updateChatSession(oldAddress, this);

        Uri oldChatUri = mChatURI;
        Uri oldMessageUri = mMessageURI;
        init(group, false);
        //copyHistoryMessages(oldParticipant);

        mContentResolver.delete(oldMessageUri, NON_CHAT_MESSAGE_SELECTION, null);
        mContentResolver.delete(oldChatUri, null, null);

        mListenerAdapter.notifyChatSessionConverted();
        mConvertingToGroupChat = false;
    }

    /**
     * private void copyHistoryMessages(Contact oldParticipant) {
     * List<Message> historyMessages = mChatSession.getHistoryMessages();
     * int total = historyMessages.size();
     * int start = total > MAX_HISTORY_COPY_COUNT ? total - MAX_HISTORY_COPY_COUNT : 0;
     * for (int i = start; i < total; i++) {
     * Message msg = historyMessages.get(i);
     * boolean incoming = msg.getFrom().equals(oldParticipant.getAddress());
     * String contact = incoming ? oldParticipant.getName() : null;
     * long time = msg.getDateTime().getTime();
     * insertMessageInDb(contact, msg.getBody(), time, incoming ? Imps.MessageType.INCOMING
     * : Imps.MessageType.OUTGOING);
     * }
     * }
     */

    private void insertOrUpdateChat(String message) {
        insertOrUpdateChat(message, System.currentTimeMillis());
    }

    private void insertOrUpdateChat(String message, long time) {
        if (!ConferenceUtils.isInvisibleMessage(message)) {
            ContentValues values = new ContentValues(2);

            values.put(Imps.Chats.LAST_MESSAGE_DATE, time);
            values.put(Imps.Chats.LAST_UNREAD_MESSAGE, message);

            values.put(Imps.Chats.GROUP_CHAT, mIsGroupChat);
            // ImProvider.insert() will replace the chat if it already exist.
            mContentResolver.insert(mChatURI, values);
        }
    }

    // Pattern for recognizing a URL, based off RFC 3986
    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static final Pattern aesGcmUrlPattern = Pattern.compile(
            "(?:^|[\\W])(aesgcm:\\/\\/)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);


    private String checkForLinkedMedia(String jid, String message, boolean allowWebDownloads) {
        Matcher matcher = aesGcmUrlPattern.matcher(message);

        //if we match the aesgcm crypto pattern, then it is a match
        if (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            return message.substring(matchStart, matchEnd);
        } else if (allowWebDownloads) {
            //if someone sends us a random URL, only get it if it is from the same host as the jabberid
            matcher = urlPattern.matcher(message);
            if (matcher.find()) {
                int matchStart = matcher.start(1);
                int matchEnd = matcher.end();
                String urlDownload = message.substring(matchStart, matchEnd);
                try {
                    String domain = JidCreate.bareFrom(jid).getDomain().toString();

                    //remove the conference subdomain when checking a match to the media upload
                    if (domain.contains("conference."))
                        domain = domain.replace("conference.", "");

                    if (urlDownload.contains(domain))
                        return urlDownload;
                } catch (XmppStringprepException se) {
                    //This shouldn't happeN!
                }
            }
        }

        return null;

    }

    private long insertOrUpdateGroupContactInDb(ChatGroup group) {
        // Insert a record in contacts table
        ContentValues values = new ContentValues(4);
        values.put(Imps.Contacts.USERNAME, group.getAddress().getAddress());
        values.put(Imps.Contacts.NICKNAME, group.getName());
        values.put(Imps.Contacts.CONTACTLIST, ContactListManagerAdapter.LOCAL_GROUP_LIST_ID);
        values.put(Imps.Contacts.TYPE, Imps.Contacts.TYPE_GROUP);

        Uri contactUri = ContentUris.withAppendedId(
                ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, mConnection.mProviderId),
                mConnection.mAccountId);

        ContactListManagerAdapter listManager = (ContactListManagerAdapter) mConnection
                .getContactListManager();

        long id = listManager.queryGroup(group);

        if (id == -1) {
            id = ContentUris.parseId(mContentResolver.insert(contactUri, values));
            for (Contact member : group.getMembers()) {
                insertGroupMemberInDb(member);
            }
        }

        return id;
    }

    private void insertGroupMemberInDb(Contact member) {
        String username = member.getAddress().getAddress();
        String nickname = getContactName(Address.stripResource(username));
        member.setName(nickname);

        insertOrUpdateGroupMemberInDb(username, nickname, member);
    }

    private String getContactName(String address) {
        // update locally
        if (address.equals(ImApp.sImApp.getDefaultUsername())) {
            long mAccountId = ImApp.sImApp.getDefaultAccountId();
            return Imps.Account.getAccountName(mContentResolver, mAccountId);
        }

        String name = Imps.Contacts.getNicknameFromAddress(mContentResolver, address);
        if (!TextUtils.isEmpty(name)) return name;
        return address;
    }

    private void insertOrUpdateGroupMemberInDb(String oldUsername, String oldNickname, Contact member) {

        if (mChatURI != null) {
            String username = member.getAddress().getAddress();
            String nickname = member.getName();

            ContentValues values = new ContentValues(4);
            values.put(Imps.GroupMembers.USERNAME, username);
            values.put(Imps.GroupMembers.NICKNAME, nickname);

            long groupId = ContentUris.parseId(mChatURI);
            Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);

            Map.Entry<String, String[]> whereEntry = getGroupMemberWhereClause(oldUsername, oldNickname);

            long databaseId = 0;
            Cursor c = mContentResolver.query(uri, new String[]{"_id"}, whereEntry.getKey(), whereEntry.getValue(), null);
            if (c != null) {
                if (c.moveToFirst()) {
                    databaseId = c.getLong(c.getColumnIndex("_id"));
                }
                c.close();
            }
            if (databaseId > 0) {
                mContentResolver.update(uri, values, "_id=?", new String[]{String.valueOf(databaseId)});
            } else {
                values.put(Imps.GroupMembers.ROLE, "none");
                values.put(Imps.GroupMembers.AFFILIATION, "none");
                mContentResolver.insert(uri, values);
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
                    Imps.GroupMembers.updateNicknameFromGroupUri(mContentResolver, uri, wpKMemberDtos.getIdentifier());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateGroupMemberRoleAndAffiliationInDb(Contact member, String role, String affiliation) {
        if (mChatURI != null) {
            String username = member.getAddress().getAddress();
            String nickname = member.getName();

            ContentValues values = new ContentValues(4);
            values.put(Imps.GroupMembers.ROLE, role);
            if (affiliation != null) {
                values.put(Imps.GroupMembers.AFFILIATION, affiliation);
            }

            long groupId = ContentUris.parseId(mChatURI);
            Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);

            Map.Entry<String, String[]> whereEntry = getGroupMemberWhereClause(username, nickname);
            mContentResolver.update(uri, values, whereEntry.getKey(), whereEntry.getValue());
        }
    }


    private void deleteAllGroupMembers() {

        if (mChatURI != null) {
            long groupId = ContentUris.parseId(mChatURI);
            Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);
            mContentResolver.delete(uri, null, null);
        }
        //  insertMessageInDb(member.getName(), null, System.currentTimeMillis(),
        //    Imps.MessageType.PRESENCE_UNAVAILABLE);
    }

    private Map.Entry<String, String[]> getGroupMemberWhereClause(String username, String nickname) {
        String whereClause = "";
        ArrayList<String> selection = new ArrayList<>();
        if (!TextUtils.isEmpty(nickname)) {
            whereClause += Imps.GroupMembers.NICKNAME + "=?";
            selection.add(nickname);
        }
        if (!TextUtils.isEmpty(username)) {
            if (whereClause.length() > 0) {
                whereClause += " OR ";
            }
            whereClause += Imps.GroupMembers.USERNAME + "=?";
            selection.add(username);
        }
        long groupId = ContentUris.parseId(mChatURI);
        if (whereClause.length() > 0) {
            whereClause = "(" + whereClause + ") AND " + Imps.GroupMembers.GROUP + "=?";
        } else {
            whereClause = Imps.GroupMembers.GROUP + "=?";
        }
        selection.add(String.valueOf(groupId));
        return new AbstractMap.SimpleEntry<String, String[]>(whereClause, selection.toArray(new String[0]));
    }

    private void deleteGroupMemberInDb(Contact member) {
        if (mChatURI != null) {
            String username = member.getAddress().getAddress();
            String nickname = member.getName();

            long groupId = ContentUris.parseId(mChatURI);
            Uri uri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, groupId);
            Map.Entry<String, String[]> entry = getGroupMemberWhereClause(username, nickname);
            mContentResolver.delete(uri, entry.getKey(), entry.getValue());
        }
        //  insertMessageInDb(member.getName(), null, System.currentTimeMillis(),
        //    Imps.MessageType.PRESENCE_UNAVAILABLE);
    }

    void insertPresenceUpdatesMsg(String contact, Presence presence) {
        int status = presence.getStatus();

        Integer previousStatus = mContactStatusMap.get(contact);
        if (previousStatus != null && previousStatus == status) {
            // don't insert the presence message if it's the same status
            // with the previous presence update notification
            return;
        }

        mContactStatusMap.put(contact, status);
        int messageType;
        switch (status) {
            case Presence.AVAILABLE:
                messageType = Imps.MessageType.PRESENCE_AVAILABLE;
                break;

            case Presence.AWAY:
            case Presence.IDLE:
                messageType = Imps.MessageType.PRESENCE_AWAY;
                break;

            case Presence.DO_NOT_DISTURB:
                messageType = Imps.MessageType.PRESENCE_DND;
                break;

            default:
                messageType = Imps.MessageType.PRESENCE_UNAVAILABLE;
                break;
        }

        if (mIsGroupChat) {
            insertMessageInDb(contact, null, System.currentTimeMillis(), messageType, null);
        } else {
            insertMessageInDb(null, null, System.currentTimeMillis(), messageType, null);
        }
    }

    private void removeMessageInDb(int type) {
        mContentResolver.delete(mMessageURI, Imps.Messages.TYPE + "=?",
                new String[]{Integer.toString(type)});
    }

    private Uri insertMessageInDb(String contact, String body, long time, int type, String mimeType) {
        return insertMessageInDb(contact, body, time, type, 0/*No error*/, nextID(), mimeType);
    }

    /**
     * A prefix helps to make sure that ID's are unique across mutliple instances.
     */
    private static String prefix = StringUtils.randomString(5) + "-";

    /**
     * Keeps track of the current increment, which is appended to the prefix to
     * forum a unique ID.
     */
    private static long id = 0;

    static String nextID() {
        return prefix + Long.toString(id++);
    }

    Uri insertMessageInDb(String contact, String body, long time, int type, int errCode, String id, String mimeType) {
        return Imps.insertMessageInDb(mContentResolver, mIsGroupChat, mContactId, false, contact, body, time, type, errCode, id, mimeType);
    }

    int updateMessageInDb(String id, int type, long time, String body) {

        if (body != null)
            Imps.updateMessageBody(mContentResolver, id, body, null);

        return Imps.updateMessageInDb(mContentResolver, id, type, time, mContactId);
    }

    public int updateMessageInDb(String id, String msg) {
        return Imps.updateMessageBodyInThreadByPacketId(mContentResolver, getChatUri(), mMessageURI, id, msg);
    }

    public int deleteMessageInDb(String id) {
        long lastMessageDate = Imps.Chats.getLastMessageDate(mContentResolver, getChatUri());
        long messageDate = Imps.Messages.getDate(mContentResolver, id);
        //Check is last message , reset history chat list
        if (messageDate != -1 && messageDate == lastMessageDate) {
            insertOrUpdateChat("");
        }
        return mContentResolver.delete(mMessageURI, Imps.Messages.PACKET_ID + "=?",
                new String[]{id});
    }


    class ListenerAdapter implements MessageListener, GroupMemberListener, OtrEngineListener {

        public synchronized boolean onIncomingMessage(ChatSession ses, final Message msg) {
//            Debug.d("message Id " + msg.getID());
            String body = msg.getBody();
            if (msg.getBody().startsWith(ConferenceConstant.DELETE_CHAT_FREFIX)) {
                String packet_id = msg.getBody().replace(ConferenceConstant.DELETE_CHAT_FREFIX, "");
                deleteMessageInDb(packet_id);
                deleteMessageInDb(msg.getID());
                return false;
            } else if (msg.getBody().startsWith(ConferenceConstant.EDIT_CHAT_FREFIX)) {
                String[] message_edit = ConferenceUtils.getEditedMessage(msg.getBody());
                updateMessageInDb(message_edit[0], message_edit[1]);
                deleteMessageInDb(msg.getID());
                return false;
            } else if (msg.getBody().startsWith(ConferenceConstant.SEND_BACKGROUND_CHAT_PREFIX)) {
                String imageName = msg.getBody().replace(ConferenceConstant.SEND_BACKGROUND_CHAT_PREFIX, "");
                ConferenceUtils.saveBitmapPreferences(imageName, msg.getFrom().getUser(), mConnection.getContext());
                Intent i = new Intent(ConferenceConstant.SEND_BACKGROUND_CHAT_PREFIX);
                mConnection.getContext().sendBroadcast(i);
            } else if (msg.getBody().startsWith(ConferenceConstant.ERROR_ENCRYPTION_FROM_IOS)) {
                Debug.d(ConferenceConstant.ERROR_ENCRYPTION_FROM_IOS);
                return false;
            } else if (msg.getBody().startsWith(ConferenceConstant.CONFERENCE_PREFIX))
                if (Constant.CONFERENCE_ENABLED) {
                    if (ses.getParticipant() instanceof Contact) {
                        ConferenceMessage conferenceMessage = new ConferenceMessage(body);
                        if (conferenceMessage.isMissedOrDeclined()) {
                            if (ConferenceActivity.getsIntance() != null)
                                ConferenceActivity.getsIntance().onDenyConference(conferenceMessage);
                            return true;
                        }
                    }
                } else {
                    return true;
                }
            String username = msg.getFrom().getAddress();
            String bareUsername = msg.getFrom().getBareAddress();
            String nickname = getNickName(username);
            String bareAddress = mIsGroupChat ? new XmppAddress(nickname).getBareAddress() : bareUsername;
            bareAddress = bareAddress.toLowerCase();
            Contact contact;
            try {
                String regexUUID = "[0-9a-f]{8}-[0-9a-f]{4}-[34][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";
                contact = mConnection.getContactListManager().getContactByAddress(bareUsername);
                if (contact != null && !contact.getAddress().getAddress().toLowerCase().contains(contact.getName().toLowerCase())) {
                    nickname = contact.getName();
                } else {
                    if (bareAddress.contains(Constant.EMAIL_DOMAIN) || bareAddress.equals(nickname.toLowerCase())) {
                        nickname = Imps.Contacts.getNicknameFromAddress(mContentResolver, bareAddress);
                        if (TextUtils.isEmpty(nickname)) {
                            if (bareAddress.contains(Constant.EMAIL_DOMAIN))
                                nickname = bareAddress.replace(Constant.EMAIL_DOMAIN, "");
                            else
                                nickname = bareAddress;
                        }
                    }

                    if (!TextUtils.isEmpty(nickname)) {
                        String accountName = Imps.Account.getAccountName(mContentResolver, mConnection.getAccountId());
                        if (nickname.equals(accountName)) {
                            msg.setType(Imps.MessageType.OUTGOING_ENCRYPTED_VERIFIED);
                        }
                    }
                }
            } catch (Exception e) {
            }

            long time = msg.getDateTime().getTime();

            if (msg.getID() != null
                    &&
                    Imps.messageExists(mContentResolver, msg.getID())) {
                return false; //this message is a duplicate
            }

            boolean allowWebDownloads = true;
            String mediaLink = checkForLinkedMedia(username, body, allowWebDownloads);

            if (mediaLink == null) {
                insertOrUpdateChat(body, time);

                boolean wasMessageSeen = false;

                Uri messageUri = null;

                if (msg.getID() == null)
                    messageUri = insertMessageInDb(nickname, body, time, msg.getType(), null);
                else
                    messageUri = insertMessageInDb(nickname, body, time, msg.getType(), 0, msg.getID(), null);

                if (messageUri == null) //couldn't write to the database, so return false
                    return false;


                if (TextUtils.isEmpty(nickname)) {
                    updateUnknownFriendInfoInGroup(messageUri, bareAddress, msg.getFrom().getResource());
                }

                try {
                    synchronized (mRemoteListeners) {
                        int N = mRemoteListeners.beginBroadcast();
                        for (int i = 0; i < N; i++) {
                            IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                            try {
                                boolean wasSeen = listener.onIncomingMessage(ChatSessionAdapter.this, msg);

                                if (wasSeen)
                                    wasMessageSeen = wasSeen;

                            } catch (RemoteException e) {
                                // The RemoteCallbackList will take care of removing the
                                // dead listeners.
                            }
                        }
                        mRemoteListeners.finishBroadcast();
                    }
                } catch (Exception e) {
                }
                //Check conference trigger
                checkTriggerConference(messageUri, bareAddress, nickname, msg);

                // Due to the move to fragments, we could have listeners for ChatViews that are not visible on the screen.
                // This is for fragments adjacent to the current one.  Therefore we can't use the existence of listeners
                // as a filter on notifications.
                String message = ConferenceUtils.getInfoMessage(mConnection.getContext(), body);
                if (!wasMessageSeen && !TextUtils.isEmpty(message)) {
                    if (isGroupChatSession()) {
                        if (!isMuted()) {
                            ChatGroup group = (ChatGroup) ses.getParticipant();
                            mStatusBarNotifier.notifyGroupChat(mConnection.getProviderId(), mConnection.getAccountId(),
                                    getId(), group.getAddress().getBareAddress(), group.getName(), nickname, message, false);
                        }
                    } else {
                        //reinstated body display here in the notification; perhaps add preferences to turn that off
                        mStatusBarNotifier.notifyChat(mConnection.getProviderId(), mConnection.getAccountId(),
                                getId(), bareUsername, nickname, message, false);
                    }
                }
            } else {
                try {
                    Downloader dl = new Downloader();
                    File fileDownload = dl.openSecureStorageFile(mContactId + "", mediaLink);

                    OutputStream storageStream = new info.guardianproject.iocipher.FileOutputStream(fileDownload);
                    boolean downloaded = dl.get(mediaLink, storageStream);

                    if (downloaded) {
                        String mimeType = dl.getMimeType();

                        try {
                            //boolean isVerified = getDefaultOtrChatSession().isKeyVerified(bareUsername);
                            //int type = isVerified ? Imps.MessageType.INCOMING_ENCRYPTED_VERIFIED : Imps.MessageType.INCOMING_ENCRYPTED;
                            int type = Imps.MessageType.INCOMING;
                            if (mediaLink.startsWith("aesgcm"))
                                type = Imps.MessageType.INCOMING_ENCRYPTED_VERIFIED;

                            Uri vfsUri = SecureMediaStore.vfsUri(fileDownload.getAbsolutePath());

                            long startTime = System.currentTimeMillis();
                            insertOrUpdateChat(vfsUri.toString(), startTime);

                            Uri messageUri = Imps.insertMessageInDb(service.getContentResolver(),
                                    mIsGroupChat, getId(),
                                    true, nickname,
                                    vfsUri.toString(), startTime, type,
                                    0, msg.getID(), mimeType);

                            int percent = (int) (100);

                            String[] path = mediaLink.split("/");
                            String sanitizedPath = SystemServices.sanitize(path[path.length - 1]);

                            int N = 0;

                            try {
                                N = mRemoteListeners.beginBroadcast();
                                for (int i = 0; i < N; i++) {
                                    IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                                    try {
                                        listener.onIncomingFileTransferProgress(sanitizedPath, percent);
                                    } catch (RemoteException e) {
                                        // The RemoteCallbackList will take care of removing the
                                        // dead listeners.
                                    }
                                }
                                mRemoteListeners.finishBroadcast();
                            } catch (Exception e) {
                            }

                            if (N == 0) {

                                if (!isMuted())
                                    mStatusBarNotifier.notifyChat(mConnection.getProviderId(), mConnection.getAccountId(),
                                            getId(), bareUsername, nickname, service.getString(R.string.file_notify_text, mimeType, nickname), false);
                            }
                        } catch (Exception e) {
                            Log.e(ImApp.LOG_TAG, "Error updating file transfer progress", e);
                        }
                    } else {
                        //file doesn't exist... ignore?
                    }

                } catch (Exception e) {
                    Log.e(ImApp.LOG_TAG, "error downloading incoming media", e);

                }
            }

            mHasUnreadMessages = true;
            return true;
        }

        private void updateUnknownFriendInfoInGroup(final Uri uri, final String bareAddress, String jid) {
            RestAPI.GetDataWrappy(ImApp.sImApp, RestAPI.getMemberByIdUrl(jid), new RestAPIListener() {
                @Override
                public void OnComplete(String s) {
                    Debug.d(s);
                    try {
                        WpKMemberDto wpKMemberDtos = new Gson().fromJson(s, new TypeToken<WpKMemberDto>() {
                        }.getType());
                        Imps.updateMessageNickname(mContentResolver, uri, wpKMemberDtos.getIdentifier());
                        Imps.GroupMembers.updateNicknameFromGroup(mContentResolver, bareAddress, wpKMemberDtos.getIdentifier());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void checkTriggerConference(final Uri uri, final String bareAddress, String nickname, Message message) {
            if (Constant.CONFERENCE_ENABLED) {
                String body = message.getBody();
                if (!TextUtils.isEmpty(body) && body.startsWith(ConferenceConstant.CONFERENCE_PREFIX)) {
                    long startTime = message.getDateTime().getTime();
                    ConferenceMessage conference = new ConferenceMessage(body);
                    if (System.currentTimeMillis() - startTime > Constant.MISSED_CALL_TIME || conference.isEnded()) {
                        if (conference.isEnded()) {
                            conference.accept();
                        } else {
                            conference.missed();
                        }
                        updateMessageInDb(message.getID(), conference.toString());
                    } else {
                        ConferenceCall conferenceCall = new ConferenceCall(bareAddress, nickname, body, uri, getChatUri());
                        conferenceCall.setGroup(isGroupChatSession());
                        Intent intent = ConferencePopupActivity.newIntent(conferenceCall);
                        mConnection.getContext().startActivity(intent);
                    }
                }
            }
        }

        public void onSendMessageError(ChatSession ses, final Message msg, final ImErrorInfo error) {
            insertMessageInDb(null, null, System.currentTimeMillis(), Imps.MessageType.OUTGOING,
                    error.getCode(), null, null);

            try {
                final int N = mRemoteListeners.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                    try {
                        listener.onSendMessageError(ChatSessionAdapter.this, msg, error);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing the
                        // dead listeners.
                    }
                }
                mRemoteListeners.finishBroadcast();
            } catch (Exception e) {
            }
        }

        public void onSubjectChanged(ChatGroup group, String subject) {
            if (mChatURI != null) {
                ContentValues values1 = new ContentValues(1);
                values1.put(Imps.Contacts.NICKNAME, subject);
                ContentValues values = values1;

                Uri uriContact = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, mContactId);
                mContentResolver.update(uriContact, values, null, null);

                try {
                    synchronized (mRemoteListeners) {
                        final int N = mRemoteListeners.beginBroadcast();
                        for (int i = 0; i < N; i++) {
                            IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                            try {
                                listener.onGroupSubjectChanged(ChatSessionAdapter.this);
                            } catch (RemoteException e) {
                                // The RemoteCallbackList will take care of removing the
                                // dead listeners.
                            }
                        }
                        mRemoteListeners.finishBroadcast();
                    }
                } catch (Exception e) {
                }
            }
        }

        public void onMembersReset() {
            deleteAllGroupMembers();
        }

        public void onMemberJoined(ChatGroup group, final Contact contact) {
            insertGroupMemberInDb(contact);

            try {
                final int N = mRemoteListeners.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                    try {
                        listener.onContactJoined(ChatSessionAdapter.this, contact);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing the
                        // dead listeners.
                    }
                }
                mRemoteListeners.finishBroadcast();
            } catch (Exception e) {
            }
        }

        @Override
        public void onMemberRoleChanged(ChatGroup chatGroup, Contact contact, String role, String affiliation) {
            updateGroupMemberRoleAndAffiliationInDb(contact, role, affiliation);
            try {
                final int N = mRemoteListeners.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                    try {
                        listener.onContactRoleChanged(ChatSessionAdapter.this, contact);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing the
                        // dead listeners.
                    }
                }
                mRemoteListeners.finishBroadcast();
            } catch (Exception e) {
            }
        }

        public void onMemberLeft(ChatGroup group, final Contact contact) {
            deleteGroupMemberInDb(contact);

            try {
                final int N = mRemoteListeners.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                    try {
                        listener.onContactLeft(ChatSessionAdapter.this, contact);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing the
                        // dead listeners.
                    }
                }
                mRemoteListeners.finishBroadcast();
            } catch (Exception e) {
            }
        }

        public void onError(ChatGroup group, final ImErrorInfo error) {
            // TODO: insert an error message?
            try {
                final int N = mRemoteListeners.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                    try {
                        listener.onInviteError(ChatSessionAdapter.this, error);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing the
                        // dead listeners.
                    }
                }
                mRemoteListeners.finishBroadcast();
            } catch (Exception e) {
            }
        }

        public void notifyChatSessionConverted() {
            try {
                final int N = mRemoteListeners.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                    try {
                        listener.onConvertedToGroupChat(ChatSessionAdapter.this);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing the
                        // dead listeners.
                    }
                }
                mRemoteListeners.finishBroadcast();
            } catch (Exception e) {
            }
        }

        @Override
        public void onIncomingReceipt(ChatSession ses, String id) {
            Imps.updateConfirmInDb(mContentResolver, mContactId, id, true);
        }

        @Override
        public void onMessagePostponed(ChatSession ses, String id) {
            updateMessageInDb(id, Imps.MessageType.QUEUED, -1, null);
        }

        @Override
        public void onReceiptsExpected(ChatSession ses, boolean isExpected) {
            // TODO

        }

        @Override
        public void sessionStatusChanged(SessionID sessionID) {

            if (sessionID.getRemoteUserId().equals(mChatSession.getParticipant().getAddress().getAddress()))
                onStatusChanged(mChatSession, OtrChatManager.getInstance().getSessionStatus(sessionID));


        }


        @Override
        public void onStatusChanged(ChatSession session, SessionStatus status) {
            try {
                final int N = mRemoteListeners.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                    try {
                        listener.onStatusChanged(ChatSessionAdapter.this);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing the
                        // dead listeners.   // TODO Auto-generated method stub
                    }
                }
                mRemoteListeners.finishBroadcast();
            } catch (Exception e) {
            }
            mDataHandler.onOtrStatusChanged(status);

            if (status == SessionStatus.ENCRYPTED) {
                sendPostponedMessages();
            }

            mLastSessionStatus = status;

        }

        @Override
        public void onIncomingDataRequest(ChatSession session, Message msg, byte[] value) {
            mDataHandler.onIncomingRequest(msg.getFrom(), msg.getTo(), value);
        }

        @Override
        public void onIncomingDataResponse(ChatSession session, Message msg, byte[] value) {
            mDataHandler.onIncomingResponse(msg.getFrom(), msg.getTo(), value);
        }

        @Override
        public void onIncomingTransferRequest(final OtrDataHandler.Transfer transfer) {

        }


    }

    class ChatConvertor implements GroupListener, GroupMemberListener {
        private ChatGroupManager mGroupMgr;
        private String mGroupName;

        public ChatConvertor() {
            mGroupMgr = mConnection.mGroupManager;
        }

        public void convertToGroupChat(String nickname) {
            mGroupMgr.addGroupListener(this);
            mGroupName = "G" + System.currentTimeMillis();
            try {
                mGroupMgr.createChatGroupAsync(mGroupName, nickname, nickname);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onGroupCreated(ChatGroup group) {
            if (mGroupName.equalsIgnoreCase(group.getName())) {
                mGroupMgr.removeGroupListener(this);
                group.addMemberListener(this);
                mGroupMgr.inviteUserAsync(group, (Contact) mChatSession.getParticipant());
            }
        }

        @Override
        public void onMembersReset() {
        }

        public void onMemberJoined(ChatGroup group, Contact contact) {
            if (mChatSession.getParticipant().equals(contact)) {
                onConvertToGroupChatSuccess(group);
            }

            mContactStatusMap.put(contact.getName(), contact.getPresence().getStatus());
        }

        @Override
        public void onMemberRoleChanged(ChatGroup chatGroup, Contact contact, String role, String affiliation) {

        }

        public void onSubjectChanged(ChatGroup group, String subject) {
        }

        public void onGroupDeleted(ChatGroup group) {
        }

        public void onGroupError(int errorType, String groupName, ImErrorInfo error) {
        }

        public void onJoinedGroup(ChatGroup group) {
        }

        public void onLeftGroup(ChatGroup group) {
        }

        public void onError(ChatGroup group, ImErrorInfo error) {
        }

        public void onMemberLeft(ChatGroup group, Contact contact) {
            mContactStatusMap.remove(contact.getName());
        }
    }

    @Override
    public void setDataListener(IDataListener dataListener) throws RemoteException {

        mDataListener = dataListener;
        mDataHandler.setDataListener(mDataListener);
    }

    @Override
    public void setIncomingFileResponse(String transferForm, boolean acceptThis, boolean acceptAll) {

        mAcceptTransfer = acceptThis;
        mAcceptAllTransfer = acceptAll;
        mWaitingForResponse = false;

        mDataHandler.acceptTransfer(mLastFileUrl, transferForm);

    }

    class DataHandlerListenerImpl extends IDataListener.Stub {

        @Override
        public void onTransferComplete(boolean outgoing, String offerId, String from, String url, String mimeType, String filePath) {


            try {


                if (outgoing) {
                    Imps.updateConfirmInDb(service.getContentResolver(), mContactId, offerId, true);
                } else {

                    try {
                        boolean isVerified = getDefaultOtrChatSession().isKeyVerified(from);

                        int type = isVerified ? Imps.MessageType.INCOMING_ENCRYPTED_VERIFIED : Imps.MessageType.INCOMING_ENCRYPTED;

                        long time = System.currentTimeMillis();
                        insertOrUpdateChat(filePath, time);

                        Uri messageUri = Imps.insertMessageInDb(service.getContentResolver(),
                                mIsGroupChat, getId(),
                                true, from,
                                filePath, time, type,
                                0, offerId, mimeType);

                        int percent = (int) (100);

                        String[] path = url.split("/");
                        String sanitizedPath = SystemServices.sanitize(path[path.length - 1]);

                        int N = 0;

                        try {
                            N = mRemoteListeners.beginBroadcast();
                            for (int i = 0; i < N; i++) {
                                IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                                try {
                                    listener.onIncomingFileTransferProgress(sanitizedPath, percent);
                                } catch (RemoteException e) {
                                    // The RemoteCallbackList will take care of removing the
                                    // dead listeners.
                                }
                            }
                            mRemoteListeners.finishBroadcast();
                        } catch (Exception e) {
                        }

                        if (N == 0) {
                            String nickname = getNickName(from);
                            if (!isMuted())
                                mStatusBarNotifier.notifyChat(mConnection.getProviderId(), mConnection.getAccountId(),
                                        getId(), from, nickname, service.getString(R.string.file_notify_text, mimeType, nickname), false);
                        }
                    } catch (Exception e) {
                        Log.e(ImApp.LOG_TAG, "Error updating file transfer progress", e);
                    }

                }

                /**
                 if (mimeType != null && mimeType.startsWith("audio"))
                 {
                 MediaPlayer mp = new MediaPlayer();
                 try {
                 mp.setDataSource(file.getCanonicalPath());

                 mp.prepare();
                 mp.start();

                 } catch (IOException e) {
                 // TODO Auto-generated catch block
                 //e.printStackTrace();
                 }
                 }*/

            } catch (Exception e) {
                //   mHandler.showAlert(service.getString(R.string.error_chat_file_transfer_title), service.getString(R.string.error_chat_file_transfer_body));
                OtrDebugLogger.log("error reading file", e);
            }


        }

        @Override
        public synchronized void onTransferFailed(boolean outgoing, String offerId, String from, String url, String reason) {


            String[] path = url.split("/");
            String sanitizedPath = SystemServices.sanitize(path[path.length - 1]);

            try {
                final int N = mRemoteListeners.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                    try {
                        listener.onIncomingFileTransferError(sanitizedPath, reason);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing the
                        // dead listeners.
                    }
                }
                mRemoteListeners.finishBroadcast();
            } catch (Exception e) {
            }
        }

        @Override
        public synchronized void onTransferProgress(boolean outgoing, String offerId, String from, String url, float percentF) {

            int percent = (int) (100 * percentF);

            String[] path = url.split("/");
            String sanitizedPath = SystemServices.sanitize(path[path.length - 1]);

            try {
                final int N = mRemoteListeners.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                    try {
                        listener.onIncomingFileTransferProgress(sanitizedPath, percent);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing the
                        // dead listeners.
                    }
                }
            } catch (Exception e) {
                Log.w(ImApp.LOG_TAG, "error broadcasting progress: " + e);
            } finally {
                mRemoteListeners.finishBroadcast();
            }
        }


        private String mLastTransferFrom;
        private String mLastTransferUrl;

        public void checkLastTransferRequest() {
            if (mLastTransferFrom != null) {
                onTransferRequested(mLastTransferUrl, mLastTransferFrom, mLastTransferFrom, mLastTransferUrl);
                mLastTransferFrom = null;
                mLastTransferUrl = null;
            }
        }

        @Override
        public synchronized boolean onTransferRequested(String offerId, String from, String to, String transferUrl) {

            mAcceptTransfer = false;
            mWaitingForResponse = true;
            mLastFileUrl = transferUrl;

            if (mAcceptAllTransfer) {
                mAcceptTransfer = true;
                mWaitingForResponse = false;
                mLastTransferFrom = from;
                mLastTransferUrl = transferUrl;

                mDataHandler.acceptTransfer(mLastFileUrl, from);
            } else {
                try {
                    final int N = mRemoteListeners.beginBroadcast();

                    if (N > 0) {
                        for (int i = 0; i < N; i++) {
                            IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                            try {
                                listener.onIncomingFileTransfer(from, transferUrl);
                            } catch (RemoteException e) {
                                // The RemoteCallbackList will take care of removing the
                                // dead listeners.
                            }
                        }
                    } else {
                        mLastTransferFrom = from;
                        mLastTransferUrl = transferUrl;
                        String nickname = getNickName(from);

                        //reinstated body display here in the notification; perhaps add preferences to turn that off
                        if (!isMuted())
                            mStatusBarNotifier.notifyChat(mConnection.getProviderId(), mConnection.getAccountId(),
                                    getId(), from, nickname, "Incoming file request", false);
                    }
                } finally {
                    mRemoteListeners.finishBroadcast();
                }

                mAcceptTransfer = false; //for now, wait for the user callback
            }

            return mAcceptTransfer;

        }


    }

    public boolean sendPushWhitelistToken(@NonNull String token) {
        if (mConnection.getState() == ImConnection.SUSPENDED) {
            // TODO Is it possible to postpone a TLV message? e.g: insertMessageInDb with type QUEUED
            return false;
        }

        // Whitelist tokens are intended for one recipient, for now
        if (isGroupChatSession())
            return false;

        Message msg = new Message("");

        msg.setFrom(mConnection.getLoginUser().getAddress());
        msg.setType(Imps.MessageType.OUTGOING);

        mChatSession.sendPushWhitelistTokenAsync(msg, new String[]{token});
        return true;
    }

    public synchronized void setContactTyping(Contact contact, boolean isTyping) {
        try {
            int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IChatListener listener = mRemoteListeners.getBroadcastItem(i);
                try {
                    listener.onContactTyping(ChatSessionAdapter.this, contact, isTyping);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing the
                    // dead listeners.
                }
            }
            mRemoteListeners.finishBroadcast();
        } catch (IllegalStateException ise) {
            //sometimes this broadcast overlaps with others
        }
    }

    public void sendTypingStatus(boolean isTyping) {
        // mConnection.sendTypingStatus("fpp", isTyping);
    }

    public boolean isEncrypted() {
        if (mChatSession.canOmemo()) {
            return true;
        } else {
            IOtrChatSession otrChatSession = getDefaultOtrChatSession();
            if (otrChatSession != null) {
                try {

                    SessionStatus chatStatus = SessionStatus.values()[otrChatSession.getChatStatus()];

                    if (chatStatus == SessionStatus.ENCRYPTED) {
                        //boolean isVerified = otrChatSession.isKeyVerified(mChatSession.getParticipant().getAddress().toString());
                        // holder.mStatusIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_outline_black_18dp));

                        return true;
                    }
                } catch (Exception e) {

                }
            }
        }

        return false;

    }

    @Override
    public void setGroupChatSubject(String subject) throws RemoteException {
        try {
            if (isGroupChatSession()) {
                ChatGroup group = (ChatGroup) mChatSession.getParticipant();
                getGroupManager().setGroupSubject(group, subject);

                //update the database
                ContentValues values1 = new ContentValues(1);
                values1.put(Imps.Contacts.NICKNAME, subject);
                ContentValues values = values1;

                Uri uriContact = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, mContactId);
                mContentResolver.update(uriContact, values, null, null);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Contact getGroupChatOwner() {
        try {
            if (isGroupChatSession()) {
                ChatGroup group = (ChatGroup) mChatSession.getParticipant();
                return group.getOwner();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setMuted(boolean muted) {
        int newChatType = muted ? Imps.ChatsColumns.CHAT_TYPE_MUTED : Imps.ChatsColumns.CHAT_TYPE_ACTIVE;
        ContentValues values = new ContentValues();
        values.put(Imps.Chats.CHAT_TYPE, newChatType);
        mContentResolver.update(mChatURI, values, null, null);
        mIsMuted = muted;
    }

    public boolean isMuted() {
        return mIsMuted;
    }

    private void initMuted() {
        int type = Imps.ChatsColumns.CHAT_TYPE_ACTIVE;

        String[] projection = {Imps.Chats.CHAT_TYPE};
        Cursor c = mContentResolver.query(mChatURI, projection, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                type = c.getInt(0);
            }
            c.close();
        }
        mIsMuted = type == Imps.ChatsColumns.CHAT_TYPE_MUTED;
    }

}
