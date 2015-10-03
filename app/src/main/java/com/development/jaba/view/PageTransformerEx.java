package com.development.jaba.view;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * A {@link android.support.v4.view.ViewPager.PageTransformer} derived class which
 * supports several transitions.
 */
public class PageTransformerEx implements ViewPager.PageTransformer {

    /**
     * Supported page transition styles.
     */
    public enum TransformType {
        /**
         * Rotates the pages.
         */
        ROTATE,

        /**
         * Zooms the pages.
         */
        ZOOM_OUT,

        /**
         * Overlaps the pages.
         */
        DEPTH
    }

    private TransformType mTransType;

    private static final float MIN_SCALE_DEPTH = 0.75f;
    private static final float MIN_SCALE_ZOOM = 0.85f;
    private static final float MIN_ALPHA_ZOOM = 0.5f;

    private static Matrix OFFSET_MATRIX;
    private static Camera OFFSET_CAMERA;
    private static float[] OFFSET_TEMP_FLOAT;

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param type The Type of transformation to perform.
     */
    public PageTransformerEx(TransformType type) {
        mTransType = type;

        // ROTATE uses a few objects we need to allocate.
        if (type == TransformType.ROTATE) {
            OFFSET_MATRIX = new Matrix();
            OFFSET_CAMERA = new Camera();
            OFFSET_TEMP_FLOAT = new float[2];
        }
    }

    /**
     * Executes the page transformation for a given position.
     *
     * @param view     The {@link View} to transform.
     * @param position The position of the transform.
     */
    public void transformPage(View view, float position) {
        switch (mTransType) {
            case ZOOM_OUT: {
                // http://developer.android.com/training/animation/screen-slide.html#pagetransformer
                zoomOut(view, position);
                break;
            }
            case DEPTH: {
                // http://developer.android.com/training/animation/screen-slide.html#pagetransformer
                depth(view, position);
                break;
            }

            // https://github.com/ToxicBakery/ViewPagerTransforms/blob/master/library/src/main/java/com/ToxicBakery/viewpager/transforms/TabletTransformer.java
            case ROTATE: {
                final float rotation = (position < 0 ? 30f : -30f) * Math.abs(position);

                view.setTranslationX(getOffsetXForRotation(rotation, view.getWidth(), view.getHeight()));
                view.setPivotX(view.getWidth() * 0.5f);
                view.setPivotY(0);
                view.setRotationY(rotation);
                break;
            }

            default:
                break;
        }
    }

    /**
     * Computes the X offset for the ROTATE transformation.
     *
     * @param degrees The rotation angle.
     * @param width   The width of the view.
     * @param height  The height og the view.
     * @return the X offset.
     */
    protected static float getOffsetXForRotation(float degrees, int width, int height) {
        OFFSET_MATRIX.reset();
        OFFSET_CAMERA.save();
        OFFSET_CAMERA.rotateY(Math.abs(degrees));
        OFFSET_CAMERA.getMatrix(OFFSET_MATRIX);
        OFFSET_CAMERA.restore();


        OFFSET_MATRIX.preTranslate(-width * 0.5f, -height * 0.5f);
        OFFSET_MATRIX.postTranslate(width * 0.5f, height * 0.5f);
        OFFSET_TEMP_FLOAT[0] = width;
        OFFSET_TEMP_FLOAT[1] = height;
        OFFSET_MATRIX.mapPoints(OFFSET_TEMP_FLOAT);
        return (width - OFFSET_TEMP_FLOAT[0]) * (degrees > 0.0f ? 1.0f : -1.0f);
    }

    /**
     * Executes the ZOOM_OUT transformation for a given position.
     *
     * @param view     The {@link View} to transform.
     * @param position The position of the transform.
     */
    public void zoomOut(View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            float scaleFactor = Math.max(MIN_SCALE_ZOOM, 1 - Math.abs(position));
            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            view.setAlpha(MIN_ALPHA_ZOOM +
                    (scaleFactor - MIN_SCALE_ZOOM) /
                            (1 - MIN_SCALE_ZOOM) * (1 - MIN_ALPHA_ZOOM));

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }

    /**
     * Executes the DEPTH transformation for a given position.
     *
     * @param view     The {@link View} to transform.
     * @param position The position of the transform.
     */
    public void depth(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            view.setAlpha(1 - position);

            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE_DEPTH
                    + (1 - MIN_SCALE_DEPTH) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}