package com.development.jaba.model;

/**
 * Data class for the navigation drawer items.
 */
public class NavigationDrawerItem {

    private String mTitle;
    private int mIconId;


    /**
     * Gets the title {@link String}.
     * @return The title {@link String}.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Sets the title {@link String}.
     * @param title The title {@link String}
     */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /**
     * Gets the icon resource ID for the item.
     *
     * @return The item icon resource ID.
     */
    public int getIconId() {
        return mIconId;
    }

    /**
     * Sets the icon resource ID for the item.
     *
     * @param iconId The icon resource ID for the item.
     */
    public void setIconId(int iconId) {
        this.mIconId = iconId;
    }
}
