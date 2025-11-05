package me.miran.mhdstuff;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class IconLinerLayout extends LinearLayout {
    public IconLinerLayout(Context context) {
        super(context);
    }

    public IconLinerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IconLinerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IconLinerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (width < height) {
            int newWidthSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            super.onMeasure(newWidthSpec, heightMeasureSpec);
        }
    }

}
