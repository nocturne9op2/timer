package com.timer.util.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
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
import com.timer.util.utils.NotificationUtils;
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
    protected void onStart() {
        super.onStart();
        initBackground();
        initBackgroundHandler();
        initTimer();
        initTimerUI();
        initButtonUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        removeNotification();
        if (timerState == TimerState.RUNNING) {
            removeAlarm();
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
    protected void onPause() {
        super.onPause();
        if (timerState == TimerState.RUNNING) {
            countDownTimer.cancel();
            setAlarm();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeTimerUI();
        removeBackgroundHandler();
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

    private void updateBackground() {
        background.update(getNowInHourOfDay());
    }

    private void initBackgroundHandler() {
        handler = new Handler();
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                updateBackground();
                handler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private void removeBackgroundHandler() {
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

    private void startTimer() {
        countDownTimer = new MyCountDownTimer(timeToGo * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                timeToGo -= 1;
                updateTimerUI();
            }

            public void onFinish() {
                resetTimer();
                issueNotification();
                updateTimerUI();
            }
        }.start();

        timerState = TimerState.RUNNING;
        updateButtonUI();
    }

    private void resetTimer() {
        preferences.setStartedTime(0);
        timeToGo = TIMER_LENGTH;
        timerState = TimerState.STOPPED;
        updateButtonUI();
    }

    private void initTimerUI() {
        RelativeLayout.LayoutParams smallSize = new RelativeLayout.LayoutParams((int) DimensionsUtils.SMALL_DECOVIEW_WIDTH, (int) DimensionsUtils.SMALL_DECOVIEW_WIDTH);
        RelativeLayout.LayoutParams largeSize = new RelativeLayout.LayoutParams((int) DimensionsUtils.LARGE_DECOVIEW_WIDTH, (int) DimensionsUtils.LARGE_DECOVIEW_WIDTH);
        smallSize.addRule(RelativeLayout.CENTER_IN_PARENT);
        largeSize.addRule(RelativeLayout.CENTER_IN_PARENT);

        backDecoView.setLayoutParams(smallSize);
        backDecoView.addSeries(DecoViewUtils.buildBase(true, DimensionsUtils.SMALL_DECOVIEW_LINEWIDTH));

        frontDecoView.setLayoutParams(largeSize);
        seriesIndex = frontDecoView.addSeries(DecoViewUtils.buildSeries(Color.argb(255, 255, 255, 255), (TIMER_LENGTH - timeToGo) * 10f, DimensionsUtils.LARGE_DECOVIEW_LINEWIDTH, null));
    }

    private void updateTimerUI() {
        frontDecoView.addEvent(DecoViewUtils.buildSeriesShowEvent((TIMER_LENGTH - timeToGo) * 10f, seriesIndex, 0, Resources.getSystem().getInteger(android.R.integer.config_shortAnimTime)));
    }

    private void removeTimerUI() {
        frontDecoView.deleteAll();
        backDecoView.deleteAll();
    }

    private void initButtonUI() {
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timerState == TimerState.STOPPED) {
                    preferences.setStartedTime(getNowInSeconds());
                    startTimer();
                    removeNotification();
                } else {
                    countDownTimer.cancel();
                    resetTimer();
                    updateTimerUI();
                }
            }
        });
    }

    private void updateButtonUI() {
        if (timerState == TimerState.STOPPED) {
            timerButton.setText(R.string.start);
        } else {
            timerButton.setText(R.string.stop);
        }
    }

    private void issueNotification() {
        NotificationUtils.issue(this, NotificationUtils.buildBase(this, null));
    }

    private void removeNotification() {
        NotificationUtils.remove(this, NotificationUtils.NOTIFICATION_ID);
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

    private long getNowInSeconds() {
        return Calendar.getInstance().getTimeInMillis() / 1000;
    }

    private int getNowInHourOfDay() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }
}