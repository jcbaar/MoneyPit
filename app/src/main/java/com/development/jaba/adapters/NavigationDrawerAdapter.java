package com.development.jaba.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.development.jaba.model.NavigationDrawerItem;
import com.development.jaba.moneypit.R;

import java.util.Collections;
import java.util.List;

/**
 * {@link com.development.jaba.adapters.BaseRecyclerViewAdapter} derived class for displaying the entries
 * in the navigation drawer {@link android.support.v7.widget.RecyclerView}.
 */
public class NavigationDrawerAdapter extends BaseRecyclerViewAdapter<NavigationDrawerAdapter.NavigationDrawerViewHolder> {

    private final Context mContext;
    private final LayoutInflater mInflater;
    private List<NavigationDrawerItem> mData = Collections.emptyList();

    /**
     * Initializes an instance of the object.
     * @param context The context.
     * @param data The data that is to be managed by this adapter.
     */
    public NavigationDrawerAdapter(Context context, List<NavigationDrawerItem> data) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mData = data;
    }

    /**
     * Creates a new {@link com.development.jaba.adapters.NavigationDrawerAdapter.NavigationDrawerViewHolder} object that manages
     * the {@link View} of the row.
     * @param parent The parent {@link android.view.ViewGroup}.
     * @param viewType The type of the view.
     * @return The created {@link com.development.jaba.adapters.NavigationDrawerAdapter.NavigationDrawerViewHolder}.
     */
    @Override
    public NavigationDrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = mInflater.inflate(R.layout.navigation_row_template, parent, false);
        NavigationDrawerViewHolder viewHolder = new NavigationDrawerViewHolder(mContext, rowView);

        // Make sure that we are listening to item clicks.
        viewHolder.setOnItemClickListener(this);
        return viewHolder;
    }

    /**
     * Setup the data to display for the given {@link com.development.jaba.adapters.NavigationDrawerAdapter.NavigationDrawerViewHolder}.
     * @param holder The {@link com.development.jaba.adapters.NavigationDrawerAdapter.NavigationDrawerViewHolder}.
     * @param position The position to setup the data for.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NavigationDrawerViewHolder vh = (NavigationDrawerViewHolder)holder;
        NavigationDrawerItem item = mData.get(position);

        // Setup the label to display the item title.
        vh.getLabel().setText(item.getTitle());
    }

    /**
     * Return the number of items in this adapter.
     * @return The number of items in the adapter.
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * A {@link com.development.jaba.adapters.BaseViewHolder} derived class to manage the {@link View} of the
     * navigation drawer items.
     */
    public class NavigationDrawerViewHolder extends BaseViewHolder {

        private final TextView mLabel;

        /**
         * Constructor. Initializes an instance of the object and caches the
         * child {@link View} objects.
         * @param context The context.
         * @param itemView The {@link View} which this instance will manage.
         */
        public NavigationDrawerViewHolder(Context context, View itemView) {
            super(context, itemView);
            mLabel = (TextView) itemView.findViewById(R.id.navigationLabel);
        }

        /**
         * Returns the instance of the cached {@link TextView} object representing the
         * navigation drawer item title.
         * @return The cached {@link TextView} title object.
         */
        public TextView getLabel() {
            return mLabel;
        }
    }
}
