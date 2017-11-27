package net.wrappy.im.ui;

import android.os.Bundle;
import android.util.Log;

import net.wrappy.im.BuildConfig;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;

import java.util.Map;

public class ConferenceActivity extends JitsiMeetActivity {
    JitsiMeetView view;

    @Override
    protected JitsiMeetView initializeView() {
        view = super.initializeView();

        // XXX In order to increase (1) awareness of API breakages and (2) API
        // coverage, utilize JitsiMeetViewListener in the Debug configuration of
        // the app.
        if (BuildConfig.DEBUG && view != null) {
            view.setListener(new JitsiMeetViewListener() {
                private void on(String name, Map<String, Object> data) {
                    // Log with the tag "ReactNative" in order to have the log
                    // visible in react-native log-android as well.
                    Log.d(
                            "ReactNative",
                            JitsiMeetViewListener.class.getSimpleName() + " "
                                    + name + " "
                                    + data);
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
                public void onConferenceWillJoin(Map<String, Object> data) {
                    on("CONFERENCE_WILL_JOIN", data);
                }

                @Override
                public void onConferenceWillLeave(Map<String, Object> data) {
                    on("CONFERENCE_WILL_LEAVE", data);
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
        Bundle config = new Bundle();
        config.putBoolean("startWithAudioMuted", false);
        config.putBoolean("startWithVideoMuted", true);
        Bundle urlObject = new Bundle();
        urlObject.putBundle("config", config);
        urlObject.putString("url", "https://meet.jit.si/Test123");
        if (view != null) {
            view.loadURLObject(urlObject);
        }
    }
}