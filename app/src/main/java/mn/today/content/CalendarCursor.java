package mn.today.content;

/**
 * Created by Tortuvshin Byambaa on 1/31/2017.
 */

import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.CalendarContract;

/**
 * {@link android.provider.CalendarContract.Calendars} cursor wrapper
 */
@Deprecated
public class CalendarCursor extends CursorWrapper {

    /**
     * {@link android.provider.CalendarContract.Calendars} query projection
     */
    public static final String[] PROJECTION = new String[]{
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
    };
    private static final int PROJECTION_INDEX_ID = 0;
    private static final int PROJECTION_INDEX_DISPLAY_NAME = 1;

    public CalendarCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Gets calendar ID
     * @return  calendar ID
     */
    public long getId() {
        return getLong(PROJECTION_INDEX_ID);
    }

    /**
     * Gets calendar display name
     * @return  calendar display name
     */
    public String getDisplayName() {
        return getString(PROJECTION_INDEX_DISPLAY_NAME);
    }
}