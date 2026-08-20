package com.jay.ai;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class JayDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "jay.db";
    private static final int DATABASE_VERSION = 1;

    public JayDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE memories (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "memory_key TEXT UNIQUE," +
                        "memory_value TEXT," +
                        "created_at INTEGER" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE conversations (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_message TEXT," +
                        "jay_response TEXT," +
                        "created_at INTEGER" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE inventory (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "part_name TEXT," +
                        "quantity INTEGER DEFAULT 0," +
                        "low_stock_threshold INTEGER DEFAULT 0" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE customers (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT," +
                        "phone TEXT" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE repairs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "customer_id INTEGER," +
                        "phone_model TEXT," +
                        "issue TEXT," +
                        "status TEXT," +
                        "price REAL," +
                        "created_at INTEGER" +
                        ")"
        );
    }

    public void saveMemory(String key, String value) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("memory_key", key);
        values.put("memory_value", value);
        values.put("created_at", System.currentTimeMillis());

        db.insertWithOnConflict(
                "memories",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public String getMemory(String key) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "memories",
                new String[]{"memory_value"},
                "memory_key = ?",
                new String[]{key},
                null,
                null,
                null
        );

        try {

            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }

            return null;

        } finally {

            cursor.close();
        }
    }

    public void saveConversation(
            String userMessage,
            String jayResponse) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("user_message", userMessage);
        values.put("jay_response", jayResponse);
        values.put("created_at", System.currentTimeMillis());

        db.insert("conversations", null, values);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        // Future migrations go here.
    }
          }
