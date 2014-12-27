package com.development.jaba.adapters;

import android.view.View;

/**
 * Callback (listener) interface definition for responding to clicks on an
 * item of a {@link android.support.v7.widget.RecyclerView} widget.
 */
public interface OnRecyclerItemClicked {

    /**
     * The method called when an item of a {@link android.support.v7.widget.RecyclerView} was clicked.
     * @param view The {@link View} that was clicked on.
     * @param position The position of the item that was clicked on.
     * @param isLongClick Will be true when the click was a long click.
     * @return Must return true when the listener has processed the click and the isLongClick parameter is true.
     */
    public boolean onRecyclerItemClicked(View view, int position, boolean isLongClick);
}