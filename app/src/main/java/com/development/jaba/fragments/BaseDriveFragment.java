package com.development.jaba.fragments;

/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Basically this code is the base activity code from the Google samples.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

/**
 * {@link android.support.v4.app.Fragment} derived class that serves as a base class for
 * Google Drive related fragments.
 */
public class BaseDriveFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * Logging tag.
     */
    private static final String TAG = "BaseDriveFragment";

    /**
     * True when we are trying to resolve connection errors.
     */
    private boolean mResolvingError = false;

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1001;

    /**
     * Unique tag for error dialog fragment.
     */
    private static final String DIALOG_ERROR = "dialog_error";

    /**
     * Tag for storing error resolving state.
     */
    private static final String STATE_RESOLVING_ERROR = "resolving_error";


    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Called when activity gets visible. A connection to Drive services need to
     * be initiated as soon as the activity is visible. Registers
     * {@code ConnectionCallbacks} and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .useDefaultAccount()
                    .build();
        }
        mGoogleApiClient.connect();
    }

    /**
     * Called when this {@link BaseDriveFragment} is created. Used to restore
     * the error resolving state.
     *
     * @param savedInstanceState {@link Bundle} containing the instance states or null.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResolvingError = savedInstanceState != null &&
                savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
    }

    /**
     * Save instance states. Used to save error resolving state.
     *
     * @param outState The {@link Bundle} in which to save the states.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }


    /**
     * Handles resolution callbacks.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION) {
            mResolvingError = false;
            if (resultCode == Activity.RESULT_OK) {
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    /**
     * Called when activity gets invisible. Connection to Drive service needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    public void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
        mResolvingError = false;
    }

    /**
     * Called when {@code mGoogleApiClient} is disconnected.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution is
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (!result.hasResolution()) {
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
            return;
        }
        try {
            mResolvingError = true;
            result.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            mGoogleApiClient.connect();
        }
    }

    /**
     * Creates a dialog for an error message
     */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog and pass it
        // the error to display.
        ErrorDialogFragment dialogFragment = ErrorDialogFragment.newInstance(this, errorCode);
        dialogFragment.show(getActivity().getSupportFragmentManager(), "errordialog");
    }

    /**
     *  Called from ErrorDialogFragment when the dialog is dismissed.
     */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /**
     * Changes the account used for drive backup and restore.
     */
    public void changeAccount() {
        if (mGoogleApiClient != null) {
            mResolvingError = false;
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.clearDefaultAccountAndReconnect();
            } else {
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Getter for the {@link GoogleApiClient} instance.
     *
     * @return The {@link GoogleApiClient} instance.
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * A fragment to display an error dialog
     */
    public static class ErrorDialogFragment extends DialogFragment {
        BaseDriveFragment mCaller;

        /**
         * Instantiate a new instance of the {@link com.development.jaba.fragments.BaseDriveFragment.ErrorDialogFragment}.
         *
         * @param caller    The {@link BaseDriveFragment} that is calling the error dialog.
         * @param errorCode The error to show.
         * @return A new instance of {@link com.development.jaba.fragments.BaseDriveFragment.ErrorDialogFragment}.
         */
        public static ErrorDialogFragment newInstance(BaseDriveFragment caller, int errorCode) {
            ErrorDialogFragment dialog = new ErrorDialogFragment();
            dialog.mCaller = caller;
            Bundle args = new Bundle();
            args.putInt(DIALOG_ERROR, errorCode);
            dialog.setArguments(args);
            return dialog;
        }

        /**
         * Constructor.Initializes and instance of the object.
         */
        public ErrorDialogFragment() {
        }

        /**
         * Called to create the dialog fragment.
         *
         * @param savedInstanceState Saved instance variables.
         * @return The {@link Dialog}.
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_CODE_RESOLUTION);
        }

        /**
         * Called when the user dismisses the error dialog.
         *
         * @param dialog The {@link DialogInterface}.
         */
        @Override
        public void onDismiss(DialogInterface dialog) {
            // If we have a caller tell it the dialog was dismissed.
            if (mCaller != null) {
                mCaller.onDialogDismissed();
            }
        }
    }
}
