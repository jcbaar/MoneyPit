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
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A {@link GraphFragment} subclass containing the distance-per-month
 * and economy-per-month graphs.
 */
public class CarDetailsCostFragment extends GraphFragment {

    @Bind(R.id.costPerMonth) CombinedChart mCostPerMonth;
    @Bind(R.id.costPerDistanceUnit) CombinedChart mCostPerDistanceUnit;

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
        View view = inflater.inflate(R.layout.fragment_car_details_cost, container, false);
        ButterKnife.bind(this, view);

        setupChart(mCostPerMonth, new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return FormattingHelper.toPrice(mCar, value) + " ";
            }
        });

        setupChart(mCostPerDistanceUnit, new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return FormattingHelper.toPricePerDistanceUnit(mCar, value) + " ";
            }
        });

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
            List<BarEntry> dataCM = db.getFuelCostPerMonth(mCar.getId(), mCurrentYear);
            List<BarEntry> dataCD = db.getFuelCostPerKilometerPerMonth(mCar.getId(), mCurrentYear);

            setupChartData(mCostPerMonth, dataCM, R.string.graph_cost_legend);
            setupChartData(mCostPerDistanceUnit, dataCD, R.string.graph_cost_legend);

            mCostPerMonth.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                    if (e.getVal() != 0 && dataSetIndex == 0) {
                        Snackbar.make(mCostPerMonth, String.format(getResources().getString(R.string.graph_cost_per_month), DateHelper.toMonthNameString(e.getXIndex()), mCurrentYear) +
                                " " +
                                FormattingHelper.toPrice(mCar, e.getVal()), Snackbar.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onNothingSelected() {

                }
            });

            mCostPerDistanceUnit.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                    if (e.getVal() != 0 && dataSetIndex == 0) {
                        Snackbar.make(mCostPerDistanceUnit, String.format(getResources().getString(R.string.graph_cost_per_distance_per_month), getResources().getString(R.string.longKilometer).toLowerCase(), DateHelper.toMonthNameString(e.getXIndex()), mCurrentYear) +
                                " " +
                                FormattingHelper.toPricePerDistanceUnit(mCar, e.getVal()), Snackbar.LENGTH_LONG).show();
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
}
