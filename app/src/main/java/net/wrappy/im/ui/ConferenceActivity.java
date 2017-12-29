package net.wrappy.im.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import net.wrappy.im.BuildConfig;
import net.wrappy.im.model.ConferenceMessage;
import net.wrappy.im.util.BundleKeyConstant;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;

import java.util.Map;

public class ConferenceActivity extends JitsiMeetActivity {
    JitsiMeetView view;
    private static final String AUDIO_MUTED = "startWithAudioMuted";
    private static final String VIDEO_MUTED = "startWithVideoMuted";

    private int numberParticipants = 0;
    String roomId;

    private static ConferenceActivity sIntance;

    public static void startVideoCall(Context context, String roomId) {
        startConference(context, roomId, false);
    }

    public static void startAudioCall(Context context, String roomId) {
        startConference(context, roomId, true);
    }

    private static void startConference(Context context, String roomId, boolean isAudioCall) {
        Intent intent = new Intent(context, ConferenceActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(VIDEO_MUTED, isAudioCall);
        bundle.putBoolean(AUDIO_MUTED, !isAudioCall);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BundleKeyConstant.ROOM_ID, roomId);
        context.startActivity(intent);
    }

    public static ConferenceActivity getsIntance() {
        return sIntance;
    }

    public void onDenyConference(ConferenceMessage message) {
        if (!TextUtils.isEmpty(roomId) && message.getRoomId().equals(roomId)) {
            finish();
        }
    }

    @Override
    protected JitsiMeetView initializeView() {
        view = super.initializeView();

        // XXX In order to increase (1) awareness of API breakages and (2) API
        // coverage, utilize JitsiMeetViewListener in the Debug configuration of
        // the app.
        if (view != null) {
            view.setListener(new JitsiMeetViewListener() {
                private void on(String name, Map<String, Object> data) {
                    // Log with the tag "ReactNative" in order to have the log
                    // visible in react-native log-android as well.
                    if (BuildConfig.DEBUG) {
                        Log.d(
                                "ReactNative",
                                JitsiMeetViewListener.class.getSimpleName() + " "
                                        + name + " "
                                        + data);
                    }
                }

                @Override
                public void onConferenceFailed(Map<String, Object> data) {
                    on("CONFERENCE_FAILED", data);
                }

                @Override
                public void onConferenceJoined(Map<String, Object> data) {
                    on("CONFERENCE_JOINED", data);
                }

                @Override
                public void onConferenceLeft(Map<String, Object> data) {
                    on("CONFERENCE_LEFT", data);
                }

                @Override
                public void onParticipantJoined(Map<String, Object> data) {
                    on("PARTICIPANT_JOINED", data);
                    if (data.containsKey("url")) {
                        numberParticipants++;
                    }
                }

                @Override
                public void onParticipantLeft(Map<String, Object> data) {
                    on("PARTICIPANT_LEFT", data);
                    numberParticipants--;
                    if (numberParticipants == 0) {
                        finish();
                    }
                }

                @Override
                public void onConferenceWillJoin(Map<String, Object> data) {
                    on("CONFERENCE_WILL_JOIN", data);
                }

                @Override
                public void onConferenceWillLeave(Map<String, Object> data) {
                    on("CONFERENCE_WILL_LEAVE", data);
                    finish();
                }

                @Override
                public void onLoadConfigError(Map<String, Object> data) {
                    on("LOAD_CONFIG_ERROR", data);
                }
            });
        }
        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // As this is the Jitsi Meet app (i.e. not the Jitsi Meet SDK), we do
        // want the Welcome page to be enabled. It defaults to disabled in the
        // SDK at the time of this writing but it is clearer to be explicit
        // about what we want anyway.
        setWelcomePageEnabled(false);
        super.onCreate(savedInstanceState);
        sIntance = this;
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle config = new Bundle();
            config.putBoolean(AUDIO_MUTED, intent.getBooleanExtra(AUDIO_MUTED, false));
            config.putBoolean(VIDEO_MUTED, intent.getBooleanExtra(VIDEO_MUTED, false));
            Bundle urlObject = new Bundle();
            urlObject.putBundle("config", config);
            roomId = intent.getStringExtra(BundleKeyConstant.ROOM_ID);
            urlObject.putString("url", "https://meet.jit.si/" + roomId);
//            urlObject.putString("url", String.format(ConferenceConstant.CONFERENCE_HOST, roomId));
            if (view != null) {
                view.loadURLObject(urlObject);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        sIntance = null;
        super.onDestroy();
    }
}