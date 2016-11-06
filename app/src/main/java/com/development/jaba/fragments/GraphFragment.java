package com.development.jaba.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.StyleableRes;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.development.jaba.database.MoneyPitDbContext;
import com.development.jaba.model.Car;
import com.development.jaba.moneypit.Keys;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.DateHelper;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A {@link BaseDetailsFragment} subclass which servers as super class for the
 * graph fragments.
 */
public class GraphFragment extends BaseDetailsFragment {

    MoneyPitDbContext mDbContext; // Database context.

    private int mTextColor = 0,
            mBackgroundColor = 0,
            mBarColor = 0;

    /**
     * Gets the {@link MoneyPitDbContext}.
     * @return The {@link MoneyPitDbContext}.
     */
    public MoneyPitDbContext getDbContext() {
        if(mDbContext == null) {
            mDbContext = new MoneyPitDbContext(getActivity());
        }
        return mDbContext;
    }

    /**
     * Sets up the {@link CombinedChart}.
     * @param chart The {@link CombinedChart} to setup.
     * @param formatter The {@link IAxisValueFormatter} used to format the values of the chart.
     */
    public void setupChart(CombinedChart chart, IAxisValueFormatter formatter) {

        Context context = getContext();

        int[] attrs = {R.attr.chartAxisTextColor, R.attr.chartBackgroundColor, R.attr.colorPrimary};
        mTextColor = ContextCompat.getColor(context, android.R.color.primary_text_light);
        mBackgroundColor = ContextCompat.getColor(context, android.R.color.background_light);
        mBarColor = ContextCompat.getColor(context, R.color.primaryColor);

        @StyleableRes int textColor = 0;
        @StyleableRes int backColor = 1;
        @StyleableRes int barColor = 2;

        // Get out our attributes.
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs);

            try {
                mTextColor = a.getColor(textColor, mTextColor);
                mBackgroundColor = a.getColor(backColor, mBackgroundColor);
                mBarColor = a.getColor(barColor, mBarColor);
            } catch (Exception e) {
                Log.e("setupChart", "Unable to load attributes");
            } finally {
                a.recycle();
            }
        }

        chart.setDescription(null);
        chart.setDrawValueAboveBar(true);
        chart.setPinchZoom(false);
        chart.setClickable(false);
        chart.setScaleXEnabled(false);
        chart.setScaleYEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setBackgroundColor(mBackgroundColor);
        chart.setGridBackgroundColor(mBackgroundColor);
        chart.getLegend().setTextColor(mTextColor);

        XAxis xa = chart.getXAxis();
        xa.setPosition(XAxis.XAxisPosition.BOTTOM);
        xa.setDrawGridLines(false);
        xa.setLabelCount(12);
        xa.setTextColor(mTextColor);
        xa.setGranularity(1);
        xa.setSpaceMin(1);
        xa.setValueFormatter(new MonthFormatter());

        YAxis ya = chart.getAxisLeft();
        ya.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        ya.setDrawGridLines(false);
        ya.setValueFormatter(formatter);
        ya.setTextColor(mTextColor);
        ya.setSpaceMin(0);
    }

    /**
     * Setup the data in the {@link CombinedChart} views.
     * @param chart The {@link CombinedChart} to setup the data for.
     * @param values The values to display in the chart.
     * @param legendResId The legend text resource id.
     */
    public void setupChartData(CombinedChart chart, ArrayList<BarEntry> values, int legendResId) {
        if (mCurrentYear == 0) {
            mCurrentYear = DateHelper.getYearFromDate(new Date());
        }

        if(!isAdded()) {
            return;
        }

        MoneyPitDbContext db = getDbContext();
        if (db != null && mCar != null) {
            final Resources res = getResources();

            // First the values as a BarChart.
            BarData bd = new BarData();
            BarDataSet bds = new BarDataSet(values, res.getString(legendResId));
            bds.setColor(mBarColor);
            bd.addDataSet(bds);

            // Setup both the BarChart data and the LineChart data.
            CombinedData cd = new CombinedData();
            cd.setData(bd);
            cd.setData(getAverageSet(values));

            // Set and animate in view.
            chart.setData(cd);
            cd.setValueTextColor(mTextColor);
            chart.animateY(200);
        }
    }

    /**
     * Gets a {@link LineData} object containing the averages of the input data.
     * @param data The {@link List<BarEntry>} containing the data to average.
     * @return The {@link LineData} with the averages.
     */
    LineData getAverageSet(ArrayList<BarEntry> data) {
        LineData ld = new LineData();
        ArrayList<Entry> avgData = new ArrayList<>();

        float total = 0, num = 0;
        for(BarEntry bd : data) {
            // We only average when there actually is data.
            if(bd.getY() != 0) {
                total += bd.getY();
                num++;
            }
        }

        // Build the average set.
        float avg = total / num;
        for (BarEntry db : data) {
            Entry e = new Entry(db.getX(), avg);
            avgData.add(e);
        }

        LineDataSet lds = new LineDataSet(avgData, getResources().getString(R.string.graph_average));
        lds.setColor(ContextCompat.getColor(getActivity(), R.color.accentColor));
        lds.setDrawValues(false);
        lds.setDrawCircles(false);
        lds.setLineWidth(2);
        ld.addDataSet(lds);
        return ld;
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
     * Class implementing the {@link IAxisValueFormatter} interface for showing the month axis.
     */
    public class MonthFormatter implements IAxisValueFormatter {

        List<String> mMonths; // Month names to show on the X-axis.

        public MonthFormatter() {
            mMonths = Arrays.asList(getResources().getStringArray(R.array.months));
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int month = (int)value;
            if(month >= 0 && month < 12) {
                return mMonths.get(month);
            }
            return "";
        }

        @Override
        public int getDecimalDigits() {
            return 0;
        }
    }
}
