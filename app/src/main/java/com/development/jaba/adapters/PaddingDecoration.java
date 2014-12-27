package com.development.jaba.adapters;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * {@link android.support.v7.widget.RecyclerView.ItemDecoration} derived class for adding padding
 * between the items.
 */
public class PaddingDecoration extends RecyclerView.ItemDecoration {

    private Rect mPadding;

    /**
     * Constructor. Initializes an instance of the object.
     * @param padding The {@link Rect} containing the padding values.
     */
    public PaddingDecoration(Rect padding) {
        mPadding = padding;
    }

    /**
     * Called to get the offsets for the item decoration.
     * @param outRect The rectangle in which the offsets are to be stored.
     * @param view The {@link View} for which the offsets are to be returned.
     * @param parent The parent {@link android.support.v7.widget.RecyclerView}.
     * @param state The {@link android.support.v7.widget.RecyclerView.State} of the item.
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(mPadding);
    }
}
