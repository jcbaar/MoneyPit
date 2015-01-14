package com.development.jaba.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import com.development.jaba.moneypit.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class with {@link android.graphics.Bitmap} helper methods.
 */
public final class BitmapHelper {

    /**
     * Queries the orientation of the given image {@link android.net.Uri}.
     *
     * @param photoUri The {@link android.net.Uri} pointing to the image.
     * @return The orientation of the image.
     */
    public static int getOrientation(Uri photoUri) {
        try {
            ExifInterface exif = new ExifInterface(photoUri.getPath());
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        } catch (IOException e) {
            Log.e("getOrientation", "Failed to get image orientation.");
            return -1;
        }
    }

    /**
     * Rotates a {@link android.graphics.Bitmap} to the given EXIF rotation.
     * <p/>
     * NOTE: When no rotation was either necessary or possible (unknown orientation
     * value) the input {@link android.graphics.Bitmap} is returned. Otherwise the
     * rotated {@link android.graphics.Bitmap} is returned and the input {@link android.graphics.Bitmap}
     * is recycled.
     *
     * @param bitmap      The {@link android.graphics.Bitmap} to rotate.
     * @param orientation The EXIF orientation to set for the {@link android.graphics.Bitmap}.
     * @return The rotated {@link android.graphics.Bitmap} or null in case of a out of memory error.
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            Log.e("rotateBitmap", "Out of memory during bitmap rotation.");
            return null;
        }
    }

    /**
     * Decodes an image at the given Uri and automatically scales it to the proper size and
     * performs a proper rotation according to the image EXIF.
     *
     * @param context       The {@link android.content.Context}
     * @param selectedImage The {@link android.net.Uri} of the picture to decode.
     * @return The {@link Bitmap} containing the decoded image.
     * @throws java.io.FileNotFoundException
     */
    public static Bitmap decodeUriAsBitmap(Context context, Uri selectedImage) throws FileNotFoundException {

        InputStream stream = null;

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        try {
            stream = context.getContentResolver().openInputStream(selectedImage);
            BitmapFactory.decodeStream(stream, null, o);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e("decodeUriAsBitmap", "Failed to close stream for decodeBounds.");
                }
                stream = null;
            }
        }

        // The new size we want to scale to
        final int maxSize = context.getResources().getInteger(R.integer.image_scale_max_size);
        final int orientation = getOrientation(selectedImage);

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < maxSize || height_tmp / 2 < maxSize) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;

        try {
            stream = context.getContentResolver().openInputStream(selectedImage);
            Bitmap bm = BitmapFactory.decodeStream(stream, null, o2);
            return rotateBitmap(bm, orientation);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e("decodeUriAsBitmap", "Failed to close stream for decodeBounds.");
                }
            }
        }
    }

    /**
     * Decodes an image at the given Uri and automatically scales it to the proper size and
     * performs a proper rotation according to the image EXIF.
     * <p/>
     * NOTE: The image is compressed as a 80% quality JPEG.
     *
     * @param context       The {@link android.content.Context}
     * @param selectedImage The {@link android.net.Uri} of the picture to decode.
     * @return The byte[] array containing the decoded image.
     * @throws java.io.FileNotFoundException
     */
    public static byte[] decodeUriAsByteArray(Context context, Uri selectedImage) throws FileNotFoundException {

        Bitmap bm = decodeUriAsBitmap(context, selectedImage);
        if (bm != null) {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 80, bs);
            bm.recycle();
            return bs.toByteArray();
        }
        return null;
    }
}