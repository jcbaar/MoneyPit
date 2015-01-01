package com.development.jaba.adapters;

import com.development.jaba.model.Car;
import com.development.jaba.model.Fillup;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.FormattingHelper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * ArrayAdapter for displaying the fill-ups in the database in a ListView
 */
public class FillupRowAdapter extends BaseRecyclerViewAdapter<FillupRowAdapter.FillupRowViewHolder> {

    private final LayoutInflater mInflater;
    private Car mCar; // Car instance the fill-ups are bound to.
    private List<Fillup> mData = Collections.emptyList();
    private final Context mContext;

    //region Construction
    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context The context.
     * @param car The car the data is linked to.
     * @param values The data set which is managed by this adapter.
     */
    public FillupRowAdapter(Context context, Car car, List<Fillup> values) {
        mInflater = LayoutInflater.from(context);
        mCar = car;
        mData = values;
        mContext = context;
    }
    //endregion

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
     * @return The {@link Fillup} entity from the last clicked position or null if there is no last
     * clicked position or it was out of bounds.
     */
    public Fillup getLastClickedItem() {
        return getItem(getLastClickedPosition());
    }

    /**
     * A {@link com.development.jaba.adapters.BaseViewHolder} derived class to manage the {@link View} of the
     * {@link Car} row items.
     */
    public class FillupRowViewHolder extends BaseViewHolder {

        private final TextView mDate, mOdometer, mDistance,
                mDays, mTotalCost, mVolume, mCost, mEconomy;
        private final ImageButton mMenuButton;

        /**
         * Constructor. Initializes an instance of the object and caches the
         * child {@link View} objects.
         *
         * @param context The context.
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
    }
}
