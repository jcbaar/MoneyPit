package com.development.jaba.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.development.jaba.model.Car;
import com.development.jaba.model.Fillup;
import com.development.jaba.moneypit.R;
import com.development.jaba.utilities.FormattingHelper;
import com.development.jaba.utilities.ImageDownloadHelperTask;
import com.development.jaba.utilities.SettingsHelper;
import com.development.jaba.utilities.UtilsHelper;
import com.development.jaba.view.LinearLayoutEx;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * ArrayAdapter for displaying the fill-ups in the database in a ListView
 */
public class FillupRowAdapter extends BaseRecyclerViewAdapter<FillupRowAdapter.FillupRowViewHolder> {

    public static final int MENU_NAV = 100;

    private final LayoutInflater mInflater;
    private final Car mCar; // Car instance the fill-ups are bound to.
    private List<Fillup> mData = Collections.emptyList();
    private final Context mContext;
    private final Vector<String> mExpandedItems;
    private final SettingsHelper mSettings;

    //region Construction

    /**
     * Constructor. Initializes an instance of the object.
     *
     * @param context The context.
     * @param car     The car the data is linked to.
     * @param values  The data set which is managed by this adapter.
     */
    public FillupRowAdapter(Context context, Car car, List<Fillup> values, SettingsHelper settings) {
        mExpandedItems = new Vector<>();
        mInflater = LayoutInflater.from(context);
        mCar = car;
        mData = values;
        mContext = context;
        mSettings = settings;
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
     * Starts a {@link com.development.jaba.utilities.ImageDownloadHelperTask} derived task to either download
     * and cache the static map or load the static map from a previously cached copy. The task is also responsible
     * for setting the static map image in the {@link android.widget.ImageView}.
     *
     * @param map The {@link android.widget.ImageView} that is to be used to show the static map image.
     * @param lat The latitude of the center position of the static map.
     * @param lon The longitude of the center position of the static map.
     */
    private void showMap(ImageView map, double lat, double lon) {
        map.setVisibility(View.VISIBLE);

        String slat = String.valueOf(lat),
                slon = String.valueOf(lon);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("maps.googleapis.com")
                .appendPath("maps")
                .appendPath("api")
                .appendPath("staticmap")
                .appendQueryParameter("center", slat + "," + slon)
                .appendQueryParameter("zoom", "16")
                .appendQueryParameter("size", "400x225")
                .appendQueryParameter("markers", "color:" + String.format("0x%06X", 0xFFFFFF & ContextCompat.getColor(mContext, R.color.accentColor)) + "|label:*|" + slat + "," + slon);

        String cacheFilename = slat + "_" + slon;

        new GetStaticMap(map, "/MoneyPit/mapcache/").execute(builder.toString(), cacheFilename);
    }

    /**
     * Setup the data to display for the given {@link com.development.jaba.adapters.CarRowAdapter.CarRowViewHolder}.
     *
     * @param holder   The {@link com.development.jaba.adapters.CarRowAdapter.CarRowViewHolder}.
     * @param position The position to setup the data for.
     */
    @Override
    public void onBindViewHolder(FillupRowViewHolder holder, final int position) {
        final Fillup item = mData.get(position);

        holder.getDate().setText(FormattingHelper.toShortDate(item.getDate()));
        holder.getOdometer().setText(FormattingHelper.toDistance(mCar, item.getOdometer()));
        holder.getDistance().setText(FormattingHelper.toDistance(mCar, item.getDistance()));
        holder.getDays().setText(FormattingHelper.toSpanInDays(item.getDaysSinceLastFillup()));
        holder.getTotalCost().setText(FormattingHelper.toPrice(mCar, item.getTotalPrice()));
        holder.getVolume().setText(FormattingHelper.toVolumeUnit(mCar, item.getVolume()));
        holder.getCost().setText(FormattingHelper.toPricePerVolumeUnit(mCar, item.getPrice()));
        holder.getEconomy().setText(FormattingHelper.toEconomy(mCar, item.getFuelConsumption()));
        holder.getLocation().setVisibility(item.getLongitude() == 0 && item.getLatitude() == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.getNote().setVisibility(TextUtils.isEmpty(item.getNote()) ? View.INVISIBLE : View.VISIBLE);
        holder.getNoteContent().setText(item.getNote());

        // When the fill-up is a partial fill-up we mark this by giving the fill-up
        // icon the accent color and hiding the economy view.
        Drawable d = ContextCompat.getDrawable(mContext, R.drawable.ic_local_gas_station_grey600_24dp);
        if (!item.getFullTank()) {
            PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
            d.mutate().setColorFilter(ContextCompat.getColor(mContext, R.color.accentColor), mode);
            holder.getEconomy().setVisibility(View.GONE);
        } else {
            holder.getEconomy().setVisibility(View.VISIBLE);
        }
        holder.getFull().setImageDrawable(d);

        // No note? No need to show the view then.
        boolean hasNote = !TextUtils.isEmpty(item.getNote());
        if (!hasNote) {
            holder.getNoteContent().setVisibility(View.GONE);
        } else {
            holder.getNoteContent().setVisibility(View.VISIBLE);
        }

        // When the item to show has a lat/lon position we make the ImageView for the
        // map visible. Also we append the navigate to item to the popup menu.
        //
        // Otherwise we hide the ImageView and navigate to item.
        PopupMenu menu = holder.getPopupMenu();
        if (menu != null) {
            menu.getMenu().removeGroup(MENU_NAV);
        }
        boolean hasMap = item.getLatitude() != 0 || item.getLongitude() != 0;
        if (hasMap) {
            if (menu != null) {
                menu.getMenu().add(MENU_NAV, MENU_NAV, MENU_NAV, R.string.navigate_to);
            }
            holder.getMap().setVisibility(View.VISIBLE);
        } else {
            holder.getMap().setVisibility(View.GONE);
        }

        if (hasNote || hasMap) {
            holder.getExpandable().setExpansionStateListener(new LinearLayoutEx.ExpansionStateListener() {
                @Override
                public void OnExpansionStateChanged(boolean isExpanded) {
                    onExpansionStateChanged(position, isExpanded);
                }
            });
        } else {
            holder.getExpandable().setExpansionStateListener(null);
        }

        // Forces the LinearLayoutEx to re-compute it's height necessary
        // to display all available information.
        holder.getExpandable().recomputeHeight();

        // See if this is an "expanded" position. If it is we
        // need to expand it without animation.
        boolean wasExpanded = false;
        for (String i : mExpandedItems) {
            if (i.equals(String.valueOf(position))) {
                holder.getExpandable().expandNoAnim();
                wasExpanded = true;

                // When we have a lat/lon we need to load the map from the internet or
                // the local cache. Note that we only do this when this view is to be
                // expanded.
                if (hasMap) {
                    showMap(holder.getMap(), item.getLatitude(), item.getLongitude());
                }
                break;
            }
        }

        // This was not an expanded position. Collapse it without
        // animation.
        if (!wasExpanded) {
            holder.getExpandable().collapseNoAnim();
        }
    }

    /**
     * Gets the number of items in this adapter.
     *
     * @return The number of items in the adapter.
     */
    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
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
                        mExpandedItems.add(String.valueOf(position));

                        // Load the map position only when we are expanding.
                        ImageView map = (ImageView) lle.findViewById(R.id.map);
                        if (map != null) {
                            showMap(map, item.getLatitude(), item.getLongitude());
                        }
                    } else {
                        mExpandedItems.remove(String.valueOf(position));
                    }
                    lle.requestLayout();
                    if(mSettings.getBooleanValue(SettingsHelper.PREF_TRANSITIONS, true)) {
                        lle.toggle();
                    }
                    else {
                        lle.toggleNoAnim();
                    }
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

        @Bind(R.id.fillupDate) TextView mDate;
        @Bind(R.id.fillupOdometer) TextView mOdometer;
        @Bind(R.id.fillupDistance) TextView mDistance;
        @Bind(R.id.fillupSpan) TextView mDays;
        @Bind(R.id.fillupTotalCost) TextView mTotalCost;
        @Bind(R.id.fillupVolume) TextView mVolume;
        @Bind(R.id.fillupCost) TextView mCost;
        @Bind(R.id.fillupEconomy) TextView mEconomy;
        @Bind(R.id.noteContent) TextView mNoteContents;
        @Bind(R.id.headerMenu) ImageButton mMenuButton;
        @Bind(R.id.location) ImageView mLocation;
        @Bind(R.id.note) ImageView mNote;
        @Bind(R.id.full) ImageView mFull;
        @Bind(R.id.map) ImageView mMap;
        @Bind(R.id.animateView) LinearLayoutEx mExpandable;

        /**
         * Constructor. Initializes an instance of the object and caches the
         * child {@link View} objects.
         *
         * @param context  The context.
         * @param itemView The {@link View} which this instance will manage.
         */
        public FillupRowViewHolder(Context context, View itemView) {
            super(context, itemView);
            ButterKnife.bind(this, itemView);

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
     * {@link com.development.jaba.utilities.ImageDownloadHelperTask} derived class to download static
     * map images from either the google API website or the local file cache.
     */
    private class GetStaticMap extends ImageDownloadHelperTask {

        private final ImageView mImageView;

        /**
         * Constructor. Initializes an instance of the object.
         *
         * @param imageView The {@link android.widget.ImageView} in which the downloaded image is displayed.
         * @param cacheName The filename for the cached image. This is used to check if it already exists
         *                  in the local file cache or to save the downloaded image to the file cache. You need
         *                  to make sure this is unique.
         */
        public GetStaticMap(ImageView imageView, String cacheName) {
            super(cacheName);
            mImageView = imageView;
        }

        /**
         * The image has been downloaded. Here we set it to the {@link android.widget.ImageView}.
         *
         * @param result The (down)loaded {@link android.graphics.Bitmap}.
         */
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            mImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_loadfail));
            if (result != null) {
                UtilsHelper.blendInImage(mContext, mImageView, result);
            } else {
                // Default to not loaded image.
                Toast.makeText(mContext, mContext.getResources().getString(R.string.error_map_image), Toast.LENGTH_SHORT).show();
            }
        }
    }
}