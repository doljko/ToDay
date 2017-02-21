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
@Deprecated
public class UnderDotSpan extends ReplacementSpan {

    private final float mSize;
    private final int mDotColor;
    private final int mTextColor;

    public UnderDotSpan(Context context) {
        super();
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{
                R.attr.colorAccent,
                android.R.attr.textColorPrimary
        });
        mDotColor = ta.getColor(0, ContextCompat.getColor(context, R.color.greenA700));
        //noinspection ResourceType
        mTextColor = ta.getColor(1, 0);
        ta.recycle();
        mSize = context.getResources().getDimension(R.dimen.dot_size);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end));
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        float textSize = paint.measureText(text, start, end);
        paint.setColor(mDotColor);
        canvas.drawCircle(x + textSize / 2, // text center X
                bottom + mSize, // dot center Y
                mSize / 2, // radius
                paint);
        paint.setColor(mTextColor);
        canvas.drawText(text, start, end, x, y, paint);
    }
}
