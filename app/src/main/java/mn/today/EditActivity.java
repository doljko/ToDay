package mn.today;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import mn.today.content.CalendarCursor;
import mn.today.widget.EventEditView;


public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * {@link android.os.Parcelable} extra contains {@link EventEditView.Event} to edit
     */
    public static final String EXTRA_EVENT = "extra:event";
    private static final String STATE_EVENT = "state:event";
    private static final String EXTRA_CALENDAR_ID = "extra:calendarId";
    private static final int LOADER_CALENDARS = 0;
    private static final int LOADER_SELECTED_CALENDAR = 1;

    private EventEditView mEventEditView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermissions()) {
            // simply relaunch app if permissions are revoked
            finish();
            startActivity(getPackageManager().getLaunchIntentForPackage(getPackageName())
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            return;
        }
        setContentView(R.layout.activity_edit);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_today));
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        mEventEditView = (EventEditView) findViewById(R.id.event_edit_view);
        EventEditView.Event event;
        if (savedInstanceState == null) {
            event = getIntent().getParcelableExtra(EXTRA_EVENT);
            if (event == null) {
                event = EventEditView.Event.createInstance();
            }
            //noinspection ConstantConditions
            mEventEditView.setEvent(event);
            Bundle args = new Bundle();
            args.putLong(EXTRA_CALENDAR_ID, event.getCalendarId());
            getSupportLoaderManager().initLoader(LOADER_SELECTED_CALENDAR, args, this);
        } else {
            event = savedInstanceState.getParcelable(STATE_EVENT);
            //noinspection ConstantConditions
            mEventEditView.setEvent(event);
        }
        setTitle(event.hasId() ? R.string.edit_event : R.string.create_event);
        getSupportLoaderManager().initLoader(LOADER_CALENDARS, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_delete).setVisible(mEventEditView.getEvent().hasId());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.action_save) {
            if (save()) {
                finish();
            }
            return true;
        }
        if (item.getItemId() == R.id.action_delete) {
            confirmDelete();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_EVENT, mEventEditView.getEvent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEventEditView != null) { // may be null if not created due to missing permissions
            mEventEditView.swapCalendarSource(null);
        }
    }

    @Override
    public void onBackPressed() {
        confirmFinish();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setTitle(int titleId) {
        if (findViewById(R.id.form_title) != null) { // exist in landscape
            ((TextView) findViewById(R.id.form_title)).setText(titleId);
        } else {
            getSupportActionBar().setDisplayOptions(
                    getSupportActionBar().getDisplayOptions() | ActionBar.DISPLAY_SHOW_TITLE);
            super.setTitle(titleId);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String[] selectionArgs = null;
        if (id == LOADER_SELECTED_CALENDAR) {
            selection = CalendarContract.Calendars._ID + "=?";
            selectionArgs = new String[]{String.valueOf(args.getLong(EXTRA_CALENDAR_ID))};
        }
        return new CursorLoader(this, CalendarContract.Calendars.CONTENT_URI,
                CalendarCursor.PROJECTION,
                selection, selectionArgs,
                CalendarContract.Calendars.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_CALENDARS:
                if (data != null && data.moveToFirst()) {
                    mEventEditView.swapCalendarSource(new CalendarCursor(data));
                }
                break;
            case LOADER_SELECTED_CALENDAR:
                if (data != null && data.moveToFirst()) {
                    mEventEditView.setSelectedCalendar(new CalendarCursor(data).getDisplayName());
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_CALENDARS) {
            mEventEditView.swapCalendarSource(null);
        }
    }

    @VisibleForTesting
    protected boolean checkPermissions() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) |
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void confirmFinish() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_discard_changes)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create()
                .show();
    }

    private boolean save() {
        EventEditView.Event event = mEventEditView.getEvent();
        if (!isValid(event)) {
            return false;
        }
        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Events.TITLE, event.getTitle());
        cv.put(CalendarContract.Events.DTSTART, event.getStartDateTime());
        cv.put(CalendarContract.Events.DTEND, event.getEndDateTime());
        cv.put(CalendarContract.Events.ALL_DAY, event.isAllDay());
        cv.put(CalendarContract.Events.EVENT_END_TIMEZONE, event.getTimeZone());
        cv.put(CalendarContract.Events.EVENT_TIMEZONE, event.getTimeZone());
        cv.put(CalendarContract.Events.CALENDAR_ID, event.getCalendarId());
        if (event.hasId()) {
            Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,
                    event.getId());
            new EventQueryHandler(this)
                    .startUpdate(0, null, uri, cv, null, null);
        } else {
            new EventQueryHandler(this)
                    .startInsert(0, null, CalendarContract.Events.CONTENT_URI, cv);
        }
        return true;
    }

    private boolean isValid(EventEditView.Event event) {
        if (!event.hasCalendarId()) {
            //noinspection ConstantConditions
            Snackbar.make(findViewById(R.id.coordinator_layout),
                    R.string.warning_missing_calendar,
                    Snackbar.LENGTH_SHORT)
                    .show();
            return false;
        }
        return event.hasTitle();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete();
                        finish();
                    }
                })
                .create()
                .show();
    }

    private void delete() {
        new EventQueryHandler(this).startDelete(0, null,
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,
                        mEventEditView.getEvent().getId()),
                null, null);
    }

    static class EventQueryHandler extends AsyncQueryHandler {

        private final WeakReference<Context> mContext;

        public EventQueryHandler(Context context) {
            super(context.getContentResolver());
            mContext = new WeakReference<>(context);
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            if (mContext.get() != null) {
                Toast.makeText(mContext.get(), R.string.event_created, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            if (mContext.get() != null) {
                Toast.makeText(mContext.get(), R.string.event_updated, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            if (mContext.get() != null) {
                Toast.makeText(mContext.get(), R.string.event_deleted, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
