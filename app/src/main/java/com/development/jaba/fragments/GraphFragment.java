package com.development.jaba.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.GetChars;

import com.development.jaba.model.Car;
import com.development.jaba.moneypit.Keys;
import com.development.jaba.moneypit.R;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;

/**
 * A {@link BaseDetailsFragment} subclass which servers as super class for the
 * graph fragments.
 */
public class GraphFragment extends BaseDetailsFragment {

    protected String[] mMonths;

    /**
     * Converts SP a pixel value.
     *
     * @param context The {@link Context}.
     * @param dp      The SP value to convert to pixels.
     * @return The pixel value.
     */
    public static float spToPixels(Context context, float dp) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return dp * scaledDensity;
    }

    /**
     * Converts DP a pixel value.
     *
     * @param context The {@link Context}.
     * @param dp      The DP value to convert to pixels.
     * @return The pixel value.
     */
    public static float dpToPixels(Context context, float dp) {
        float scaledDensity = context.getResources().getDisplayMetrics().density;
        return dp * scaledDensity;
    }

    /**
     * Called when a new instance is created.
     *
     * @param savedInstanceState Saved values to restore.
     */
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

    /**
     * Setup the renderers of the GraphView.
     *
     * @param gv        The {@link com.jjoe64.graphview.GraphView} to setup.
     * @param formatter The {@link com.jjoe64.graphview.DefaultLabelFormatter} to use for label formatting.
     */
    public void setupRenderers(GraphView gv, DefaultLabelFormatter formatter) {

        gv.setTitleTextSize(spToPixels(getActivity(), 10));

        GridLabelRenderer renderer = gv.getGridLabelRenderer();
        renderer.setNumHorizontalLabels(12);
        renderer.setGridStyle(GridLabelRenderer.GridStyle.NONE);
        renderer.setLabelFormatter(formatter);
        renderer.setTextSize(dpToPixels(getActivity(), 10));

        LegendRenderer legend = gv.getLegendRenderer();
        legend.setTextSize(spToPixels(getActivity(), 10));
        legend.setVisible(true);
        legend.setAlign(LegendRenderer.LegendAlign.TOP);
        legend.setBackgroundColor(getResources().getColor(R.color.legendBackgroundColor));
        gv.setLegendRenderer(legend);
    }

    /**
     * Creates a {@link com.jjoe64.graphview.series.LineGraphSeries} for the averages of the given
     * data set.
     *
     * @param values The data set to create the average {@link com.jjoe64.graphview.series.LineGraphSeries} for.
     * @return The {@link com.jjoe64.graphview.series.LineGraphSeries} with the averages.
     */
    private LineGraphSeries<DataPoint> getAverages(DataPoint[] values) {
        DataPoint[] avg = new DataPoint[12];

        // Sum up the values in the data.
        double average = 0.0f;
        int count = 0;
        for (DataPoint value : values) {
            // We only average on values that indeed have a value
            // assigned to it.
            if(value.getY() != 0) {
                average += value.getY();
                count++;
            }
        }

        // And create the average.
        average /= count;

        // Fill the averages array with the average value.
        for (int i = 0; i < 12; i++) {
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
     * @param data  The data set to create the {@link com.jjoe64.graphview.series.BarGraphSeries} for.
     * @param title The title of the data series.
     * @return The {@link com.jjoe64.graphview.series.BarGraphSeries} with the data.
     */
    private BarGraphSeries<DataPoint> getBars(DataPoint[] data, String title, OnDataPointTapListener listener) {
        Resources res = getResources();
        BarGraphSeries<DataPoint> bars = new BarGraphSeries<>(data);
        bars.setSpacing(30);
        bars.setColor(res.getColor(R.color.primaryColor));
        bars.setTitle(title);
        bars.setOnDataPointTapListener(listener);
        return bars;
    }

    /**
     * Setup the series in the {@link com.jjoe64.graphview.GraphView} views.
     *
     * @param gv    The {@link com.jjoe64.graphview.GraphView} to setup.
     * @param data  The data to show in the {@link com.jjoe64.graphview.GraphView}
     * @param title The title of the data series.
     */
    public void setupBarsSeries(GraphView gv, DataPoint[] data, String title, OnDataPointTapListener listener) {
        gv.removeAllSeries();
        gv.addSeries(getBars(data, title, listener));
        gv.addSeries(getAverages(data));
    }
}
