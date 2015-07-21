package com.example.gohar.hydra;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.gohar.hydra.data.ResultContract;
import com.example.gohar.hydra.data.ResultDBHelper;

/**
 * Created by Gohar on 16/07/15.
 */
public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();
    public String TEST_CITY_NAME = "Lahore";
    public String TEST_LATITUDE = "31.2";
    public String TEST_LONGITUDE = "74.3";
    public String TEST_DATE = "2014-06-12";

    // brings our database to an empty state
    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                ResultContract.ResultEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                ResultContract.LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                ResultContract.ResultEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                ResultContract.LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp() {
        deleteAllRecords();
    }

    public void testInsertReadProvider() {
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        ResultDBHelper dbHelper = new ResultDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = TestDB.getLocationContentValues();

        Uri locationUri = mContext.getContentResolver().insert(ResultContract.LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                ResultContract.LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDB.validateCursor(values, cursor);

        cursor.close();

        ContentValues resultValues = TestDB.getResultContentValues(locationRowId);
        Uri insertUri = mContext.getContentResolver().insert(ResultContract.ResultEntry.CONTENT_URI, resultValues);

        // Now see if we can successfully query if we include the row id
        Cursor resultCursor = mContext.getContentResolver().query(
                ResultContract.ResultEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDB.validateCursor(resultValues, resultCursor);

        resultCursor.close();

        // For testing joins
        resultCursor = mContext.getContentResolver().query(
                ResultContract.ResultEntry.buildResultLocation(TEST_LATITUDE, TEST_LONGITUDE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDB.validateCursor(resultValues, resultCursor);

        resultCursor.close();

        // For testing joins
        resultCursor = mContext.getContentResolver().query(
                ResultContract.ResultEntry.buildResultLocationWithStartDate(TEST_LATITUDE, TEST_LONGITUDE, TEST_DATE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDB.validateCursor(resultValues, resultCursor);

        resultCursor.close();

        // For testing joins
        resultCursor = mContext.getContentResolver().query(
                ResultContract.ResultEntry.buildResultLocationWithDate(TEST_LATITUDE, TEST_LONGITUDE, TEST_DATE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDB.validateCursor(resultValues, resultCursor);

        resultCursor.close();

        dbHelper.close();
    }

    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestDB.getLocationContentValues();

        Uri locationUri = mContext.getContentResolver().
                insert(ResultContract.LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(ResultContract.LocationEntry._ID, locationRowId);
        updatedValues.put(ResultContract.LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(
                ResultContract.LocationEntry.CONTENT_URI, updatedValues, ResultContract.LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});

        assertEquals(count, 1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                ResultContract.LocationEntry.buildLocationUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDB.validateCursor(updatedValues, cursor);

        cursor.close();
    }

    public void testGetType() {
        // content://com.example.gohar.hydra.app/weather/
        String type = mContext.getContentResolver().getType(ResultContract.ResultEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.gohar.hydra.app/weather
        assertEquals(ResultContract.ResultEntry.CONTENT_TYPE, type);

        String testLatitude = TEST_LATITUDE;
        String testLongitude = TEST_LONGITUDE;
        // content://com.example.gohar.hydra.app/result/31.2/74.3
        type = mContext.getContentResolver().getType(
                ResultContract.ResultEntry.buildResultLocation(testLatitude, testLongitude));
        // vnd.android.cursor.dir/com.example.gohar.hydra.app/result
        assertEquals(ResultContract.ResultEntry.CONTENT_TYPE, type);

        String testDate = TEST_DATE;
        // content://com.example.gohar.hydra.app/result/31.2/74.3/2014-06-12
        type = mContext.getContentResolver().getType(
                ResultContract.ResultEntry.buildResultLocationWithDate(testLatitude, testLongitude, testDate));
        // vnd.android.cursor.item/com.example.gohar.hydra.app/weather
        assertEquals(ResultContract.ResultEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.gohar.hydra.app/location/
        type = mContext.getContentResolver().getType(ResultContract.LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.gohar.hydra.app/location
        assertEquals(ResultContract.LocationEntry.CONTENT_TYPE, type);

        // content://com.example.gohar.hydra.app/location/1
        type = mContext.getContentResolver().getType(ResultContract.LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.gohar.hydra.app/location
        assertEquals(ResultContract.LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }
}
