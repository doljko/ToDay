package mn.today;


import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;

import mn.today.content.CalendarCursor;
import mn.today.content.EventCursor;
import mn.today.content.EventsQueryHandler;
import mn.today.widget.CalendarSelectionView;
import mn.today.widget.EventCalendarView;
import mn.today.widget.ToDayAdapter;
import mn.today.widget.ToDayView;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String STATE_TOOLBAR_TOGGLE = "state:toolbarToggle";
    private static final int REQUEST_CODE_CALENDAR = 0;
    private static final int REQUEST_CODE_LOCATION = 1;
    private static final String SEPARATOR = ",";
    private static final int LOADER_CALENDARS = 0;
    private static final int LOADER_LOCAL_CALENDAR = 1;

    private final CalendarSelectionView.OnSelectionChangeListener mCalendarSelectionListener
            = new CalendarSelectionView.OnSelectionChangeListener() {
        @Override
        public void onSelectionChange(long id, boolean enabled) {
            if (!enabled) {
                mExcludedCalendarIds.add(String.valueOf(id));
            } else {
                mExcludedCalendarIds.remove(String.valueOf(id));
            }
            mCalendarView.invalidateData();
            mTodayView.invalidateData();
        }
    };
    private final Coordinator mCoordinator = new Coordinator();
    private View mCoordinatorLayout;
    private CheckedTextView mToolbarToggle;
    private EventCalendarView mCalendarView;
    private ToDayView mTodayView;
    private FloatingActionButton mFabAdd;
    private CalendarSelectionView mCalendarSelectionView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View mDrawer;
    private final HashSet<String> mExcludedCalendarIds = new HashSet<>();
    private boolean mWeatherEnabled, mPendingWeatherEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
        setUpContentView();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCoordinator.restoreState(savedInstanceState);
        if (savedInstanceState.getBoolean(STATE_TOOLBAR_TOGGLE, false)) {
            View toggleButton = findViewById(R.id.toolbar_toggle_frame);
            if (toggleButton != null) { // can be null as disabled in landscape
                toggleButton.performClick();
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
        mCoordinator.coordinate(mToolbarToggle, mCalendarView, mTodayView);
        if (checkCalendarPermissions()) {
            loadEvents();
        } else {
            toggleEmptyView(true);
        }
        if (mWeatherEnabled && !checkLocationPermissions()) {
            explainLocationPermissions();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_weather).setChecked(mWeatherEnabled);
        switch (CalendarUtils.sWeekStart) {
            case Calendar.SATURDAY:
                menu.findItem(R.id.action_week_start_saturday).setChecked(true);
                break;
            case Calendar.SUNDAY:
                menu.findItem(R.id.action_week_start_sunday).setChecked(true);
                break;
            case Calendar.MONDAY:
                menu.findItem(R.id.action_week_start_monday).setChecked(true);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_today) {
            mCoordinator.reset();
            return true;
        }
        if (item.getItemId() == R.id.action_weather) {
            mPendingWeatherEnabled = !mWeatherEnabled;
            if (!mWeatherEnabled && !checkLocationPermissions()) {
                requestLocationPermissions();
            } else {
            }
            return true;
        }

        if (item.getItemId() == R.id.action_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        }

        if (item.getItemId() == R.id.action_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        }
        if (item.getItemId() == R.id.action_view) {
            Intent view = new Intent(this, ViewActivity.class);
            startActivity(view);
            return true;
        }


        if (item.getItemId() == R.id.action_graphic) {
            Intent contact = new Intent(this, GraphicActivity.class);
            startActivity(contact);
            return true;
        }

        if (item.getItemId() == R.id.action_graphic) {
            Intent graph = new Intent(this, SettingsActivity.class);
            startActivity(graph);
            return true;
        }

        if (item.getItemId() == R.id.action_week_start_saturday ||
                item.getItemId() == R.id.action_week_start_sunday ||
                item.getItemId() == R.id.action_week_start_monday) {
            if (!item.isChecked()) {
                changeWeekStart(item.getItemId());
            }
            return true;
        }
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mCoordinator.saveState(outState);
        outState.putBoolean(STATE_TOOLBAR_TOGGLE, mToolbarToggle.isChecked());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCalendarView.deactivate();
        mTodayView.setAdapter(null); // force detaching adapter
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(CalendarUtils.PREF_CALENDAR_EXCLUSIONS,
                        TextUtils.join(SEPARATOR, mExcludedCalendarIds))
                .apply();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawer)) {
            mDrawerLayout.closeDrawer(mDrawer);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_CALENDAR:
                if (checkCalendarPermissions()) {
                    toggleEmptyView(false);
                    loadEvents();
                } else {
                    toggleEmptyView(true);
                }
                break;
            case REQUEST_CODE_LOCATION:
                if (checkLocationPermissions()) {
                } else {
                    explainLocationPermissions();
                }
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String[] selectionArgs = null;
        if (id == LOADER_LOCAL_CALENDAR) {
            selection = CalendarContract.Calendars.ACCOUNT_TYPE + "=?";
            selectionArgs = new String[]{String.valueOf(CalendarContract.ACCOUNT_TYPE_LOCAL)};
        }
        return new CursorLoader(this,
                CalendarContract.Calendars.CONTENT_URI,
                CalendarCursor.PROJECTION, selection, selectionArgs,
                CalendarContract.Calendars.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_CALENDARS:
                if (data != null && data.moveToFirst()) {
                    mCalendarSelectionView.swapCursor(new CalendarCursor(data), mExcludedCalendarIds);
                }
                break;
            case LOADER_LOCAL_CALENDAR:
                if (data == null || data.getCount() == 0) {
                    createLocalCalendar();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCalendarSelectionView.swapCursor(null, null);
    }


    private void setUpContentView() {
        mCoordinatorLayout = findViewById(R.id.coordinator_layout);
        mCalendarSelectionView = (CalendarSelectionView) findViewById(R.id.list_view_calendar);
        //noinspection ConstantConditions
        mCalendarSelectionView.setOnSelectionChangeListener(mCalendarSelectionListener);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = findViewById(R.id.drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.open_drawer, R.string.close_drawer);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mToolbarToggle = (CheckedTextView) findViewById(R.id.toolbar_toggle);
        View toggleButton = findViewById(R.id.toolbar_toggle_frame);
        if (toggleButton != null) { // can be null as disabled in landscape
            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mToolbarToggle.toggle();
                    toggleCalendarView();
                }
            });
        }
        mCalendarView = (EventCalendarView) findViewById(R.id.calendar_view);
        mTodayView = (ToDayView) findViewById(R.id.agenda_view);
        mFabAdd = (FloatingActionButton) findViewById(R.id.fab);
        mFabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEvent();
            }
        });
        //noinspection ConstantConditions
        mFabAdd.hide();
    }

    private void toggleCalendarView() {
        if (mToolbarToggle.isChecked()) {
            mCalendarView.setVisibility(View.VISIBLE);
        } else {
            mCalendarView.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void toggleEmptyView(boolean visible) {
        if (visible) {
            findViewById(R.id.empty).setVisibility(View.VISIBLE);
            findViewById(R.id.empty).bringToFront();
            findViewById(R.id.button_permission)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestCalendarPermissions();
                        }
                    });
        } else {
            findViewById(R.id.empty).setVisibility(View.GONE);
        }
    }

    private void changeWeekStart(@IdRes int selection) {
        switch (selection) {
            case R.id.action_week_start_saturday:
                CalendarUtils.sWeekStart = Calendar.SATURDAY;
                break;
            case R.id.action_week_start_sunday:
                CalendarUtils.sWeekStart = Calendar.SUNDAY;
                break;
            case R.id.action_week_start_monday:
                CalendarUtils.sWeekStart = Calendar.MONDAY;
                break;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(CalendarUtils.PREF_WEEK_START, CalendarUtils.sWeekStart)
                .apply();
        supportInvalidateOptionsMenu();
        mCoordinator.reset();
    }

    private void createEvent() {
        startActivity(new Intent(this, EditActivity.class));
    }

    private void loadEvents() {
        getSupportLoaderManager().initLoader(LOADER_CALENDARS, null, this);
        getSupportLoaderManager().initLoader(LOADER_LOCAL_CALENDAR, null, this);
        mFabAdd.show();
        mCalendarView.setCalendarAdapter(new CalendarCursorAdapter(this, mExcludedCalendarIds));
        mTodayView.setAdapter(new ToDayCursorAdapter(this, mExcludedCalendarIds));
    }



    private void createLocalCalendar() {
        String name = getString(R.string.default_calendar_name);
        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Calendars.ACCOUNT_NAME, BuildConfig.APPLICATION_ID);
        cv.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        cv.put(CalendarContract.Calendars.NAME, name);
        cv.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name);
        cv.put(CalendarContract.Calendars.CALENDAR_COLOR, 0);
        cv.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                CalendarContract.Calendars.CAL_ACCESS_OWNER);
        cv.put(CalendarContract.Calendars.OWNER_ACCOUNT, BuildConfig.APPLICATION_ID);
        new CalendarQueryHandler(getContentResolver())
                .startInsert(0, null, CalendarContract.Calendars.CONTENT_URI
                                .buildUpon()
                                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "1")
                                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME,
                                        BuildConfig.APPLICATION_ID)
                                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE,
                                        CalendarContract.ACCOUNT_TYPE_LOCAL)
                                .build()
                        , cv);
    }

    @VisibleForTesting
    protected boolean checkCalendarPermissions() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) |
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)) ==
                PackageManager.PERMISSION_GRANTED;
    }

    @VisibleForTesting
    protected boolean checkLocationPermissions() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) |
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) ==
                PackageManager.PERMISSION_GRANTED;
    }

    @VisibleForTesting
    protected void requestCalendarPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR},
                REQUEST_CODE_CALENDAR);
    }

    @VisibleForTesting
    protected void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_CODE_LOCATION);
    }

    private void explainLocationPermissions() {
        Snackbar.make(mCoordinatorLayout, R.string.location_permission_required,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.grant_access, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestLocationPermissions();
                    }
                })
                .show();
    }

    /**
     * Coordinator utility that synchronizes widgets as selected date changes
     */
    static class Coordinator {
        private static final String STATE_SELECTED_DATE = "state:selectedDate";

        private final EventCalendarView.OnChangeListener mCalendarListener
                = new EventCalendarView.OnChangeListener() {
            @Override
            public void onSelectedDayChange(long calendarDate) {
                sync(calendarDate, mCalendarView);
            }
        };
        private final ToDayView.OnDateChangeListener mAgendaListener
                = new ToDayView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(long dayMillis) {
                sync(dayMillis, mTodayView);
            }
        };
        private TextView mTextView;
        private EventCalendarView mCalendarView;
        private ToDayView mTodayView;
        private long mSelectedDayMillis = CalendarUtils.NO_TIME_MILLIS;

        /**
         * Set up widgets to be synchronized
         * @param textView      title
         * @param calendarView  calendar view
         * @param ToDayView    agenda view
         */
        public void coordinate(@NonNull TextView textView,
                               @NonNull EventCalendarView calendarView,
                               @NonNull ToDayView ToDayView) {
            if (mCalendarView != null) {
                mCalendarView.setOnChangeListener(null);
            }
            if (mTodayView != null) {
                mTodayView.setOnDateChangeListener(null);
            }
            mTextView = textView;
            mCalendarView = calendarView;
            mTodayView = ToDayView;
            if (mSelectedDayMillis < 0) {
                mSelectedDayMillis = CalendarUtils.today();
            }
            mCalendarView.setSelectedDay(mSelectedDayMillis);
            ToDayView.setSelectedDay(mSelectedDayMillis);
            updateTitle(mSelectedDayMillis);
            calendarView.setOnChangeListener(mCalendarListener);
            ToDayView.setOnDateChangeListener(mAgendaListener);
        }

        void saveState(Bundle outState) {
            outState.putLong(STATE_SELECTED_DATE, mSelectedDayMillis);
        }

        void restoreState(Bundle savedState) {
            mSelectedDayMillis = savedState.getLong(STATE_SELECTED_DATE,
                    CalendarUtils.NO_TIME_MILLIS);
        }

        void reset() {
            mSelectedDayMillis = CalendarUtils.today();
            if (mCalendarView != null) {
                mCalendarView.reset();
            }
            if (mTodayView != null) {
                mTodayView.reset();
            }
            updateTitle(mSelectedDayMillis);
        }

        private void sync(long dayMillis, View originator) {
            mSelectedDayMillis = dayMillis;
            if (originator != mCalendarView) {
                mCalendarView.setSelectedDay(dayMillis);
            }
            if (originator != mTodayView) {
                mTodayView.setSelectedDay(dayMillis);
            }
            updateTitle(dayMillis);
        }

        private void updateTitle(long dayMillis) {
            mTextView.setText(CalendarUtils.toMonthString(mTextView.getContext(), dayMillis));
        }
    }

    static class ToDayCursorAdapter extends ToDayAdapter {

        @VisibleForTesting
        final DayEventsQueryHandler mHandler;

        public ToDayCursorAdapter(Context context, Collection<String> excludedCalendarIds) {
            super(context);
            mHandler = new DayEventsQueryHandler(context.getContentResolver(), this,
                    excludedCalendarIds);
        }

        @Override
        protected void loadEvents(long timeMillis) {
            mHandler.startQuery(timeMillis, timeMillis, timeMillis + DateUtils.DAY_IN_MILLIS);
        }
    }

    static class CalendarCursorAdapter extends EventCalendarView.CalendarAdapter {
        private final MonthEventsQueryHandler mHandler;

        public CalendarCursorAdapter(Context context, Collection<String> excludedCalendarIds) {
            mHandler = new MonthEventsQueryHandler(context.getContentResolver(), this,
                    excludedCalendarIds);
        }

        @Override
        protected void loadEvents(long monthMillis) {
            long startTimeMillis = CalendarUtils.monthFirstDay(monthMillis),
                    endTimeMillis = startTimeMillis + DateUtils.DAY_IN_MILLIS *
                            CalendarUtils.monthSize(monthMillis);
            mHandler.startQuery(monthMillis, startTimeMillis, endTimeMillis);
        }
    }

    static class DayEventsQueryHandler extends EventsQueryHandler {

        private final ToDayCursorAdapter mAgendaCursorAdapter;

        public DayEventsQueryHandler(ContentResolver cr,
                                     ToDayCursorAdapter agendaCursorAdapter,
                                     @NonNull Collection<String> excludedCalendarIds) {
            super(cr, excludedCalendarIds);
            mAgendaCursorAdapter = agendaCursorAdapter;
        }

        @Override
        protected void handleQueryComplete(int token, Object cookie, EventCursor cursor) {
            mAgendaCursorAdapter.bindEvents((Long) cookie, cursor);
        }
    }

    static class MonthEventsQueryHandler extends EventsQueryHandler {

        private final CalendarCursorAdapter mAdapter;

        public MonthEventsQueryHandler(ContentResolver cr,
                                       CalendarCursorAdapter adapter,
                                       @NonNull Collection<String> excludedCalendarIds) {
            super(cr, excludedCalendarIds);
            mAdapter = adapter;
        }

        @Override
        protected void handleQueryComplete(int token, Object cookie, EventCursor cursor) {
            mAdapter.bindEvents((Long) cookie, cursor);
        }
    }

    static class CalendarQueryHandler extends AsyncQueryHandler {

        public CalendarQueryHandler(ContentResolver cr) {
            super(cr);
        }
    }
}
