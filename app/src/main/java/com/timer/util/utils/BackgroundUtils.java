package com.timer.util.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.RelativeLayout;

import com.timer.util.R;

public class BackgroundUtils {
    private enum BackgroundState {
        EARLY_MORNING,
        SUNRISE,
        LATE_MORNING_TO_AFTERNOON,
        SUNSET,
        EARLY_NIGHT,
        LATE_NIGHT
    }

    private Drawable gradient1;
    private Drawable gradient2;
    private Drawable gradient3;
    private Drawable gradient4;
    private Drawable gradient5;
    private Drawable gradient6;

    private TransitionDrawable transition1;
    private TransitionDrawable transition2;
    private TransitionDrawable transition3;
    private TransitionDrawable transition4;
    private TransitionDrawable transition5;
    private TransitionDrawable transition6;

    private RelativeLayout relativeLayout;
    private BackgroundState backgroundState;

    public BackgroundUtils(Context context, RelativeLayout relativeLayout) {
        gradient1 = ContextCompat.getDrawable(context, R.drawable.gradient1);
        gradient2 = ContextCompat.getDrawable(context, R.drawable.gradient2);
        gradient3 = ContextCompat.getDrawable(context, R.drawable.gradient3);
        gradient4 = ContextCompat.getDrawable(context, R.drawable.gradient4);
        gradient5 = ContextCompat.getDrawable(context, R.drawable.gradient5);
        gradient6 = ContextCompat.getDrawable(context, R.drawable.gradient6);

        transition1 = (TransitionDrawable) ContextCompat.getDrawable(context, R.drawable.transition1);
        transition2 = (TransitionDrawable) ContextCompat.getDrawable(context, R.drawable.transition2);
        transition3 = (TransitionDrawable) ContextCompat.getDrawable(context, R.drawable.transition3);
        transition4 = (TransitionDrawable) ContextCompat.getDrawable(context, R.drawable.transition4);
        transition5 = (TransitionDrawable) ContextCompat.getDrawable(context, R.drawable.transition5);
        transition6 = (TransitionDrawable) ContextCompat.getDrawable(context, R.drawable.transition6);

        this.relativeLayout = relativeLayout;
    }

    public void init(int hour) {
        if (hour >= 4 && hour <= 6) {
            relativeLayout.setBackground(gradient1);
            backgroundState = BackgroundState.EARLY_MORNING;
        } else if (hour >= 7 && hour <= 8) {
            relativeLayout.setBackground(gradient2);
            backgroundState = BackgroundState.SUNRISE;
        } else if (hour >= 9 && hour <= 16) {
            relativeLayout.setBackground(gradient3);
            backgroundState = BackgroundState.LATE_MORNING_TO_AFTERNOON;
        } else if (hour >= 17 && hour <= 18) {
            relativeLayout.setBackground(gradient4);
            backgroundState = BackgroundState.SUNSET;
        } else if (hour >= 19 && hour <= 21) {
            relativeLayout.setBackground(gradient5);
            backgroundState = BackgroundState.EARLY_NIGHT;
        } else if (hour >= 22 && hour <= 23 || hour >= 0 && hour <= 3) {
            relativeLayout.setBackground(gradient6);
            backgroundState = BackgroundState.LATE_NIGHT;
        }
    }

    public void update(int hour) {
        if (hour >= 4 && hour <= 6 && backgroundState != BackgroundState.EARLY_MORNING) {
            relativeLayout.setBackground(transition1);
            transition1.startTransition(3000);
            backgroundState = BackgroundState.EARLY_MORNING;
        } else if (hour >= 7 && hour <= 8 && backgroundState != BackgroundState.SUNRISE) {
            relativeLayout.setBackground(transition2);
            transition2.startTransition(3000);
            backgroundState = BackgroundState.SUNRISE;
        } else if (hour >= 9 && hour <= 16 && backgroundState != BackgroundState.LATE_MORNING_TO_AFTERNOON) {
            relativeLayout.setBackground(transition3);
            transition3.startTransition(3000);
            backgroundState = BackgroundState.LATE_MORNING_TO_AFTERNOON;
        } else if (hour >= 17 && hour <= 18 && backgroundState != BackgroundState.SUNSET) {
            relativeLayout.setBackground(transition4);
            transition4.startTransition(3000);
            backgroundState = BackgroundState.SUNSET;
        } else if (hour >= 19 && hour <= 21 && backgroundState != BackgroundState.EARLY_NIGHT) {
            relativeLayout.setBackground(transition5);
            transition5.startTransition(3000);
            backgroundState = BackgroundState.EARLY_NIGHT;
        } else if ((hour >= 22 && hour <= 23 || hour >= 0 && hour <= 3) && backgroundState != BackgroundState.LATE_NIGHT) {
            relativeLayout.setBackground(transition6);
            transition6.startTransition(3000);
            backgroundState = BackgroundState.LATE_NIGHT;
        }
    }
}