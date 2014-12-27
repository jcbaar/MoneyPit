package com.development.jaba.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.development.jaba.model.Car;
import com.development.jaba.model.CarAverage;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.FormattingHelper;

import java.util.Collections;
import java.util.List;

/**
 * {@link com.development.jaba.adapters.BaseRecyclerViewAdapter} derived class for displaying the entries
 * in the navigation drawer {@link android.support.v7.widget.RecyclerView}.
 */
public class CarRowAdapter extends BaseRecyclerViewAdapter<CarRowAdapter.CarRowViewHolder> {

    private LayoutInflater mInflater;
    private List<Car> mData = Collections.emptyList();
    private Context mContext;

    /**
     * Initializes an instance of the object.
     * @param context The context.
     * @param data The data that is to be managed by this adapter.
     */
    public CarRowAdapter(Context context, List<Car> data) {
        mInflater = LayoutInflater.from(context);
        mData = data;
        mContext = context;
    }

    /**
     * Creates a new {@link com.development.jaba.adapters.CarRowAdapter.CarRowViewHolder} object that manages
     * the {@link View} of the row.
     * @param parent The parent {@link android.view.ViewGroup}.
     * @param viewType The type of the view.
     * @return The created {@link com.development.jaba.adapters.CarRowAdapter.CarRowViewHolder}.
     */
    @Override
    public CarRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = mInflater.inflate(R.layout.car_row_template, parent, false);
        CarRowViewHolder viewHolder = new CarRowViewHolder(rowView);

        // Make sure that we are listening to item clicks.
        viewHolder.setOnItemClickListener(this);
        return viewHolder;
    }

    /**
     * Setup the data to display for the given {@link com.development.jaba.adapters.CarRowAdapter.CarRowViewHolder}.
     * @param holder The {@link com.development.jaba.adapters.CarRowAdapter.CarRowViewHolder}.
     * @param position The position to setup the data for.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CarRowViewHolder vh = (CarRowViewHolder)holder;
        Car item = mData.get(position);

        // Setup the View with the item data.
        // Replace the default picture if one is provided from the database.
        if (item.getPicture() != null) {
            vh.getImage().setImageBitmap(item.getImage());
        }

        vh.getMake().setText(item.toString());
        vh.getBuild().setText(String.valueOf(item.getLicensePlate()) + " (" + String.valueOf(item.getBuildYear()) + ")");

        CarAverage avg = item.getAverages();
        if(avg != null) {
            vh.getAverage().setText(String.format(mContext.getResources().getString(R.string.car_list_averages),
                    FormattingHelper.toPricePerVolumeUnit(item, avg.getAveragePricePerVolumeUnit()),
                    FormattingHelper.toVolumeUnit(item, avg.getAverageVolumePerFillup())));
        }
    }

    /**
     * Return the number of items in this adapter.
     * @return The number of items in the adapter.
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public Car getItem(int position) {
        if(mData != null && position >= 0 && position < mData.size()) {
            return mData.get(position);
        }
        return null;
    }

    /**
     * A {@link com.development.jaba.adapters.BaseViewHolder} derived class to manage the {@link View} of the
     * {@link Car} row items.
     */
    public class CarRowViewHolder extends BaseViewHolder {

        private ImageView mImage;
        private TextView mMake, mBuild, mAverage;

        /**
         * Constructor. Initializes an instance of the object and caches the
         * child {@link View} objects.
         * @param itemView The {@link View} which this instance will manage.
         */
        public CarRowViewHolder(View itemView) {
            super(itemView);
            mImage = (ImageView) itemView.findViewById(R.id.carPicture);
            mMake = (TextView) itemView.findViewById(R.id.carMakeModel);
            mBuild = (TextView) itemView.findViewById(R.id.carBuildYear);
            mAverage = (TextView) itemView.findViewById(R.id.carAverages);
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

        public TextView getAverage() {
            return mAverage;
        }
    }
}
