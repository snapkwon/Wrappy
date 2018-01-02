package net.wrappy.im.ui.conference;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.model.Address;
import net.wrappy.im.model.ConferenceCall;
import net.wrappy.im.model.ConferenceMessage;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.ui.ConferenceActivity;
import net.wrappy.im.util.BundleKeyConstant;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.LogCleaner;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by USER on 11/30/2017.
 */

public class ConferencePopupActivity extends Activity {
    @BindView(R.id.name)
    TextView tvNameOfContact;

    String name;
    String message;
    String mRemoteAddress;
    Uri messageUri, chatUri;
    boolean isGroupChat;
    ConferenceMessage conference;

    public static Intent newIntent(ConferenceCall conferenceCall) {
        Intent intent = new Intent(ImApp.sImApp, ConferencePopupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BundleKeyConstant.NICK_NAME_KEY, conferenceCall.getNickname());
        intent.putExtra(BundleKeyConstant.KEY_MESSAGE, conferenceCall.getBody());
        intent.putExtra(BundleKeyConstant.KEY_GROUP, conferenceCall.isGroup());
        intent.putExtra(BundleKeyConstant.ADDRESS_KEY, conferenceCall.getBareAddress());
        intent.putExtra(BundleKeyConstant.MESSAGE_URI_KEY, conferenceCall.getMessageUri());
        intent.putExtra(BundleKeyConstant.CHAT_URI_KEY, conferenceCall.getChaturi());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_ringing_dialog);
        ButterKnife.bind(this);
        bindViews();
        new Handler().postDelayed(new TimerCallDetect(this), Constant.MISSED_CALL_TIME);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private static final class TimerCallDetect implements Runnable {
        WeakReference<ConferencePopupActivity> reference;

        public TimerCallDetect(ConferencePopupActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            if (reference != null && reference.get() != null) {
                reference.get().missedCall();
            }
        }
    }

    private void bindViews() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        if (intent != null) {
            name = intent.getStringExtra(BundleKeyConstant.NICK_NAME_KEY);
            message = intent.getStringExtra(BundleKeyConstant.KEY_MESSAGE);
            isGroupChat = intent.getBooleanExtra(BundleKeyConstant.KEY_GROUP, false);
            mRemoteAddress = intent.getStringExtra(BundleKeyConstant.ADDRESS_KEY);
            messageUri = intent.getParcelableExtra(BundleKeyConstant.MESSAGE_URI_KEY);
            chatUri = intent.getParcelableExtra(BundleKeyConstant.CHAT_URI_KEY);
            tvNameOfContact.setText(name);
            conference = new ConferenceMessage(message);
        } else {
            finish();
        }
    }

    @OnClick(R.id.btnAccept)
    void acceptCall() {
        doConference();
        finish();
    }

    @OnClick(R.id.btnDecline)
    void declineCall() {
        if (conference != null) {
            conference.decline();
            sendMessage(conference.toString());
            updateMessageBody(conference.toString());
        }
    }

    void missedCall() {
        if (conference != null) {
            conference.missed();
            String message = conference.toString();
            sendMessage(conference.toString());
            updateMessageBody(conference.toString());
            insertOrUpdateChat(message);
        }
        finish();
    }

    private void insertOrUpdateChat(String message) {
        Imps.Chats.insertOrUpdateChat(getContentResolver(), chatUri, message, isGroupChat);
    }

    public void startAudioConference(String roomId) {
        ConferenceActivity.startAudioCall(getApplicationContext(), roomId);
    }

    public void startVideoConference(String roomId) {
        ConferenceActivity.startVideoCall(getApplicationContext(), roomId);
    }

    private void doConference() {
        if (conference != null) {
            conference.accept();
            Debug.d("doConference");
            updateMessageBody(conference.toString());
            if (conference.isAudio()) {
                startAudioConference(conference.getRoomId());
            } else {
                startVideoConference(conference.getRoomId());
            }
        }
    }

    private void updateMessageBody(String body) {
        Imps.updateMessageBodyInDb(getContentResolver(), messageUri, body);
        finish();
    }

    boolean sendMessage(String msg) {
        //don't send empty messages
        if (TextUtils.isEmpty(msg.trim())) {
            return false;
        }

        //otherwise get the session, create if necessary, and then send
        IChatSession session = getChatSession();

        if (session == null)
            session = createChatSession();

        if (session != null) {
            try {

                session.sendMessage(msg, true);
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

    public IChatSession getChatSession() {

        try {
            IImConnection mConn = ImApp.getConnection(ImApp.sImApp.getDefaultProviderId(), ImApp.sImApp.getDefaultAccountId());
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

    private IChatSession createChatSession() {
        IImConnection mConn = ImApp.getConnection(ImApp.sImApp.getDefaultProviderId(), ImApp.sImApp.getDefaultAccountId());
        try {
            if (mConn != null) {
                IChatSessionManager sessionMgr = mConn.getChatSessionManager();
                if (sessionMgr != null) {

                    String remoteAddress = mRemoteAddress;
                    IChatSession session = null;

                    if (isGroupChat) {
                        //Contact contactGroup = new Contact(new XmppAddress(mRemoteAddress),mRemoteNickname,Imps.Contacts.TYPE_GROUP);
                        session = sessionMgr.createMultiUserChatSession(mRemoteAddress, name, null, false);

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
}
