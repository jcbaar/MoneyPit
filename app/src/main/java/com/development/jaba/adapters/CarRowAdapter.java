package com.development.jaba.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.development.jaba.model.Car;
import com.development.jaba.model.CarAverage;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.FormattingHelper;
import com.development.jaba.utilities.GetCarImageHelper;

import java.text.Normalizer;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * {@link com.development.jaba.adapters.BaseRecyclerViewAdapter} derived class for displaying the entries
 * in the navigation drawer {@link android.support.v7.widget.RecyclerView}.
 */
public class CarRowAdapter extends BaseRecyclerViewAdapter<CarRowAdapter.CarRowViewHolder> {

    private final LayoutInflater mInflater;
    private List<Car> mData = Collections.emptyList();
    private final Context mContext;

    /**
     * Initializes an instance of the object.
     *
     * @param context The context.
     * @param data    The data that is to be managed by this adapter.
     */
    public CarRowAdapter(Context context, List<Car> data) {
        mInflater = LayoutInflater.from(context);
        mData = data;
        mContext = context;
    }

    /**
     * Creates a new {@link com.development.jaba.adapters.CarRowAdapter.CarRowViewHolder} object that manages
     * the {@link View} of the row.
     *
     * @param parent   The parent {@link android.view.ViewGroup}.
     * @param viewType The type of the view.
     * @return The created {@link com.development.jaba.adapters.CarRowAdapter.CarRowViewHolder}.
     */
    @Override
    public CarRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = mInflater.inflate(R.layout.car_row_template, parent, false);
        CarRowViewHolder viewHolder = new CarRowViewHolder(mContext, rowView);

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
    public void onBindViewHolder(CarRowViewHolder holder, int position) {
        final Car item = mData.get(position);

        // Replace the default picture if one is provided from the database.
        new GetCarImageHelper(mContext, holder.getImage(), item, ContextCompat.getColor(mContext, R.color.primaryColor), false).execute();

        // Setup the View with the item data.
        holder.getMake().setText(item.toString());
        holder.getBuild().setText(String.valueOf(item.getLicensePlate()) + " (" + String.valueOf(item.getBuildYear()) + ")");

        CarAverage avg = item.getAverages();
        if (avg != null) {
            holder.getAveragePrice().setText(String.format(mContext.getResources().getString(R.string.car_list_average_price),
                    FormattingHelper.toPricePerVolumeUnit(item, avg.getAveragePricePerVolumeUnit())));
            holder.getAverageVolume().setVisibility(View.VISIBLE);
            holder.getAverageVolume().setText(String.format(mContext.getResources().getString(R.string.car_list_average_volume),
                    FormattingHelper.toVolumeUnit(item, avg.getAverageVolumePerFillup())));
        } else {
            holder.getAveragePrice().setText(R.string.no_data_to_average);
            holder.getAverageVolume().setVisibility(View.GONE);
        }
    }

    /**
     * Gets the number of items in this adapter.
     *
     * @return The number of items in the adapter.
     */
    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    /**
     * Gets the {@link Car} entity from the given position.
     *
     * @param position The position from which to get the {@link Car} entity.
     * @return The {@link Car} entity from the given position or null if a position was given that
     * is out of bounds.
     */
    public Car getItem(int position) {
        if (mData != null && position >= 0 && position < mData.size()) {
            return mData.get(position);
        }
        return null;
    }

    /**
     * Gets the {@link Car} entity from the last clicked position.
     *
     * @return The {@link Car} entity from the last clicked position or null if there is no last
     * clicked position or it was out of bounds.
     */
    public Car getLastClickedItem() {
        return getItem(getLastClickedPosition());
    }

    /**
     * A {@link com.development.jaba.adapters.BaseViewHolder} derived class to manage the {@link View} of the
     * {@link Car} row items.
     */
    public class CarRowViewHolder extends BaseViewHolder {

        @Bind(R.id.headerMenu) ImageButton mMenuButton;
        @Bind(R.id.carPicture) ImageView mImage;
        @Bind(R.id.carMakeModel) TextView mMake;
        @Bind(R.id.carBuildYear) TextView mBuild;
        @Bind(R.id.carAveragePrice) TextView mAveragePrice;
        @Bind(R.id.carAverageFillup) TextView mAverageFillup;

        /**
         * Constructor. Initializes an instance of the object and caches the
         * child {@link View} objects.
         *
         * @param context  The context.
         * @param itemView The {@link View} which this instance will manage.
         */
        public CarRowViewHolder(Context context, View itemView) {
            super(context, itemView);
            ButterKnife.bind(this, itemView);

            // Attach a PopupMenu to the menu button.
            setMenuView(mMenuButton, mContext.getResources().getStringArray(R.array.edit_delete_summary));
        }

        public ImageView getImage() {
            return mImage;
        }

        public TextView getMake() {
            return mMake;
        }

        public TextView getBuild() {
            return mBuild;
        }

        public TextView getAveragePrice() {
            return mAveragePrice;
        }

        public TextView getAverageVolume() {
            return mAverageFillup;
        }
    }
}
