package com.development.jaba.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.Car;
import com.development.jaba.model.CarSummary;
import com.development.jaba.model.Fillup;
import com.development.jaba.moneypit.Keys;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DateHelper;
import com.development.jaba.utilities.FormattingHelper;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A {@link BaseDetailsFragment} subclass containing the summary information
 * of a car for a given period.
 */
public class CarDetailsSummaryFragment extends BaseDetailsFragment {

    private View mLayout;
    @SuppressWarnings("unused")
    @Bind(R.id.summary_data) LinearLayout mData;
    @SuppressWarnings("unused")
    @Bind(R.id.summary_nodata) TextView mNoData;
    @SuppressWarnings("unused")
    @Bind(R.id.carLabel) TextView mCarLabel;
    @SuppressWarnings("unused")
    @Bind(R.id.labelLayout) LinearLayout mLabelLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (mCar == null) {
                mCar = (Car) savedInstanceState.getSerializable(Keys.EK_CAR);
                mCurrentYear = savedInstanceState.getInt(Keys.EK_CURRENTYEAR);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_details_summary, container, false);
        ButterKnife.bind(this, view);
        mLayout = view;
        summarize();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        summarize();
    }

    @Override
    public void setCar(Car car) {
        super.setCar(car);
        summarize();
    }

    /**
     * The user selected another year. Update the data to show the data
     * of the new year.
     *
     * @param year The year the user has selected.
     */
    @Override
    public void onYearSelected(int year) {
        super.onYearSelected(year);
        summarize();
    }

    /**
     * Fires off the {@link com.development.jaba.fragments.CarDetailsSummaryFragment.SummaryTask} to
     * process the data.
     */
    private void summarize() {
        if(mCar != null) {
            new SummaryTask().execute();
        }
    }

    /**
     * {@link android.os.AsyncTask} derived class to get the data to summarize and to do the
     * actual summarizing of that data.
     */
    private class SummaryTask extends AsyncTask<Void, Void, CarSummary> {

        private Date mOldest,
                mNewest;

        /**
         * Loads and summarizes the data from the database.
         *
         * @param params Parameters (not used).
         * @return Noting.
         */
        @Override
        protected CarSummary doInBackground(Void... params) {
            // TODO: This uses the same data set as the CarDetailsFillups fragment. They should really share it...
            MoneyPitDbContext context = new MoneyPitDbContext(getActivity());
            List<Fillup> data = context.getFillupsOfCar(mCar.getId(), mCurrentYear);
            if(data.size() > 1) {
                mOldest = data.get(data.size() - 1).getDate();
                mNewest = data.get(0).getDate();
                CarSummary summary = new CarSummary();
                summary.setup(data);
                return summary;
            }
            else {
                mOldest = new Date();
                mNewest = new Date();
                return null;
            }
        }

        void setHeader(int resId, int labelId) {
            View header = mLayout.findViewById(resId);
            if (header != null) {
                TextView label = (TextView) header.findViewById(R.id.headerText);
                if (label != null) {
                    label.setText(labelId);
                }
            }
        }

        void setData(int resId, int headerId, String dataText) {
            View container = mLayout.findViewById(resId);
            if (container != null) {
                TextView label = (TextView) container.findViewById(R.id.header),
                        data = (TextView) container.findViewById(R.id.data);
                if (label != null && data != null) {
                    label.setText(headerId);
                    data.setText(dataText);
                }
            }
        }

        void setData(int resId, String headerText, String dataText) {
            View container = mLayout.findViewById(resId);
            if (container != null) {
                TextView label = (TextView) container.findViewById(R.id.header),
                        data = (TextView) container.findViewById(R.id.data);
                if (label != null && data != null) {
                    label.setText(headerText);
                    data.setText(dataText);
                }
            }
        }

        /**
         * Back in the UI thread. Update the visuals with the summarized data.
         *
         * @param result The {@link com.development.jaba.model.CarSummary}.
         */
        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(CarSummary result) {

            if(!isAdded()) {
                return;
            }
            mCarLabel.setText(mCar.getLicensePlate() + " (" + mCar.getBuildYear() + "), " + FormattingHelper.toShortDate(mOldest) + " " + getString(R.string.upto) + " " + FormattingHelper.toShortDate(mNewest));
            mLabelLayout.setVisibility(View.VISIBLE);

            if(result == null) {
                mNoData.setVisibility(View.VISIBLE);
                mData.setVisibility(View.GONE);
                return;
            }
            mNoData.setVisibility(View.GONE);
            mData.setVisibility(View.VISIBLE);

            setHeader(R.id.headerTotals, R.string.summary_totals);
            setData(R.id.distance, R.string.summary_distance_total, FormattingHelper.toDistance(mCar, result.TotalDistance));
            setData(R.id.fuel, R.string.summary_cost_total, FormattingHelper.toPrice(mCar, result.TotalFuelCost));
            setData(R.id.volume, R.string.summary_volume_total, FormattingHelper.toVolumeUnit(mCar, result.TotalVolume));
            setData(R.id.fillups, getString(R.string.summary_fillups), Integer.toString(result.Fillups));

            setHeader(R.id.headerAvg, R.string.summary_averages);
            setData(R.id.economy, R.string.summary_economy_average, FormattingHelper.toEconomy(mCar, result.AverageFuelEconomy));
            setData(R.id.costMonth, R.string.summary_cost_average, FormattingHelper.toPrice(mCar, result.AverageCostPerMonth));
            setData(R.id.costDistance, getString(R.string.summary_cost_per) + " " + mCar.getDistanceUnit().toString().toLowerCase() + ": ", FormattingHelper.toPrice(mCar, result.AverageFuelCostPerDistanceUnit));
            setData(R.id.costVolume, getString(R.string.summary_cost_per) + " " + mCar.getVolumeUnit().toString().toLowerCase() + ": ", FormattingHelper.toPrice(mCar, result.AverageFuelCostPerVolumeUnit));
            setData(R.id.costFillup, getString(R.string.summary_cost_per_fillup), FormattingHelper.toPrice(mCar, result.AverageCostPerFillup));
            setData(R.id.distanceFillup, getString(R.string.summary_distance_per_fillup), FormattingHelper.toDistance(mCar, result.AverageDistancePerFillup));

            setHeader(R.id.summary, R.string.summary_summary);
            setData(R.id.expensiveMonth, R.string.summary_most_expensive_month, FormattingHelper.toPrice(mCar, result.MostExpensiveMonth.Value) + " (" + DateHelper.toMonthYearString(result.MostExpensiveMonth.Date) + ")");
            setData(R.id.cheapMonth, R.string.summary_least_expensive_month, FormattingHelper.toPrice(mCar, result.LeastExpensiveMonth.Value) + " (" + DateHelper.toMonthYearString(result.LeastExpensiveMonth.Date) + ")");

            setData(R.id.bestEconomy, R.string.summary_best_economy_fillup, FormattingHelper.toEconomy(mCar, result.BestEconomyFillup.Value) + " (" + FormattingHelper.toShortDate(result.BestEconomyFillup.Date) + ")");
            setData(R.id.worstEconomy, R.string.summary_worst_economy_fillup, FormattingHelper.toEconomy(mCar, result.WorstEconomyFillup.Value) + " (" + FormattingHelper.toShortDate(result.WorstEconomyFillup.Date) + ")");

            setData(R.id.expensiveFillup, R.string.summary_most_expensive_fillup, FormattingHelper.toPrice(mCar, result.MostExpensiveFillup.Value) + " (" + FormattingHelper.toShortDate(result.MostExpensiveFillup.Date) + ")");
            setData(R.id.cheapFillup, R.string.summary_least_expensive_fillup, FormattingHelper.toPrice(mCar, result.LeastExpensiveFillup.Value) + " (" + FormattingHelper.toShortDate(result.LeastExpensiveFillup.Date) + ")");
        }
    }
}
