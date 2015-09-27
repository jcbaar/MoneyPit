package com.development.jaba.utilities;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * General helper methods.
 */
public class UtilsHelper {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in a {@link android.view.View}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    @SuppressLint("NewApi")
    public static int generateViewId() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        }
        else {
            return View.generateViewId();
        }
    }

    /**
     * Generates a tinted {@link Drawable} from the given drawable resource and color.
     * Note that this will work best if the original drawable is grey-scaled.
     *
     * @param res           The {@link Resources} instance to get the {@link Drawable} from.
     * @param drawableResId The resource ID of the {@link Drawable}
     * @param color         The color to tint the {@link Drawable} with.
     * @return The tinted {@link Drawable} or null in case of an error.
     */
    public static Drawable getTintedDrawable(Resources res,
                                             @DrawableRes int drawableResId,
                                             int color) {
        Drawable drawable = res.getDrawable(drawableResId);
        if (drawable != null) {
            drawable.setColorFilter(color, PorterDuff.Mode.OVERLAY);
        }
        return drawable;
    }

}
