package com.development.jaba.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.development.jaba.bitmaps.RecyclingBitmapDrawable;
import com.development.jaba.moneypit.R;

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
     * @param context       The {@link Context} instance to get the {@link Drawable} from.
     * @param drawableResId The resource ID of the {@link Drawable}
     * @param color         The color to tint the {@link Drawable} with.
     * @return The tinted {@link Drawable} or null in case of an error.
     */
    public static Drawable getTintedDrawable(Context context,
                                             @DrawableRes int drawableResId,
                                             int color) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        if (drawable != null) {
            drawable.setColorFilter(color, PorterDuff.Mode.OVERLAY);
        }
        return drawable;
    }

    /**
     * Blends in a {@link Bitmap} in the given {@link ImageView}.
     *
     * @param context The {@link Context}
     * @param view    The {@link ImageView}
     * @param bitmap  The {@link Bitmap}
     */
    public static void blendInImage(Context context, ImageView view, Bitmap bitmap) {
        view.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_loadfail));
        if (bitmap != null) {
            // Transition drawable with a transparent drawable and the final drawable
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[]{
                            new ColorDrawable(0),
                            new RecyclingBitmapDrawable(context.getResources(), bitmap)
                    });
            // Set background to loading bitmap
            view.setImageDrawable(td);
            td.startTransition(200);
        }
    }

    /**
     * Sets the background {@link Drawable} for the given {@link View}.
     *
     * @param view     The {@link View} to set the background {@link Drawable} for.
     * @param drawable The {@link Drawable} to set.
     */
    public static void setBackgroundDrawable(View view, Drawable drawable) {
        // On SDK 16 and higher we use setBackground(). Otherwise we use
        // setBackgroundDrawable().
        if (Build.VERSION.SDK_INT >= 16) {
            view.setBackground(drawable);
        } else {
            //noinspection deprecation
            view.setBackgroundDrawable(drawable);
        }
    }
}
