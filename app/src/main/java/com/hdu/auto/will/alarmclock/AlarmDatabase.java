package com.hdu.auto.will.alarmclock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmDatabase extends SQLiteOpenHelper {
    static final String TABLE_NAME = "alarms";
    static final String ID = "_id";
    static final String TITLE = "title";
    static final String CONTENT = "content";
    static final String HOUR = "hour";
    static final String MINUTE = "minute";
    static final String DATE_COUNT = "dateCount";
    static final String WEEK = "week";
    static final String STATE = "state";

    AlarmDatabase(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "
                + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TITLE + " TEXT NOT NULL,"
                + CONTENT + " TEXT,"
                + HOUR + " INTEGER NOT NULL,"
                + MINUTE + " INTEGER NOT NULL,"
                + DATE_COUNT + " INTEGER NOT NULL,"
                + WEEK + " TEXT,"
                + STATE + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
