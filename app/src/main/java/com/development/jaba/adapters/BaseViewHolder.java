package com.development.jaba.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Base class implementation for the {@link android.support.v7.widget.RecyclerView.ViewHolder}. Ite extends the
 * {@link android.support.v7.widget.RecyclerView.ViewHolder} class with the possibility to capture and act on
 * item clicked.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private OnRecyclerItemClicked mClickListener;

    /**
     * Constructor. Initializes an instance of the object.
     * @param itemView The {@link View} this instance will manage.
     */
    public BaseViewHolder(View itemView) {
        super(itemView);

        // Setup ourselves as the listener for click events on the view we manage. We use this listener
        // to forward the click events to the adapter.
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    /**
     * Setup the callback we need to call when a click on the {@link View} we manage is detected.
     * @param listener The {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback.
     */
    public void setOnItemClickListener(OnRecyclerItemClicked listener) {
        mClickListener = listener;
    }

    /**
     * The callback that is called when a click on the {@link View} we manage is detected. If we have
     * a valid {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback registered we forward
     * the click event to that.
     * @param v The {@link View} that was clicked.
     */
    @Override
    public void onClick(View v) {
        if(mClickListener != null) {
            mClickListener.onRecyclerItemClicked(v, getPosition(), false);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return mClickListener != null && mClickListener.onRecyclerItemClicked(v, getPosition(), true);
    }
}
