package com.development.jaba.drive;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.development.jaba.moneypit.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

/**
 * Base class for async Google Drive operations.
 */
public abstract class BaseDriveAsyncTask extends AsyncTask<Void, Void, String> {

    protected final static String BACKUP_FOLDER = "MoneyPit";
    protected final static String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";
    protected final static String MIME_TYPE_FILE = "application/octet-stream";

    private final Context mContext;
    private final GoogleApiClient mGoogleApiClient;
    private String mLastError;

    /**
     * Constructor.Initializes an instance of the object.
     *
     * @param context The {@link android.content.Context}.
     * @param client  The {@link com.google.android.gms.common.api.GoogleApiClient}. It is assumed this is
     *                setup properly and connected.
     */
    public BaseDriveAsyncTask(Context context, GoogleApiClient client) {
        mContext = context;
        mGoogleApiClient = client;
        mLastError = "";
    }

    /**
     * Helper which tries to locate the backup folder on the user Google Drive.
     *
     * @return The {@link com.google.android.gms.drive.DriveFolder} of the backup folder or null
     * if it is not present on the user Google Drive.
     */
    protected DriveFolder findBackupFolder() {

        mLastError = "";

        // Query for the presence of the 'MoneyPit' folder.
        Filter mime = Filters.eq(SearchableField.MIME_TYPE, MIME_TYPE_FOLDER),
                title = Filters.eq(SearchableField.TITLE, BACKUP_FOLDER),
                trash = Filters.eq(SearchableField.TRASHED, false);     // TODO: This does not seem to work. Find out why.

        Query query = new Query.Builder()
                .addFilter(Filters.and(mime, title, trash))
                .build();
        DriveApi.MetadataBufferResult queryResult = Drive.DriveApi.query(mGoogleApiClient, query).await();
        if (!queryResult.getStatus().isSuccess()) {
            mLastError = mContext.getString(R.string.drive_error_query_folders);
            return null;
        }

        // See if the folder was found. Note that this will select the first
        // folder that meets the criteria. It is assumed there can be only one.
        DriveFolder backupFolder = null;
        MetadataBuffer buffer = queryResult.getMetadataBuffer();
        for (Metadata md : buffer) {
            // For some reason unclear to me the query defined above does not
            // filter out the trashed folders like I would want it to...
            if (!md.isTrashed()) {
                backupFolder = Drive.DriveApi.getFolder(mGoogleApiClient, md.getDriveId());
                break;
            }
        }
        buffer.release();
        return backupFolder;
    }

    /**
     * Helper which creates the backup folder on the user Google Drive.
     *
     * @return The {@link com.google.android.gms.drive.DriveFolder} of the backup folder or null
     * if it failed to create.
     */
    protected DriveFolder createBackupFolder() {

        mLastError = "";

        // Find the root folder.
        DriveFolder root = Drive.DriveApi.getRootFolder(mGoogleApiClient);
        if (root == null) {
            mLastError = mContext.getString(R.string.drive_error_root_folder);
            return null;
        }

        MetadataChangeSet folder = new MetadataChangeSet.Builder()
                .setTitle(BACKUP_FOLDER)
                .setMimeType(MIME_TYPE_FOLDER)
                .build();

        DriveFolder.DriveFolderResult createResult = root.createFolder(mGoogleApiClient, folder).await();
        if (!createResult.getStatus().isSuccess()) {
            mLastError = mContext.getString(R.string.drive_error_create_folder);
            return null;
        }

        // Get a reference to the created folder.
        return createResult.getDriveFolder();
    }

    /**
     * Gets the{@link android.content.Context}.
     *
     * @return The {@link android.content.Context}.
     */
    protected Context getContext() {
        return mContext;
    }

    /**
     * Gets the {@link com.google.android.gms.common.api.GoogleApiClient}.
     *
     * @return The {@link com.google.android.gms.common.api.GoogleApiClient}.
     */
    protected GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Gets the error string of the last error.
     *
     * @return The last error occurred or null in case of no error.
     */
    protected String getLastError() {
        return TextUtils.isEmpty(mLastError) ? null : mLastError;
    }

    /**
     * Set's the last error string.
     *
     * @param error The error string to set.
     */
    protected void setLastError(String error) {
        mLastError = error;
    }
}
