package com.jiuj.absen.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class BahnscriftTextView extends AppCompatTextView {
    public BahnscriftTextView(Context paramContext)
    {
        super(paramContext);
        init();
    }

    public BahnscriftTextView(Context paramContext, AttributeSet paramAttributeSet)
    {
        super(paramContext, paramAttributeSet);
        init();
    }

    public BahnscriftTextView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);
        init();
    }

    private void init()
    {
        if (!isInEditMode()) {
            setTypeface(Typeface.createFromAsset(getContext().getAssets(), "BAHNSCHRIFT.TTF"));
        }
    }
}
