package com.timer.util.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hookedonplay.decoviewlib.DecoView;
import com.timer.util.R;
import com.timer.util.entities.NewCountDownTimer;
import com.timer.util.receivers.TimerExpiredReceiver;
import com.timer.util.utils.BackgroundUtils;
import com.timer.util.utils.DecoViewUtils;
import com.timer.util.utils.DimensionsUtils;
import com.timer.util.utils.PrefUtils;

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

    private Handler handler;
    private Runnable runnable;

    private static final long TIMER_LENGTH = 3600;
    private long timeToGo;
    private NewCountDownTimer countDownTimer;
    private TimerState state;

    private BackgroundUtils background;
    private PrefUtils preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        handler.removeCallbacks(runnable);

        if (state == TimerState.RUNNING) {
            countDownTimer.cancel();
            setAlarm();
        }
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

    private void initTimer() {
        long startTime = preferences.getStartedTime();

        if (startTime > 0) {
            timeToGo = (TIMER_LENGTH - (getNowInSeconds() - startTime));

            if (timeToGo <= 0) { // timer expired
                onTimerFinish();
            } else {
                startTimer();
            }
        } else {
            resetTimer();
        }
    }

    private void initTimeUI() {
        backDecoView.addSeries(DecoViewUtils.buildBase(true, DimensionsUtils.smallDecoViewLineWidth));
        seriesIndex = frontDecoView.addSeries(DecoViewUtils.buildSeries(Color.argb(255, 255, 255, 255), (TIMER_LENGTH - timeToGo) / 36f, DimensionsUtils.largeDecoViewLineWidth, null));
    }

    private void initButton() {
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == TimerState.STOPPED) {
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
        countDownTimer = new NewCountDownTimer(timeToGo * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                timeToGo -= 1;
                updateTimeUI();
            }

            public void onFinish() {
                onTimerFinish();
                updateTimeUI();
            }
        }.start();

        timerButton.setText(R.string.stop);
        state = TimerState.RUNNING;
    }

    private void onTimerFinish() {
        Toast.makeText(this, R.string.timer_finished, Toast.LENGTH_SHORT).show();
        resetTimer();
    }

    private void resetTimer() {
        preferences.setStartedTime(0);
        timeToGo = TIMER_LENGTH;
        timerButton.setText(R.string.start);
        state = TimerState.STOPPED;
    }

    private void updateBackground() {
        background.update(getNowInHourOfDay());
    }

    private void updateTimeUI() {
        frontDecoView.addEvent(DecoViewUtils.buildSeriesShowEvent((TIMER_LENGTH - timeToGo) / 36f, seriesIndex, 0, 250));
    }

    private long getNowInSeconds() {
        Calendar rightNow = Calendar.getInstance();
        return rightNow.getTimeInMillis() / 1000;
    }

    private int getNowInHourOfDay() {
        Calendar rightNow = Calendar.getInstance();
        return rightNow.get(Calendar.HOUR_OF_DAY);
    }

    public void setAlarm() {
        long wakeUpTime = (preferences.getStartedTime() + TIMER_LENGTH) * 1000;
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TimerExpiredReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(wakeUpTime, sender), sender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, wakeUpTime, sender);
        }
    }

    public void removeAlarm() {
        Intent intent = new Intent(this, TimerExpiredReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }
}