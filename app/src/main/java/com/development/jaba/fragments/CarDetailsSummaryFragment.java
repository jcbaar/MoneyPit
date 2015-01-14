package com.development.jaba.fragments;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.Car;
import com.development.jaba.model.CarSummary;
import com.development.jaba.model.Fillup;
import com.development.jaba.moneypit.Keys;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DateHelper;
import com.development.jaba.utilities.FormattingHelper;

import java.util.List;

/**
 * A {@link BaseDetailsFragment} subclass containing the summary information
 * of a car for a given period.
 */
public class CarDetailsSummaryFragment extends BaseDetailsFragment {

    private MoneyPitDbContext mContext;
    private TextView mHeader,
            mData;
    private ImageView mImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (mCar == null) {
                mCar = (Car) savedInstanceState.getSerializable(Keys.EK_CAR);
            }
            mCurrentYear = savedInstanceState.getInt(Keys.EK_CURRENTYEAR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_details_summary, container, false);

        mContext = new MoneyPitDbContext(getActivity());
        if (mCar != null) {
            mHeader = (TextView) view.findViewById(R.id.header);
            mData = (TextView) view.findViewById(R.id.data);
            mImage = (ImageView) view.findViewById(R.id.image);

            Bitmap picture = mCar.getImage();
            if (picture != null) {
                mImage.setVisibility(View.VISIBLE);
                mImage.setImageBitmap(picture);
            } else {
                mImage.setVisibility(View.GONE);
            }
            summarize();
        }
        return view;
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
        if (mContext != null && mCar != null) {
            summarize();
        }
    }

    /**
     * Adds a category header to the summary text.
     *
     * @param h     The header {@link java.lang.StringBuilder}.
     * @param resId The resource ID of the header text.
     * @param d     The data {@link java.lang.StringBuilder}.
     */
    private void addHeader(StringBuilder h, int resId, StringBuilder d) {
        h.append("<b>");
        h.append(getString(resId));
        h.append("</b><br/>");

        d.append("<br/>");
    }

    /**
     * Helper to add a data line to the summary text.
     * @param h The header {@link java.lang.StringBuilder}.
     * @param resId The resource ID of the header text.
     * @param d The data {@link java.lang.StringBuilder}.
     * @param data The data string.
     */
    private void addLine(StringBuilder h, int resId, StringBuilder d, String data) {
        h.append(getString(resId));
        h.append("<br/>");

        d.append(data);
        d.append("<br/>");
    }

    /**
     * Helper to add a data line to the summary text.
     *
     * @param h     The header {@link java.lang.StringBuilder}.
     * @param hData The header string.
     * @param d     The data {@link java.lang.StringBuilder}.
     * @param data  The data string.
     */
    private static void addLine(StringBuilder h, String hData, StringBuilder d, String data) {
        h.append(hData);
        h.append("<br/>");

        d.append(data);
        d.append("<br/>");
    }

    /**
     * Helper to add an empty line in both the header and the data {@link android.widget.TextView}.
     *
     * @param h The header {@link java.lang.StringBuilder}.
     * @param d The data {@link java.lang.StringBuilder}.
     */
    private static void addEmptyLine(StringBuilder h, StringBuilder d) {
        h.append("<br/>");
        d.append("<br/>");
    }

    /**
     * Fires off the {@link com.development.jaba.fragments.CarDetailsSummaryFragment.SummaryTask} to
     * process the data.
     */
    private void summarize() {
        new SummaryTask().execute();
    }

    /**
     * Show the summary texts in the UI.
     */
    private void showSummary(Spanned header, Spanned data) {
        mHeader.setText(header);
        mData.setText(data);
    }

    /**
     * {@link android.os.AsyncTask} derived class to get the data to summarize and to do the
     * actual summarizing of that data.
     */
    private class SummaryTask extends AsyncTask<Void, Void, Void> {

        /**
         * The {@link com.development.jaba.model.CarSummary} containing the summarized data.
         */
        CarSummary mSummary;

        /**
         * {@link java.lang.StringBuilder} objects containing the header and data information.
         */
        StringBuilder mHeaderText = new StringBuilder(),
                mDataText = new StringBuilder();

        /**
         * Loads and summarizes the data from the database.
         *
         * @param params Parameters (not used).
         * @return Noting.
         */
        @Override
        protected Void doInBackground(Void... params) {
            // TODO: This uses the same data set as the CarDetailsFillups fragment. They should really share it...
            MoneyPitDbContext context = new MoneyPitDbContext(getActivity());
            List<Fillup> data = context.getFillupsOfCar(mCar.getId(), mCurrentYear);
            mSummary = new CarSummary();
            mSummary.setup(data);

            addHeader(mHeaderText, R.string.summary_totals, mDataText);
            addLine(mHeaderText, R.string.summary_distance_total, mDataText, FormattingHelper.toDistance(mCar, mSummary.TotalDistance));
            addLine(mHeaderText, R.string.summary_cost_total, mDataText, FormattingHelper.toPrice(mCar, mSummary.TotalFuelCost));
            addLine(mHeaderText, R.string.summary_volume_total, mDataText, FormattingHelper.toVolumeUnit(mCar, mSummary.TotalVolume));
            addEmptyLine(mHeaderText, mDataText);

            addHeader(mHeaderText, R.string.summary_averages, mDataText);
            addLine(mHeaderText, R.string.summary_economy_average, mDataText, FormattingHelper.toEconomy(mCar, mSummary.AverageFuelEconomy));
            addLine(mHeaderText, R.string.summary_cost_average, mDataText, FormattingHelper.toPrice(mCar, mSummary.AverageCostPerMonth));
            addLine(mHeaderText, getString(R.string.summary_cost_per) + " " + mCar.getDistanceUnit().toString().toLowerCase() + ": ", mDataText, FormattingHelper.toPrice(mCar, mSummary.AverageFuelCostPerDistanceUnit));
            addLine(mHeaderText, getString(R.string.summary_cost_per) + " " + mCar.getVolumeUnit().toString().toLowerCase() + ": ", mDataText, FormattingHelper.toPrice(mCar, mSummary.AverageFuelCostPerVolumeUnit));
            addEmptyLine(mHeaderText, mDataText);

            addHeader(mHeaderText, R.string.summary_summary, mDataText);
            addLine(mHeaderText, R.string.summary_most_expensive_month, mDataText, FormattingHelper.toPrice(mCar, mSummary.MostExpensiveMonth.Value) + " (" + DateHelper.toMonthYearString(mSummary.MostExpensiveMonth.Date) + ")");
            addLine(mHeaderText, R.string.summary_least_expensive_month, mDataText, FormattingHelper.toPrice(mCar, mSummary.LeastExpensiveMonth.Value) + " (" + DateHelper.toMonthYearString(mSummary.LeastExpensiveMonth.Date) + ")");
            addEmptyLine(mHeaderText, mDataText);

            addLine(mHeaderText, R.string.summary_best_economy_fillup, mDataText, FormattingHelper.toEconomy(mCar, mSummary.BestEconomyFillup.Value) + " (" + FormattingHelper.toShortDate(mSummary.BestEconomyFillup.Date) + ")");
            addLine(mHeaderText, R.string.summary_worst_economy_fillup, mDataText, FormattingHelper.toEconomy(mCar, mSummary.WorstEconomyFillup.Value) + " (" + FormattingHelper.toShortDate(mSummary.WorstEconomyFillup.Date) + ")");
            addEmptyLine(mHeaderText, mDataText);

            addLine(mHeaderText, R.string.summary_most_expensive_fillup, mDataText, FormattingHelper.toPrice(mCar, mSummary.MostExpensiveFillup.Value) + " (" + FormattingHelper.toShortDate(mSummary.MostExpensiveFillup.Date) + ")");
            addLine(mHeaderText, R.string.summary_least_expensive_fillup, mDataText, FormattingHelper.toPrice(mCar, mSummary.LeastExpensiveFillup.Value) + " (" + FormattingHelper.toShortDate(mSummary.LeastExpensiveFillup.Date) + ")");
            return null;
        }

        /**
         * Back in the UI thread. Update the visuals with the summarized data.
         *
         * @param result Nothing.
         */
        @Override
        protected void onPostExecute(Void result) {
            showSummary(Html.fromHtml(mHeaderText.toString()), Html.fromHtml(mDataText.toString()));
        }
    }
}
