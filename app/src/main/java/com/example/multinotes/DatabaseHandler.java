package com.example.multinotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "multinotes";
    private static final String TABLE_NOTE = "note";
    // Tên các thuộc tính của table
    private static final String KEY_ID = "id";

    private static final String KEY_TITLE = "title";

    private static final String KEY_CONTENT = "content";
    private static final String KEY_REMINDER_DATE = "reminder_date";
    private static final String KEY_UPDATE_TIME = "update_time";
    private static final String KEY_IMAGE_PATH = "image_path";

    public DatabaseHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOTE_TABLE = "CREATE TABLE " + TABLE_NOTE + " ("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " VARCHAR,"
                + KEY_CONTENT + " TEXT,"
                + KEY_REMINDER_DATE + " TEXT,"
                + KEY_UPDATE_TIME + " TEXT, "
                + KEY_IMAGE_PATH + " TEXT)";
        db.execSQL(CREATE_NOTE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // Xóa bảng nếu nó tồn tại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);

        // Tạo lại bảng
        onCreate(db);
    }

    // Thêm note mới
    public void addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, note.getId());
        values.put(KEY_TITLE, note.getTitle());
        values.put(KEY_CONTENT, note.getContent());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        if (note.getReminderDate() != null) {
            values.put(KEY_REMINDER_DATE, dateFormat.format(note.getReminderDate()));
        }
        values.put(KEY_UPDATE_TIME, dateFormat.format(new Date()));
        values.put(KEY_IMAGE_PATH, note.getImagePath());
        db.insert(TABLE_NOTE, null, values);
        db.close();
    }
    public Note getNote(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTE, new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_REMINDER_DATE},
                KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Note note = new Note();
        note.setId(Integer.parseInt(cursor.getString(0)));
        note.setTitle(cursor.getString(1));
        note.setContent(cursor.getString(2));
        // Chuyển đổi ngày từ chuỗi sang đối tượng Date
        String dateString = cursor.getString(3);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        if (dateString != null && !dateString.isEmpty()) {
            try {
                Date reminderDate = dateFormat.parse(dateString);
                note.setReminderDate(reminderDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String updateTimeString = cursor.getString(4);
        try {
            note.setUpdateTime(dateFormat.parse(updateTimeString));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return note;
    }
    public List getAllNotes() {
        List noteList = new ArrayList<>();

        // Tạo query
        String selectQuery = "SELECT  * FROM " + TABLE_NOTE + " ORDER BY " + KEY_UPDATE_TIME + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // lặp từng hàng và thêm vào danh sách
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(Integer.parseInt(cursor.getString(0)));
                note.setTitle(cursor.getString(1));
                note.setContent(cursor.getString(2));
                String dateString = cursor.getString(3);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                if (dateString != null && !dateString.isEmpty()) {
                    try {
                        Date reminderDate = dateFormat.parse(dateString);
                        note.setReminderDate(reminderDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                String updateTimeString = cursor.getString(4);
                try {
                    note.setUpdateTime(dateFormat.parse(updateTimeString));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                note.setImagePath(cursor.getString(5));
                // Thêm liên hệ đến list
                noteList.add(note);
            } while (cursor.moveToNext());
        }
        return noteList;
    }

    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.getTitle());
        values.put(KEY_CONTENT, note.getContent());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        if (note.getReminderDate() != null) {
            values.put(KEY_REMINDER_DATE, dateFormat.format(note.getReminderDate()));
        } else {
            values.putNull(KEY_REMINDER_DATE);
        }
        values.put(KEY_UPDATE_TIME, dateFormat.format(new Date()));
        values.put(KEY_IMAGE_PATH, note.getImagePath());

        int update = db.update(TABLE_NOTE, values, KEY_ID + "=?", new String[]{String.valueOf(note.getId())});
        db.close();
        return update;
    }
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);
        onCreate(db);
        db.close();
    }
    public int deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int delete = db.delete(TABLE_NOTE, KEY_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return delete;
    }
}
