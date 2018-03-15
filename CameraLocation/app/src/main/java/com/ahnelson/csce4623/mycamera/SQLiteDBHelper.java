package com.ahnelson.csce4623.mycamera;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Russ on 11/1/17.
 */

public class SQLiteDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "photo_database";
    public static final String PHOTO_TABLE_NAME = "photo";
    public static final String PHOTO_COLUMN_ID = "_id";
    public static final String PHOTO_COLUMN_URL = "url";
    public static final String PHOTO_COLUMN_LAT = "latitude";
    public static final String PHOTO_COLUMN_LON = "longitude";

    public SQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        /*UNCOMMENT THIS IF YOU WANT TO CLEAR THE DATABASE*/
        //context.deleteDatabase(DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE " + PHOTO_TABLE_NAME + " (" +
                PHOTO_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PHOTO_COLUMN_URL + " VARCHAR, " +
                PHOTO_COLUMN_LAT + " DOUBLE, " +
                PHOTO_COLUMN_LON + " DOUBLE " + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PHOTO_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}