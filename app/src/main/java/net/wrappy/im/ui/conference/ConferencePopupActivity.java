package net.wrappy.im.ui.conference;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.model.ConferenceMessage;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.ui.ConferenceActivity;
import net.wrappy.im.util.Debug;

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
    Uri messageUri;
    boolean isGroupChat;
    ConferenceMessage conference;

    private static String KEY_NICK_NAME = "nickname";
    private static String KEY_MESSAGE = "message";
    private static String KEY_GROUP = "isGroup";

    public static Intent newIntentP2P(String nickname, String message, Uri uri) {
        return newIntent(nickname, message, uri, false);
    }

    public static Intent newIntentGroup(String nickname, String message, Uri uri) {
        return newIntent(nickname, message, uri, true);
    }

    private static Intent newIntent(String nickname, String message, Uri uri, boolean isGroupChat) {
        Intent intent = new Intent(ImApp.sImApp, ConferencePopupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_NICK_NAME, nickname);
        intent.putExtra(KEY_MESSAGE, message);
        intent.putExtra(KEY_GROUP, isGroupChat);
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
}
