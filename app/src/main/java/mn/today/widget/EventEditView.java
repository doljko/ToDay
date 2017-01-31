package mn.today.widget;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.TimeZone;

import mn.today.CalendarUtils;
import mn.today.R;
import mn.today.ViewUtils;
import mn.today.content.CalendarCursor;

/**
 * Created by Tortuvshin Byambaa on 1/31/2017.
 */

public class EventEditView extends RelativeLayout {

    private final TextInputLayout mTextInputTitle;
    private final EditText mEditTextTitle;
    private final SwitchCompat mSwitchAllDay;
    private final TextView mTextViewStartDate;
    private final TextView mTextViewStartTime;
    private final TextView mTextViewEndDate;
    private final TextView mTextViewEndTime;
    private final TextView mTextViewCalendar;
    private final int[] mColors;
    private final int mTransparentColor;
    private Event mEvent = Event.createInstance();
    private CalendarCursor mCursor;

    public EventEditView(Context context) {
        this(context, null);
    }

    public EventEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.event_edit_view, this);
        int horizontalPadding = context.getResources()
                .getDimensionPixelSize(R.dimen.horizontal_padding),
                verticalPadding = context.getResources()
                        .getDimensionPixelSize(R.dimen.vertical_padding);
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        mTextInputTitle = (TextInputLayout) findViewById(R.id.text_input_title);
        mEditTextTitle = (EditText) findViewById(R.id.edit_text_title);
        mSwitchAllDay = (SwitchCompat) findViewById(R.id.switch_all_day);
        mTextViewStartDate = (TextView) findViewById(R.id.text_view_start_date);
        mTextViewStartTime = (TextView) findViewById(R.id.text_view_start_time);
        mTextViewEndDate = (TextView) findViewById(R.id.text_view_end_date);
        mTextViewEndTime = (TextView) findViewById(R.id.text_view_end_time);
        mTextViewCalendar = (TextView) findViewById(R.id.text_view_calendar);
        mTransparentColor = ContextCompat.getColor(context, android.R.color.transparent);
        if (isInEditMode()) {
            mColors = new int[]{mTransparentColor};
        } else {
            mColors = ViewUtils.getCalendarColors(context);
        }
        setupViews();
        setEvent(mEvent);
    }

    /**
     * Sets view model for this view
     * @param event    view model representing event to edit
     */
    public void setEvent(@NonNull Event event) {
        mEvent = event;
        mEditTextTitle.setText(event.title);
        mEditTextTitle.setSelection(mEditTextTitle.length());
        mSwitchAllDay.setChecked(event.isAllDay);
        setCalendarId(mEvent.calendarId);
        setDate(true);
        setDate(false);
        setTime(true);
        setTime(false);
    }

    /**
     * Gets view model representing event being edited
     * @return  view model representing editing event
     */
    @NonNull
    public Event getEvent() {
        return mEvent;
    }

    /**
     * Sets data source for calendars from {@link android.provider.CalendarContract.Calendars}
     * @param cursor    cursor to access list of calendars
     */
    public void swapCalendarSource(CalendarCursor cursor) {
        mCursor = cursor;
        mTextViewCalendar.setEnabled(mCursor != null && mCursor.getCount() > 0);
    }

    /**
     * Sets name of selected event calendar
     * @param calendarName    selected calendar name
     */
    public void setSelectedCalendar(String calendarName) {
        mTextViewCalendar.setText(calendarName);
    }

    private void setupViews() {
        mTextInputTitle.setErrorEnabled(true);
        mEditTextTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEvent.title = s != null ? s.toString() : "";
                mTextInputTitle.setError(TextUtils.isEmpty(s) ?
                        getResources().getString(R.string.warning_empty_title) : null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no op
            }
        });
        findViewById(R.id.text_view_all_day).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitchAllDay.toggle();
            }
        });
        mSwitchAllDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mEvent.isAllDay == isChecked) {
                    return;
                }
                mEvent.setIsAllDay(isChecked);
                if (isChecked) {
                    setDate(true);
                    setDate(false);
                    setTime(true);
                    setTime(false);
                }
            }
        });
        mTextViewStartDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(true);
            }
        });
        mTextViewEndDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(false);
            }
        });
        mTextViewStartTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(true);
            }
        });
        mTextViewEndTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(false);
            }
        });
        mTextViewCalendar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendarPicker();
            }
        });
        mTextViewCalendar.setEnabled(false);
    }

    private void setDate(boolean startDate) {
        TextView label = startDate ? mTextViewStartDate : mTextViewEndDate;
        Calendar dateTime = startDate ? mEvent.localStart : mEvent.localEnd;
        label.setText(CalendarUtils.toDayString(getContext(), dateTime.getTimeInMillis()));
        ensureValidDates(startDate);
    }

    private void setTime(boolean startTime) {
        TextView label = startTime ? mTextViewStartTime : mTextViewEndTime;
        Calendar dateTime = startTime ? mEvent.localStart : mEvent.localEnd;
        label.setText(CalendarUtils.toTimeString(getContext(), dateTime.getTimeInMillis()));
        ensureValidTimes(startTime);
    }

    @VisibleForTesting
    void changeCalendar(int selection) {
        mCursor.moveToPosition(selection);
        mTextViewCalendar.setText(mCursor.getDisplayName());
        setCalendarId(mCursor.getId());
    }

    private void setCalendarId(long calendarId) {
        mEvent.calendarId = calendarId;
        if (calendarId == Event.NO_ID) {
            setBackgroundColor(mTransparentColor);
        } else {
            setBackgroundColor(mColors[(int) (Math.abs(mEvent.calendarId) % mColors.length)]);
        }
    }

    private void ensureValidDates(boolean startDateChanged) {
        if (startDateChanged) {
            if (mEvent.localStart.after(mEvent.localEnd)) {
                mEvent.localEnd.setTimeInMillis(mEvent.localStart.getTimeInMillis());
                setDate(false);
                setTime(false);
            }
        } else {
            if (mEvent.localEnd.before(mEvent.localStart)) {
                mEvent.localStart.setTimeInMillis(mEvent.localEnd.getTimeInMillis());
                setDate(true);
                setTime(true);
            }
        }
    }

    private void ensureValidTimes(boolean startTimeChanged) {
        if (startTimeChanged) {
            if (mEvent.localStart.after(mEvent.localEnd)) {
                mEvent.localEnd.setTimeInMillis(mEvent.localStart.getTimeInMillis());
                setTime(false);
            }
        } else {
            if (mEvent.localEnd.before(mEvent.localStart)) {
                mEvent.localStart.setTimeInMillis(mEvent.localEnd.getTimeInMillis());
                setTime(true);
            }
        }
    }

    private void showDatePicker(final boolean startDate) {
        final Calendar dateTime = startDate ? mEvent.localStart : mEvent.localEnd;
        new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        dateTime.set(year, monthOfYear, dayOfMonth);
                        mSwitchAllDay.setChecked(false);
                        setDate(startDate);
                    }
                },
                dateTime.get(Calendar.YEAR),
                dateTime.get(Calendar.MONTH),
                dateTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker(final boolean startTime) {
        final Calendar dateTime = startTime ? mEvent.localStart : mEvent.localEnd;
        new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        dateTime.set(Calendar.MINUTE, minute);
                        mSwitchAllDay.setChecked(false);
                        setTime(startTime);
                    }
                },
                dateTime.get(Calendar.HOUR_OF_DAY),
                dateTime.get(Calendar.MINUTE),
                false)
                .show();
    }

    private void showCalendarPicker() {
        new AlertDialog.Builder(getContext())
                .setCursor(mCursor, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeCalendar(which);
                    }
                }, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    /**
     * View model for {@link EventEditView}.
     * Event represented by this class is assumed to be in system timezone.
     */
    public static class Event implements Parcelable {

        public static final Creator<Event> CREATOR = new Creator<Event>() {
            @Override
            public Event createFromParcel(Parcel in) {
                return new Event(in);
            }

            @Override
            public Event[] newArray(int size) {
                return new Event[size];
            }
        };

        /**
         * Creates an instance of {@link Event} that starts at 'earliest' future time
         * @return  an {@link Event} instance
         */
        public static Event createInstance() {
            return new Event();
        }

        /**
         * Builder utility to build an {@link Event}
         */
        public static class Builder {
            private final Event event = new Event();

            /**
             * Sets event ID
             * @param id    event ID
             * @return  this instance (fluent API)
             */
            public Builder id(long id) {
                event.id = id;
                return this;
            }

            /**
             * Sets event calendar ID
             * @param calendarId    event calendar ID
             * @return  this instance (fluent API)
             */
            public Builder calendarId(long calendarId) {
                event.calendarId = calendarId;
                return this;
            }

            /**
             * Sets event title
             * @param title    event title
             * @return  this instance (fluent API)
             */
            public Builder title(String title) {
                event.title = title;
                return this;
            }

            /**
             * Sets event start date time
             * @param timeMillis    start date time in milliseconds
             * @return  this instance (fluent API)
             */
            public Builder start(long timeMillis) {
                event.localStart.setTimeInMillis(timeMillis);
                return this;
            }

            /**
             * Sets event end date time
             * @param timeMillis    end date time in milliseconds
             * @return  this instance (fluent API)
             */
            public Builder end(long timeMillis) {
                event.localEnd.setTimeInMillis(timeMillis);
                return this;
            }

            /**
             * Sets event all day status
             * @param isAllDay    true if event is all day, false otherwise
             * @return  this instance (fluent API)
             */
            public Builder allDay(boolean isAllDay) {
                event.isAllDay = isAllDay;
                return this;
            }

            /**
             * Creates the {@link Event} that has been built by this builder
             * @return  an {@link Event} instance
             */
            public Event build() {
                return event;
            }
        }

        private static final long NO_ID = -1;

        long id = NO_ID;
        long calendarId = NO_ID;
        String title;
        boolean isAllDay = false;
        final Calendar localStart = Calendar.getInstance();
        final Calendar localEnd = Calendar.getInstance();

        Event() {
            localStart.add(Calendar.HOUR_OF_DAY, 1);
            localStart.set(Calendar.MINUTE, 0);
            localEnd.add(Calendar.HOUR_OF_DAY, 2);
            localEnd.set(Calendar.MINUTE, 0);
        }

        Event(Parcel in) {
            id = in.readLong();
            calendarId = in.readLong();
            title = in.readString();
            isAllDay = in.readByte() != 0;
            localStart.setTimeInMillis(in.readLong());
            localEnd.setTimeInMillis(in.readLong());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeLong(calendarId);
            dest.writeString(title);
            dest.writeByte((byte) (isAllDay ? 1 : 0));
            dest.writeLong(localStart.getTimeInMillis());
            dest.writeLong(localEnd.getTimeInMillis());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * Gets event ID
         * @return  event ID
         */
        public long getId() {
            return id;
        }

        /**
         * Checks if this instance has event ID
         * @return  true for existing events, false otherwise
         */
        public boolean hasId() {
            return id != NO_ID;
        }

        /**
         * Checks if this instance has calendar ID
         * @return  true if have calendar ID, false otherwise
         */
        public boolean hasCalendarId() {
            return calendarId != NO_ID;
        }

        /**
         * Checks if event has non-empty title
         * @return  true if has non-empty title, false otherwise
         */
        public boolean hasTitle() {
            return !TextUtils.isEmpty(title);
        }

        /**
         * Gets event title
         * @return  event title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Gets event start date time
         * @return  start date time in local timezone, or midnight UTC if event is all day
         */
        public long getStartDateTime() {
            if (isAllDay) {
                return CalendarUtils.toUtcTimeZone(localStart.getTimeInMillis());
            } else {
                return localStart.getTimeInMillis();
            }
        }

        /**
         * Gets event end date time
         * @return  end date time in local timezone, or midnight UTC if event is all day
         */
        public long getEndDateTime() {
            if (isAllDay) {
                return CalendarUtils.toUtcTimeZone(localEnd.getTimeInMillis());
            } else {
                return localEnd.getTimeInMillis();
            }
        }

        /**
         * Checks if event is all day
         * @return  true if event is all day, false otherwise
         */
        public boolean isAllDay() {
            return isAllDay;
        }

        /**
         * Gets event timezone
         * @return  local system timezone ID, or UTC if event is all day
         */
        public String getTimeZone() {
            return isAllDay ? CalendarUtils.TIMEZONE_UTC : TimeZone.getDefault().getID();
        }

        /**
         * Gets event calendar ID
         * @return  event calendar ID
         */
        public long getCalendarId() {
            return calendarId;
        }

        void setIsAllDay(boolean isAllDay) {
            this.isAllDay = isAllDay;
            if (isAllDay) {
                localStart.set(Calendar.HOUR_OF_DAY, 0);
                localStart.set(Calendar.MINUTE, 0);
                localEnd.set(Calendar.HOUR_OF_DAY, 0);
                localEnd.set(Calendar.MINUTE, 0);
                if (localEnd.equals(localStart)) {
                    localEnd.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
        }
    }
}
