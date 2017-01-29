package mn.today.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import mn.today.Model.Todo;

/**
 * Created by Doljko on 1/27/2017.
 */

public class ToDoAdapter extends DatabaseHelper{
    private static final String TAG = "TodoAdapter : ";

    public static final String TABLE_TODO   = "todo";
    public static final String TODO_ID       = "id";
    public static final String TODO_TITLE     = "title";
    public static final String TODO_DESCRIPTION    = "description";
    public static final String TODO_START_DATE = "start";
    public static final String TODO_END_DATE = "end";

    private static final int TODO_ID_INDEX       = 0;
    private static final int TODO_TITLE_INDEX     = 1;
    private static final int TODO_DESCRIPTION_INDEX    = 2;
    private static final int TODO_START_INDEX = 3;
    private static final int TODO_END_INDEX = 4;

    private static final String[] PROJECTIONS_TODO = {TODO_ID, TODO_TITLE, TODO_DESCRIPTION, TODO_START_DATE, TODO_END_DATE};

    public ToDoAdapter(Context context) {
        super(context);
    }

    public void addTodo(Todo todo) {
        if (todo == null) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        if (db == null) {
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put(TODO_TITLE, todo.getTodoTitle());
        cv.put(TODO_DESCRIPTION, todo.getTodoDescription());
        cv.put(TODO_START_DATE, todo.getTodoStart());
        cv.put(TODO_END_DATE, todo.getTodoEnd());
        // Inserting Row
        db.insert(TABLE_TODO, null, cv);
        db.close();
    }

    public Todo getTodo(int id) {
        SQLiteDatabase db = getReadableDatabase();
        if (db == null) {
            return null;
        }
        Cursor cursor = db.query(TABLE_TODO, PROJECTIONS_TODO, TODO_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (!cursor.moveToFirst()) {
            return null;
        }
        Todo todo = new Todo(cursor.getInt(TODO_ID_INDEX),
                cursor.getString(TODO_TITLE_INDEX),
                cursor.getString(TODO_DESCRIPTION_INDEX),
                cursor.getString(TODO_START_INDEX),
                cursor.getString(TODO_END_INDEX));
        cursor.close();
        return todo;
    }

    public Cursor checkTodo(String todoTitle){

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_TODO, new String[]{TODO_ID, TODO_TITLE, TODO_DESCRIPTION, TODO_START_DATE, TODO_END_DATE},
                TODO_TITLE + "='" + todoTitle + "'", null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public List<Todo> getAllTodo() {
        List<Todo> todos = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TODO;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(TODO_ID_INDEX);
                String title = cursor.getString(TODO_TITLE_INDEX);
                String description = cursor.getString(TODO_DESCRIPTION_INDEX);
                String start = cursor.getString(TODO_START_INDEX);
                String end = cursor.getString(TODO_END_INDEX);
                Todo todo = new Todo(id, title, description, start, end);
                todos.add(todo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return todos;
    }
    public int updateTodo(Todo todo) {
        if (todo == null) {
            return -1;
        }
        SQLiteDatabase db = getWritableDatabase();
        if (db == null) {
            return -1;
        }
        ContentValues cv = new ContentValues();
        cv.put(TODO_TITLE, todo.getTodoTitle());
        cv.put(TODO_DESCRIPTION, todo.getTodoDescription());
        cv.put(TODO_START_DATE, todo.getTodoStart());
        cv.put(TODO_END_DATE, todo.getTodoEnd());
        // Upating the row
        int rowCount = db.update(TABLE_TODO, cv, TODO_ID + "=?",
                new String[]{String.valueOf(todo.getTodoId())});
        db.close();
        return rowCount;
    }

    public void deleteTodo(Todo todo) {
        if (todo == null) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        if (db == null) {
            return;
        }
        db.delete(TABLE_TODO, TODO_ID + "=?", new String[]{String.valueOf(todo.getTodoId())});
        db.close();
    }

    public int getTodoCount() {
        String query = "SELECT * FROM  " + TABLE_TODO;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}
