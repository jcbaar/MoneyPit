package com.development.jaba.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import com.development.jaba.model.Car;
import com.development.jaba.moneypit.R;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * A {@link BaseFragment} subclass which servers as super class for the
 * graph fragments.
 */
public class GraphFragment extends BaseFragment {

    protected Car mCar;
    protected int mCurrentYear;
    protected String[] mMonths;

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
    public void setupRenderers(GraphView gv, DefaultLabelFormatter formatter) {
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
    public void setupBarsSeries(GraphView gv, DataPoint[] data) {
        gv.removeAllSeries();
        gv.addSeries(getBars(data));
        gv.addSeries(getAverages(data));
    }
}
