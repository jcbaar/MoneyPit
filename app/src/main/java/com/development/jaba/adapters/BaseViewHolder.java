package com.development.jaba.adapters;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * Base class implementation for the {@link android.support.v7.widget.RecyclerView.ViewHolder}. It extends the
 * {@link android.support.v7.widget.RecyclerView.ViewHolder} class with the possibility to capture and act on
 * item clicks and support for popup menus.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private final Context mContext;
    private PopupMenu mPopupMenu;
    private View mMenuView;
    private OnRecyclerItemClicked mClickListener;

    /**
     * Constructor. Initializes an instance of the object.
     * @param itemView The {@link View} this instance will manage.
     */
    public BaseViewHolder(Context context, View itemView) {
        super(itemView);

        mContext = context;

        // Setup ourselves as the listener for click events on the view we manage. We use this listener
        // to forward the click events to the adapter.
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    /**
     * Setup the given view with a popup menu.
     * @param view The {@link View} which is to be setup to open the given popup menu.
     * @param menuId The resource ID of the popup menu.
     */
    public void setMenuView(View view, int menuId) {
        setMenuView(view, menuId, null);
    }

    /**
     * Setup the given view with a popup menu.
     * @param view The {@link View} which is to be setup to open the given popup menu.
     * @param items A {@link String[]} containing the items to show in the popup menu.
     */
    public void setMenuView(View view, String[] items) {
        setMenuView(view, 0, items);
    }

    /**
     * Setup the callback we need to call when a click on the {@link View} we manage is detected.
     * @param listener The {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback.
     */
    public void setOnItemClickListener(OnRecyclerItemClicked listener) {
        mClickListener = listener;
    }

    /**
     * The callback that is called when a click on the {@link View} we manage is detected. If we have
     * a valid {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback registered we forward
     * the click event to that.
     * @param v The {@link View} that was clicked.
     */
    @Override
    public void onClick(View v) {
        if(mClickListener != null) {
            mClickListener.onRecyclerItemClicked(v, getPosition(), false);
        }
    }

    /**
     * The callback that is called when a long-click on the {@link View} we manage is detected. If we have
     * a valid {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback registered we forward
     * the click event to that.
     * @param v The {@link View} that was long-clicked.
     */
    @Override
    public boolean onLongClick(View v) {
        return mClickListener != null && mClickListener.onRecyclerItemClicked(v, getPosition(), true);
    }

    /**
     * Helper function to link a popup menu to the given {@link View}. The {@link View}
     * will get a {@link android.view.View.OnClickListener} attached to it. When this listener
     * is called the popup menu is shown. Clicks on the menu items are sent through the {@link com.development.jaba.adapters.OnRecyclerItemClicked}
     * callback.
     * @param view The {@link View} the {@link PopupMenu} is attached to.
     * @param menuId The resource ID of the popup menu. When this is <= 0 The menuItems parameter is expected to hold
     *               menu items.
     * @param menuItems A {@link String[]} array containing the menu items to show in the {@link PopupMenu}.
     */
    private void setMenuView(View view, int menuId, String[] menuItems) {
        if(mPopupMenu != null) {
            mPopupMenu.dismiss();
            mPopupMenu = null;
        }

        if (view == null) {
            mMenuView = null;
            return;
        }

        mMenuView = view;
        buildPopupMenu(menuId, menuItems);

        mMenuView.setSelected(false);
        mMenuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupMenu != null) {
                    mPopupMenu.show();
                    mMenuView.setSelected(true);
                }
            }
        });
    }

    /**
     * Helper method that builds the {@link PopupMenu} and links it to the {@link View}.
     * Click and dismiss listeners are linked to the created {@link PopupMenu} to capture
     * and forward events through the {@link com.development.jaba.adapters.OnRecyclerItemClicked} callback.
     *
     * @param menuId The resource ID of the popup menu. When this is <= 0 The menuItems parameter is expected to hold
     *               menu items.
     * @param menuItems A {@link String[]} array containing the menu items to show in the {@link PopupMenu}.
     */
    private void buildPopupMenu(int menuId, String[] menuItems) {

        if(menuId <= 0 && (menuItems == null || menuItems.length == 0 )) {
            return;
        }

        if (mPopupMenu == null) {
            mPopupMenu = new PopupMenu(mContext, mMenuView);

            // Do we load the menus from the string array?
            if(menuId == 0) {
                for (int i = 0; i < menuItems.length; i++) {
                    mPopupMenu.getMenu().add(Menu.NONE, i, i, menuItems[i]);
                }
            }
            else {
                // Load the menu from the resources.
                MenuInflater inflater = mPopupMenu.getMenuInflater();
                inflater.inflate(menuId, mPopupMenu.getMenu());
            }
        }

        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mMenuView.setSelected(false);
                if(mClickListener != null) {
                    mClickListener.onRecyclerItemMenuSelected(getPosition(), item);
                }
                return false;
            }
        });

        mPopupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                if (mMenuView != null)
                    mMenuView.setSelected(false);
            }
        });
    }
}