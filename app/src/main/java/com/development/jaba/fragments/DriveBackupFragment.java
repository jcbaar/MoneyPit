package com.development.jaba.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.drive.DriveCheckBackupFolderAsyncTask;
import com.development.jaba.drive.DriveCreateBackupAsyncTask;
import com.development.jaba.drive.DriveListRestoreFileAsyncTask;
import com.development.jaba.drive.DriveRestoreBackupAsyncTask;
import com.development.jaba.drive.RestoreFile;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DialogHelper;
import com.development.jaba.view.CircularProgressView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;

/**
 * Fragment class for handling database backup and restore to Google Drive.
 */
public class DriveBackupFragment extends BaseDriveFragment {
    private DriveFolder mBackupFolder;
    private Button mBackup, mRestore;
    private Spinner mRestoreList;
    private CircularProgressView mProgress;
    private LinearLayout mContainer;
    private ArrayAdapter<RestoreFile> mAdapter;

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
        mProgress.stop();
        mContainer.setVisibility(View.VISIBLE);

        // Check for the existence of the backup folder.
        new CheckBackupFolderAsyncTask(getActivity(), getGoogleApiClient()).execute();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        super.onConnectionSuspended(cause);
        mBackup.setEnabled(false);
        mContainer.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        mProgress.stop();
        mContainer.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mProgress.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_backup_restore, container, false);

        mContainer = (LinearLayout) view.findViewById(R.id.layoutContainer);
        mContainer.setVisibility(View.GONE);

        mProgress = (CircularProgressView) view.findViewById(R.id.progress);
        mProgress.start();

        mRestoreList = (Spinner) view.findViewById(R.id.restore_list);
        mBackup = (Button) view.findViewById(R.id.backup);
        if (mBackup != null) {
            mBackup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backup();
                }
            });
        }
        mRestore = (Button) view.findViewById(R.id.restore);
        if (mRestore != null) {
            mRestore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogHelper.showYesNoDialog(getString(R.string.warning),
                            Html.fromHtml(getString(R.string.restore_warning)),
                            new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    restore();
                                }
                            }, getActivity());
                }
            });
        }
        return view;
    }

    /**
     * Performs the database backup.
     */
    private synchronized void backup() {
        new DriveBackupAsyncTask(getActivity(), getGoogleApiClient(), mBackupFolder).execute();
    }

    private synchronized void restore() {
        if (mAdapter != null) {
            mBackup.setEnabled(false);
            mRestore.setEnabled(false);
            mRestoreList.setEnabled(false);
            mProgress.start();
            new DriveRestoreAsyncTask(getActivity(), getGoogleApiClient(), mAdapter.getItem(mRestoreList.getSelectedItemPosition()).Id).execute();
        }
    }

    /**
     * Checks for the presence of the backup folder and creates it if it does not
     * yet exists.
     */
    private class CheckBackupFolderAsyncTask extends DriveCheckBackupFolderAsyncTask {

        public CheckBackupFolderAsyncTask(Context context, GoogleApiClient client) {
            super(context, client);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (getActivity() != null) {
                if (!TextUtils.isEmpty(s)) {
                    // Something failed.
                    showToast(s);
                } else {
                    // Folder exists. Enable UI and start loading the
                    // backup files available for restoring.
                    mBackupFolder = getBackupFolder();
                    mBackup.setEnabled(true);
                    new DriveLoadBackupsAsyncTask(getContext(), getGoogleApiClient(), getBackupFolder()).execute();
                }
            }
        }
    }

    /**
     * Internal {@link android.os.AsyncTask} derived class handling the loading of
     * available backups to restore from Google Drive.
     */
    private class DriveLoadBackupsAsyncTask extends DriveListRestoreFileAsyncTask {

        public DriveLoadBackupsAsyncTask(Context context, GoogleApiClient client, DriveFolder backupFolder) {
            super(context, client, backupFolder);
        }

        /**
         * Show the result of the file query.
         *
         * @param result The error message or null to indicate success.
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (getActivity() != null) {
                if (result != null) {
                    // Something failed.
                    showToast(result);
                } else {
                    // Loads the available files into the UI for selection.
                    RestoreFile[] files = new RestoreFile[getResults().size()];
                    getResults().toArray(files);
                    mAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_row_template_smalll, files);
                    mRestoreList.setAdapter(mAdapter);
                    mRestore.setEnabled(getResults().size() > 0);
                }
            }
        }
    }

    /**
     * Internal {@link android.os.AsyncTask} derived class handling the database backup
     * to the users Google Dive.
     */
    private class DriveBackupAsyncTask extends DriveCreateBackupAsyncTask {

        public DriveBackupAsyncTask(Context context, GoogleApiClient client, DriveFolder backupFolder) {
            super(context, client, backupFolder);
        }

        /**
         * Show the result of the backup procedure.
         *
         * @param result The error or success message.
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (getActivity() != null) {
                showToast(result);
                new DriveLoadBackupsAsyncTask(getContext(), getGoogleApiClient(), mBackupFolder).execute();
            }
        }
    }

    /**
     * Internal {@link android.os.AsyncTask} derived class handling the database restore
     * from the users Google Dive.
     */
    private class DriveRestoreAsyncTask extends DriveRestoreBackupAsyncTask {

        public DriveRestoreAsyncTask(Context context, GoogleApiClient client, DriveId backupFile) {
            super(context, client, backupFile);
        }

        /**
         * Show the result of the restore procedure.
         *
         * @param result The error or success message.
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (getActivity() != null) {
                showToast(result);
                mProgress.stop();
                mBackup.setEnabled(true);
                mRestore.setEnabled(mAdapter.getCount() > 0);
                mRestoreList.setEnabled(mAdapter.getCount() > 0);
            }
        }
    }
}
