package com.ribeiro.trackingservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class LocationHistoryDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "LocationHistory.db";
    private static LocationHistoryDbHelper sInstance;
    private final SQLiteDatabase db;
    static String TAG = "RIBEIROTRACKING_LocationHistoryDbHelper";

    public static synchronized LocationHistoryDbHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new LocationHistoryDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public LocationHistoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.db = getWritableDatabase();
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LocationHistoryContract.SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(LocationHistoryContract.SQL_DROPTABLE);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Provide access to our database.
     */
    public SQLiteDatabase getDb() {
        return db;
    }

    public List<LocationHistory> loadHandler() {
        List<LocationHistory> list = new ArrayList<LocationHistory>();
        String result = "";
        String query = LocationHistoryContract.SQL_GETALL_ENTRIES;
        Cursor cursor = db.rawQuery(query, null);
        //if TABLE has rows
        if (cursor.moveToFirst()) {
            //Loop through the table rows
            do {
                LocationHistory loc = new LocationHistory();
                loc.setId(cursor.getString(0));
                loc.setDeviceId(cursor.getString(1));
                loc.setDateTime(cursor.getString(2));
                loc.setLatitude(cursor.getDouble(3));
                loc.setLongitude(cursor.getDouble(4));
                loc.setMessage(cursor.getString(5));
                loc.setLocation();
                //Add movie details to list
                list.add(loc);
            } while (cursor.moveToNext());
        }
        db.close();
        return list;
    }

    public void addHandler(LocationHistory loc) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationHistoryContract.LocationHistory.COLUMN_NAME_LOCATIONHISTORYID, loc.getId());
        values.put(LocationHistoryContract.LocationHistory.COLUMN_NAME_LOCATIONHISTORYDEVICEID, loc.getDeviceId());
        values.put(LocationHistoryContract.LocationHistory.COLUMN_NAME_LOCATIONHISTORYDATETIME, loc.getDateTime());
        values.put(LocationHistoryContract.LocationHistory.COLUMN_NAME_LOCATIONHISTORYLATITUDE, loc.getLatitude().toString());
        values.put(LocationHistoryContract.LocationHistory.COLUMN_NAME_LOCATIONHISTORYLONGITUDE, loc.getLongitude().toString());
        values.put(LocationHistoryContract.LocationHistory.COLUMN_NAME_LOCATIONHISTORYMESSAGE, loc.getMessage());
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(LocationHistoryContract.LocationHistory.TABLE_NAME, null, values);
        db.close();
    }

    public boolean deleteHandler() {
        String result = "";
        String query = LocationHistoryContract.SQL_DELETEALL_ENTRIES;
        Cursor cursor = db.rawQuery(query, null);
        cursor.close();
        db.close();
        return true;
    }
    public boolean deleteOneHandler(String id) {
        String whereClause = LocationHistoryContract.LocationHistory.COLUMN_NAME_LOCATIONHISTORYID + " = \"" + id.trim() + "\"";
        return db.delete(LocationHistoryContract.LocationHistory.TABLE_NAME, whereClause, null ) > 0;
    }
}
