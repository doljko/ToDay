package mn.today.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

/**
 * Created by Doljko on 1/27/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper{
    private Context myContext;

    private static final String TAG = "DatabaseHandler : ";
    private static final int    DATABASE_VERSION = 1;
    private static final String DATABASE_NAME    = "today.db";

    private static final String CREATE_TABLE_TODO = "CREATE TABLE todo (" +
            ToDoAdapter.TODO_ID + " INTEGER PRIMARY KEY," +
            ToDoAdapter.TODO_TITLE + " TEXT," +
            ToDoAdapter.TODO_DESCRIPTION + " TEXT," +
            ToDoAdapter.TODO_START_DATE + " TEXT," +
            ToDoAdapter.TODO_END_DATE + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_TABLE_TODO);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(ToDoAdapter.TABLE_TODO);
        onCreate(db);
    }

    public void dropTable(String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        if (db == null || TextUtils.isEmpty(tableName)) {
            return;
        }
        db.execSQL("DROP TABLE IF EXISTS " + ToDoAdapter.TABLE_TODO);
    }
}
