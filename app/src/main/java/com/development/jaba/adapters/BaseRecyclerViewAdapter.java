package com.development.jaba.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Base class implementation of the {@link android.support.v7.widget.RecyclerView.Adapter} which supports
 * detecting of item clicks.
 * @param <VH> The type of the {@link com.development.jaba.adapters.BaseViewHolder} derived type that will act
 *            as the {@link android.support.v7.widget.RecyclerView.ViewHolder} for this {@link android.support.v7.widget.RecyclerView.Adapter}.
 */
public abstract class BaseRecyclerViewAdapter<VH> extends RecyclerView.Adapter
                                                  implements OnRecyclerItemClicked {

    private OnRecyclerItemClicked mClickListener;

    /**
     * Sets up the {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback which will be called when a
     * item click is detected.
     * @param listener The {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback to call when an
     *                 item was clicked.
     */
    public void setOnRecyclerItemClicked(OnRecyclerItemClicked listener) {
        mClickListener = listener;
    }

    /**
     * Forwards item clicked to the registered {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback.
     * @param view The {@link View} that was clicked.
     * @param position The position of the {@link View} that was clicked.
     * @param isLongClick Will be true is the click was a long click.
     * @return True if the lock click was handled.
     */
    @Override
    public boolean onRecyclerItemClicked(View view, int position, boolean isLongClick) {
        return mClickListener != null && mClickListener.onRecyclerItemClicked(view, position, isLongClick);
    }
}