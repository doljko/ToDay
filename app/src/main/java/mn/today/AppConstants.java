package mn.today;

/**
 * Created by Tortuvshin Byambaa on 2/15/2017.
 */

public class AppConstants {

    public static final String APP_DATA_MANAGER = "APP_DATA_MANAGER";
    public static final String COMPLEX_PREFS = "COMPLEX_PREFS";
    public static final String RESTORED_USER_FLOWS = "RESTORED_USER_FLOWS";
    public static final String RESTORED_DATA_MANAGER = "RESTORED_DATA_MANAGER";
    public static final String EXTRA_PASSING_UUID = "EXTRA_PASSING_UUID";
    public static final String MENU_ITEMS_NATIVE = "MENU_ITEMS_NATIVE";
    public static final String MENU_ITEMS_HIDE = "MENU_ITEMS_HIDE";
    public static final String MENU_ITEMS_PARTIAL = "menu_items_partial";
    public static final int EARLY_EXIT = -1;
    public static final int FINISHED = 1;
    public static final int NOT_FINISHED = 0;
    public static final String FS_NOTIFICATION_ACTIVE = "FS_NOTIFICATION_ACTIVE";
    public static final String FS_UI_ACTIVE = "FS_UI_ACTIVE";
    public static final String GS_MCL_CHECKABLE = "GS_MCL_CHECKABLE";
    public static final String GS_DRAG_DROP = "GS_DRAP_DROP";
    public static final String FAB_HIDE = "fab_hide";
    public static final String FAB_NATIVE = "fab_native";
    public static final String ANIMATION_ENTRY = "animation_entry";
    public static final String ANIMATION_EXIT = "animation_exit";
    public static final int FLOW_STATE_NOTIFICATION_ID = 100;

    public static final int DESIGNER_REQUEST_CODE = 111;
    public static final int FS_REQUEST_CODE = 222;
    public static final String EXTRA_FORMATTED_TIME = "extra_formatted_time";
    public static final String EXTRA_MILLIS_IN_FLOW = "extra_millis_in_flow";
    public static final String UNIT_MINUTES = "minutes";
    public static final String UNIT_HOURS = "hours";

    public static final String EXTRA_ELEMENT_PARCEL = "extra_element_parcel";
    public static final String APP_DATA_FILE_NAME = "appdata.json";
    public static final String APP_STATS_EXPORT_FILE_NAME = "stats_export.csv";
    public static final String[] EXPORT_DATA_CSV_HEADER = {
            "Flow name", "Task count", "Estimate hours", "Estimated minutes",
            "Times completed","Actual hours taken","Actual minutes","Actual seconds"
    };
    public static final String FRAGMENT_FLOW_STATE = "fragment_flow_state";
    public static final boolean FS_OVERTIME_FALSE = false;
    public static final boolean FS_OVERTIME_TRUE = true;
    public static final String EXTRA_POSITION_SELECTED = "extra_position_selected";


    public static final String STATUS_CANCELLED = "status_cancelled";
    public static final String STATUS_CONFIRM_CANCEL = "status_confirm_cancel";
    public static final String STATUS_COMMIT_EDITS = "status_commit_edits";
    public static final String STATUS_CONFIRM_EDITS = "status_confirm_edits";


    public static final String KEY_NEW_NAME = "key_new_name";
    public static final String KEY_NEW_TIME = "key_new_time";
    public static final String KEY_NEW_UNITS = "key_new_units";
    public static final String KEY_NEW_NOTES = "key_new_notes";
    public static final String ORIGINAL_TIME = "original_time";
    public static final String CHANGE_TIME = "change_time";
    public static final int EXPORT_RESOLVE_CONNECTION_REQUEST_CODE = 343;
    public static final int EXPORT_CREATOR_REQUEST_CODE = 323;
}
