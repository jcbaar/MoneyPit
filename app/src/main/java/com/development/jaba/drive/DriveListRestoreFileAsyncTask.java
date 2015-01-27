package com.development.jaba.drive;

import android.content.Context;

import com.development.jaba.moneypit.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;

import java.util.ArrayList;

/**
 * {@link com.development.jaba.drive.BaseDriveAsyncTask} derived class handling the loading of
 * available backups to restore from Google Drive.
 */
public abstract class DriveListRestoreFileAsyncTask extends BaseDriveAsyncTask {

    private final DriveFolder mBackupFolder;
    private ArrayList<RestoreFile> mResults;

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context      The {@link android.content.Context}.
     * @param client       The {@link com.google.android.gms.common.api.GoogleApiClient}.
     * @param backupFolder The {@link com.google.android.gms.drive.DriveFolder} representing the backup folder.
     */
    public DriveListRestoreFileAsyncTask(Context context, GoogleApiClient client, DriveFolder backupFolder) {
        super(context, client);
        mBackupFolder = backupFolder;
    }

    /**
     * Gets the files read from Google Drive.
     *
     * @return The {@link java.util.ArrayList} containing the backup files.
     */
    public ArrayList<RestoreFile> getResults() {
        return mResults;
    }

    /**
     * The business end of the {@link com.development.jaba.drive.DriveListRestoreFileAsyncTask}. This will load
     * the available backup files.
     *
     * @param arg0 Task parameters. Not used.
     * @return A string containing an error or success message.
     */
    @Override
    protected String doInBackground(Void... arg0) {

        mResults = new ArrayList<>();

        // Build a query in which we list all backup files from the folder.
        Filter mime = Filters.eq(SearchableField.MIME_TYPE, MIME_TYPE_FILE),
                title = Filters.contains(SearchableField.TITLE, ".sqlite"),
                trash = Filters.eq(SearchableField.TRASHED, false);

        Query query = new Query.Builder()
                .addFilter(Filters.and(mime, title, trash))
                .setSortOrder(new SortOrder.Builder()
                        .addSortDescending(SortableField.CREATED_DATE)
                        .build())
                .build();

        DriveApi.MetadataBufferResult queryResult = mBackupFolder.queryChildren(getGoogleApiClient(), query).await();
        if (!queryResult.getStatus().isSuccess()) {
            return getContext().getString(R.string.drive_error_file_list);
        }

        // Results can be paginated. If they are we loop to load them
        // all. TODO: When results are paginated do we really want to show them all?
        MetadataBuffer buffer = queryResult.getMetadataBuffer();
        String token;
        do {
            for (Metadata md : buffer) {
                // Excluding this from the query does not seem to work properly...
                if (!md.isTrashed()) {
                    RestoreFile file = new RestoreFile();
                    file.Title = md.getTitle();
                    file.Id = md.getDriveId();
                    mResults.add(file);
                }
            }

            // See if we need to load more.
            token = buffer.getNextPageToken();
            buffer.release();

            if (token != null) {
                query = new Query.Builder()
                        .setPageToken(token)
                        .build();
                queryResult = mBackupFolder.queryChildren(getGoogleApiClient(), query).await();
                if (!queryResult.getStatus().isSuccess()) {
                    return getContext().getString(R.string.drive_error_file_list);
                }
                buffer = queryResult.getMetadataBuffer();
            }
        } while (token != null);

        // Indicates success.
        return null;
    }
}
