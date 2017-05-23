package com.timer.util.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.hookedonplay.decoviewlib.DecoView;
import com.timer.util.R;
import com.timer.util.receivers.TimerExpiredReceiver;
import com.timer.util.utils.DecoViewUtils;
import com.timer.util.utils.DimensionsUtils;
import com.timer.util.utils.PrefUtils;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private enum TimerState {
        STOPPED,
        RUNNING
    }

    @BindView(R.id.main_timer_button)
    Button timerButton;

    @BindView(R.id.dynamicArcViewBack)
    DecoView decoViewBack;

    @BindView(R.id.dynamicArcViewFront)
    DecoView decoViewFront;

    private static final long TIMER_LENGTH = 10 * 1000 + 1000; // 3600 seconds plus adjustments
    private long timeToGo;
    private CountDownTimer countDownTimer;
    private TimerState state;
    private int seriesIndex;

    PrefUtils preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        decoViewBack.addSeries(DecoViewUtils.buildBase(true, DimensionsUtils.smallDecoViewLineWidth));
        seriesIndex = decoViewFront.addSeries(DecoViewUtils.buildSeries(Color.GREEN, 0, DimensionsUtils.largeDecoViewLineWidth, null));

        preferences = new PrefUtils(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initTimer();
        removeAlarm();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (state == TimerState.RUNNING) {
            countDownTimer.cancel();
            setAlarm();
        }
    }

    private void initTimer() {
        long startTime = preferences.getStartedTime();

        if (startTime > 0) {
            timeToGo = (TIMER_LENGTH - (getNow() - startTime));

            if (timeToGo <= 0) { // timer expired
                onTimerFinish();
            } else {
                startTimer();
            }
        } else {
            resetTimer();
        }
    }

    private void startTimer() {
        preferences.setStartedTime(getNow());

        countDownTimer = new CountDownTimer(timeToGo, 500) {
            public void onTick(long millisUntilFinished) {
                timeToGo -= 500;
                updateTimeUI();
            }

            public void onFinish() {
                onTimerFinish();
                updateTimeUI();
            }
        }.start();

        state = TimerState.RUNNING;
        updateButtonStart();
    }

    private void onTimerFinish() {
        Toast.makeText(this, R.string.timer_finished, Toast.LENGTH_SHORT).show();
        resetTimer();
    }

    private void resetTimer() {
        preferences.setStartedTime(0);
        timeToGo = TIMER_LENGTH;
        state = TimerState.STOPPED;
        updateButtonStop();
    }

    private void updateButtonStart() {
        timerButton.setText(R.string.stop);
    }

    private void updateButtonStop() {
        timerButton.setText(R.string.start);
    }

    private void updateTimeUI() {
        decoViewFront.addEvent(DecoViewUtils.buildSeriesShowEvent((TIMER_LENGTH - timeToGo) / 100.0f, seriesIndex, 0, 250));
    }

    @OnClick(R.id.main_timer_button)
    public void onButtonClicked() {
        if (state == TimerState.STOPPED) {
            startTimer();
        } else {
            countDownTimer.onFinish();
            countDownTimer.cancel();
        }
    }

    private long getNow() {
        Calendar rightNow = Calendar.getInstance();
        return rightNow.getTimeInMillis();
    }

    public void setAlarm() {
        long wakeUpTime = (preferences.getStartedTime() + TIMER_LENGTH);
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