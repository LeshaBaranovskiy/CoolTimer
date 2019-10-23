package com.example.cooltimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ankushgrover.hourglass.Hourglass;

public class MainActivity extends AppCompatActivity {

    private Button buttonStart;
    private Button buttonPause;
    private Button buttonStop;
    private TextView textView;
    private SeekBar seekBar;
    private Hourglass hourglass;

    private boolean isPaused;
    private boolean isButtonStopPressed = false;

    private int max = 200;
    private long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        buttonStart = findViewById(R.id.buttonStart);
        buttonPause = findViewById(R.id.buttonPause);
        buttonStop = findViewById(R.id.buttonStop);

        buttonStop.setVisibility(View.INVISIBLE);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(max);
        seekBar.setProgress(30);

        buttonPause.setVisibility(View.INVISIBLE);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long progressInMillis = progress * 1000;
                updateTimer(progressInMillis);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void startTimer(View view) {
        seekBar.setEnabled(false);
        buttonStart.setVisibility(View.INVISIBLE);
        buttonPause.setVisibility(View.VISIBLE);
        buttonStop.setVisibility(View.VISIBLE);

        if (time == 0) {
            time = seekBar.getProgress() * 1000;
        }

        hourglass = new Hourglass(time, 1000) {
            @Override
            public void onTimerTick(long timeRemaining) {
                time = timeRemaining;
                if (timeRemaining < 1000) {
                    isButtonStopPressed = false;
                }
                updateTimer(timeRemaining);
            }

            @Override
            public void onTimerFinish() {
                if (!isButtonStopPressed) {
                    updateTimer(0);
                    seekBar.setProgress(0);
                }
                resetTimerView();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (sharedPreferences.getBoolean("enable_sound", true) && !isButtonStopPressed) {
                    int raw;
                    String melodyName = sharedPreferences.getString("timer_melody", "bell");

                    if (melodyName.equals("bell")) {
                        raw = R.raw.bell_sound;
                    } else if (melodyName.equals("alarm siren")) {
                        raw = R.raw.alarm_siren_sound;
                    } else {
                        raw = R.raw.bip_sound;
                    }
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), raw);
                    mediaPlayer.start();
                }

            }
        };
        if (!isPaused) {
            hourglass.startTimer();
        } else {
            hourglass.resumeTimer();
        }
    }

    public void pauseTimer(View view) {
        isPaused = true;

        buttonStart.setText(R.string.text_resume);

        hourglass.pauseTimer();
        buttonStart.setVisibility(View.VISIBLE);
        buttonPause.setVisibility(View.INVISIBLE);
    }

    public void resetTimer(View view) {
        isButtonStopPressed = true;

        hourglass.stopTimer();
        time = 0;

        updateTimer(seekBar.getProgress() * 1000);
        resetTimerView();
    }

    public void updateTimer(long millisUntilFinished) {
        String secondsString;
        String minutesString;

        int minutes = (int) millisUntilFinished/1000/60;
        int seconds = (int) millisUntilFinished/1000 - (minutes * 60);

        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = "" + minutes;
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        textView.setText(String.format("%s:%s", minutesString, secondsString));
    }

    private void resetTimerView() {
        seekBar.setEnabled(true);
        buttonStart.setVisibility(View.VISIBLE);
        buttonStart.setText(R.string.button_start);
        buttonPause.setVisibility(View.INVISIBLE);
        buttonStop.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent openSettings = new Intent(this, SettingsActivity.class);
            startActivity(openSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
