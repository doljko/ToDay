package mn.today.widget;

import android.content.Context;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mn.today.CalendarUtils;
import mn.today.R;
import mn.today.content.EventCursor;
import mn.today.theme.CircleSpan;
import mn.today.theme.UnderDotSpan;

/**
 * Created by Tortuvshin Byambaa on 1/31/2017.
 */

class MonthView extends RecyclerView {
    private static final int SPANS_COUNT = 7; // days in week
    @VisibleForTesting
    long mMonthMillis;
    private GridAdapter mAdapter;
    private OnDateChangeListener mListener;

    /**
     * Callback interface for date selection events
     */
    interface OnDateChangeListener {
        /**
         * Fired when a new selection has been made via UI interaction
         * @param dayMillis  selected day in milliseconds
         */
        void onSelectedDayChange(long dayMillis);
    }

    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonthView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Sets listener to be notified when day selection changes
     * @param listener  listener to be notified
     */
    void setOnDateChangeListener(OnDateChangeListener listener) {
        mListener = listener;
    }

    private void init() {
        setLayoutManager(new GridLayoutManager(getContext(), SPANS_COUNT));
        setHasFixedSize(true);
        setCalendar(CalendarUtils.today());
    }

    /**
     * Sets month to display
     * @param monthMillis  month to display in milliseconds
     */
    void setCalendar(long monthMillis) {
        if (CalendarUtils.isNotTime(monthMillis)) {
            throw new IllegalArgumentException("Invalid timestamp value");
        }
        if (CalendarUtils.sameMonth(mMonthMillis, monthMillis)) {
            return;
        }
        mMonthMillis = monthMillis;
        mAdapter = new GridAdapter(monthMillis);
        mAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                if (mListener == null) {
                    return;
                }
                if (payload instanceof SelectionPayload) {
                    mListener.onSelectedDayChange(((SelectionPayload) payload).timeMillis);
                }
            }
        });
        setAdapter(mAdapter);
    }

    /**
     * Sets selected day if it falls within this month, unset any previously selected day otherwise
     * @param dayMillis    selected day in milliseconds, {@link CalendarUtils#NO_TIME_MILLIS} to clear
     */
    void setSelectedDay(long dayMillis) {
        if (CalendarUtils.isNotTime(mMonthMillis)) {
            return;
        }
        if (CalendarUtils.isNotTime(dayMillis)) {
            mAdapter.setSelectedDay(CalendarUtils.NO_TIME_MILLIS);
        } else if (CalendarUtils.sameMonth(mMonthMillis, dayMillis)) {
            mAdapter.setSelectedDay(dayMillis);
        } else {
            mAdapter.setSelectedDay(CalendarUtils.NO_TIME_MILLIS);
        }
    }

    /**
     * Swaps cursor for calendar events
     * @param cursor    {@link CalendarContract.Events} cursor wrapper
     */
    void swapCursor(@NonNull EventCursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    static class GridAdapter extends Adapter<CellViewHolder> {
        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_CONTENT = 1;
        private final String[] mWeekdays;
        private final int mStartOffset;
        private final int mDays;
        private final long mBaseTimeMillis;
        @VisibleForTesting final Set<Integer> mEvents = new HashSet<>();
        private EventCursor mCursor;
        private int mSelectedPosition = -1;

        public GridAdapter(long monthMillis) {
            mWeekdays = DateFormatSymbols.getInstance().getShortWeekdays();
            mBaseTimeMillis = CalendarUtils.monthFirstDay(monthMillis);
            mStartOffset = CalendarUtils.monthFirstDayOffset(mBaseTimeMillis) + SPANS_COUNT;
            mDays = mStartOffset + CalendarUtils.monthSize(monthMillis);
        }

        @Override
        public CellViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    return new HeaderViewHolder(inflater.inflate(
                            R.layout.grid_item_header, parent, false));
                case VIEW_TYPE_CONTENT:
                default:
                    return new ContentViewHolder(inflater.inflate(
                            R.layout.grid_item_content, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(CellViewHolder holder, int position) {
            if (holder instanceof HeaderViewHolder) {
                int index;
                switch (CalendarUtils.sWeekStart) {
                    case Calendar.SATURDAY:
                        index = position == 0 ? Calendar.SATURDAY : position;
                        break;
                    case Calendar.SUNDAY:
                    default:
                        index = position + Calendar.SUNDAY;
                        break;
                    case Calendar.MONDAY:
                        index = position + Calendar.MONDAY == mWeekdays.length ?
                                Calendar.SUNDAY : position + Calendar.MONDAY;
                        break;
                }
                ((HeaderViewHolder) holder).textView.setText(mWeekdays[index]);
            } else { // holder instanceof ContentViewHolder
                if (position < mStartOffset) {
                    ((ContentViewHolder) holder).textView.setText(null);
                } else {
                    final int adapterPosition = holder.getAdapterPosition();
                    TextView textView = ((ContentViewHolder) holder).textView;
                    int dayIndex = adapterPosition - mStartOffset;
                    String dayString = String.valueOf(dayIndex + 1);
                    SpannableString spannable = new SpannableString(dayString);
                    if (mSelectedPosition == adapterPosition) {
                        spannable.setSpan(new CircleSpan(textView.getContext()), 0,
                                dayString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else if (mEvents.contains(dayIndex)) {
                        spannable.setSpan(new UnderDotSpan(textView.getContext()),
                                0, dayString.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    textView.setText(spannable, TextView.BufferType.SPANNABLE);
                    textView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setSelectedPosition(adapterPosition, true);
                        }
                    });
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position < SPANS_COUNT) {
                return VIEW_TYPE_HEADER;
            }
            return VIEW_TYPE_CONTENT;
        }

        @Override
        public int getItemCount() {
            return mDays;
        }

        void setSelectedDay(long dayMillis) {
            setSelectedPosition(CalendarUtils.isNotTime(dayMillis) ? -1 :
                    mStartOffset + CalendarUtils.dayOfMonth(dayMillis) - 1, false);
        }

        void swapCursor(@NonNull EventCursor cursor) {
            if (mCursor == cursor) {
                return;
            }
            mCursor = cursor;
            Iterator<Integer> iterator = mEvents.iterator();
            while (iterator.hasNext()) {
                int dayIndex = iterator.next();
                iterator.remove();
                notifyItemChanged(dayIndex + mStartOffset);
            }
            if (!mCursor.moveToFirst()) {
                return;
            }
            // TODO improve performance
            do {
                long start = mCursor.getDateTimeStart();
                long end = mCursor.getDateTimeEnd();
                boolean allDay = mCursor.getAllDay();
                // all-day time in Calendar Provider is midnight in UTC, need to convert to local
                if (allDay) {
                    start = CalendarUtils.toLocalTimeZone(start);
                    end = CalendarUtils.toLocalTimeZone(end) - DateUtils.DAY_IN_MILLIS;
                }
                int startIndex = (int) ((start - mBaseTimeMillis) / DateUtils.DAY_IN_MILLIS);
                int endIndex = (int) ((end - mBaseTimeMillis) / DateUtils.DAY_IN_MILLIS);
                endIndex = Math.min(endIndex, getItemCount() - mStartOffset - 1);
                for (int dayIndex = startIndex; dayIndex <= endIndex; dayIndex++) {
                    if (!mEvents.contains(dayIndex)) {
                        mEvents.add(dayIndex);
                        notifyItemChanged(dayIndex + mStartOffset);
                    }
                }
            } while (mCursor.moveToNext());
        }

        private void setSelectedPosition(int position, boolean notifyObservers) {
            int last = mSelectedPosition;
            if (position == last) {
                return;
            }
            mSelectedPosition = position;
            if (last >= 0) {
                notifyItemChanged(last);
            }
            if (position >= 0) {
                long timeMillis = mBaseTimeMillis + (mSelectedPosition - mStartOffset) *
                        DateUtils.DAY_IN_MILLIS;
                notifyItemChanged(position, notifyObservers ?
                        new SelectionPayload(timeMillis) : null);
            }
        }
    }

    static abstract class CellViewHolder extends RecyclerView.ViewHolder {

        public CellViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class HeaderViewHolder extends CellViewHolder {

        final TextView textView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }

    static class ContentViewHolder extends CellViewHolder {

        final TextView textView;

        public ContentViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }

    static class SelectionPayload {
        final long timeMillis;

        public SelectionPayload(long timeMillis) {
            this.timeMillis = timeMillis;
        }
    }
}

