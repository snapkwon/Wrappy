package net.wrappy.im.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewAdapter;

import java.util.Map;

public class ConferenceActivity extends BaseActivity {

    private JitsiMeetView view;

    @Override
    public void onBackPressed() {
        if (!view.onBackPressed()) {
            // Invoke the default handler if it wasn't handled by React.
            super.onBackPressed();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new JitsiMeetView(this);
        view.setListener(new JitsiMeetViewAdapter() {
            @Override
            public void onConferenceFailed(Map<String, Object> data) {
                super.onConferenceFailed(data);
                log("onConferenceFailed");
            }

            private void log(String log) {
                Log.d("tag", log);
            }

            @Override
            public void onConferenceJoined(Map<String, Object> data) {
                super.onConferenceJoined(data);
                log("onConferenceJoined");
            }

            @Override
            public void onConferenceLeft(Map<String, Object> data) {
                super.onConferenceLeft(data);
                log("onConferenceLeft");
            }

            @Override
            public void onConferenceWillJoin(Map<String, Object> data) {
                super.onConferenceWillJoin(data);
                log("onConferenceWillJoin");
            }

            @Override
            public void onConferenceWillLeave(Map<String, Object> data) {
                super.onConferenceWillLeave(data);
                log("onConferenceWillLeave");
            }

            @Override
            public void onLoadConfigError(Map<String, Object> data) {
                super.onLoadConfigError(data);
                log("onLoadConfigError");
            }
        });
        Bundle config = new Bundle();
        config.putBoolean("startWithAudioMuted", true);
        config.putBoolean("startWithVideoMuted", false);
        Bundle urlObject = new Bundle();
        urlObject.putBundle("config", config);

        urlObject.putString("url", "https://meet.jit.si/tata2");
//        view.setWelcomePageEnabled(true);
        view.setBackgroundColor(Color.WHITE);
        view.loadURLObject(config);

        setContentView(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        view.dispose();
        view = null;

        JitsiMeetView.onHostDestroy(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (view != null)
            view.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        view.onHostPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        view.onHostResume(this);
    }
}