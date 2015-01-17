package com.development.jaba.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.development.jaba.model.Car;
import com.development.jaba.model.Fillup;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.FormattingHelper;
import com.development.jaba.view.LinearLayoutEx;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * ArrayAdapter for displaying the fill-ups in the database in a ListView
 */
public class FillupRowAdapter extends BaseRecyclerViewAdapter<FillupRowAdapter.FillupRowViewHolder> {

    private final LayoutInflater mInflater;
    private Car mCar; // Car instance the fill-ups are bound to.
    private List<Fillup> mData = Collections.emptyList();
    private final Context mContext;
    private Vector<Integer> mExpandedItems;

    //region Construction

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context The context.
     * @param car     The car the data is linked to.
     * @param values  The data set which is managed by this adapter.
     */
    public FillupRowAdapter(Context context, Car car, List<Fillup> values) {
        mExpandedItems = new Vector<>();
        mInflater = LayoutInflater.from(context);
        mCar = car;
        mData = values;
        mContext = context;
    }
    //endregion

    /**
     * Setup a complete new data set for this adapter.
     *
     * @param data The new dataset.
     */
    public void setData(List<Fillup> data) {
        mData = data;
        mExpandedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Creates a new {@link com.development.jaba.adapters.FillupRowAdapter.FillupRowViewHolder} object that manages
     * the {@link View} of the row.
     *
     * @param parent   The parent {@link android.view.ViewGroup}.
     * @param viewType The type of the view.
     * @return The created {@link com.development.jaba.adapters.FillupRowAdapter.FillupRowViewHolder}.
     */
    @Override
    public FillupRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = mInflater.inflate(R.layout.fillup_row_template, parent, false);
        FillupRowViewHolder viewHolder = new FillupRowViewHolder(mContext, rowView);

        // Make sure that we are listening to item clicks.
        viewHolder.setOnItemClickListener(this);
        return viewHolder;
    }

    /**
     * Setup the data to display for the given {@link com.development.jaba.adapters.CarRowAdapter.CarRowViewHolder}.
     *
     * @param holder   The {@link com.development.jaba.adapters.CarRowAdapter.CarRowViewHolder}.
     * @param position The position to setup the data for.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final FillupRowViewHolder vh = (FillupRowViewHolder) holder;
        final Fillup item = mData.get(position);

        vh.getDate().setText(FormattingHelper.toShortDate(item.getDate()));
        vh.getOdometer().setText(FormattingHelper.toDistance(mCar, item.getOdometer()));
        vh.getDistance().setText(FormattingHelper.toDistance(mCar, item.getDistance()));
        vh.getDays().setText(FormattingHelper.toSpanInDays(item.getDaysSinceLastFillup()));
        vh.getTotalCost().setText(FormattingHelper.toPrice(mCar, item.getTotalPrice()));
        vh.getVolume().setText(FormattingHelper.toVolumeUnit(mCar, item.getVolume()));
        vh.getCost().setText(FormattingHelper.toPricePerVolumeUnit(mCar, item.getPrice()));
        vh.getEconomy().setText(FormattingHelper.toEconomy(mCar, item.getFuelConsumption()));
        vh.getLocation().setVisibility(item.getLongitude() == 0 && item.getLatitude() == 0 ? View.INVISIBLE : View.VISIBLE);
        vh.getNote().setVisibility(TextUtils.isEmpty(item.getNote()) ? View.INVISIBLE : View.VISIBLE);
        vh.getFull().setVisibility(!item.getFullTank() ? View.INVISIBLE : View.VISIBLE);
        vh.getNoteContent().setText(item.getNote());

        // See if this is an "expanded" position. If it is we
        // need to expand it without animation.
        boolean wasExpanded = false;
        for (Integer i : mExpandedItems) {
            if (i == position) {
                vh.getExpandable().expandNoAnim();
                wasExpanded = true;
                break;
            }
        }

        // This was not an expanded position. Collapse it without
        // animation.
        if (wasExpanded == false) {
            vh.getExpandable().collapseNoAnim();
        }
    }

    /**
     * Gets the number of items in this adapter.
     *
     * @return The number of items in the adapter.
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Gets the {@link Fillup} entity from the given position.
     *
     * @param position The position from which to get the {@link Fillup} entity.
     * @return The {@link Fillup} entity from the given position or null if a position was given that
     * is out of bounds.
     */
    public Fillup getItem(int position) {
        if (mData != null && position >= 0 && position < mData.size()) {
            return mData.get(position);
        }
        return null;
    }

    /**
     * Gets the {@link Fillup} entity from the last clicked position.
     *
     * @return The {@link Fillup} entity from the last clicked position or null if there is no last
     * clicked position or it was out of bounds.
     */
    public Fillup getLastClickedItem() {
        return getItem(getLastClickedPosition());
    }

    /**
     * Captures clicks on the item views so that we can determine whether or not we need to
     * toggle the expandable view expand/collapse state.
     *
     * @param view        The {@link View} that was clicked.
     * @param position    The position of the {@link View} that was clicked.
     * @param isLongClick Will be true is the click was a long click.
     * @return True when the click was a long click and the request was processed.
     */
    @Override
    public boolean onRecyclerItemClicked(View view, int position, boolean isLongClick) {
        // We do nothing with long-clicks.
        if (!isLongClick) {
            // Get the data item.
            Fillup item = getItem(position);
            if (item != null) {
                // We can only expand or collapse when we have
                // either a valid longitude/latitude pair and/or
                // a note.
                if (item.getLongitude() != 0 ||
                        item.getLatitude() != 0 ||
                        !TextUtils.isEmpty(item.getNote())) {
                    LinearLayoutEx lle = (LinearLayoutEx) view.findViewById(R.id.animateView);

                    // If the item is not expanded we add it to the expanded list since
                    // the toggle will expand it. Otherwise we simply remove the item from
                    // the expanded list.
                    if (lle.isExpanded() == false) {
                        mExpandedItems.add((Integer) position);
                    } else {
                        mExpandedItems.remove((Integer) position);
                    }
                    lle.requestLayout();
                    lle.toggle();
                }
            }
        }
        return super.onRecyclerItemClicked(view, position, isLongClick);
    }

    /**
     * A {@link com.development.jaba.adapters.BaseViewHolder} derived class to manage the {@link View} of the
     * {@link Car} row items.
     */
    public class FillupRowViewHolder extends BaseViewHolder {

        private final TextView mDate, mOdometer, mDistance,
                mDays, mTotalCost, mVolume, mCost, mEconomy, mNoteContents;
        private final ImageButton mMenuButton;
        private final ImageView mLocation, mNote, mFull;
        private final LinearLayoutEx mExpandable;

        /**
         * Constructor. Initializes an instance of the object and caches the
         * child {@link View} objects.
         *
         * @param context  The context.
         * @param itemView The {@link View} which this instance will manage.
         */
        public FillupRowViewHolder(Context context, View itemView) {
            super(context, itemView);

            mDate = (TextView) itemView.findViewById(R.id.fillupDate);
            mOdometer = (TextView) itemView.findViewById(R.id.fillupOdometer);
            mDistance = (TextView) itemView.findViewById(R.id.fillupDistance);
            mDays = (TextView) itemView.findViewById(R.id.fillupSpan);
            mTotalCost = (TextView) itemView.findViewById(R.id.fillupTotalCost);
            mVolume = (TextView) itemView.findViewById(R.id.fillupVolume);
            mCost = (TextView) itemView.findViewById(R.id.fillupCost);
            mEconomy = (TextView) itemView.findViewById(R.id.fillupEconomy);
            mMenuButton = (ImageButton) itemView.findViewById(R.id.headerMenu);
            mLocation = (ImageView) itemView.findViewById(R.id.location);
            mNote = (ImageView) itemView.findViewById(R.id.note);
            mFull = (ImageView) itemView.findViewById(R.id.full);
            mExpandable = (LinearLayoutEx) itemView.findViewById(R.id.animateView);
            mNoteContents = (TextView) itemView.findViewById(R.id.noteContent);

            // Attach a PopupMenu to the menu button.
            setMenuView(mMenuButton, mContext.getResources().getStringArray(R.array.edit_delete));
        }

        public TextView getDate() {
            return mDate;
        }

        public TextView getOdometer() {
            return mOdometer;
        }

        public TextView getDistance() {
            return mDistance;
        }

        public TextView getDays() {
            return mDays;
        }

        public TextView getTotalCost() {
            return mTotalCost;
        }

        public TextView getVolume() {
            return mVolume;
        }

        public TextView getCost() {
            return mCost;
        }

        public TextView getEconomy() {
            return mEconomy;
        }

        public ImageView getLocation() {
            return mLocation;
        }

        public ImageView getNote() {
            return mNote;
        }

        public ImageView getFull() {
            return mFull;
        }

        public LinearLayoutEx getExpandable() {
            return mExpandable;
        }

        public TextView getNoteContent() {
            return mNoteContents;
        }
    }
}