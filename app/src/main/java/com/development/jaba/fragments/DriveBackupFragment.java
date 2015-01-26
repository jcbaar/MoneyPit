package com.development.jaba.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.moneypit.MoneyPitApp;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DateHelper;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Fragment class for handling database backup and restore to Google Drive.
 */
public class DriveBackupFragment extends BaseDriveFragment {
    private static final String TAG = "DriveBackupFragment";

    private Button mBackup;

    /**
     * Static factory method. Creates a new instance of a {@link com.development.jaba.fragments.DriveBackupFragment} class.
     *
     * @return The created fragment.
     */
    public static DriveBackupFragment newInstance() {

        return new DriveBackupFragment();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        mBackup.setEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        super.onConnectionSuspended(cause);
        mBackup.setEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_backup_restore, container, false);

        mBackup = (Button) view.findViewById(R.id.backup);
        if (mBackup != null) {
            mBackup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backup();
                }
            });
        }
        return view;
    }

    private synchronized void backup() {
        new DriveBackupAsyncTask().execute();
    }

    /**
     * Internal {@link android.os.AsyncTask} derived class handling the database backup
     * to the users Google Dive.
     */
    private class DriveBackupAsyncTask extends AsyncTask<Void, Void, String> {

        private final static String BACKUP_FOLDER = "MoneyPit";
        private final static String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";
        private final static String MIME_TYPE_FILE = "application/octet-stream";

        /**
         * The business end of the {@link com.development.jaba.fragments.DriveBackupFragment.DriveBackupAsyncTask}. This will perform
         * the following actions:
         * </p>
         * <li>
         * <ul>Try to locate the 'MoneyPit' folder on the users Google Drive and</ul>
         * <ul>If the 'MoneyPit' folder was not found it will create it and</ul>
         * <ul>Write the current database as a backup to the 'MoneyPit folder.</ul>
         * </li>
         *
         * @param arg0 Task parameters. Not used.
         * @return A string containing an error or success message.
         */
        @Override
        protected String doInBackground(Void... arg0) {

            // Query for the presence of the 'MoneyPit' folder.
            Filter a = Filters.eq(SearchableField.MIME_TYPE, MIME_TYPE_FOLDER),
                    b = Filters.eq(SearchableField.TITLE, BACKUP_FOLDER),
                    c = Filters.eq(SearchableField.TRASHED, false);     // TODO: This does not seem to work. Find out why.

            Query query = new Query.Builder()
                    .addFilter(Filters.and(a, b, c))
                    .build();
            DriveApi.MetadataBufferResult queryResult = Drive.DriveApi.query(getGoogleApiClient(), query).await();
            if (!queryResult.getStatus().isSuccess()) {
                return getString(R.string.drive_error_query_folders);
            }

            // See if the folder was found. Note that this will select the first
            // folder that meets the criteria. It is assumed there can be only one.
            DriveFolder backupFolder = null;
            for (Metadata md : queryResult.getMetadataBuffer()) {
                // For some reason unclear to me the query defined above does not
                // filter out the trashed folders like I would want it to...
                if (!md.isTrashed()) {
                    backupFolder = Drive.DriveApi.getFolder(getGoogleApiClient(), md.getDriveId());
                    break;
                }
            }

            // Did we find the folder?
            if (backupFolder == null) {

                // The folder was not found. Create it in the root folder.
                DriveFolder root = Drive.DriveApi.getRootFolder(getGoogleApiClient());
                if (root == null) {
                    return getString(R.string.drive_error_root_folder);
                }

                MetadataChangeSet folder = new MetadataChangeSet.Builder()
                        .setTitle(BACKUP_FOLDER)
                        .setMimeType(MIME_TYPE_FOLDER)
                        .build();

                DriveFolder.DriveFolderResult createResult = root.createFolder(getGoogleApiClient(), folder).await();
                if (!createResult.getStatus().isSuccess()) {
                    return getString(R.string.drive_error_create_folder);
                }

                // Get a reference to the created folder.
                backupFolder = createResult.getDriveFolder();
            }

            // Right here the backup folder should be known.
            if (backupFolder != null) {

                // Create a new file in the backup folder to which we write the backup of
                // the database.
                DriveApi.DriveContentsResult backupFileResult = Drive.DriveApi.newDriveContents(getGoogleApiClient()).await();
                if (!backupFileResult.getStatus().isSuccess()) {
                    return getString(R.string.drive_error_create_file);
                }

                DriveContents backupFile = backupFileResult.getDriveContents();
                final String currentDBPath = MoneyPitApp.getContext().getDatabasePath(MoneyPitDbContext.DATABASE_NAME).getPath();

                // Write the current database contents to the drive
                // file in blocks of 4 KByte.
                try {
                    FileInputStream src = new FileInputStream(currentDBPath);
                    OutputStream outputStream = backupFile.getOutputStream();

                    byte[] buffer = new byte[4096];
                    int read = src.read(buffer);
                    while (read > 0) {
                        outputStream.write(buffer, 0, read);
                        read = src.read(buffer);
                    }
                    src.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    return getString(R.string.drive_error_io_error);
                }

                // Create the file.
                String fileTitle = "MoneyPit_" + DateHelper.toDateTimeString(new Date()) + ".sqlite";
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(fileTitle)
                        .setMimeType(MIME_TYPE_FILE)
                        .setStarred(true).build();

                DriveFolder.DriveFileResult createFileResult = backupFolder
                        .createFile(getGoogleApiClient(), changeSet, backupFile).await();
                if (!createFileResult.getStatus().isSuccess()) {
                    return getString(R.string.drive_error_create_file);
                } else {
                    return getString(R.string.drive_backup_success);
                }
            }
            return getString(R.string.drive_error_backup_folder);
        }

        /**
         * Show the result of the backup procedure.
         *
         * @param result The error or success message.
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            showToast(result);
        }
    }
}
