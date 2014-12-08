package com.development.jaba.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.development.jaba.model.Car;
import com.development.jaba.moneypit.R;

import java.util.List;

/**
 * ArrayAdapter for displaying the cars in the database in a ListView
 */
public class CarRowAdapter extends ArrayAdapter<Car> {

    //region Construction
    /**
     * Constructor. Initializes an instance of the object.
     * @param context The context.
     * @param values The values to display.
     */
    public CarRowAdapter(Context context, List<Car> values) {
        super(context, R.layout.car_row_template, values);
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
            rowView = rowInflater.inflate(R.layout.car_row_template, parent, false);
        }
        else {
            rowView = convertView;
        }

        if(rowView != null) {
            Car item = getItem(position);

            // Replace the default picture if one is provided from the database.
            if (item.getPicture() != null) {
                ImageView image = (ImageView) rowView.findViewById(R.id.carPicture);
                image.setImageBitmap(item.getImage());
            }

            TextView make = (TextView) rowView.findViewById(R.id.carMakeModel);
            make.setText(item.toString());
            TextView build = (TextView) rowView.findViewById(R.id.carBuildYear);
            build.setText(String.valueOf(item.getBuildYear()));
        }
        return rowView;
    }
    //endregion
}
