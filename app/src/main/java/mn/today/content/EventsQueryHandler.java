package mn.today.content;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import mn.today.CalendarUtils;
/**
 * Created by Tortuvshin Byambaa on 1/31/2017.
 */
@Deprecated
public abstract class EventsQueryHandler extends AsyncQueryHandler {

    private static final String SORT = CalendarContract.Events.DTSTART + " ASC";
    private static final String AND = " AND ";
    private static final String OR = " OR ";
    private static final String INT_TRUE = "1";
    private static final String INT_FALSE = "0";
    private static final String ALL_DAY = CalendarContract.Events.ALL_DAY + "=?";
    private static final String DELETED = CalendarContract.Events.DELETED + "=?";
    private static final String NOT_CALENDAR_ID = CalendarContract.Events.CALENDAR_ID + "!=?";
    // select events that starts within query range
    private static final String START_WITHIN = "(" +
            CalendarContract.Events.DTSTART + ">=?" + AND +
            CalendarContract.Events.DTSTART + "<?" +
            ")";
    // select events that starts before but end within or after query range
    private static final String START_BEF_END_WITHIN_AFTER = "(" +
            CalendarContract.Events.DTSTART + "<?" + AND +
            CalendarContract.Events.DTEND + ">?" +
            ")";
    // select non all-day events
    private static final String SELECTION_NON_ALL_DAY_EVENTS = "(" +
            ALL_DAY + AND +
            "(" + START_WITHIN + OR + START_BEF_END_WITHIN_AFTER + ")" +
            ")";
    // select all-day events
    private static final String SELECTION_ALL_DAY_EVENTS = "(" +
            ALL_DAY + AND +
            "(" + START_WITHIN + OR + START_BEF_END_WITHIN_AFTER + ")" +
            ")";
    // select non-deleted events from either set
    private static final String SELECTION = "(" +
            DELETED + AND + "(" + SELECTION_NON_ALL_DAY_EVENTS + OR + SELECTION_ALL_DAY_EVENTS + ")" +
            ")";

    @NonNull
    private final Collection<String> mExcludedCalendarIds;

    /**
     * Contrsucts an instance of async query handler for {@link android.provider.CalendarContract.Events}
     * @param cr                     content resolver
     * @param excludedCalendarIds    collection of excluded calendar IDs
     */
    public EventsQueryHandler(ContentResolver cr,
                              @NonNull Collection<String> excludedCalendarIds) {
        super(cr);
        mExcludedCalendarIds = excludedCalendarIds;
    }

    /**
     * Starts background query for events from given start time to given end time
     * Results will be handled asynchronously on main thread
     * via {@link #handleQueryComplete(int, Object, EventCursor)}
     * @param cookie             cookie object to be passed back on complete
     * @param startTimeMillis    start time in milliseconds
     * @param endTimeMillis      end time in milliseconds
     * @see {@link #handleQueryComplete(int, Object, EventCursor)}
     */
    public final void startQuery(Object cookie, long startTimeMillis, long endTimeMillis) {
        final String utcStart = String.valueOf(CalendarUtils.toUtcTimeZone(startTimeMillis)),
                utcEnd = String.valueOf(CalendarUtils.toUtcTimeZone(endTimeMillis)),
                localStart = String.valueOf(startTimeMillis),
                localEnd = String.valueOf(endTimeMillis);
        List<String> args = new ArrayList<String>() {{
            add(INT_FALSE); // not deleted
            add(INT_FALSE); // not all day
            add(localStart);
            add(localEnd);
            add(localStart);
            add(localStart);
            add(INT_TRUE); // all day
            add(utcStart);
            add(utcEnd);
            add(utcStart);
            add(utcStart);
        }};
        StringBuilder sb = new StringBuilder(SELECTION);
        if (!mExcludedCalendarIds.isEmpty()) {
            Iterator<String> iterator = mExcludedCalendarIds.iterator();
            sb.append(AND).append("(");
            while (iterator.hasNext()) {
                args.add(iterator.next());
                sb.append(NOT_CALENDAR_ID);
                if (iterator.hasNext()) {
                    sb.append(AND);
                }
            }
            sb.append(")");
        }
        startQuery(0, cookie, CalendarContract.Events.CONTENT_URI,
                EventCursor.PROJECTION, sb.toString(), args.toArray(new String[args.size()]), SORT);
    }

    @Override
    protected final void onQueryComplete(int token, Object cookie, Cursor cursor) {
        handleQueryComplete(token, cookie, new EventCursor(cursor));
    }

    /**
     * Handles query results. This will be called on main thread.
     * @param token     query token
     * @param cookie    query cookie
     * @param cursor    {@link android.provider.CalendarContract.Events} cursor wrapper
     * @see {@link #startQuery(int, Object, Uri, String[], String, String[], String)}
     */
    protected abstract void handleQueryComplete(int token, Object cookie, EventCursor cursor);
}
