package com.development.jaba.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An {@link android.os.AsyncTask} derived class responsible for downloading
 * images from either the internet or the local image cache.
 * <p/>
 * Images downloaded from the internet will be cached locally.
 */
public class ImageDownloadHelperTask extends AsyncTask<String, Void, Bitmap> {

    private final String LOG_TAG = "ImageDownloadHelperTask";
    private final String mCacheFolder;
    private String mCacheFileName;

    /**
     * Constructor. Initializes the object setting up the cache folder
     * to use.
     *
     * @param cacheFolder The cache folder to use. Note that this is a _relative_ path.
     *                    It is appended to the result of #getExternalStorageDirectory
     *                    to determine the full path of the storage folder.
     */
    public ImageDownloadHelperTask(String cacheFolder) {
        mCacheFolder = cacheFolder;
    }

    /**
     * This method will check to see if we have a image with the given cache file name
     * already in our cache. If we do we do not bother downloading it from the internet
     * but instead load it directly from our local cache instead.
     *
     * @param urls Parameters for the method. Index 0 equals the download URL, index 1 equals the cached filename
     *             of the image.
     * @return The (down)loaded static map image.
     */
    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];

        // Save the cache filename.
        mCacheFileName = urls[1];

        // Only download when we do not have a local copy
        // cached.
        if (cachedFileExists()) {
            return loadFileFromCache();
        }

        // We do not have it cached. Download it from the given
        // URL.
        Bitmap image = null;
        InputStream in = null;
        try {
            in = new java.net.URL(urldisplay).openStream();
            image = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }

        // Save the downloaded image into our local file cache.
        if (image != null) {
            try {
                cacheFile(image);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
        return image;
    }

    /**
     * Called after the task has completed. Derived classes should override this to actually
     * do something with the image.
     *
     * @param result The (down)loaded {@link android.graphics.Bitmap}.
     */
    protected void onPostExecute(Bitmap result) {
    }

    /**
     * Constructs the cache file path from the input data.
     *
     * @return The cache file path.
     */
    private String getCacheFilePath() {
        return Environment.getExternalStorageDirectory() + mCacheFolder + mCacheFileName;
    }

    /**
     * Checks to see if a cached image for the given
     * data already exists.
     *
     * @return true if the cached image exists, false if it does not.
     */
    private boolean cachedFileExists() {
        File file = new File(getCacheFilePath());
        return file.exists();
    }

    /**
     * Decodes the cached image.
     *
     * @return The image as a {@link android.graphics.Bitmap}
     */
    private Bitmap loadFileFromCache() {
        return BitmapFactory.decodeFile(getCacheFilePath());
    }

    /**
     * Saves the given {@link android.graphics.Bitmap} into the local
     * image cache.
     *
     * @param bitmap The static map {@link android.graphics.Bitmap} to store.
     * @throws IOException This is thrown in case of an IO error.
     */
    private void cacheFile(Bitmap bitmap) throws IOException {
        if (cachedFileExists()) {
            return;
        }

        // Save the image to the cache.
        OutputStream output = null;
        try {
            String outFileName = getCacheFilePath();
            output = new FileOutputStream(outFileName);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.flush();
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}