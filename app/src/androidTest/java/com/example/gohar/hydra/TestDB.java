package com.example.gohar.hydra;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.gohar.hydra.data.ResultContract;
import com.example.gohar.hydra.data.ResultDBHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by Gohar on 16/07/15.
 */
public class TestDB extends AndroidTestCase {
    public static final String LOG_TAG = TestDB.class.getSimpleName();
    public static String TEST_CITY_NAME = "Lahore";

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(ResultDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new ResultDBHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public static ContentValues getLocationContentValues() {
        // Test data we're going to insert into the DB to see if it works.
        double testLatitude = 31.2;
        double testLongitude = 74.3;

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ResultContract.LocationEntry.COLUMN_CITY_NAME, TEST_CITY_NAME);
        values.put(ResultContract.LocationEntry.COLUMN_LATITUDE, testLatitude);
        values.put(ResultContract.LocationEntry.COLUMN_LONGITUDE, testLongitude);
        return values;
    }

    public static ContentValues getResultContentValues(long locationRowId) {
        // Fantastic.  Now that we have a location, add some weather!
        ContentValues resultValues = new ContentValues();
        resultValues.put(ResultContract.ResultEntry.COLUMN_LOC_KEY, locationRowId);
        resultValues.put(ResultContract.ResultEntry.COLUMN_DATE, "2014-06-12");
        resultValues.put(ResultContract.ResultEntry.COLUMN_MIN_TEMP, 44.4);
        resultValues.put(ResultContract.ResultEntry.COLUMN_MAX_TEMP, 54.4);
        resultValues.put(ResultContract.ResultEntry.COLUMN_PRECIP, 4.4);
        resultValues.put(ResultContract.ResultEntry.COLUMN_ACC_PRECIP, 5.4);
        resultValues.put(ResultContract.ResultEntry.COLUMN_ACC_PRECIP_PRIOR_YEAR, 4.3);
        resultValues.put(ResultContract.ResultEntry.COLUMN_ACC_PRECIP_3_YEAR_AVERAGE, 5.6);
        resultValues.put(ResultContract.ResultEntry.COLUMN_ACC_PRECIP_LONG_TERM_AVERAGE, 6.7);
        resultValues.put(ResultContract.ResultEntry.COLUMN_SOLAR, 5.7);
        resultValues.put(ResultContract.ResultEntry.COLUMN_MIN_HUMIDITY, 5.4);
        resultValues.put(ResultContract.ResultEntry.COLUMN_MAX_HUMIDITY, 6.7);
        resultValues.put(ResultContract.ResultEntry.COLUMN_MORN_WIND, 5.1);
        resultValues.put(ResultContract.ResultEntry.COLUMN_MAX_WIND, 6.1);
        resultValues.put(ResultContract.ResultEntry.COLUMN_GDD, 2.1);
        resultValues.put(ResultContract.ResultEntry.COLUMN_ACC_GDD, 3.1);
        resultValues.put(ResultContract.ResultEntry.COLUMN_ACC_GDD_PRIOR_YEAR, 7.6);
        resultValues.put(ResultContract.ResultEntry.COLUMN_ACC_GDD_3_YEAR_AVERAGE, 8.7);
        resultValues.put(ResultContract.ResultEntry.COLUMN_ACC_GDD_LONG_TERM_AVERAGE, 1.2);
        resultValues.put(ResultContract.ResultEntry.COLUMN_PET, 3.1);
        resultValues.put(ResultContract.ResultEntry.COLUMN_ACC_PET, 4.3);
        resultValues.put(ResultContract.ResultEntry.COLUMN_PPET, 0.11);
        return resultValues;
    }

    public void testInsertReadDb() {
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        ResultDBHelper dbHelper = new ResultDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = getLocationContentValues();

        long locationRowId;
        locationRowId = db.insert(ResultContract.LocationEntry.TABLE_NAME, null, values);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                ResultContract.LocationEntry.TABLE_NAME,  // Table to Query
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(values, cursor);

        ContentValues resultValues = getResultContentValues(locationRowId);
        long weatherRowId = db.insert(ResultContract.ResultEntry.TABLE_NAME, null, resultValues);
        assertTrue(weatherRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor resultCursor = db.query(
                ResultContract.ResultEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        validateCursor(resultValues, resultCursor);

        resultCursor.close();

        dbHelper.close();
    }

    public static void validateCursor(ContentValues expectedValues, Cursor valueCursor) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}
