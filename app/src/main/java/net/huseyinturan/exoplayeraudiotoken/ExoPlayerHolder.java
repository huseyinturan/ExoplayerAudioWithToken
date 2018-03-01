package net.huseyinturan.exoplayeraudiotoken;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

/**
 * Created by zh on 2/28/18.
 */

public class ExoPlayerHolder implements PlayerAdapter {
    private static final String TAG = "ExoPlayerHolder";
    private static final int PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 1000;

    private SimpleExoPlayer exoPlayer;
    private Context mContext;
    private PlaybackInfoListener mPlaybackInfoListener;
    private Uri mRecordUri;
    private String mToken;
    private Handler handler;
    private Runnable mSeekbarPositionUpdateTask;
    private boolean isAlreadyLoaded = false;
    private State currentState = State.INIT;

    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            Log.i(TAG, "onTimelineChanged");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.i(TAG, "onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.i(TAG, "onLoadingChanged " + isLoading);

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i(TAG, "onPlayerStateChanged: playWhenReady = " + String.valueOf(playWhenReady)
                    + " playbackState = " + playbackState);
            switch (playbackState) {
                case ExoPlayer.STATE_ENDED:
                    Log.i(TAG, "Playback ended!");
                    seekTo(0);
                    stopUpdatingCallbackWithPosition();
                    pause();
                    initializeProgressCallback();
                    break;
                case ExoPlayer.STATE_READY:
                    Log.i(TAG, "ExoPlayer ready! pos: " + getCurrentPosition()
                            + " max: " + getDuration());
                    initializeProgressCallback();
                    if (!isAlreadyLoaded) {
                        isAlreadyLoaded = true;
                        exoPlayer.setPlayWhenReady(true);
                        if (mPlaybackInfoListener != null)
                            mPlaybackInfoListener.onPlayingStarted();
                        currentState = State.PLAYING;
                    }
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    Log.i(TAG, "Playback buffering!");
                    break;
                case ExoPlayer.STATE_IDLE:
                    Log.i(TAG, "ExoPlayer idle!");
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.i(TAG, "onPlayerError: " + error.getMessage());
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onError(error.getMessage());
            }
            stopUpdatingCallbackWithPosition();
            currentState = State.ERROR;
        }

        @Override
        public void onPositionDiscontinuity() {
            Log.i(TAG, "onPositionDiscontinuity");
        }
    };

    public ExoPlayerHolder(Context context) {
        mContext = context;
    }

    public void setPlaybackInfoListener(PlaybackInfoListener playbackInfoListener) {
        this.mPlaybackInfoListener = playbackInfoListener;
    }

    public boolean setMedia(String recordUrl, String token) {
        if (isAlreadyLoaded)
            return false;
        this.mRecordUri = Uri.parse(recordUrl);
        this.mToken = token;
        return true;
    }

    @Override
    public void prepare() {

        if (isAlreadyLoaded)
            return;

        TrackSelector trackSelector = new DefaultTrackSelector();

        LoadControl loadControl = new DefaultLoadControl();

        exoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector, loadControl);

        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("ExoPlayer");
        dataSourceFactory.setDefaultRequestProperty(Constants.AUTH_HEADER, Constants.AUTH_KEY + this.mToken);

        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource audioSource = new ExtractorMediaSource(this.mRecordUri, dataSourceFactory, extractorsFactory, null, null);
        exoPlayer.addListener(eventListener);

        exoPlayer.prepare(audioSource);
        startUpdatingCallbackWithPosition();
        if (mPlaybackInfoListener != null)
            mPlaybackInfoListener.onLoading();
        currentState = State.LOADING;
    }

    @Override
    public void close() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (mPlaybackInfoListener != null)
            mPlaybackInfoListener.onClosed();
        stopUpdatingCallbackWithPosition();
        initializeProgressCallback();
        isAlreadyLoaded = false;
        currentState = State.INIT;
    }

    @Override
    public void play() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            if (handler == null)
                startUpdatingCallbackWithPosition();
            if (isAlreadyLoaded) {
                currentState = State.PLAYING;
                if (mPlaybackInfoListener != null)
                    mPlaybackInfoListener.onPlaying();
            }
        }
    }

    @Override
    public void pause() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            if (isAlreadyLoaded) {
                currentState = State.PAUSED;
                if (mPlaybackInfoListener != null)
                    mPlaybackInfoListener.onPaused();
            }
        }
    }

    public int getDuration() {
        int result = 0;
        if (exoPlayer != null)
            result = (int) exoPlayer.getDuration();
        return result;
    }

    public int getCurrentPosition() {
        int result = 0;
        if (exoPlayer != null)
            result = (int) exoPlayer.getCurrentPosition();
        return result;
    }

    public int getBufferedPosition() {
        int result = 0;
        if (exoPlayer != null)
            result = (int) exoPlayer.getBufferedPosition();
        return result;
    }

    @Override
    public void seekTo(long positionMs) {
        if (exoPlayer != null)
            exoPlayer.seekTo(positionMs);
    }

    public State getState() {
        return currentState;
    }

    private void startUpdatingCallbackWithPosition() {
        if (handler == null) {
            handler = new Handler();
        }
        if (mSeekbarPositionUpdateTask == null) {
            mSeekbarPositionUpdateTask = new Runnable() {
                @Override
                public void run() {
                    updateProgressCallbackTask();
                    handler.postDelayed(this, PLAYBACK_POSITION_REFRESH_INTERVAL_MS);
                }
            };
        }
        handler.post(mSeekbarPositionUpdateTask);
    }

    private void stopUpdatingCallbackWithPosition() {
        if (handler != null && mSeekbarPositionUpdateTask != null) {
            handler.removeCallbacks(mSeekbarPositionUpdateTask);
            mSeekbarPositionUpdateTask = null;
            if (mPlaybackInfoListener != null) {
                mPlaybackInfoListener.onPositionChanged(0);
            }
            handler = null;
        }
    }

    private void updateProgressCallbackTask() {
        int currentPosition = getCurrentPosition();
        int bufferedPosition = getBufferedPosition();
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener.onBufferingChanged(bufferedPosition);
            mPlaybackInfoListener.onPositionChanged(currentPosition);
        }
    }

    public void initializeProgressCallback() {
        final int duration = getDuration();
        final int position = getCurrentPosition();
        final int bufferedPosition = getBufferedPosition();
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener.onBufferingChanged(bufferedPosition);
            mPlaybackInfoListener.onDurationChanged(duration);
            mPlaybackInfoListener.onPositionChanged(position);
        }
    }

}
