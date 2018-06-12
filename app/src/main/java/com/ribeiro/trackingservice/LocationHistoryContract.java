package com.ribeiro.trackingservice;

import android.provider.BaseColumns;

public final class LocationHistoryContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private LocationHistoryContract(){}
    /* Inner class that defines the table contents */
    public static class LocationHistory implements BaseColumns {
        public static final String TABLE_NAME = "LocationHistory";
        public static final String COLUMN_NAME_LOCATIONHISTORYID = "LocationHistoryId";
        public static final String COLUMN_NAME_LOCATIONHISTORYDEVICEID = "LocationHistoryDeviceId";
        public static final String COLUMN_NAME_LOCATIONHISTORYDATETIME = "LocationHistoryDateTime";
        public static final String COLUMN_NAME_LOCATIONHISTORYLATITUDE = "LocationHistoryLatitude";
        public static final String COLUMN_NAME_LOCATIONHISTORYLONGITUDE = "LocationHistoryLongitude";
        public static final String COLUMN_NAME_LOCATIONHISTORYMESSAGE = "LocationHistoryMessage";
    }
    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + LocationHistory.TABLE_NAME + " (" +
                    LocationHistory.COLUMN_NAME_LOCATIONHISTORYID + " TEXT PRIMARY KEY," +
                    LocationHistory.COLUMN_NAME_LOCATIONHISTORYDEVICEID + " TEXT," +
                    LocationHistory.COLUMN_NAME_LOCATIONHISTORYDATETIME + " TEXT," +
                    LocationHistory.COLUMN_NAME_LOCATIONHISTORYLATITUDE + " TEXT," +
                    LocationHistory.COLUMN_NAME_LOCATIONHISTORYLONGITUDE + " TEXT," +
                    LocationHistory.COLUMN_NAME_LOCATIONHISTORYMESSAGE + " TEXT)";

    static final String SQL_DROPTABLE =
            "DROP TABLE IF EXISTS " + LocationHistory.TABLE_NAME;

    static final String SQL_GETALL_ENTRIES =
            //"SELECT * FROM " + LocationHistory.TABLE_NAME;
    "SELECT * FROM " + LocationHistory.TABLE_NAME
    + " ORDER BY " + LocationHistory.COLUMN_NAME_LOCATIONHISTORYDATETIME + " DESC";

    static final String SQL_DELETEALL_ENTRIES =
            "DELETE FROM " + LocationHistory.TABLE_NAME;

    static final String SQL_DELETEONE_ENTRIES =
            "DELETE FROM " + LocationHistory.TABLE_NAME + " WHERE " + LocationHistory.COLUMN_NAME_LOCATIONHISTORYID ;
}
