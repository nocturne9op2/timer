package com.timer.util.utils;

import android.content.res.Resources;
import android.util.TypedValue;

public final class DimensionsUtils {
    public static final float SMALL_DECOVIEW_LINEWIDTH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, Resources.getSystem().getDisplayMetrics());
    public static final float LARGE_DECOVIEW_LINEWIDTH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, Resources.getSystem().getDisplayMetrics());

    private DimensionsUtils() {
    }
}