package com.development.jaba.drive;

import android.content.Context;
import android.util.Log;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.moneypit.MoneyPitApp;
import com.development.jaba.moneypit.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link com.development.jaba.drive.BaseDriveAsyncTask} derived class for restoring a database backup
 * from the user Google Drive.
 */
public class DriveRestoreBackupAsyncTask extends BaseDriveAsyncTask {

    private final DriveId mBackupFileId;

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context      The {@link android.content.Context}.
     * @param client       The {@link com.google.android.gms.common.api.GoogleApiClient}.
     * @param backupFileId The {@link com.google.android.gms.drive.DriveId} representing the backup file id.
     */
    public DriveRestoreBackupAsyncTask(Context context, GoogleApiClient client, DriveId backupFileId) {
        super(context, client);
        mBackupFileId = backupFileId;
    }

    /**
     * The business end of the {@link com.development.jaba.drive.DriveCreateBackupAsyncTask}. This will actually
     * restore the backup.
     *
     * @param arg0 Task parameters. Not used.
     * @return A string containing an error or success message.
     */
    @Override
    protected String doInBackground(Void... arg0) {

        setLastError("");

        // Sanity check...
        if (mBackupFileId == null) {
            return null;
        }

        // Open the file.
        DriveFile backupFile = Drive.DriveApi.getFile(getGoogleApiClient(), mBackupFileId);
        if (backupFile == null) {
            return getContext().getString(R.string.restore_error_get_file);
        }
        DriveApi.DriveContentsResult openResult = backupFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
        if (!openResult.getStatus().isSuccess()) {
            return getContext().getString(R.string.restore_error_get_file);
        }

        DriveContents fileContents = openResult.getDriveContents();

        final String currentDBPath = MoneyPitApp.getContext().getDatabasePath(MoneyPitDbContext.DATABASE_NAME).getPath();

        // Write the current database contents to the drive
        // file in blocks of 4 KByte.
        try {
            InputStream inputStream = fileContents.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(currentDBPath);

            byte[] buffer = new byte[4096];
            int read = inputStream.read(buffer);
            while (read > 0) {
                outputStream.write(buffer, 0, read);
                read = inputStream.read(buffer);
            }
            outputStream.close();
        } catch (IOException e) {
            Log.e("RestoreBackup", e.getMessage());
            return getContext().getString(R.string.drive_error_io_error);
        }

        fileContents.discard(getGoogleApiClient());
        return getContext().getString(R.string.drive_restore_success);
    }

}
