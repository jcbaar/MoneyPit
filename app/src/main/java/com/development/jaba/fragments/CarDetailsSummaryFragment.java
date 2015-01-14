package com.development.jaba.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private List<Fillup> mFillups;
    private TextView mHeader,
            mData;

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
            mFillups = mContext.getFillupsOfCar(mCar.getId(), mCurrentYear);

            mHeader = (TextView) view.findViewById(R.id.header);
            mData = (TextView) view.findViewById(R.id.data);

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
            mFillups = mContext.getFillupsOfCar(mCar.getId(), year);
            summarize();
        }
    }

    private void addHeader(StringBuilder h, int resId, StringBuilder d) {
        h.append("<b>");
        h.append(getString(resId));
        h.append("</b><br/>");

        d.append("<br/>");
    }

    private void addLine(StringBuilder h, int resId, StringBuilder d, String data) {
        h.append(getString(resId));
        h.append("<br/>");

        d.append(data);
        d.append("<br/>");
    }

    private void addLine(StringBuilder h, String hData, StringBuilder d, String data) {
        h.append(hData);
        h.append("<br/>");

        d.append(data);
        d.append("<br/>");
    }

    private void addEmptyLine(StringBuilder h, StringBuilder d) {
        h.append("<br/>");
        d.append("<br/>");
    }

    /**
     * Summarizes the fill-up information and displays it on the screen.
     */
    private void summarize() {
        CarSummary summary = new CarSummary();
        summary.setup(mFillups);

        StringBuilder header = new StringBuilder(),
                data = new StringBuilder();

        addHeader(header, R.string.summary_totals, data);
        addLine(header, R.string.summary_distance_total, data, FormattingHelper.toDistance(mCar, summary.TotalDistance));
        addLine(header, R.string.summary_cost_total, data, FormattingHelper.toPrice(mCar, summary.TotalFuelCost));
        addLine(header, R.string.summary_volume_total, data, FormattingHelper.toVolumeUnit(mCar, summary.TotalVolume));
        addEmptyLine(header, data);

        addHeader(header, R.string.summary_averages, data);
        addLine(header, R.string.summary_economy_average, data, FormattingHelper.toEconomy(mCar, summary.AverageFuelEconomy));
        addLine(header, R.string.summary_cost_average, data, FormattingHelper.toPrice(mCar, summary.AverageCostPerMonth));
        addLine(header, getString(R.string.summary_cost_per) + " " + mCar.getDistanceUnit().toString().toLowerCase() + ": ", data, FormattingHelper.toPrice(mCar, summary.AverageFuelCostPerDistanceUnit));
        addLine(header, getString(R.string.summary_cost_per) + " " + mCar.getVolumeUnit().toString().toLowerCase() + ": ", data, FormattingHelper.toPrice(mCar, summary.AverageFuelCostPerVolumeUnit));
        addEmptyLine(header, data);

        addHeader(header, R.string.summary_summary, data);
        addLine(header, R.string.summary_most_expensive_month, data, FormattingHelper.toPrice(mCar, summary.MostExpensiveMonth.Value) + " (" + DateHelper.toMonthYearString(summary.MostExpensiveMonth.Date) + ")");
        addLine(header, R.string.summary_least_expensive_month, data, FormattingHelper.toPrice(mCar, summary.LeastExpensiveMonth.Value) + " (" + DateHelper.toMonthYearString(summary.LeastExpensiveMonth.Date) + ")");
        addEmptyLine(header, data);

        addLine(header, R.string.summary_best_economy_fillup, data, FormattingHelper.toEconomy(mCar, summary.BestEconomyFillup.Value) + " (" + FormattingHelper.toShortDate(summary.BestEconomyFillup.Date) + ")");
        addLine(header, R.string.summary_worst_economy_fillup, data, FormattingHelper.toEconomy(mCar, summary.WorstEconomyFillup.Value) + " (" + FormattingHelper.toShortDate(summary.WorstEconomyFillup.Date) + ")");
        addEmptyLine(header, data);

        addLine(header, R.string.summary_most_expensive_fillup, data, FormattingHelper.toPrice(mCar, summary.MostExpensiveFillup.Value) + " (" + FormattingHelper.toShortDate(summary.MostExpensiveFillup.Date) + ")");
        addLine(header, R.string.summary_least_expensive_fillup, data, FormattingHelper.toPrice(mCar, summary.LeastExpensiveFillup.Value) + " (" + FormattingHelper.toShortDate(summary.LeastExpensiveFillup.Date) + ")");

        mHeader.setText(Html.fromHtml(header.toString()));
        mData.setText(Html.fromHtml(data.toString()));
    }
}
