package com.example.gohar.hydra.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Gohar on 16/07/15.
 */
public class ResultDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "results.db";

    public ResultDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + ResultContract.LocationEntry.TABLE_NAME + " (" +
                ResultContract.LocationEntry._ID + " INTEGER PRIMARY KEY," +
                ResultContract.LocationEntry.COLUMN_CITY_NAME + " TEXT, " +
                ResultContract.LocationEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                ResultContract.LocationEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
                "UNIQUE (" + ResultContract.LocationEntry.COLUMN_LATITUDE + ", " + ResultContract.LocationEntry.COLUMN_LONGITUDE + ") ON CONFLICT IGNORE"+
                " );";

        final String SQL_CREATE_RESULTS_TABLE = "CREATE TABLE " + ResultContract.ResultEntry.TABLE_NAME + " (" +
                ResultContract.ResultEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                ResultContract.ResultEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                ResultContract.ResultEntry.COLUMN_DATE + " TEXT NOT NULL, " +

                ResultContract.ResultEntry.COLUMN_MIN_TEMP + " REAL, " +
                ResultContract.ResultEntry.COLUMN_MAX_TEMP + " REAL, " +

                ResultContract.ResultEntry.COLUMN_PRECIP + " REAL, " +
                ResultContract.ResultEntry.COLUMN_ACC_PRECIP + " REAL, " +
                ResultContract.ResultEntry.COLUMN_ACC_PRECIP_PRIOR_YEAR + " REAL, " +
                ResultContract.ResultEntry.COLUMN_ACC_PRECIP_3_YEAR_AVERAGE + " REAL, " +
                ResultContract.ResultEntry.COLUMN_ACC_PRECIP_LONG_TERM_AVERAGE + " REAL, " +

                ResultContract.ResultEntry.COLUMN_SOLAR + " REAL, " +

                ResultContract.ResultEntry.COLUMN_MIN_HUMIDITY + " REAL, " +
                ResultContract.ResultEntry.COLUMN_MAX_HUMIDITY + " REAL, " +

                ResultContract.ResultEntry.COLUMN_MORN_WIND + " REAL, " +
                ResultContract.ResultEntry.COLUMN_MAX_WIND + " REAL, " +

                ResultContract.ResultEntry.COLUMN_GDD + " REAL, " +
                ResultContract.ResultEntry.COLUMN_ACC_GDD + " REAL, " +
                ResultContract.ResultEntry.COLUMN_ACC_GDD_PRIOR_YEAR + " REAL, " +
                ResultContract.ResultEntry.COLUMN_ACC_GDD_3_YEAR_AVERAGE + " REAL, " +
                ResultContract.ResultEntry.COLUMN_ACC_GDD_LONG_TERM_AVERAGE + " REAL, " +

                ResultContract.ResultEntry.COLUMN_PET + " REAL, " +
                ResultContract.ResultEntry.COLUMN_ACC_PET + " REAL, " +

                ResultContract.ResultEntry.COLUMN_PPET + " REAL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + ResultContract.ResultEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                ResultContract.LocationEntry.TABLE_NAME + " (" + ResultContract.LocationEntry._ID + "), " +

                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + ResultContract.ResultEntry.COLUMN_DATE + ", " +
                ResultContract.ResultEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_RESULTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ResultContract.ResultEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ResultContract.LocationEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
