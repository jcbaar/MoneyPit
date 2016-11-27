package com.development.jaba.utilities;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.development.jaba.model.Car;
import com.development.jaba.moneypit.AddOrEditCarActivity;
import com.development.jaba.moneypit.MoneyPitApp;
import com.development.jaba.moneypit.R;
import com.development.jaba.view.RecyclingImageView;

/**
 * A {@link AsyncTask} derived class to asynchronously convert the {@link Car}
 * bytes into a {@link Bitmap}
 */
public class GetCarImageHelper extends AsyncTask<Void, Void, Bitmap> {

    private final ImageView mImageView;
    private final Car mCar;
    private final Context mContext;
    private final int mTint;
    private boolean mMakeInvisible;
    private boolean mBlend;

    /**
     * Constructor. Creates an instance of the object.
     *
     * @param context       The {@link Context}.
     * @param imageView     The {@link ImageView} in which to load the image.
     * @param car           The {@link Car} from which to get the image.
     * @param makeInvisible True to make the {@link RecyclingImageView} invisible when no image is available.
     */
    public GetCarImageHelper(Context context,
                             ImageView imageView,
                             Car car,
                             int tint,
                             boolean makeInvisible ) {
        mCar = car;
        mImageView = imageView;
        mContext = context;
        mMakeInvisible = makeInvisible;
        mTint = tint;
        mBlend = true;
    }

    /**
     * Constructor. Creates an instance of the object.
     *
     * @param context       The {@link Context}.
     * @param imageView     The {@link ImageView} in which to load the image.
     * @param car           The {@link Car} from which to get the image.
     * @param makeInvisible True to make the {@link RecyclingImageView} invisible when no image is available.
     */
    public GetCarImageHelper(Context context,
                             ImageView imageView,
                             Car car,
                             int tint,
                             boolean makeInvisible,
                             boolean blend ) {
        mCar = car;
        mImageView = imageView;
        mContext = context;
        mMakeInvisible = makeInvisible;
        mTint = tint;
        mBlend = blend;
    }

    /**
     * Simply call's into getImage() of the {@link Car} instance.
     *
     * @param params No-op.
     * @return The converted bitmap or null.
     */
    @Override
    protected Bitmap doInBackground(Void... params) {
        return mCar.getImage();
    }

    /**
     * The image has been converted. Here we set it to the {@link RecyclingImageView}.
     *
     * @param result The created {@link android.graphics.Bitmap}.
     */
    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);

        if (result != null) {
            if (mMakeInvisible) {
                mImageView.setVisibility(View.VISIBLE);
            }
            if(mBlend) {
                UtilsHelper.blendInImage(mContext, mImageView, result);
            }
            else {
                mImageView.setImageBitmap(result);
            }
        } else {
            if (mMakeInvisible) {
                mImageView.setVisibility(View.GONE);
            }
            else {
                Drawable drawable = UtilsHelper.getTintedDrawable(MoneyPitApp.getContext(), R.drawable.background_header, mTint);
                UtilsHelper.setBackgroundDrawable(mImageView, drawable);
            }
        }
    }
}