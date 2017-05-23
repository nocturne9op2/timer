package com.timer.util.utils;

import android.graphics.Color;

import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.charts.SeriesLabel;
import com.hookedonplay.decoviewlib.events.DecoEvent;

public final class DecoViewUtils {
    public static SeriesItem buildBase(boolean initialVisibility, float lineWidth) {
        return new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setInitialVisibility(initialVisibility)
                .setRange(0, 100, 100)
                .setLineWidth(lineWidth)
                .build();
    }

    public static SeriesItem buildSeries(int color, float value, float lineWidth, SeriesLabel seriesLabel) {
        return new SeriesItem.Builder(color)
                .setCapRounded(false)
                .setRange(0, 100, value > 100 ? 100 : value)
                .setLineWidth(lineWidth)
                .setSeriesLabel(seriesLabel)
                .build();
    }

    public static DecoEvent buildSeriesShowEvent(float value, int index, long delay, long duration) {
        return new DecoEvent.Builder(value > 100 ? 100 : value)
                .setIndex(index)
                .setDelay(delay)
                .setDuration(duration)
                .build();
    }

    private DecoViewUtils() {
    }
}