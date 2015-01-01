package com.development.jaba.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.database.Utils;
import com.development.jaba.model.Car;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.FormattingHelper;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;

/**
 * A {@link BaseFragment} subclass containing the const-per-month
 * and cost-per-month-per-distance-unit graphs.
 */
public class CarDetailsCostFragment extends BaseFragment {

    private MoneyPitDbContext mDbContext;
    private GraphView mCostPerMonth,
            mCostPerDistanceUnit;
    private Car mCar;
    private int mCurrentYear;
    private String[] mMonths;

    /**
     * Static factory method. Creates a new instance of this fragment.
     * @param sectionNumber The section number in the Navigation Drawer.
     * @return The created fragment.
     */
    public static Fragment newInstance(int sectionNumber, Car carToShow) {
        CarDetailsCostFragment fragment = new CarDetailsCostFragment();
        fragment.mCar = carToShow;

        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Converts SP a pixel value.
     * @param context The {@link Context}.
     * @param dp The SP value to convert to pixels.
     *
     * @return The pixel value.
     */
    public static float spToPixels(Context context, float dp) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return dp*scaledDensity;
    }

    /**
     * Converts DP a pixel value.
     * @param context The {@link Context}.
     * @param dp The DP value to convert to pixels.
     *
     * @return The pixel value.
     */
    public static float dpToPixels(Context context, float dp) {
        float scaledDensity = context.getResources().getDisplayMetrics().density;
        return dp*scaledDensity;
    }

    /**
     * Called when a new instance is created.
     *
     * @param savedInstanceState Saved values to restore.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            if (mCar == null) {
                mCar = (Car) savedInstanceState.getSerializable("Car");
            }
            mCurrentYear = savedInstanceState.getInt("CurrentYear");
        }
    }

    /**
     * Called to create a new {@link View}.
     *
     * @param inflater The {@link android.view.LayoutInflater} to use for infaltion.
     * @param container The {@link ViewGroup} container.
     * @param savedInstanceState Saved values to restore.
     *
     * @return The inflated {@link View}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_car_details_cost, container, false);

        mDbContext = new MoneyPitDbContext(getActivity());
        mMonths = getResources().getStringArray(R.array.months);

        mCostPerMonth = (GraphView) view.findViewById(R.id.costPerMonth);
        mCostPerDistanceUnit = (GraphView) view.findViewById(R.id.costPerDistanceUnit);

        mCostPerMonth.setTitle(getResources().getString(R.string.graph_fuel_cost));
        mCostPerDistanceUnit.setTitle(getResources().getString(R.string.graph_fuel_cost_per_distance_unit));

        setupRenderers(mCostPerMonth, new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    if (value >= 0 && value <= 12) {
                        return mMonths[(int) value];
                    }
                    return super.formatLabel(value, isValueX);
                } else {
                    return FormattingHelper.toPrice(mCar, value) + " ";
                }
            }
        });

        setupRenderers(mCostPerDistanceUnit, new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    if (value >= 0 && value <= 12) {
                        return mMonths[(int) value];
                    }
                    return super.formatLabel(value, isValueX);
                } else {
                    return FormattingHelper.toPricePerDistanceUnit(mCar, value) + " ";
                }
            }
        });

        setupBarsAndAverages();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("Car", mCar);
        outState.putInt("CurrentYear", mCurrentYear);
        super.onSaveInstanceState(outState);
    }

    /**
     * Setup the renderers of the GraphView.
     *
     * @param gv The {@link com.jjoe64.graphview.GraphView} to setup.
     * @param formatter The {@link com.jjoe64.graphview.DefaultLabelFormatter} to use for label formatting.
     */
    private void setupRenderers(GraphView gv, DefaultLabelFormatter formatter) {
        GridLabelRenderer renderer = gv.getGridLabelRenderer();
        renderer.setTextSize(spToPixels(getActivity(), 10));
        renderer.setNumHorizontalLabels(12);
        renderer.setGridStyle(GridLabelRenderer.GridStyle.NONE);
        renderer.setLabelFormatter(formatter);

        LegendRenderer legend = gv.getLegendRenderer();
        legend.setVisible(true);
        legend.setAlign(LegendRenderer.LegendAlign.TOP);
        legend.setBackgroundColor(getResources().getColor(R.color.legendBackgroundColor));
    }

    /**
     * Creates a {@link com.jjoe64.graphview.series.LineGraphSeries} for the averages of the given
     * data set.
     *
     * @param values The data set to create the average {@link com.jjoe64.graphview.series.LineGraphSeries} for.
     *
     * @return The {@link com.jjoe64.graphview.series.LineGraphSeries} with the averages.
     */
    private LineGraphSeries<DataPoint> getAverages(DataPoint[] values) {
        DataPoint[] avg = new DataPoint[12];

        // Sum up the values in the data.
        double average = 0.0f;
        for (DataPoint value : values) {
            average += value.getY();
        }

        // And create the average.
        average /= values.length;

        // Fill the averages array with the average value.
        for ( int i = 0; i < 12; i++) {
            avg[i] = new DataPoint(i, average);
        }

        // And create a line series of this data.
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(avg);
        Resources res = getResources();
        series.setTitle(res.getString(R.string.graph_average));
        series.setColor(res.getColor(R.color.accentColor));
        return series;
    }

    /**
     * Creates a {@link com.jjoe64.graphview.series.BarGraphSeries} for the given
     * data set.
     *
     * @param data The data set to create the {@link com.jjoe64.graphview.series.BarGraphSeries} for.
     *
     * @return The {@link com.jjoe64.graphview.series.BarGraphSeries} with the data.
     */
    private BarGraphSeries<DataPoint> getBars(DataPoint[] data) {
        Resources res = getResources();
        BarGraphSeries<DataPoint> bars = new BarGraphSeries<>(data);
        bars.setSpacing((int) dpToPixels(getActivity(), 8));
        bars.setColor(res.getColor(R.color.primaryColor));
        bars.setTitle(res.getString(R.string.graph_cost));
        return bars;
    }

    /**
     * Setup the series in the {@link com.jjoe64.graphview.GraphView} views.
     */
    private void setupBarsAndAverages() {
        if(mCurrentYear == 0) {
            mCurrentYear = Utils.getYearFromDate(new Date());
        }

        if( mDbContext != null && mCar != null) {
            DataPoint[] data = mDbContext.getFuelCostPerMonth(mCar.getId(), mCurrentYear);

            mCostPerMonth.removeAllSeries();
            mCostPerMonth.addSeries(getBars(data));
            mCostPerMonth.addSeries(getAverages(data));

            data = mDbContext.getFuelCostPerKilometerPerMonth(mCar.getId(), mCurrentYear);

            mCostPerDistanceUnit.removeAllSeries();
            mCostPerDistanceUnit.addSeries(getBars(data));
            mCostPerDistanceUnit.addSeries(getAverages(data)
            );
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
        mCurrentYear = year;
        setupBarsAndAverages();
    }
}
