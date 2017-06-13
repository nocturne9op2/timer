package com.timer.util.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.hookedonplay.decoviewlib.DecoView;
import com.timer.util.R;
import com.timer.util.entities.MyCountDownTimer;
import com.timer.util.receivers.TimerExpiredReceiver;
import com.timer.util.utils.BackgroundUtils;
import com.timer.util.utils.DecoViewUtils;
import com.timer.util.utils.DimensionsUtils;
import com.timer.util.utils.PrefUtils;

import java.io.File;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private enum TimerState {
        STOPPED,
        RUNNING
    }

    private RelativeLayout relativeLayout;
    private Button timerButton;
    private DecoView backDecoView;
    private DecoView frontDecoView;
    private int seriesIndex;

    private BackgroundUtils background;
    private PrefUtils preferences;

    private Handler handler;
    private Runnable runnable;

    private static final long TIMER_LENGTH = 10;
    private long timeToGo;
    private MyCountDownTimer countDownTimer;
    private TimerState timerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        timerButton = (Button) findViewById(R.id.buttonTimer);
        backDecoView = (DecoView) findViewById(R.id.dynamicArcViewBack);
        frontDecoView = (DecoView) findViewById(R.id.dynamicArcViewFront);

        background = new BackgroundUtils(this, relativeLayout);
        preferences = new PrefUtils(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBackground();
        initHandler();
        initTimer();
        initTimeUI();
        initButton();
        removeAlarm();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeHandler();
        removeTimeUI();

        if (timerState == TimerState.RUNNING) {
            countDownTimer.cancel();
            setAlarm();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            trimCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void trimCache() {
        try {
            File dir = getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }


    private void initBackground() {
        background.init(getNowInHourOfDay());
    }

    private void initHandler() {
        handler = new Handler();
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                updateBackground();
                handler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private void removeHandler() {
        handler.removeCallbacks(runnable);
    }

    private void initTimer() {
        long startTime = preferences.getStartedTime();

        if (startTime > 0) {
            timeToGo = (TIMER_LENGTH - (getNowInSeconds() - startTime));

            if (timeToGo <= 0) { // timer expired
                resetTimer();
            } else {
                startTimer();
            }
        } else {
            resetTimer();
        }
    }

    private void initTimeUI() {
        backDecoView.addSeries(DecoViewUtils.buildBase(true, DimensionsUtils.SMALL_DECOVIEW_LINEWIDTH));
        seriesIndex = frontDecoView.addSeries(DecoViewUtils.buildSeries(Color.argb(255, 255, 255, 255), (TIMER_LENGTH - timeToGo) * 10f, DimensionsUtils.LARGE_DECOVIEW_LINEWIDTH, null));
    }

    private void removeTimeUI() {
        frontDecoView.deleteAll();
        backDecoView.deleteAll();
    }

    private void initButton() {
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timerState == TimerState.STOPPED) {
                    preferences.setStartedTime(getNowInSeconds());
                    startTimer();
                } else {
                    countDownTimer.onFinish();
                    countDownTimer.cancel();
                }
            }
        });
    }

    private void startTimer() {
        countDownTimer = new MyCountDownTimer(timeToGo * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                timeToGo -= 1;
                updateTimeUI();
            }

            public void onFinish() {
                resetTimer();
                playNotificationRingtone();
                updateTimeUI();
            }
        }.start();

        timerButton.setText(R.string.stop);
        timerState = TimerState.RUNNING;
    }

    private void resetTimer() {
        preferences.setStartedTime(0);
        timeToGo = TIMER_LENGTH;
        timerButton.setText(R.string.start);
        timerState = TimerState.STOPPED;
    }

    private void updateBackground() {
        background.update(getNowInHourOfDay());
    }

    private void updateTimeUI() {
        frontDecoView.addEvent(DecoViewUtils.buildSeriesShowEvent((TIMER_LENGTH - timeToGo) * 10f, seriesIndex, 0, 250));
    }

    private void playNotificationRingtone() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        ringtone.play();
    }

    private long getNowInSeconds() {
        return Calendar.getInstance().getTimeInMillis() / 1000;
    }

    private int getNowInHourOfDay() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public void setAlarm() {
        long wakeUpTime = (preferences.getStartedTime() + TIMER_LENGTH) * 1000;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TimerExpiredReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(wakeUpTime, pendingIntent), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent);
        }
    }

    public void removeAlarm() {
        Intent intent = new Intent(this, TimerExpiredReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}