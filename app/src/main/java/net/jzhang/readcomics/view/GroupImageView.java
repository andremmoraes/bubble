package net.jzhang.readcomics.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class GroupImageView extends ImageView {

    public GroupImageView(Context context) {
        super(context);
        setScaleType(ScaleType.CENTER_CROP);
    }

    public GroupImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setScaleType(ScaleType.CENTER_CROP);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, width * 11 / 7);
    }
}
