package net.wrappy.im.ui.conference;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.model.Address;
import net.wrappy.im.model.ConferenceMessage;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.ui.ConferenceActivity;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.LogCleaner;

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
    Uri messageUri;
    boolean isGroupChat;
    ConferenceMessage conference;

    private static String KEY_NICK_NAME = "nickname";
    private static String KEY_MESSAGE = "message";
    private static String KEY_GROUP = "isGroup";
    private static String KEY_ADDRESS = "address";

    public static Intent newIntentP2P(String address, String nickname, String message, Uri uri) {
        return newIntent(address, nickname, message, uri, false);
    }

    public static Intent newIntentGroup(String address, String nickname, String message, Uri uri) {
        return newIntent(address, nickname, message, uri, true);
    }

    private static Intent newIntent(String address, String nickname, String message, Uri uri, boolean isGroupChat) {
        Intent intent = new Intent(ImApp.sImApp, ConferencePopupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_NICK_NAME, nickname);
        intent.putExtra(KEY_MESSAGE, message);
        intent.putExtra(KEY_GROUP, isGroupChat);
        intent.putExtra(KEY_ADDRESS, address);
        intent.setData(uri);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_ringing_dialog);
        ButterKnife.bind(this);
        bindViews();
    }

    private void bindViews() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        if (intent != null) {
            name = intent.getStringExtra(KEY_NICK_NAME);
            message = intent.getStringExtra(KEY_MESSAGE);
            isGroupChat = intent.getBooleanExtra(KEY_GROUP, false);
            mRemoteAddress = intent.getStringExtra(KEY_ADDRESS);
            messageUri = intent.getData();
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
