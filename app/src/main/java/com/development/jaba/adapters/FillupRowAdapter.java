package com.development.jaba.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.development.jaba.model.Car;
import com.development.jaba.model.Fillup;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.FormattingHelper;
import com.development.jaba.view.LinearLayoutEx;
import com.google.android.gms.maps.MapsInitializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * ArrayAdapter for displaying the fill-ups in the database in a ListView
 */
public class FillupRowAdapter extends BaseRecyclerViewAdapter<FillupRowAdapter.FillupRowViewHolder> {

    private final LayoutInflater mInflater;
    private final Car mCar; // Car instance the fill-ups are bound to.
    private List<Fillup> mData = Collections.emptyList();
    private final Context mContext;
    private final Vector<Integer> mExpandedItems;

    //region Construction

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context The context.
     * @param car     The car the data is linked to.
     * @param values  The data set which is managed by this adapter.
     */
    public FillupRowAdapter(Context context, Car car, List<Fillup> values) {
        mExpandedItems = new Vector<>();
        mInflater = LayoutInflater.from(context);
        mCar = car;
        mData = values;
        mContext = context;
        MapsInitializer.initialize(mContext);
    }
    //endregion

    /**
     * Setup a complete new data set for this adapter.
     *
     * @param data The new dataset.
     */
    public void setData(List<Fillup> data) {
        mData = data;
        mExpandedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Creates a new {@link com.development.jaba.adapters.FillupRowAdapter.FillupRowViewHolder} object that manages
     * the {@link View} of the row.
     *
     * @param parent   The parent {@link android.view.ViewGroup}.
     * @param viewType The type of the view.
     * @return The created {@link com.development.jaba.adapters.FillupRowAdapter.FillupRowViewHolder}.
     */
    @Override
    public FillupRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = mInflater.inflate(R.layout.fillup_row_template, parent, false);
        FillupRowViewHolder viewHolder = new FillupRowViewHolder(mContext, rowView);

        // Make sure that we are listening to item clicks.
        viewHolder.setOnItemClickListener(this);
        return viewHolder;
    }

    /**
     * Starts a {@link com.development.jaba.adapters.FillupRowAdapter.DownloadStaticMapTask} task to either download
     * and cache the static map or load the static map from a previously cached copy. The task is also responsible
     * for setting the static map image in the {@link android.widget.ImageView}.
     *
     * @param map The {@link android.widget.ImageView} that is to be used to show the static map image.
     * @param lat The latitude of the center position of the static map.
     * @param lon The longitude of the center position of the static map.
     */
    private void showMap(ImageView map, double lat, double lon) {
        map.setVisibility(View.VISIBLE);
        String url = String.format("http://maps.google.com/maps/api/staticmap?center=%f,%f&zoom=16&size=400x200&markers=color:blue%%7Clabel:H%%7C%f,%f",
                lat, lon,
                lat, lon);
        new DownloadStaticMapTask(map).execute(url, String.valueOf(lat), String.valueOf(lon));
    }

    /**
     * Setup the data to display for the given {@link com.development.jaba.adapters.CarRowAdapter.CarRowViewHolder}.
     *
     * @param holder   The {@link com.development.jaba.adapters.CarRowAdapter.CarRowViewHolder}.
     * @param position The position to setup the data for.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final FillupRowViewHolder vh = (FillupRowViewHolder) holder;
        final Fillup item = mData.get(position);

        vh.getDate().setText(FormattingHelper.toShortDate(item.getDate()));
        vh.getOdometer().setText(FormattingHelper.toDistance(mCar, item.getOdometer()));
        vh.getDistance().setText(FormattingHelper.toDistance(mCar, item.getDistance()));
        vh.getDays().setText(FormattingHelper.toSpanInDays(item.getDaysSinceLastFillup()));
        vh.getTotalCost().setText(FormattingHelper.toPrice(mCar, item.getTotalPrice()));
        vh.getVolume().setText(FormattingHelper.toVolumeUnit(mCar, item.getVolume()));
        vh.getCost().setText(FormattingHelper.toPricePerVolumeUnit(mCar, item.getPrice()));
        vh.getEconomy().setText(FormattingHelper.toEconomy(mCar, item.getFuelConsumption()));
        vh.getLocation().setVisibility(item.getLongitude() == 0 && item.getLatitude() == 0 ? View.INVISIBLE : View.VISIBLE);
        vh.getNote().setVisibility(TextUtils.isEmpty(item.getNote()) ? View.INVISIBLE : View.VISIBLE);
        vh.getFull().setVisibility(!item.getFullTank() ? View.INVISIBLE : View.VISIBLE);
        vh.getNoteContent().setText(item.getNote());

        // When the item to show has a lat/lon position we make the ImageView for the
        // map visible. Otherwise we make it gone.
        boolean hasMap = false;
        if (item.getLatitude() != 0 || item.getLongitude() != 0) {
            vh.getMap().setVisibility(View.VISIBLE);
            hasMap = true;
        } else {
            vh.getMap().setVisibility(View.GONE);
        }

        // See if this is an "expanded" position. If it is we
        // need to expand it without animation.
        boolean wasExpanded = false;
        for (Integer i : mExpandedItems) {
            if (i == position) {
                vh.getExpandable().expandNoAnim();
                wasExpanded = true;

                // When we have a lat/lon we need to load the map from the internet or
                // the local cache. Note that we only do this when this view is to be
                // expanded.
                if (hasMap) {
                    showMap(vh.getMap(), item.getLatitude(), item.getLongitude());
                }
                break;
            }
        }

        // This was not an expanded position. Collapse it without
        // animation.
        if (!wasExpanded) {
            vh.getExpandable().collapseNoAnim();
        }
    }

    /**
     * Gets the number of items in this adapter.
     *
     * @return The number of items in the adapter.
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Gets the {@link Fillup} entity from the given position.
     *
     * @param position The position from which to get the {@link Fillup} entity.
     * @return The {@link Fillup} entity from the given position or null if a position was given that
     * is out of bounds.
     */
    public Fillup getItem(int position) {
        if (mData != null && position >= 0 && position < mData.size()) {
            return mData.get(position);
        }
        return null;
    }

    /**
     * Gets the {@link Fillup} entity from the last clicked position.
     *
     * @return The {@link Fillup} entity from the last clicked position or null if there is no last
     * clicked position or it was out of bounds.
     */
    public Fillup getLastClickedItem() {
        return getItem(getLastClickedPosition());
    }

    /**
     * Captures clicks on the item views so that we can determine whether or not we need to
     * toggle the expandable view expand/collapse state.
     *
     * @param view        The {@link View} that was clicked.
     * @param position    The position of the {@link View} that was clicked.
     * @param isLongClick Will be true is the click was a long click.
     * @return True when the click was a long click and the request was processed.
     */
    @Override
    public boolean onRecyclerItemClicked(View view, int position, boolean isLongClick) {
        // We do nothing with long-clicks.
        if (!isLongClick) {
            // Get the data item.
            Fillup item = getItem(position);
            if (item != null) {
                // We can only expand or collapse when we have
                // either a valid longitude/latitude pair and/or
                // a note.
                if (item.getLongitude() != 0 ||
                        item.getLatitude() != 0 ||
                        !TextUtils.isEmpty(item.getNote())) {
                    LinearLayoutEx lle = (LinearLayoutEx) view.findViewById(R.id.animateView);

                    // If the item is not expanded we add it to the expanded list since
                    // the toggle will expand it. Otherwise we simply remove the item from
                    // the expanded list.
                    if (!lle.isExpanded()) {
                        mExpandedItems.add((Integer) position);

                        // Load the map position only when we are expanding.
                        ImageView map = (ImageView) lle.findViewById(R.id.map);
                        if (map != null && map.getVisibility() == View.VISIBLE) {
                            showMap(map, item.getLatitude(), item.getLongitude());
                        }
                    } else {
                        mExpandedItems.remove((Integer) position);
                    }
                    lle.requestLayout();
                    lle.toggle();
                }
            }
        }
        return super.onRecyclerItemClicked(view, position, isLongClick);
    }

    /**
     * A {@link com.development.jaba.adapters.BaseViewHolder} derived class to manage the {@link View} of the
     * {@link Car} row items.
     */
    public class FillupRowViewHolder extends BaseViewHolder {

        private final TextView mDate, mOdometer, mDistance,
                mDays, mTotalCost, mVolume, mCost, mEconomy, mNoteContents;
        private final ImageButton mMenuButton;
        private final ImageView mLocation, mNote, mFull, mMap;
        private final LinearLayoutEx mExpandable;

        /**
         * Constructor. Initializes an instance of the object and caches the
         * child {@link View} objects.
         *
         * @param context  The context.
         * @param itemView The {@link View} which this instance will manage.
         */
        public FillupRowViewHolder(Context context, View itemView) {
            super(context, itemView);

            mDate = (TextView) itemView.findViewById(R.id.fillupDate);
            mOdometer = (TextView) itemView.findViewById(R.id.fillupOdometer);
            mDistance = (TextView) itemView.findViewById(R.id.fillupDistance);
            mDays = (TextView) itemView.findViewById(R.id.fillupSpan);
            mTotalCost = (TextView) itemView.findViewById(R.id.fillupTotalCost);
            mVolume = (TextView) itemView.findViewById(R.id.fillupVolume);
            mCost = (TextView) itemView.findViewById(R.id.fillupCost);
            mEconomy = (TextView) itemView.findViewById(R.id.fillupEconomy);
            mMenuButton = (ImageButton) itemView.findViewById(R.id.headerMenu);
            mLocation = (ImageView) itemView.findViewById(R.id.location);
            mNote = (ImageView) itemView.findViewById(R.id.note);
            mFull = (ImageView) itemView.findViewById(R.id.full);
            mExpandable = (LinearLayoutEx) itemView.findViewById(R.id.animateView);
            mNoteContents = (TextView) itemView.findViewById(R.id.noteContent);
            mMap = (ImageView) itemView.findViewById(R.id.map);

            // Attach a PopupMenu to the menu button.
            setMenuView(mMenuButton, mContext.getResources().getStringArray(R.array.edit_delete));
        }

        public TextView getDate() {
            return mDate;
        }

        public TextView getOdometer() {
            return mOdometer;
        }

        public TextView getDistance() {
            return mDistance;
        }

        public TextView getDays() {
            return mDays;
        }

        public TextView getTotalCost() {
            return mTotalCost;
        }

        public TextView getVolume() {
            return mVolume;
        }

        public TextView getCost() {
            return mCost;
        }

        public TextView getEconomy() {
            return mEconomy;
        }

        public ImageView getLocation() {
            return mLocation;
        }

        public ImageView getNote() {
            return mNote;
        }

        public ImageView getFull() {
            return mFull;
        }

        public LinearLayoutEx getExpandable() {
            return mExpandable;
        }

        public TextView getNoteContent() {
            return mNoteContents;
        }

        public ImageView getMap() {
            return mMap;
        }
    }

    /**
     * An {@link android.os.AsyncTask} derived class responsible for downloading
     * static map images and caching them if necessary.
     */
    private class DownloadStaticMapTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView bmImage;
        private String mLat, mLon;

        /**
         * Constructor. Initializes with the ImageView instance to set the
         * static map image on.
         *
         * @param bmImage The {@link android.widget.ImageView} the static map image is loaded into.
         */
        public DownloadStaticMapTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        /**
         * This method will check to see if we have a static map image of the given position
         * already in our cache. If we do we do not bother downloading it from the internet
         * but instead load it directly from our local cache instead.
         *
         * @param urls Parameters for the method. Index 0 equals the download URL, index 1 the
         *             latitude and index 3 the longitude.
         * @return The (down)loaded static map image.
         */
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            mLat = urls[1];
            mLon = urls[2];

            // Only download when we do not have a local copy
            // cached.
            if (cachedFileExists()) {
                return loadFileFromCache();
            }

            // We do not have it cached. Download it from the given
            // URL.
            Bitmap mapImage = null;
            InputStream in = null;
            try {
                in = new java.net.URL(urldisplay).openStream();
                mapImage = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e("Error", e.getMessage());
                    }
                }
            }

            // Save the downloaded image into our local file cache.
            if (mapImage != null) {
                try {
                    cacheFile(mapImage);
                } catch (IOException e) {
                    Log.e("Error", e.getMessage());
                }
            }
            return mapImage;
        }

        /**
         * Called after the task has completed.
         *
         * @param result The (down)loaded {@link android.graphics.Bitmap}.
         */
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }

        /**
         * Checks to see whether or not the cache folder exists on the
         * external storage. If not it creates it.
         */
        private void checkFolders() {
            File f = new File(Environment.getExternalStorageDirectory() + "/MoneyPit");
            if (!f.exists() || (f.exists() && !f.isDirectory())) {
                f.mkdir();
            }
            f = new File(Environment.getExternalStorageDirectory() + "/MoneyPit/mapcache");
            if (!f.exists() || (f.exists() && !f.isDirectory())) {
                f.mkdir();
            }
        }

        /**
         * Constructs the cache file name from the input data.
         *
         * @return The cache file name.
         */
        private String getCacheFilename() {
            return Environment.getExternalStorageDirectory() + "/MoneyPit/mapcache/" + mLat + "_" + mLon;
        }

        /**
         * Checks to see if a cached static map image for the given
         * data already exists.
         *
         * @return true if the cached static image exists, false if it does not.
         */
        private boolean cachedFileExists() {
            File file = new File(getCacheFilename());
            return file.exists();
        }

        /**
         * Decodes the cached static map image.
         *
         * @return The static map image as a {@link android.graphics.Bitmap}
         */
        private Bitmap loadFileFromCache() {
            return BitmapFactory.decodeFile(getCacheFilename());
        }

        /**
         * Saves the given {@link android.graphics.Bitmap} into the local static
         * map image cache.
         *
         * @param bitmap The static map {@link android.graphics.Bitmap} to store.
         * @throws IOException This is thrown in case of an IO error.
         */
        private void cacheFile(Bitmap bitmap) throws IOException {
            if (cachedFileExists()) {
                return;
            }

            // Make sure the cache folder exists.
            checkFolders();

            // Save the image to the cache.
            OutputStream output = null;
            try {
                String outFileName = getCacheFilename();
                output = new FileOutputStream(outFileName);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                output.flush();
            } finally {
                if (output != null) {
                    output.close();
                }
            }
        }
    }
}