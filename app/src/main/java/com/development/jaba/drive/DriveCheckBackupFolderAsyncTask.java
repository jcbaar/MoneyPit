package com.development.jaba.drive;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveFolder;

/**
 * Simple {@link com.development.jaba.drive.BaseDriveAsyncTask} derived class which checks for
 * the availability of the backup folder on the users Google Drive.
 */
public class DriveCheckBackupFolderAsyncTask extends BaseDriveAsyncTask {

    private DriveFolder mBackupFolder;

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context The {@link android.content.Context}.
     * @param client  The {@link com.google.android.gms.common.api.GoogleApiClient}.
     */
    public DriveCheckBackupFolderAsyncTask(Context context, GoogleApiClient client) {
        super(context, client);
    }

    /**
     * Gets the {@link com.google.android.gms.drive.DriveFolder} representing the backup folder.
     *
     * @return The {@link com.google.android.gms.drive.DriveFolder}.
     */
    public DriveFolder getBackupFolder() {
        return mBackupFolder;
    }

    /**
     * Tries to find the backup folder on the users Google Drive. If not found it tries to
     * create it.
     *
     * @param params Not used.
     * @return An error string or null in case of success.
     */
    @Override
    protected String doInBackground(Void... params) {

        mBackupFolder = findBackupFolder();
        if (mBackupFolder == null) {
            String error = getLastError();
            if (!TextUtils.isEmpty(error)) {
                return error;
            }

            mBackupFolder = createBackupFolder();
            if (mBackupFolder == null) {
                return error;
            }
        }
        return null;
    }
}
