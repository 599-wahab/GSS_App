package com.example.gss.ui.theme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DATABASE_NAME = "user_data.db";
    private static final int DATABASE_VERSION = 1;

    // Table name and column names
    private static final String TABLE_USER_DATA = "user_data";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Create table statement
    private static final String CREATE_USER_DATA_TABLE = "CREATE TABLE " +
            TABLE_USER_DATA + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_EMAIL + " TEXT," +
            COLUMN_PHONE + " TEXT," +
            COLUMN_USERNAME + " TEXT UNIQUE," + // Ensure usernames are unique
            COLUMN_PASSWORD + " TEXT" +
            ")";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_DATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the table if it exists and recreate it
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_DATA);
        onCreate(db);
    }

    // Insert user data into the database
    public long insertUserData(String email, String phone, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USER_DATA, null, values);
        db.close();
        return result;
    }

    // Check if a username is available
    public Cursor checkUsernameAvailability(String username) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USER_DATA,
                new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
}
