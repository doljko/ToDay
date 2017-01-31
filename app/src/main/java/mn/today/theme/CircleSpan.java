package mn.today.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;

import mn.today.R;

/**
 * Created by Tortuvshin Byambaa on 1/31/2017.
 */

public class CircleSpan extends ReplacementSpan {

    private final float mPadding;
    private final int mCircleColor;
    private final int mTextColor;

    public CircleSpan(Context context) {
        super();
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{
                R.attr.colorAccent,
                android.R.attr.textColorPrimaryInverse
        });
        mCircleColor = ta.getColor(0, ContextCompat.getColor(context, R.color.greenA700));
        //noinspection ResourceType
        mTextColor = ta.getColor(1, 0);
        ta.recycle();
        mPadding = context.getResources().getDimension(R.dimen.padding_circle);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end) + mPadding * 2); // left + right
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        float textSize = paint.measureText(text, start, end);
        paint.setColor(mCircleColor);
        // ensure radius covers at least 2 characters even if there is only 1
        canvas.drawCircle(x + textSize / 2 + mPadding, // center X
                (top + bottom) / 2, // center Y
                (text.length() == 1 ? textSize : textSize / 2) + mPadding, // radius
                paint);
        paint.setColor(mTextColor);
        canvas.drawText(text, start, end, mPadding + x, y, paint);
    }
}

