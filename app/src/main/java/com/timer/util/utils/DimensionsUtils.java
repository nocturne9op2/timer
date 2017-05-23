package com.timer.util.utils;

import android.content.res.Resources;
import android.util.TypedValue;

public final class DimensionsUtils {
    public final static float smallDecoViewLineWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, Resources.getSystem().getDisplayMetrics());
    public final static float largeDecoViewLineWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, Resources.getSystem().getDisplayMetrics());

    private DimensionsUtils() {
    }
}