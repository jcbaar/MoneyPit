package com.development.jaba.adapters;

import com.development.jaba.model.Car;
import com.development.jaba.model.Fillup;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.FormattingHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * ArrayAdapter for displaying the fill-ups in the database in a ListView
 */
public class FillupRowAdapter extends ArrayAdapter<Fillup> {

    private Car mCar; // Car instance the fill-ups are bound to.

    //region Helpers
    /**
     * Set the {@link Car} instance the fill-ups in this {@link ArrayAdapter} are
     * bound to.
     * @param car The {@link Car} instance.
     */
    public void setCar(Car car) {
        mCar = car;
    }
    //endregion

    //region Construction
    /**
     * Constructor. Initializes an instance of the object.
     * @param context The context.
     * @param values The values to display.
     */
    public FillupRowAdapter(Context context, List<Fillup> values) {
        super(context, R.layout.fillup_row_template, values);
    }
    //endregion

    //region Overrides
    /**
     * Override for the getView() method. This will setup a view with the contents
     * of the row.
     * @param position The position to setup the view for.
     * @param convertView The view to convert.
     * @param parent The parent ViewGroup.
     * @return The inflated view for the row.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater rowInflater = LayoutInflater.from(getContext());
        View rowView;

        // We only inflate a new view when we do not have a
        // valid convertView.
        if(convertView == null) {
            rowView = rowInflater.inflate(R.layout.fillup_row_template, parent, false);
        }
        else {
            rowView = convertView;
        }

        if(rowView != null) {
            Fillup item = getItem(position);

            TextView date = (TextView) rowView.findViewById(R.id.fillupDate);
            date.setText(FormattingHelper.toShortDate(item.getDate()));

            TextView odo = (TextView) rowView.findViewById(R.id.fillupOdometer);
            odo.setText(FormattingHelper.toDistance(mCar, item.getOdometer()));

            TextView dist = (TextView) rowView.findViewById(R.id.fillupDistance);
            dist.setText(FormattingHelper.toDistance(mCar, item.getDistance()));

            TextView days = (TextView) rowView.findViewById(R.id.fillupSpan);
            days.setText(FormattingHelper.toSpanInDays(item.getDaysSinceLastFillup()));

            TextView total = (TextView) rowView.findViewById(R.id.fillupTotalCost);
            total.setText(FormattingHelper.toPrice(mCar, item.getTotalPrice()));

            TextView volume = (TextView) rowView.findViewById(R.id.fillupVolume);
            volume.setText(FormattingHelper.toVolumeUnit(mCar, item.getVolume()));

            TextView cost = (TextView) rowView.findViewById(R.id.fillupCost);
            cost.setText(FormattingHelper.toPricePerVolumeUnit(mCar, item.getPrice()));

            TextView economy = (TextView) rowView.findViewById(R.id.fillupEconomy);
            economy.setText(FormattingHelper.toEconomy(mCar, item.getFuelConsumption()));
        }
        return rowView;
    }
    //endregion
}
