package com.development.jaba.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DateHelper;
import com.development.jaba.utilities.FormattingHelper;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.Date;

/**
 * A {@link GraphFragment} subclass containing the distance-per-month
 * and economy-per-month graphs.
 */
public class CarDetailsEconomyFragment extends GraphFragment {

    private MoneyPitDbContext mDbContext;
    private GraphView mDistancePerMonth,
            mEconomyPerMonth;

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

        mDbContext = new MoneyPitDbContext(getActivity());
        mMonths = getResources().getStringArray(R.array.months);

        mDistancePerMonth = (GraphView) view.findViewById(R.id.distancePerMonth);
        mEconomyPerMonth = (GraphView) view.findViewById(R.id.economyPerMonth);

        mDistancePerMonth.setTitle(getResources().getString(R.string.graph_distance));
        mEconomyPerMonth.setTitle(getResources().getString(R.string.graph_economy));

        setupRenderers(mDistancePerMonth, new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    if (value >= 0 && value <= 12) {
                        return mMonths[(int) value];
                    }
                    return super.formatLabel(value, true);
                } else {
                    return FormattingHelper.toDistance(mCar, value) + " ";
                }
            }
        });

        setupBarsAndAverages();
        setupRenderers(mEconomyPerMonth, new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    if (value >= 0 && value <= 12) {
                        return mMonths[(int) value];
                    }
                    return super.formatLabel(value, true);
                } else {
                    return FormattingHelper.toEconomy(mCar, value) + " ";
                }
            }
        });

        return view;
    }

    /**
     * Setup the series in the {@link com.jjoe64.graphview.GraphView} views.
     */
    private void setupBarsAndAverages() {
        if (mCurrentYear == 0) {
            mCurrentYear = DateHelper.getYearFromDate(new Date());
        }

        if (mDbContext != null && mCar != null) {
            final Resources res = getResources();
            DataPoint[] data = mDbContext.getDistancePerMonth(mCar.getId(), mCurrentYear);
            setupBarsSeries(mDistancePerMonth, data, res.getString(R.string.graph_distance_legend), new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Snackbar.make(mDistancePerMonth, String.format(res.getString(R.string.graph_distance_per_month), DateHelper.toMonthNameString((int) dataPoint.getX()), mCurrentYear) +
                            " " +
                            FormattingHelper.toDistance(mCar, dataPoint.getY()), Snackbar.LENGTH_LONG).show();
                }
            });
            data = mDbContext.getEconomyPerMonth(mCar.getId(), mCurrentYear);
            setupBarsSeries(mEconomyPerMonth, data, res.getString(R.string.graph_economy_legend), new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Snackbar.make(mEconomyPerMonth, String.format(res.getString(R.string.graph_economy_per_month), DateHelper.toMonthNameString((int) dataPoint.getX()), mCurrentYear) +
                            " " +
                            FormattingHelper.toEconomy(mCar, dataPoint.getY()), Snackbar.LENGTH_LONG).show();
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
