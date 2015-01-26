package com.development.jaba.drive;

import com.google.android.gms.drive.DriveId;

/**
 * Class for holding the available backup files.
 */
public class RestoreFile {

    /**
     * The file it's title.
     */
    public String Title;

    /**
     * The file its {@link com.google.android.gms.drive.DriveId}.
     */
    public DriveId Id;

    /**
     * Converts the object to a human readable {@link java.lang.String}.
     *
     * @return The {@link java.lang.String} representing the object.
     */
    @Override
    public String toString() {
        return Title;
    }
}
