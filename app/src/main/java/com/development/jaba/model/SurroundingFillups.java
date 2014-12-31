package com.development.jaba.model;

/**
 * Object which stores the first fill up before and after a specific date. This
 * information is used to validate some of the entered values when entering a
 * new fill up or editing an existing fill up.
 */
public class SurroundingFillups
{
    private Fillup mBefore;
    private Fillup mAfter;

    /**
     * Gets the "before" {@link Fillup} entity.
     *
     * @return The "before" {@link Fillup} entity.
     */
    public Fillup getBefore() {
        return mBefore;
    }

    /**
     * Sets the "before" {@link Fillup} entity.
     */
    public void setBefore(Fillup mBefore) {
        this.mBefore = mBefore;
    }

    /**
     * Gets the "after" {@link Fillup} entity.
     *
     * @return The "after" {@link Fillup} entity.
     */
    public Fillup getAfter() {
        return mAfter;
    }

    /**
     * Gets the "after" {@link Fillup} entity.
     */
    public void setAfter(Fillup mAfter) {
        this.mAfter = mAfter;
    }
}