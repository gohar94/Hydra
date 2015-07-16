package com.example.gohar.hydra;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.gohar.hydra.data.ResultsContract;
import com.example.gohar.hydra.data.ResultsDBHelper;

/**
 * Created by Gohar on 16/07/15.
 */
public class TestDB extends AndroidTestCase {
    public static final String LOG_TAG = TestDB.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(ResultsDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new ResultsDBHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        // Test data we're going to insert into the DB to see if it works.
        String testCityName = "Lahore";
        double testLatitude = 31.2;
        double testLongitude = 74.353;

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        ResultsDBHelper dbHelper = new ResultsDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ResultsContract.LocationEntry.COLUMN_CITY_NAME, testCityName);
        values.put(ResultsContract.LocationEntry.COLUMN_LATITUDE, testLatitude);
        values.put(ResultsContract.LocationEntry.COLUMN_LONGITUDE, testLongitude);

        long locationRowId;
        locationRowId = db.insert(ResultsContract.LocationEntry.TABLE_NAME, null, values);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Specify which columns you want.
        String[] columns = {
                ResultsContract.LocationEntry._ID,
                ResultsContract.LocationEntry.COLUMN_CITY_NAME,
                ResultsContract.LocationEntry.COLUMN_LATITUDE,
                ResultsContract.LocationEntry.COLUMN_LONGITUDE
        };

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                ResultsContract.LocationEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // If possible, move to the first row of the query results.
        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex((ResultsContract.LocationEntry.COLUMN_CITY_NAME));
            String name = cursor.getString(nameIndex);

            int latIndex = cursor.getColumnIndex((ResultsContract.LocationEntry.COLUMN_LATITUDE));
            double latitude = cursor.getDouble(latIndex);

            int longIndex = cursor.getColumnIndex((ResultsContract.LocationEntry.COLUMN_LONGITUDE));
            double longitude = cursor.getDouble(longIndex);

            // Hooray, data was returned!  Assert that it's the right data, and that the database
            // creation code is working as intended.
            assertEquals(testCityName, name);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);
        } else {
            // That's weird
            fail("No values returned :(");
        }

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues resultValues = new ContentValues();
        resultValues.put(ResultsContract.ResultEntry.COLUMN_LOC_KEY, locationRowId);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_START_DATE, "2014-04-06");
        resultValues.put(ResultsContract.ResultEntry.COLUMN_MIN_TEMP, 44.4);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_MAX_TEMP, 54.4);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_PRECIP, 4.4);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_ACC_PRECIP, 5.4);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_ACC_PRECIP_PRIOR_YEAR, 4.3);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_ACC_PRECIP_3_YEAR_AVERAGE, 5.6);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_ACC_PRECIP_LONG_TERM_AVERAGE, 6.7);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_SOLAR, 5.7);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_MIN_HUMIDITY, 5.4);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_MAX_HUMIDITY, 6.7);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_MORN_WIND, 5.1);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_MAX_WIND, 6.1);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_GDD, 2.1);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_ACC_GDD, 3.1);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_ACC_GDD_PRIOR_YEAR, 7.6);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_ACC_GDD_3_YEAR_AVERAGE, 8.7);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_ACC_GDD_LONG_TERM_AVERAGE, 1.2);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_PET, 3.1);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_ACC_PET, 4.3);
        resultValues.put(ResultsContract.ResultEntry.COLUMN_PPET, 0.11);

        long weatherRowId = db.insert(ResultsContract.ResultEntry.TABLE_NAME, null, resultValues);
        assertTrue(weatherRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor resultCursor = db.query(
                ResultsContract.ResultEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        if (!resultCursor.moveToFirst()) {
            fail("No weather data returned!");
        }

        assertEquals(resultCursor.getInt(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_LOC_KEY)), locationRowId);
        assertEquals(resultCursor.getString(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_START_DATE)), "2014-04-06");
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_MIN_TEMP)), 44.4);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_MAX_TEMP)), 54.4);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_PRECIP)), 4.4);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_ACC_PRECIP)), 5.4);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_ACC_PRECIP_PRIOR_YEAR)), 4.3);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_ACC_PRECIP_3_YEAR_AVERAGE)), 5.6);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_ACC_PRECIP_LONG_TERM_AVERAGE)), 6.7);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_SOLAR)), 5.7);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_MIN_HUMIDITY)), 5.4);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_MAX_HUMIDITY)), 6.7);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_MORN_WIND)), 5.1);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_MAX_WIND)), 6.1);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_GDD)), 2.1);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_ACC_GDD)), 3.1);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_ACC_GDD_PRIOR_YEAR)), 7.6);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_ACC_GDD_3_YEAR_AVERAGE)), 8.7);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_ACC_GDD_LONG_TERM_AVERAGE)), 1.2);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_PET)), 3.1);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_ACC_PET)), 4.3);
        assertEquals(resultCursor.getDouble(
                resultCursor.getColumnIndex(ResultsContract.ResultEntry.COLUMN_PPET)), 0.11);

        resultCursor.close();

        dbHelper.close();
    }
}
