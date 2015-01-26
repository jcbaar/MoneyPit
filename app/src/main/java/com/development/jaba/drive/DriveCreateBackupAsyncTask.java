package com.development.jaba.drive;

import android.content.Context;
import android.util.Log;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.moneypit.MoneyPitApp;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DateHelper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * {@link com.development.jaba.drive.BaseDriveAsyncTask} derived class for creatinbg a database backup
 * on the user Google Drive.
 */
public class DriveCreateBackupAsyncTask extends BaseDriveAsyncTask {

    private final DriveFolder mBackupFolder;

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context      The {@link android.content.Context}.
     * @param client       The {@link com.google.android.gms.common.api.GoogleApiClient}.
     * @param backupFolder The {@link com.google.android.gms.drive.DriveFolder} representing the backup folder.
     */
    public DriveCreateBackupAsyncTask(Context context, GoogleApiClient client, DriveFolder backupFolder) {
        super(context, client);
        mBackupFolder = backupFolder;
    }

    /**
     * The business end of the {@link com.development.jaba.drive.DriveCreateBackupAsyncTask}. This will actually
     * create the backup.
     *
     * @param arg0 Task parameters. Not used.
     * @return A string containing an error or success message.
     */
    @Override
    protected String doInBackground(Void... arg0) {

        setLastError("");

        // Sanity check...
        if (mBackupFolder == null) {
            return null;
        }

        // Create a new file in the backup folder to which we write the backup of
        // the database.
        DriveApi.DriveContentsResult backupFileResult = Drive.DriveApi.newDriveContents(getGoogleApiClient()).await();
        if (!backupFileResult.getStatus().isSuccess()) {
            return getContext().getString(R.string.drive_error_create_file);
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
            Log.e("CreateBackup", e.getMessage());
            return getContext().getString(R.string.drive_error_io_error);
        }

        // Create the file.
        String fileTitle = "MoneyPit_" + DateHelper.toDateTimeString(new Date()) + ".sqlite";
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(fileTitle)
                .setMimeType(MIME_TYPE_FILE)
                .setStarred(true).build();

        DriveFolder.DriveFileResult createFileResult = mBackupFolder
                .createFile(getGoogleApiClient(), changeSet, backupFile).await();
        if (!createFileResult.getStatus().isSuccess()) {
            return getContext().getString(R.string.drive_error_create_file);
        } else {
            return getContext().getString(R.string.drive_backup_success);
        }
    }
}
