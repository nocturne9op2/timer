package com.timer.util.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {
    private static final String STARTED_TIME_ID = "com.timer.util.time";
    private SharedPreferences preferences;

    public PrefUtils(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public long getStartedTime() {
        return preferences.getLong(STARTED_TIME_ID, 0);
    }

    public void setStartedTime(long started) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(STARTED_TIME_ID, started);
        editor.apply();
    }
}