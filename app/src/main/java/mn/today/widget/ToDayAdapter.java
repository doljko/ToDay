package mn.today.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.text.AllCapsTransformationMethod;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import mn.today.CalendarUtils;
import mn.today.EditActivity;
import mn.today.weather.Weather;
import mn.today.R;
import mn.today.content.EventCursor;

/**
 * Created by Tortuvshin Byambaa on 1/31/2017.
 */
@Deprecated
public abstract class ToDayAdapter extends RecyclerView.Adapter<ToDayAdapter.RowViewHolder> {
    private static final String STATE_EVENT_GROUPS = "state:eventGroups";
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_CONTENT = 1;
    private static final int MONTH_SIZE = 31;
    @VisibleForTesting
    static final int BLOCK_SIZE = MONTH_SIZE;
    @VisibleForTesting static final int MAX_SIZE = MONTH_SIZE * 3;

    private final EventGroup.EventObserver mEventObserver = new EventGroup.EventObserver() {
        @Override
        public void onChange(long timeMillis) {
            if (!mLock) {
                loadEvents(timeMillis);
            }
        }
    };
    private final EventGroupList mEventGroups = new EventGroupList(BLOCK_SIZE);
    private final LayoutInflater mInflater;
    private final int mTransparentColor;
    private final int mIconTint;
    private int mColors[];
    private Weather mWeather;
    private boolean mLock;

    public ToDayAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mTransparentColor = ContextCompat.getColor(context, android.R.color.transparent);
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.textColorTertiary
        });
        mIconTint = ta.getColor(0, 0);
        ta.recycle();
        mColors = new int[]{mTransparentColor};
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        deactivate();
    }

    @Override
    public final RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new GroupViewHolder(mInflater.inflate(R.layout.list_item_header,
                        parent, false));
            case VIEW_TYPE_CONTENT:
            default:
                return new ContentViewHolder(mInflater.inflate(R.layout.list_item_content,
                        parent, false));
        }
    }

    @Override
    public final void onBindViewHolder(RowViewHolder holder, int position) {
        final AdapterItem item = getAdapterItem(position);
        bindTitle(item, holder);
        if (item instanceof EventGroup) {
            loadEvents(position);
            bindWeather((EventGroup) item, (GroupViewHolder) holder);
        } else {
            bindTime((EventItem) item, (ContentViewHolder) holder);
            bindColor((EventItem) item, (ContentViewHolder) holder);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editEvent(v.getContext(), (EventItem) item);
                }
            });
        }
    }

    @Override
    public final int getItemCount() {
        return mEventGroups.groupAndChildrenSize();
    }

    @Override
    public final int getItemViewType(int position) {
        if (getAdapterItem(position) instanceof EventGroup) {
            return VIEW_TYPE_HEADER;
        } else { // EventItem
            return VIEW_TYPE_CONTENT;
        }
    }

    /**
     * Loads events for given day, each event should either
     * start and end within the day,
     * or starts before and end within of after the day
     * {@link #bindEvents(long, EventCursor)} should be called afterwards with results
     * @param timeMillis    time in millis that represents day in agenda
     * @see {@link #bindEvents(long, EventCursor)}
     */
    protected void loadEvents(long timeMillis) {
        // override to load events
    }

    /**
     * Binds events for given day, each event should either
     * start and end within the day,
     * or starts before and end within of after the day.
     * Bound cursor should be deactivated via {@link #deactivate()} when appropriate
     * @param timeMillis    time in millis that represents day in agenda
     * @param cursor        {@link CalendarContract.Events} cursor wrapper
     * @see {@link #loadEvents(long)}
     * @see {@link #deactivate()}
     */
    public final void bindEvents(long timeMillis, EventCursor cursor) {
        if (mLock) {
            return;
        }
        Pair<EventGroup, Integer> pair = findGroup(timeMillis);
        if (pair != null) {
            pair.first.setCursor(cursor, mEventObserver);
            notifyEventsChanged(pair.first, pair.second);
        }
    }

    void setCalendarColors(int[] calendarColors) {
        mColors = calendarColors;
    }

    /**
     * Closes bound cursors and unregisters their observers
     * that have been previously bound by {@link #bindEvents(long, EventCursor)},
     * wipes all adapter data
     * @see {@link #bindEvents(long, EventCursor)}
     */
    void deactivate() {
        mEventGroups.clear();
    }

    /**
     * Closes bound cursors and unregisters their observers
     * that have been previously bound by {@link #bindEvents(long, EventCursor)},
     * but keeps adapter data to rebind new cursors
     * @see {@link #bindEvents(long, EventCursor)}
     */
    void invalidate() {
        mEventGroups.invalidate();
        notifyItemRangeChanged(0, getItemCount());
    }

    /**
     * Saves this adapter state
     * @return  saved state
     * @see {@link #restoreState(Bundle)}
     */
    Bundle saveState() {
        Bundle outState = new Bundle();
        outState.putParcelableArrayList(STATE_EVENT_GROUPS, new ArrayList<>(mEventGroups));
        return outState;
    }

    /**
     * Restores adapter's previously saved state
     * @param savedState    saved state
     * @see {@link #saveState()}
     */
    void restoreState(Bundle savedState) {
        ArrayList<EventGroup> savedGroups =
                savedState.getParcelableArrayList(STATE_EVENT_GROUPS);
        // only restore 'no event' groups, actual event binding once cursor is rebound
        mEventGroups.addAll(savedGroups);
    }

    /**
     * Gets adapter position for given day, prepends or appends days
     * to the list if out of range
     * @param context       resources provider
     * @param timeMillis    time in milliseconds representing given day
     * @return  adapter position or {@link RecyclerView#NO_POSITION} if not a valid day (no time)
     */
    int getPosition(Context context, long timeMillis) {
        if (timeMillis < mEventGroups.get(0).mTimeMillis) {
            while (timeMillis < mEventGroups.get(0).mTimeMillis) {
                prepend(context);
            }
        } else if (timeMillis > mEventGroups.get(mEventGroups.size() - 1).mTimeMillis) {
            while (timeMillis > mEventGroups.get(mEventGroups.size() - 1).mTimeMillis) {
                append(context);
            }
        }
        Pair<EventGroup, Integer> pair = findGroup(timeMillis);
        if (pair == null) {
            return RecyclerView.NO_POSITION;
        }
        return pair.second;
    }

    /**
     * Gets {@link AdapterItem} at given position
     * @param position    adapter position
     * @return  an {@link EventGroup} or {@link EventItem}
     */
    AdapterItem getAdapterItem(int position) {
        return mEventGroups.getGroupOrItem(position);
    }

    /**
     * Adds days to beginning of this adapter data set
     * Added days should immediately precede current adapter days.
     * Last days in adapter may be pruned to keep its size constantly small.
     * @param context    resources provider
     * @see {@link #append(Context)}
     */
    void prepend(Context context) {
        long daysMillis = mEventGroups.size() * DateUtils.DAY_IN_MILLIS;
        int count = BLOCK_SIZE, inserted = 0;
        for (int i = 0; i < count; i++) {
            EventGroup last = mEventGroups.get(mEventGroups.size() - 1 - i);
            EventGroup first = new EventGroup(context, last.mTimeMillis - daysMillis);
            inserted += first.itemCount() + 1;
            mEventGroups.add(0, first);
        }
        notifyItemRangeInserted(0, inserted);
        prune(false);
    }

    /**
     * Adds days to end of this adapter data set
     * Added days should immediately succeed current adapter days.
     * First days in adapter may be pruned to keep its size constantly small.
     * @param context    resources provider
     * @see {@link #prepend(Context)}
     */
    void append(Context context) {
        if (mEventGroups.isEmpty()) {
            int count = BLOCK_SIZE;
            long today = CalendarUtils.today();
            for (int i = -count; i < count; i++) {
                mEventGroups.add(new EventGroup(context, today + DateUtils.DAY_IN_MILLIS * i));
            }
        } else {
            int count = BLOCK_SIZE;
            long daysMillis = mEventGroups.size() * DateUtils.DAY_IN_MILLIS;
            int inserted = 0;
            for (int i = 0; i < count; i++) {
                EventGroup first = mEventGroups.get(i);
                EventGroup last = new EventGroup(context, first.mTimeMillis + daysMillis);
                inserted += last.itemCount() + 1;
                mEventGroups.add(last);
            }
            notifyItemRangeInserted(getItemCount() - inserted + 1, inserted);
            prune(true);
        }
    }

    /**
     * Temporarily locks view holder binding until {@link #unlockBinding()} is called.
     * This can be used in case {@link RecyclerView} is being scrolled and binding
     * needs to be disabled temporarily to prevent scroll offset changes
     * @see {@link #unlockBinding()}
     */
    void lockBinding() {
        mLock = true;
    }

    /**
     * Unlocks view holder binding that may have been previously locked by {@link #lockBinding()},
     * notifying adapter to rebind view holders as a result
     * @see {@link #loadEvents(long)}
     */
    void unlockBinding() {
        mLock = false;
        notifyItemRangeChanged(0, getItemCount());
    }

    /**
     * Sets weather information to be displayed
     * @param weather    weather information to be displayed, or null to disable
     */
    void setWeather(@Nullable Weather weather) {
        mWeather = weather;
        notifyItemRangeChanged(0, getItemCount());
    }

    private void bindTitle(AdapterItem item, RowViewHolder holder) {
        if (item instanceof EventGroup) {
            ((GroupViewHolder) holder).textView.setText(item.mTitle);
        } else if (item instanceof NoEventItem) {
            ((ContentViewHolder) holder).textViewTitle.setText(R.string.no_event);
        } else {
            ((ContentViewHolder) holder).textViewTitle.setText(item.mTitle);
        }
    }

    private void bindTime(EventItem eventItem, ContentViewHolder contentHolder) {
        if (eventItem instanceof NoEventItem) {
            contentHolder.textViewTime.setVisibility(View.GONE);
            return;
        }
        contentHolder.textViewTime.setVisibility(View.VISIBLE);
        Context context = contentHolder.textViewTime.getContext();
        switch (eventItem.mDisplayType) {
            case EventItem.DISPLAY_TYPE_ALL_DAY:
                contentHolder.textViewTime.setText(R.string.all_day);
                break;
            case EventItem.DISPLAY_TYPE_START_TIME:
            default:
                contentHolder.textViewTime.setText(CalendarUtils.toTimeString(
                        context, eventItem.mStartTimeMillis));
                break;
            case EventItem.DISPLAY_TYPE_END_TIME:
                String endTimeString = CalendarUtils.toTimeString(
                        context, eventItem.mEndTimeMillis);
                contentHolder.textViewTime.setText(
                        context.getString(R.string.end_time, endTimeString));
                break;
        }
    }

    private void bindColor(EventItem item, ContentViewHolder holder) {
        if (item instanceof NoEventItem) {
            holder.background.setBackgroundColor(mTransparentColor);
        } else {
            int color = mColors[(int) (Math.abs(item.mCalendarId) % mColors.length)];
            holder.background.setBackgroundColor(color);
        }
    }

    private void bindWeather(EventGroup groupItem, final GroupViewHolder holder) {
        // bind weather for today and tomorrow if exist, hide UI otherwise
        if (groupItem.mTimeMillis == CalendarUtils.today() &&
                mWeather != null && mWeather.today != null) {
            bindWeatherInfo(holder.textViewMorning, mWeather.today.morning);
            bindWeatherInfo(holder.textViewAfternoon, mWeather.today.afternoon);
            bindWeatherInfo(holder.textViewNight, mWeather.today.night);
            holder.weather.setVisibility(View.VISIBLE);
        } else if (groupItem.mTimeMillis == CalendarUtils.today() + DateUtils.DAY_IN_MILLIS &&
                mWeather != null && mWeather.tomorrow != null) {
            bindWeatherInfo(holder.textViewMorning, mWeather.tomorrow.morning);
            bindWeatherInfo(holder.textViewAfternoon, mWeather.tomorrow.afternoon);
            bindWeatherInfo(holder.textViewNight, mWeather.tomorrow.night);
            holder.weather.setVisibility(View.VISIBLE);
        } else {
            holder.weather.setVisibility(View.GONE);
        }
    }

    private void bindWeatherInfo(TextView textView, Weather.WeatherInfo info) {
        Drawable icon = info.getIcon(textView.getContext(), mIconTint);
        textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        if (info.temperature != null) {
            textView.setText(textView.getContext().getString(R.string.fahrenheit, info.temperature));
        }
    }

    private Pair<EventGroup, Integer> findGroup(long timeMillis) {
        int position = 0;
        // TODO improve searching
        for (int i = 0; i < mEventGroups.size(); i++) {
            EventGroup group = mEventGroups.get(i);
            if (group.mTimeMillis == timeMillis) {
                return Pair.create(group, position);
            } else {
                position += group.itemCount() + 1;
            }
        }
        return null;
    }

    private void notifyEventsChanged(EventGroup group, int position) {
        int lastCount = group.mLastCursorCount,
                newCount = group.mCursor.getCount(),
                refreshCount = Math.min(newCount, lastCount),
                diff = newCount - lastCount;
        // either last or current count is 0
        // we need to swap no event placeholder
        // and insert/remove the rest - 1 positions
        if (refreshCount == 0) {
            refreshCount = 1;
            diff = Math.max(--diff, 0);
        }
        notifyItemRangeChanged(position + 1, refreshCount);
        if (diff > 0) {
            notifyItemRangeInserted(position + 1 + refreshCount, diff);
        } else if (diff < 0) {
            notifyItemRangeRemoved(position + 1 + refreshCount, -diff);
        }
        group.mLastCursorCount = newCount;
    }

    private void loadEvents(int position) {
        if (mLock) {
            return;
        }
        EventGroup group = (EventGroup) getAdapterItem(position);
        if (group.mCursor == null) {
            loadEvents(group.mTimeMillis);
        }
    }

    private void editEvent(Context context, EventItem eventItem) {
        EventEditView.Event.Builder eventBuilder = new EventEditView.Event.Builder()
                .start(eventItem.mStartTimeMillis)
                .end(eventItem.mEndTimeMillis)
                .allDay(eventItem.mIsAllDay);
        if (!(eventItem instanceof NoEventItem)) {
            eventBuilder.id(eventItem.mId)
                    .calendarId(eventItem.mCalendarId)
                    .title(eventItem.mTitle);
        }
        context.startActivity(new Intent(context, EditActivity.class)
                .putExtra(EditActivity.EXTRA_EVENT, eventBuilder.build()));
    }

    private void prune(boolean start) {
        if (mEventGroups.size() <= MAX_SIZE) {
            return;
        }
        int removed = 0, index = start ? 0 : MAX_SIZE;
        while (mEventGroups.size() > MAX_SIZE) {
            removed += mEventGroups.get(index).itemCount() + 1;
            mEventGroups.remove(index);
        }
        notifyItemRangeRemoved(start ? 0 : getItemCount(), removed);
    }

    static abstract class RowViewHolder extends RecyclerView.ViewHolder {

        public RowViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class GroupViewHolder extends RowViewHolder {
        final TextView textView;
        final TextView textViewMorning;
        final TextView textViewAfternoon;
        final TextView textViewNight;
        final View weather;

        public GroupViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_view_title);
            textView.setTransformationMethod(
                    new AllCapsTransformationMethod(textView.getContext()));
            weather = itemView.findViewById(R.id.weather);
            textViewMorning = (TextView) itemView.findViewById(R.id.text_view_morning);
            textViewAfternoon = (TextView) itemView.findViewById(R.id.text_view_afternoon);
            textViewNight = (TextView) itemView.findViewById(R.id.text_view_night);
        }
    }

    static class ContentViewHolder extends RowViewHolder {

        final TextView textViewTitle;
        final TextView textViewTime;
        final View background;

        public ContentViewHolder(View itemView) {
            super(itemView);
            textViewTitle = (TextView) itemView.findViewById(R.id.text_view_title);
            textViewTime = (TextView) itemView.findViewById(R.id.text_view_time);
            background = itemView.findViewById(R.id.background);
        }
    }

    /**
     * A custom {@link ArrayList} for {@link EventGroup} that allows
     * operations to manage each group's {@link EventItem}
     */
    static class EventGroupList extends ArrayList<EventGroup> {

        int mChildrenSize = 0;

        EventGroupList(int capacity) {
            super(capacity);
        }

        @Override
        public void add(int index, EventGroup group) {
            mChildrenSize += group.itemCount();
            super.add(index, group);
        }

        @Override
        public boolean add(EventGroup group) {
            mChildrenSize += group.itemCount();
            return super.add(group);
        }

        @Override
        public boolean addAll(Collection<? extends EventGroup> collection) {
            if (collection.isEmpty()) {
                return false;
            }
            for (EventGroup group : collection) {
                add(group);
            }
            return true;
        }

        @Override
        public EventGroup remove(int index) {
            EventGroup group = super.remove(index);
            mChildrenSize -= group.itemCount();
            group.deactivate();
            return group;
        }

        @Override
        public void clear() {
            for (EventGroup group : this) {
                group.deactivate();
            }
            super.clear();
            mChildrenSize = 0;
        }

        int groupAndChildrenSize() {
            return size() + mChildrenSize;
        }

        AdapterItem getGroupOrItem(int index) {
            int count = 0;
            for (int i = 0; i < size(); i++) {
                if (index < count + 1 + get(i).itemCount()) {
                    if (index == count) {
                        return get(i);
                    } else {
                        return get(i).getItem(index - count - 1);
                    }
                } else {
                    count += 1 + get(i).itemCount();
                }
            }
            return null;
        }

        void invalidate() {
            for (EventGroup group : this) {
                mChildrenSize -= group.itemCount();
                group.deactivate();
                mChildrenSize += group.itemCount();
            }
        }
    }

    static abstract class AdapterItem implements Parcelable {
        final String mTitle;
        final long mTimeMillis;

        AdapterItem(String title, long timeMillis) {
            this.mTitle = title;
            this.mTimeMillis = timeMillis;
        }

        AdapterItem(Parcel source) {
            mTitle = source.readString();
            mTimeMillis = source.readLong();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mTitle);
            dest.writeLong(mTimeMillis);
        }
    }

    static class EventGroup extends AdapterItem {
        public static Creator<EventGroup> CREATOR = new Creator<EventGroup>() {
            @Override
            public EventGroup createFromParcel(Parcel source) {
                return new EventGroup(source);
            }

            @Override
            public EventGroup[] newArray(int size) {
                return new EventGroup[size];
            }
        };

        interface EventObserver {
            void onChange(long timeMillis);
        }

        private final ContentObserver mContentObserver = new ContentObserver(new Handler()) {
            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }

            @Override
            public void onChange(boolean selfChange) {
                mEventObserver.onChange(mTimeMillis);
            }
        };
        private EventGroup.EventObserver mEventObserver;
        int mLastCursorCount = 0;
        EventCursor mCursor;

        EventGroup(Context context, long timeMillis) {
            super(CalendarUtils.toDayString(context, timeMillis), timeMillis);
        }

        private EventGroup(Parcel source) {
            super(source);
        }

        int itemCount() {
            if (mCursor == null || mCursor.getCount() == 0) {
                return 1; // has a no event item by default
            }
            return mCursor.getCount();
        }

        EventItem getItem(int index) {
            if (mCursor == null || mCursor.getCount() == 0) {
                return new NoEventItem(null, mTimeMillis);
            }
            mCursor.moveToPosition(index);
            // TODO use an object pool
            return new EventItem(mTimeMillis, mCursor);
        }

        void setCursor(EventCursor cursor, EventObserver eventObserver) {
            deactivate(); // deactivate previously set cursor if any
            cursor.registerContentObserver(mContentObserver);
            mCursor = cursor;
            mEventObserver = eventObserver;
        }

        void deactivate() {
            mLastCursorCount = 0;
            if (mCursor != null) {
                mCursor.unregisterContentObserver(mContentObserver);
                mCursor.close();
                mCursor = null;
                mEventObserver = null;
            }
        }
    }

    static class EventItem extends AdapterItem {

        static final int DISPLAY_TYPE_START_TIME = 0;
        static final int DISPLAY_TYPE_ALL_DAY = 1;
        static final int DISPLAY_TYPE_END_TIME = 2;

        public static Creator<EventItem> CREATOR = new Creator<EventItem>() {
            @Override
            public EventItem createFromParcel(Parcel source) {
                return new EventItem(source);
            }

            @Override
            public EventItem[] newArray(int size) {
                return new EventItem[size];
            }
        };

        long mId;
        long mCalendarId;
        long mStartTimeMillis;
        long mEndTimeMillis;
        boolean mIsAllDay;
        int mDisplayType = DISPLAY_TYPE_START_TIME;

        EventItem(long timeMillis, EventCursor cursor) {
            super(cursor.getTitle(), timeMillis);
            mId = cursor.getId();
            mCalendarId = cursor.getCalendarId();
            mStartTimeMillis = cursor.getDateTimeStart();
            mEndTimeMillis = cursor.getDateTimeEnd();
            mIsAllDay = cursor.getAllDay();
            // all-day time in Calendar Provider is midnight in UTC, need to convert to local
            if (mIsAllDay) {
                mStartTimeMillis = CalendarUtils.toLocalTimeZone(mStartTimeMillis);
                mEndTimeMillis = CalendarUtils.toLocalTimeZone(mEndTimeMillis);
            }
            setDisplayType();
        }

        EventItem(String title, long timeMillis) {
            super(title, timeMillis);
        }

        private EventItem(Parcel source) {
            super(source);
            mId = source.readLong();
            mCalendarId = source.readLong();
            mStartTimeMillis = source.readLong();
            mEndTimeMillis = source.readLong();
            mIsAllDay = source.readInt() == 1;
            mDisplayType = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(mId);
            dest.writeLong(mCalendarId);
            dest.writeLong(mStartTimeMillis);
            dest.writeLong(mEndTimeMillis);
            dest.writeInt(mIsAllDay ? 1 : 0);
            dest.writeInt(mDisplayType);
        }

        private void setDisplayType() {
            if (mIsAllDay) {
                mDisplayType = DISPLAY_TYPE_ALL_DAY;
            } else if (mStartTimeMillis >= mTimeMillis) {
                // start within agenda date
                mDisplayType = DISPLAY_TYPE_START_TIME;
            } else if (mEndTimeMillis < mTimeMillis + DateUtils.DAY_IN_MILLIS) {
                // start before, end within agenda date
                mDisplayType = DISPLAY_TYPE_END_TIME;
            } else {
                // start before, end after agenda date
                mDisplayType = DISPLAY_TYPE_ALL_DAY;
            }
        }
    }

    static class NoEventItem extends EventItem {
        public static Creator<NoEventItem> CREATOR = new Creator<NoEventItem>() {
            @Override
            public NoEventItem createFromParcel(Parcel source) {
                return new NoEventItem(source);
            }

            @Override
            public NoEventItem[] newArray(int size) {
                return new NoEventItem[size];
            }
        };

        NoEventItem(String title, long timeMillis) {
            super(title, timeMillis);
            mStartTimeMillis = timeMillis;
            mEndTimeMillis = timeMillis;
        }

        NoEventItem(Parcel source) {
            super(source);
        }
    }
}
