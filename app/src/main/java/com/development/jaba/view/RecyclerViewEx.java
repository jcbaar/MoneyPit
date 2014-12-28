package com.development.jaba.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

import com.development.jaba.adapters.PaddingDecoration;
import com.development.jaba.moneypit.R;

/**
 * {@link android.support.v7.widget.RecyclerView} based {@link android.view.View} that supports
 * setting item padding through the XML attributes in the layout file.
 */
public class RecyclerViewEx extends RecyclerView {

    /**
     * Constructor. Initializes an instance of the object.
     * @param context The context.
     */
    public RecyclerViewEx(Context context) {
        this(context, null, 0);
    }

    /**
     * Constructor. Initializes an instance of the object.
     * @param context The context.
     * @param attrs The {@link android.util.AttributeSet} with the attributes.
     */
    public RecyclerViewEx(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor. Initializes an instance of the object.
     * @param context The context.
     * @param attrs The {@link android.util.AttributeSet} with the attributes.
     * @param defStyle The default style.
     */
    public RecyclerViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Get out our attributes.
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.RecyclerViewEx
                    , 0, 0);

            int left = 0, top = 0, right = 0, bottom = 0;
            try {
                left = (int)a.getDimension(R.styleable.RecyclerViewEx_rveItemPaddingLeft, 0);
                top = (int)a.getDimension(R.styleable.RecyclerViewEx_rveItemPaddingTop, 0);
                right = (int)a.getDimension(R.styleable.RecyclerViewEx_rveItemPaddingRight, 0);
                bottom = (int)a.getDimension(R.styleable.RecyclerViewEx_rveItemPaddingBottom, 0);
            } catch (Exception e) {
                Log.e("RecyclerViewEx", "Unable to load attributes");
            } finally {
                a.recycle();
            }

            // When we have a padding set then we add a padding decoration.
            if (left != 0 || top != 0 || right != 0 || bottom != 0) {
                addItemDecoration(new PaddingDecoration(new Rect(left, top, right, bottom)));
            }
        }
    }
}
