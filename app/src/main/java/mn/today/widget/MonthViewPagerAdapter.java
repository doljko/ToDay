package mn.today.widget;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import mn.today.CalendarUtils;
import mn.today.content.EventCursor;

/**
 * Created by Tortuvshin Byambaa on 1/31/2017.
 */

class MonthViewPagerAdapter extends PagerAdapter {
    private static final String STATE_FIRST_MONTH_MILLIS = "state:month";
    private static final String STATE_SELECTED_DAY_MILLIS = "state:selectedDay";
    static final int ITEM_COUNT = 5; // buffer, left, active, right, buffer

    @VisibleForTesting
    final List<MonthView> mViews = new ArrayList<>(getCount());
    @VisibleForTesting long mSelectedDayMillis = CalendarUtils.today();
    private final List<Long> mMonths = new ArrayList<>(getCount());
    private final MonthView.OnDateChangeListener mListener;
    private final List<EventCursor> mCursors = new ArrayList<>(getCount());
    private final ArrayMap<EventCursor, ContentObserver> mObservers =
            new ArrayMap<>(getCount());

    public MonthViewPagerAdapter(MonthView.OnDateChangeListener listener) {
        mListener = listener;
        int mid = ITEM_COUNT / 2;
        long todayMillis = CalendarUtils.monthFirstDay(CalendarUtils.today());
        for (int i = 0; i < getCount(); i++) {
            mMonths.add(CalendarUtils.addMonths(todayMillis, i - mid));
            mViews.add(null);
            mCursors.add(null);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        MonthView view = new MonthView(container.getContext());
        view.setLayoutParams(new ViewPager.LayoutParams());
        view.setOnDateChangeListener(mListener);
        mViews.set(position, view);
        container.addView(view); // views are not added in same order as adapter items
        bind(position);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((MonthView) object).setOnDateChangeListener(null);
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return ITEM_COUNT;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Parcelable saveState() {
        Bundle bundle = new Bundle();
        bundle.putLong(STATE_FIRST_MONTH_MILLIS, mMonths.get(0));
        bundle.putLong(STATE_SELECTED_DAY_MILLIS, mSelectedDayMillis);
        return bundle;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        Bundle savedState = (Bundle) state;
        if (savedState == null) {
            return;
        }
        mSelectedDayMillis = savedState.getLong(STATE_SELECTED_DAY_MILLIS);
        long firstMonthMillis = savedState.getLong(STATE_FIRST_MONTH_MILLIS);
        for (int i = 0; i < getCount(); i++) {
            mMonths.set(i, CalendarUtils.addMonths(firstMonthMillis, i));
        }
    }

    /**
     * Sets selected day for page at given position, which either sets new selected day
     * if it falls within that month, or unsets previously selected day if any
     * @param position      page position
     * @param dayMillis     selected day in milliseconds
     * @param notifySelf    true to rebind page at given position, false otherwise
     */
    void setSelectedDay(int position, long dayMillis, boolean notifySelf) {
        mSelectedDayMillis = dayMillis;
        if (notifySelf) {
            bindSelectedDay(position);
        }
        if (position > 0) {
            bindSelectedDay(position - 1);
        }
        if (position < getCount() - 1) {
            bindSelectedDay(position + 1);
        }
    }

    /**
     * Gets month of calendar in given position
     * @param position    adapter position
     * @return  month in milliseconds
     */
    long getMonth(int position) {
        return mMonths.get(position);
    }

    /**
     * Shifts Jan, Feb, Mar, Apr, [May] to Apr, [May], Jun, Jul, Aug
     * Rebinds views in view pool if needed
     */
    void shiftLeft() {
        for (int i = 0; i < getCount() - 2; i++) {
            mMonths.add(CalendarUtils.addMonths(mMonths.remove(0), getCount()));
        }
        // TODO only deactivate non reusable cursors
        for (int i = 0; i < getCount(); i++) {
            swapCursor(i, null, null);
        }
        // rebind current item (2nd) and 2 adjacent items
        for (int i = 0; i <= 2; i++) {
            bind(i);
        }
    }

    /**
     * Shifts [Jan], Feb, Mar, Apr, May to Oct, Nov, Dec, [Jan], Feb
     * Rebinds views in view pool if needed
     */
    void shiftRight() {
        for (int i = 0; i < getCount() - 2; i++) {
            mMonths.add(0, CalendarUtils.addMonths(mMonths.remove(getCount() - 1), -getCount()));
            mCursors.add(0, mCursors.remove(getCount() - 1));
        }
        // TODO only deactivate non reusable cursors
        for (int i = 0; i < getCount(); i++) {
            swapCursor(i, null, null);
        }
        // rebind current item (2nd to last) and 2 adjacent items
        for (int i = 0; i <= 2; i++) {
            bind(getCount() - 1 - i);
        }
    }

    /**
     * Rebinds month, events and selected day for calendar at given position
     * @param position    adapter position
     */
    void bind(int position) {
        if (mViews.get(position) != null) {
            mViews.get(position).setCalendar(mMonths.get(position));
        }
        bindCursor(position);
        bindSelectedDay(position);
    }

    /**
     * Gets cursor for calendar events at given position
     * @param position    adapter position
     * @return  {@link android.provider.CalendarContract.Events} cursor wrapper or null
     * @see {@link #swapCursor(long, EventCursor, ContentObserver)}
     */
    EventCursor getCursor(int position) {
        return mCursors.get(position);
    }

    /**
     * Swaps cursor for calendar events for given month
     * Closes previously bound cursor, unregisters observer if any
     * @param monthMillis       month in milliseconds
     * @param cursor            {@link android.provider.CalendarContract.Events} cursor wrapper or null
     * @param contentObserver   content observer for given cursor
     */
    void swapCursor(long monthMillis, @Nullable EventCursor cursor,
                    ContentObserver contentObserver) {
        for (int i = 0; i < mMonths.size(); i++) {
            if (CalendarUtils.sameMonth(monthMillis, mMonths.get(i))) {
                swapCursor(i, cursor, contentObserver);
                break;
            }
        }
    }

    /**
     * Deactivates all previously bound cursors and unregisters their observers
     */
    void deactivate() {
        for (EventCursor cursor : mCursors) {
            deactivate(cursor);
        }
    }

    /**
     * Deactivates all previously bound cursors and unregisters their observers,
     * prepares views for new data bindings
     */
    void invalidate() {
        for (int i = 0; i < mCursors.size(); i++) {
            swapCursor(i, null, null);
        }
    }

    private void bindSelectedDay(int position) {
        if (mViews.get(position) != null) {
            mViews.get(position).setSelectedDay(mSelectedDayMillis);
        }
    }

    private void swapCursor(int position, @Nullable EventCursor cursor,
                            ContentObserver contentObserver) {
        deactivate(mCursors.get(position));
        if (cursor != null) {
            cursor.registerContentObserver(contentObserver);
            mObservers.put(cursor, contentObserver);
        }
        mCursors.set(position, cursor);
        bindCursor(position);
    }

    private void bindCursor(int position) {
        if (mCursors.get(position) != null && mViews.get(position) != null) {
            mViews.get(position).swapCursor(mCursors.get(position));
        }
    }

    private void deactivate(EventCursor cursor) {
        if (cursor != null) {
            cursor.unregisterContentObserver(mObservers.get(cursor));
            mObservers.remove(cursor);
            cursor.close();
        }
    }
}
