package net.wrappy.im.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import net.wrappy.im.ImApp;
import net.wrappy.im.ui.widgets.VisualizerView;
import net.wrappy.im.util.HttpMediaStreamer;
import net.wrappy.im.util.Utils;

public class AudioPlayer {
    private Context mContext;
    private String mFileName;
    private String mMimeType;

    private static MediaPlayer mediaPlayer;
    private HttpMediaStreamer streamer;

    private Visualizer mVisualizer;
    private VisualizerView mVisualizerView;
    private TextView mInfoView;

    private int mDuration = -1;
    private boolean mPrepared = false;
    private boolean mPlayOnPrepare = false;

    private OnFinishPlaying onFinishPlaying;

    public interface OnFinishPlaying {
        void onFinishPlaying();
    }

    public void setOnFinishPlaying(OnFinishPlaying onFinishPlaying) {
        this.onFinishPlaying = onFinishPlaying;
    }

    public AudioPlayer(Context context, String fileName, String mimeType, VisualizerView visualizerView, TextView infoView) throws Exception {
        mContext = context.getApplicationContext();
        mFileName = fileName;
        mMimeType = mimeType;
        mVisualizerView = visualizerView;
        mInfoView = infoView;

    }

    public int getDuration() {
        return mDuration;
    }

    public void play() {

        if (mPrepared) {

            int permissionCheck = ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.RECORD_AUDIO);

            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                if (mVisualizer == null)
                    setupVisualizerFxAndUI();

                if (!mVisualizer.getEnabled())
                    mVisualizer.setEnabled(true);
            }

            mediaPlayer.start();
        } else {
            mPlayOnPrepare = true;
            new InitAudioTask().execute(this);
        }
    }

    private static class InitAudioTask extends AsyncTask<AudioPlayer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(AudioPlayer... audioPlayers) {
            try {
                audioPlayers[0].initPlayer();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public boolean isPlaying() {
        if (mediaPlayer != null)
            return mediaPlayer.isPlaying();
        else
            return false;
    }

    public boolean isPaused() {
        if (mediaPlayer != null)
            return (!mediaPlayer.isPlaying()) && (mediaPlayer.getCurrentPosition() > 0);
        else
            return false;
    }

    public void stop() {
        killPlayer();
    }

    private void killPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (streamer != null) {
            streamer.destroy();
            streamer = null;
        }
    }

    public void initPlayer() throws Exception {

        info.guardianproject.iocipher.File fileStream = new info.guardianproject.iocipher.File(mFileName);

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();

            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if (fileStream.exists()) {
            streamer = new HttpMediaStreamer(fileStream, mMimeType);
            Uri uri = streamer.getUri();
            mediaPlayer.setDataSource(mContext, uri);
        } else {
            mediaPlayer.setDataSource(mFileName);
        }

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {

                Log.d("AudioPlayer", "there was an error loading music: " + i + " " + i1);
                return true;
            }

        });
        mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {

                mPrepared = true;
                mDuration = mediaPlayer.getDuration();
                mInfoView.setText(Utils.formatDurationMedia(getDuration()));

                if (mPlayOnPrepare)
                    play();

            }
        });
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                //killPlayer();
                if (mVisualizer != null)
                    mVisualizer.setEnabled(false);

                if (onFinishPlaying != null) {
                    onFinishPlaying.onFinishPlaying();
                }
            }
        });

        mediaPlayer.prepareAsync();
    }


    private void setupVisualizerFxAndUI() {

        try {

            // Create the Visualizer object and attach it to our media player.
            mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            mVisualizer.setDataCaptureListener(
                    new Visualizer.OnDataCaptureListener() {
                        public void onWaveFormDataCapture(Visualizer visualizer,
                                                          byte[] bytes, int samplingRate) {
                            mVisualizerView.updateVisualizer(bytes);
                        }

                        public void onFftDataCapture(Visualizer visualizer,
                                                     byte[] bytes, int samplingRate) {
                        }
                    }, Visualizer.getMaxCaptureRate() / 2, true, false);

        } catch (RuntimeException re) {
            Log.w(ImApp.LOG_TAG, "unable to init audio player visualizaer", re);
        }
    }


}
