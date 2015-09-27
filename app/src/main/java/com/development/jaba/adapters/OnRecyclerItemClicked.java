package com.development.jaba.adapters;

import android.view.MenuItem;
import android.view.View;

/**
 * Callback (listener) interface definition for responding to clicks on an
 * item of a {@link android.support.v7.widget.RecyclerView} widget.
 */
public interface OnRecyclerItemClicked {

    /**
     * The method called when an item of a {@link android.support.v7.widget.RecyclerView} was clicked.
     *
     * @param view        The {@link View} that was clicked on.
     * @param position    The position of the item that was clicked on.
     * @param isLongClick Will be true when the click was a long click.
     * @return Must return true when the listener has processed the click and the isLongClick parameter is true.
     */
    boolean onRecyclerItemClicked(View view, int position, boolean isLongClick);

    /**
     * The method called when a {@link android.view.MenuItem} was clicked.
     *
     * @param position The position of the view of which a {@link android.view.MenuItem} was clicked.
     * @param item     The {@link android.view.MenuItem} that was clicked.
     * @return Must return true when the listener has processed the {@link android.view.MenuItem} click.
     */
    boolean onRecyclerItemMenuSelected(int position, MenuItem item);

    /**
     * The method called when the expanded state of one of the {@link android.support.v7.widget.RecyclerView} items
     * changed.
     *
     * @param position   The position of the item that changed.
     * @param isExpanded true if the items has expanded, false if it has collapsed.
     */
    void onExpansionStateChanged(int position, boolean isExpanded);
}
