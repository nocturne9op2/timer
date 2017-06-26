package com.timer.util.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public final class DimensionsUtils {
    private static final DisplayMetrics DISPLAY_METRICS = Resources.getSystem().getDisplayMetrics();
    private static final float SCREEN_WIDTH = DISPLAY_METRICS.widthPixels / DISPLAY_METRICS.density;
    private static final float SCREEN_RATIO = SCREEN_WIDTH / 360f;

    public static final float SMALL_DECOVIEW_WIDTH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, DISPLAY_METRICS) * SCREEN_RATIO;
    public static final float LARGE_DECOVIEW_WIDTH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, DISPLAY_METRICS) * SCREEN_RATIO;

    public static final float SMALL_DECOVIEW_LINEWIDTH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, DISPLAY_METRICS) * SCREEN_RATIO;
    public static final float LARGE_DECOVIEW_LINEWIDTH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, DISPLAY_METRICS) * SCREEN_RATIO;

    private DimensionsUtils() {
    }
}