package net.huseyinturan.exoplayeraudiotoken;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Formatter;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private SeekBar seekPlayerProgress;
    private Button btnPlay, btnPause, btnClose, btnLoad;
    private TextView txtCurrentTime, txtEndTime, txtMessage;
    private PlayerAdapter playerAdapter;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects() //google leaks, not us
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath() // cannot activate it because of:  https://github.com/wasabeef/picasso-transformations/issues/27
                .build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLoadButton();
        initPlayButton();
        initPauseButton();
        initCloseButton();
        initSeekBar();
        initTxt();

        playerAdapter = new ExoPlayerHolder(this);
        playerAdapter.setPlaybackInfoListener(new PlaybackInfoListener() {
            @Override
            void onDurationChanged(int duration) {
                Log.d(TAG, "onDurationChanged " + duration);
                seekPlayerProgress.setMax(duration);
                txtEndTime.setText(stringForTime(duration));
            }

            @Override
            void onBufferingChanged(int position) {
                seekPlayerProgress.setSecondaryProgress(position);
            }

            @Override
            void onPositionChanged(int position) {
                txtCurrentTime.setText(stringForTime(position));
                seekPlayerProgress.setProgress(position);
            }

            @Override
            void onPlayingStarted() {
                Log.d(TAG, "onPlayingStarted");
                txtMessage.setText("Playing Started");
            }

            @Override
            void onLoading() {
                Log.d(TAG, "onLoading");
                txtMessage.setText("Loading...");
            }

            @Override
            void onError(String message) {
                Log.d(TAG, "onError " + message);
                txtMessage.setText("Error " + message);
            }

            @Override
            void onPaused() {
                Log.d(TAG, "Paused ");
                txtMessage.setText("Paused");
            }

            @Override
            void onClosed() {
                Log.d(TAG, "onClosed ");
                txtMessage.setText("Closed ");
            }

            @Override
            void onPlaying() {
                Log.d(TAG, "onPlaying ");
                txtMessage.setText("Playing ");
            }
        });
    }

    private void initPlayButton() {
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerAdapter.play();
            }
        });
    }


    private void initPauseButton() {
        btnPause = (Button) findViewById(R.id.btnPause);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerAdapter.pause();
            }
        });
    }

    private void initCloseButton() {
        btnClose = (Button) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerAdapter.close();
            }
        });
    }

    private void initLoadButton() {
        btnLoad = (Button) findViewById(R.id.btnLoad);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerAdapter.setMedia("https://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3", "Token");
                playerAdapter.prepare();
            }
        });
    }

    private void initTxt() {
        txtMessage = (TextView) findViewById(R.id.text_message);
        txtCurrentTime = (TextView) findViewById(R.id.time_current);
        txtEndTime = (TextView) findViewById(R.id.player_end_time);
    }

    private String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder;
        Formatter mFormatter;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void initSeekBar() {
        seekPlayerProgress = (SeekBar) findViewById(R.id.mediacontroller_progress);
        seekPlayerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                playerAdapter.seekTo(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

}
