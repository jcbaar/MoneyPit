package com.development.jaba.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DateHelper;
import com.development.jaba.utilities.FormattingHelper;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A {@link GraphFragment} subclass containing the distance-per-month
 * and economy-per-month graphs.
 */
public class CarDetailsEconomyFragment extends GraphFragment {

    @Bind(R.id.distancePerMonth) CombinedChart mDistancePerMonth;
    @Bind(R.id.economyPerMonth) CombinedChart mEconomyPerMonth;

    /**
     * Called to create a new {@link View}.
     *
     * @param inflater           The {@link android.view.LayoutInflater} to use for infaltion.
     * @param container          The {@link ViewGroup} container.
     * @param savedInstanceState Saved values to restore.
     * @return The inflated {@link View}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_car_details_economy, container, false);
        ButterKnife.bind(this, view);

        setupChart(mDistancePerMonth, new DistanceFormatter());
        setupChart(mEconomyPerMonth, new EconomyFormatter());

        setupBarsAndAverages();
        return view;
    }

    /**
     * Setup the data.
     */
    private void setupBarsAndAverages() {
        if (mCurrentYear == 0) {
            mCurrentYear = DateHelper.getYearFromDate(new Date());
        }

        if(!isAdded()) {
            return;
        }

        MoneyPitDbContext db = getDbContext();
        if (db != null && mCar != null) {
            ArrayList<BarEntry> dataDM = db.getDistancePerMonth(mCar.getId(), mCurrentYear);
            ArrayList<BarEntry> dataEM = db.getEconomyPerMonth(mCar.getId(), mCurrentYear);

            setupChartData(mDistancePerMonth, dataDM, R.string.graph_distance_legend);
            setupChartData(mEconomyPerMonth, dataEM, R.string.graph_economy_legend);

            mDistancePerMonth.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    if (e instanceof BarEntry && e.getX() >= 0 && e.getX() <= 11 && e.getY() != 0) {
                        Snackbar.make(mDistancePerMonth, String.format(getResources().getString(R.string.graph_distance_per_month), DateHelper.toMonthNameString((int)e.getX()), mCurrentYear) +
                                " " +
                                FormattingHelper.toDistance(mCar, e.getY()), Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onNothingSelected() {

                }
            });

            mEconomyPerMonth.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    if(e instanceof BarEntry && e.getX() >= 0 && e.getX() <= 11 && e.getY() != 0) {
                        Snackbar.make(mEconomyPerMonth, String.format(getResources().getString(R.string.graph_economy_per_month), DateHelper.toMonthNameString((int)e.getX()), mCurrentYear) +
                                " " +
                                FormattingHelper.toEconomy(mCar, e.getY()), Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onNothingSelected() {

                }
            });
        }
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
        setupBarsAndAverages();
    }

    /**
     * The data to display has changed. Reload it.
     */
    @Override
    public void onDataChanged() {
        super.onDataChanged();
        setupBarsAndAverages();
    }

    /**
     * Class implementing the {@link IAxisValueFormatter} interface for showing the distance axis.
     */
    public class DistanceFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            if(value != 0) {
                return FormattingHelper.toDistance(mCar, value) + " ";
            }
            return "";
        }

        @Override
        public int getDecimalDigits() {
            return 2;
        }
    }

    /**
     * Class implementing the {@link IAxisValueFormatter} interface for showing the economy axis.
     */
    public class EconomyFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            if(value != 0) {
                return FormattingHelper.toEconomy(mCar, value) + " ";
            }
            return "";
        }

        @Override
        public int getDecimalDigits() {
            return 2;
        }
    }
}
