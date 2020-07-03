package com.hdu.auto.will.alarmclock;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class AlarmDBHandler {
    private SQLiteDatabase database;
    private SQLiteOpenHelper dbhelper;

    private static final String[] columns = {
            AlarmDatabase.ID, AlarmDatabase.TITLE, AlarmDatabase.CONTENT, AlarmDatabase.HOUR, AlarmDatabase.MINUTE, AlarmDatabase.DATE_COUNT, AlarmDatabase.WEEK, AlarmDatabase.STATE
    };

    AlarmDBHandler(Context context) {
        dbhelper = new AlarmDatabase(context);
    }

    void Open() {
        database = dbhelper.getWritableDatabase();
    }

    void Close() {
        dbhelper.close();
    }

    void addAlarm(Alarm alarm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AlarmDatabase.TITLE, alarm.getTitle());
        contentValues.put(AlarmDatabase.CONTENT, alarm.getContent());
        contentValues.put(AlarmDatabase.HOUR, alarm.getHour());
        contentValues.put(AlarmDatabase.MINUTE, alarm.getMinute());
        contentValues.put(AlarmDatabase.DATE_COUNT, alarm.getDateCount());
        contentValues.put(AlarmDatabase.WEEK, alarm.getWeekInString());
        contentValues.put(AlarmDatabase.STATE, alarm.getIsActivated());
        long insertId = database.insert(AlarmDatabase.TABLE_NAME, null, contentValues);
        alarm.setId(insertId);
    }

    Alarm getAlarm(long id) {
        @SuppressLint("Recycle") Cursor cursor = database.query(AlarmDatabase.TABLE_NAME, columns, AlarmDatabase.ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        assert cursor != null;
        return new Alarm(cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), stringToInt(cursor.getString(6)),cursor.getInt(7));
    }

    List<Alarm> getAllAlarms() {
        @SuppressLint("Recycle") Cursor cursor = database.query(AlarmDatabase.TABLE_NAME, columns, null, null, null, null, null);
        List<Alarm> alarms = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Alarm alarm = new Alarm();
                alarm.setId(cursor.getLong(cursor.getColumnIndex(AlarmDatabase.ID)));
                alarm.setTitle(cursor.getString(cursor.getColumnIndex(AlarmDatabase.TITLE)));
                alarm.setContent(cursor.getString(cursor.getColumnIndex(AlarmDatabase.CONTENT)));
                alarm.setTime(cursor.getInt(cursor.getColumnIndex(AlarmDatabase.HOUR)), cursor.getInt(cursor.getColumnIndex(AlarmDatabase.MINUTE)));
                alarm.setDateCount(cursor.getInt(cursor.getColumnIndex(AlarmDatabase.DATE_COUNT)));
                alarm.setWeek(stringToInt(cursor.getString(cursor.getColumnIndex(AlarmDatabase.WEEK))));
                alarm.setIsActivated(cursor.getInt(cursor.getColumnIndex(AlarmDatabase.STATE)));
                alarms.add(alarm);
            }
        }
        return alarms;
    }

    void updateAlarm(Alarm alarm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AlarmDatabase.TITLE, alarm.getTitle());
        contentValues.put(AlarmDatabase.CONTENT, alarm.getContent());
        contentValues.put(AlarmDatabase.HOUR, alarm.getHour());
        contentValues.put(AlarmDatabase.MINUTE, alarm.getMinute());
        contentValues.put(AlarmDatabase.DATE_COUNT, alarm.getDateCount());
        contentValues.put(AlarmDatabase.WEEK, alarm.getWeekInString());
        contentValues.put(AlarmDatabase.STATE, alarm.getIsActivated());
        database.update(AlarmDatabase.TABLE_NAME, contentValues, AlarmDatabase.ID + "=?", new String[]{String.valueOf(alarm.getId())});
    }

    void removeAlarm(Alarm alarm) {
        database.delete(AlarmDatabase.TABLE_NAME, AlarmDatabase.ID + "=" + alarm.getId(), null);
    }

    private int[] stringToInt(String s) {
        int[] res = new int[7];
        for (int i = 0; i < 7; i++) {
            res[i] = Integer.parseInt(String.valueOf(s.charAt(i)));
        }
        return res;
    }
}
