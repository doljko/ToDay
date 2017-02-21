package mn.today.content;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.CalendarContract;

/**
 * Created by Tortuvshin Byambaa on 1/31/2017.
 */
@Deprecated
public class EventCursor extends CursorWrapper {

    /**
     * {@link android.provider.CalendarContract.Events} query projection
     */
    public static final String[] PROJECTION = new String[]{
            CalendarContract.Events._ID,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY
    };
    private static final int PROJECTION_INDEX_ID = 0;
    private static final int PROJECTION_INDEX_CALENDAR_ID = 1;
    private static final int PROJECTION_INDEX_TITLE = 2;
    private static final int PROJECTION_INDEX_DTSTART = 3;
    private static final int PROJECTION_INDEX_DTEND = 4;
    private static final int PROJECTION_INDEX_ALL_DAY = 5;

    public EventCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Gets event ID
     * @return  event ID
     */
    public long getId() {
        return getLong(PROJECTION_INDEX_ID);
    }

    /**
     * Gets event calendar ID
     * @return  event calendar ID
     */
    public long getCalendarId() {
        return getLong(PROJECTION_INDEX_CALENDAR_ID);
    }

    /**
     * Gets event title
     * @return  event title
     */
    public String getTitle() {
        return getString(PROJECTION_INDEX_TITLE);
    }

    /**
     * Gets start time in milliseconds.
     * If {@link #getAllDay()} is true, time will be midnight in UTC.
     * @return  start time in milliseconds
     * @see {@link #getAllDay()}
     */
    public long getDateTimeStart() {
        return getLong(PROJECTION_INDEX_DTSTART);
    }

    /**
     * Gets end time in milliseconds.
     * If {@link #getAllDay()} is true, time will be midnight in UTC.
     * @return  end time in milliseconds
     * @see {@link #getAllDay()}
     */
    public long getDateTimeEnd() {
        return getLong(PROJECTION_INDEX_DTEND);
    }

    /**
     * Checks if event is all day. All-day event has start and end time midnight in UTC.
     * @return  true if all-day event, false otherwise
     * @see {@link #getDateTimeStart()}
     * @see {@link #getDateTimeEnd()}
     */
    public boolean getAllDay() {
        return getInt(PROJECTION_INDEX_ALL_DAY) == 1;
    }
}
