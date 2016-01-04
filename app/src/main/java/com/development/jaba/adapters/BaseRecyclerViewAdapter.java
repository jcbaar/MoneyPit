package com.development.jaba.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

/**
 * Base class implementation of the {@link android.support.v7.widget.RecyclerView.Adapter} which supports
 * detecting of item clicks.
 *
 * @param <VH> The type of the {@link com.development.jaba.adapters.BaseViewHolder} derived type that will act
 *             as the {@link android.support.v7.widget.RecyclerView.ViewHolder} for this {@link android.support.v7.widget.RecyclerView.Adapter}.
 */
public abstract class BaseRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>
        implements OnRecyclerItemClicked {

    private View mEmptyView;
    private OnRecyclerItemClicked mClickListener;
    private int mLastClickedPosition;

    /**
     * Constructor. Initializes an instance of this object.
     */
    protected BaseRecyclerViewAdapter() {

        // Register an observer which will show the mEmptyView View when the
        // Adapter does not contain any data.
        RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {

            private void checkEmptyView() {
                if (mEmptyView != null) {
                    int viewMode = getItemCount() == 0 ? View.VISIBLE : View.GONE;
                    if (viewMode != mEmptyView.getVisibility()) {
                        mEmptyView.setVisibility(viewMode);
                    }
                }
            }

            @Override
            public void onChanged() {
                super.onChanged();
                checkEmptyView();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmptyView();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmptyView();
            }
        };
        registerAdapterDataObserver(dataObserver);
    }

    /**
     * Sets up the {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback which will be called when a
     * item click is detected.
     *
     * @param listener The {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback to call when an
     *                 item was clicked.
     */
    public void setOnRecyclerItemClicked(OnRecyclerItemClicked listener) {
        mClickListener = listener;
    }

    /**
     * Forwards item clicked to the registered {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback.
     *
     * @param view        The {@link View} that was clicked.
     * @param position    The position of the {@link View} that was clicked.
     * @param isLongClick Will be true is the click was a long click.
     * @return True if the lock click was handled.
     */
    @Override
    public boolean onRecyclerItemClicked(View view, int position, boolean isLongClick) {
        mLastClickedPosition = position;
        return mClickListener != null && mClickListener.onRecyclerItemClicked(view, position, isLongClick);
    }

    /**
     * Forwards a menu item selection to the registered {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback.
     *
     * @param position The position of the {@link View} that was clicked.
     * @param item     The selected {@link android.view.MenuItem}.
     * @return true if the selection was handled, false if it was not.
     */
    @Override
    public boolean onRecyclerItemMenuSelected(int position, MenuItem item) {
        mLastClickedPosition = position;
        return mClickListener != null && mClickListener.onRecyclerItemMenuSelected(position, item);
    }

    /**
     * Forwards a expansion state change to the registered {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback.
     *
     * @param position   The position of the {@link View} that was clicked.
     * @param isExpanded true if the item was expanded, false if it was collapsed.
     */
    @Override
    public void onExpansionStateChanged(int position, boolean isExpanded) {
        mLastClickedPosition = position;
        if (mClickListener != null) {
            mClickListener.onExpansionStateChanged(position, isExpanded);
        }
    }

    /**
     * Gets the position of the last clicked item.
     *
     * @return The position of the last clicked item.
     */
    public int getLastClickedPosition() {
        return mLastClickedPosition;
    }

    /**
     * Setup the {@link View} which is shown when there are no items in the
     * {@link com.development.jaba.adapters.BaseRecyclerViewAdapter}.
     *
     * @param view The {@link View} to show when there is no data.
     */
    public void setEmptyView(final View view) {
        mEmptyView = view;
        if (view != null) {
            view.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }
}