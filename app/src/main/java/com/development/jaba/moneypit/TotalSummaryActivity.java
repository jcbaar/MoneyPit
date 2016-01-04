package com.development.jaba.moneypit;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;

import com.development.jaba.fragments.BaseDetailsFragment;
import com.development.jaba.model.Car;
import com.development.jaba.utilities.UtilsHelper;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Simple summary activity. Presents the user with a total summary of a specified
 * {@link Car}
 */
public class TotalSummaryActivity extends BaseActivity {

    @SuppressWarnings("unused")
    @Bind(R.id.image) ImageView image;

    private Car mCar;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(Keys.EK_CAR, mCar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
        {
            mCar = (Car)savedInstanceState.getSerializable(Keys.EK_CAR);
        }
        else {
            // Extract the Car instance if this Activity is called to edit
            // an existing Car entity. Otherwise we instantiate a new Car
            // entity.
            Bundle b = getIntent().getExtras();
            if (b != null) {
                mCar = (Car) b.getSerializable(Keys.EK_CAR);
            }
        }

        // Setup the fragment with the car to summarize.
        BaseDetailsFragment fragment = (BaseDetailsFragment)getSupportFragmentManager().findFragmentById(R.id.content);
        fragment.setCar(mCar);

        // Bind the views and setup the rest of the UI.
        ButterKnife.bind(this);

        Bitmap im = mCar.getImage();
        if(im != null) {
            image.setImageBitmap(mCar.getImage());
        }
        else {
            // Ideally I would collapse the ImageView and prevent the nested scrolling from
            // resizing the AppBarLayout without affecting other layout behaviours when there
            // is no vehicle image. Since I have yet to find a way to do that I will simply
            // load a tinted header background image instead...
            Drawable drawable = UtilsHelper.getTintedDrawable(this, R.drawable.background_header, getColorPrimary());
            UtilsHelper.setBackgroundDrawable(image, drawable);
        }
        setTitle(mCar.toString());
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_totalsummary;
    }
}
