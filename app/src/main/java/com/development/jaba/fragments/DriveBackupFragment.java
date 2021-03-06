package com.development.jaba.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.development.jaba.drive.DriveCheckBackupFolderAsyncTask;
import com.development.jaba.drive.DriveCreateBackupAsyncTask;
import com.development.jaba.drive.DriveListRestoreFileAsyncTask;
import com.development.jaba.drive.DriveRestoreBackupAsyncTask;
import com.development.jaba.drive.RestoreFile;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DialogHelper;
import com.development.jaba.view.MaterialProgressViewEx;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Fragment class for handling database backup and restore to Google Drive.
 */
public class DriveBackupFragment extends BaseDriveFragment {
    private DriveFolder mBackupFolder;
    private ArrayAdapter<RestoreFile> mAdapter;

    @Bind(R.id.backup) Button mBackup;
    @Bind(R.id.restore) Button mRestore;
    @Bind(R.id.account) Button mAccount;
    @Bind(R.id.restore_list) Spinner mRestoreList;
    @Bind(R.id.progress) MaterialProgressViewEx mProgress;
    @Bind(R.id.layoutContainer) LinearLayout mContainer;

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
        mAccount.setEnabled(true);

        // Check for the existence of the backup folder.
        new CheckBackupFolderAsyncTask(getActivity(), getGoogleApiClient()).execute();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (result.getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED) {
            mContainer.setVisibility(View.VISIBLE);
            mBackup.setEnabled(false);
            mRestore.setEnabled(false);
            mRestoreList.setEnabled(false);
            mAccount.setEnabled(true);
            mProgress.stop();
        }
        super.onConnectionFailed(result);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        super.onConnectionSuspended(cause);
        mBackup.setEnabled(false);
        mAccount.setEnabled(false);
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
        ButterKnife.bind(this, view);

        mContainer.setVisibility(View.GONE);
        return view;
    }

    /**
     * Perform a backup.
     *
     * @param v The {@link View} clicked on.
     */
    @OnClick(R.id.backup)
    public void onBackup(View v) {
        backup();
    }

    /**
     * Perform a restore.
     *
     * @param v The {@link View} clicked on.
     */
    @OnClick(R.id.restore)
    public void onRestore(View v) {
        DialogHelper.showYesNoDialog(getString(R.string.warning),
                Html.fromHtml(getString(R.string.restore_warning)),
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            restore();
                        }
                    }
                }, getActivity());
    }

    /**
     * Perform a account change.
     *
     * @param v The {@link View} clicked on.
     */
    @OnClick(R.id.account)
    public void onAccount(View v) {
        mAccount.setEnabled(false);
        changeAccount();
    }

    /**
     * Performs the database backup.
     */
    private synchronized void backup() {
        new DriveBackupAsyncTask(getActivity(), getGoogleApiClient(), mBackupFolder).execute();
    }

    /**
     * Performs a database restore.
     */
    private synchronized void restore() {
        if (mAdapter != null) {
            mAccount.setEnabled(false);
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
                    Snackbar.make(mContainer, s, Snackbar.LENGTH_LONG).show();
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
                    Snackbar.make(mContainer, result, Snackbar.LENGTH_LONG).show();
                } else {
                    // Loads the available files into the UI for selection.
                    RestoreFile[] files = new RestoreFile[getResults().size()];
                    getResults().toArray(files);
                    mAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_row_template_smalll, files);
                    mRestoreList.setAdapter(mAdapter);
                    mRestore.setEnabled(getResults().size() > 0);
                    mRestoreList.setEnabled(getResults().size() > 0);
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
                Snackbar.make(mContainer, result, Snackbar.LENGTH_LONG).show();
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
                Snackbar.make(mContainer, result, Snackbar.LENGTH_LONG).show();
                mProgress.stop();
                mBackup.setEnabled(true);
                mRestore.setEnabled(mAdapter.getCount() > 0);
                mRestoreList.setEnabled(mAdapter.getCount() > 0);
            }
        }
    }
}
