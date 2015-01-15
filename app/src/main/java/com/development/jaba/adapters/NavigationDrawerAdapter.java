package com.development.jaba.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private int mSelectedItem = -1;

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
     * Marks the given position as the selected position.
     * @param position The position ro mark as selected. Negative means no selection.
     */
    public void selectItem(int position) {
        if (position != mSelectedItem) {
            // Unselect the currently selected item.
            if (mSelectedItem >= 0) {
                notifyItemChanged(mSelectedItem);
            }

            // And select the new item.
            mSelectedItem = position;
            if (mSelectedItem >= 0) {
                notifyItemChanged(mSelectedItem);
            }
        }
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

        // Show us as selected if we are the selected item.
        vh.getItemView().setSelected(mSelectedItem == position);

        // Setup the label to display the item title. The selected item
        // is shown in bold.
        vh.getLabel().setText(item.getTitle());
        vh.getLabel().setTypeface(null, mSelectedItem == position ? Typeface.BOLD : Typeface.NORMAL);

        // And lastly setup the icon.
        vh.getIcon().setImageDrawable(mContext.getResources().getDrawable(item.getIconId()));
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

        private final View mItemView;
        private final TextView mLabel;
        private final ImageView mIcon;

        /**
         * Constructor. Initializes an instance of the object and caches the
         * child {@link View} objects.
         * @param context The context.
         * @param itemView The {@link View} which this instance will manage.
         */
        public NavigationDrawerViewHolder(Context context, View itemView) {
            super(context, itemView);

            mItemView = itemView;
            mLabel = (TextView) itemView.findViewById(R.id.navigationLabel);
            mIcon = (ImageView) itemView.findViewById(R.id.navigationIcon);
        }

        /**
         * Returns the instance of the cached {@link TextView} object representing the
         * navigation drawer item title.
         * @return The cached {@link TextView} title object.
         */
        public TextView getLabel() {
            return mLabel;
        }

        /**
         * Returns the instance of the cached {@link ImageView} object representing the
         * navigation drawer item icon.
         *
         * @return The cached {@link ImageView} icon object.
         */
        public ImageView getIcon() {
            return mIcon;
        }

        /**
         * Returns the instance of the cached {@link android.view.View} object representing the
         * navigation drawer item container.
         *
         * @return The cached {@link View} object.
         */
        public View getItemView() {
            return mItemView;
        }
    }
}
