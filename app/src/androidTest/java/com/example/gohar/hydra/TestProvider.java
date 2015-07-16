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
public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();
    public String TEST_CITY_NAME = "Lahore";

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(ResultsDBHelper.DATABASE_NAME);
    }

    public void testInsertReadProvider() {
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        ResultsDBHelper dbHelper = new ResultsDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = TestDB.getLocationContentValues();

        long locationRowId;
        locationRowId = db.insert(ResultsContract.LocationEntry.TABLE_NAME, null, values);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                ResultsContract.LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDB.validateCursor(values, cursor);

        ContentValues resultValues = TestDB.getResultContentValues(locationRowId);
        long weatherRowId = db.insert(ResultsContract.ResultEntry.TABLE_NAME, null, resultValues);
        assertTrue(weatherRowId != -1);

        // Now see if we can successfully query if we include the row id
        Cursor resultCursor = mContext.getContentResolver().query(
                ResultsContract.ResultEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDB.validateCursor(resultValues, resultCursor);

        resultCursor.close();

        dbHelper.close();
    }

    public void testGetType() {
        // content://com.example.gohar.hydra.app/weather/
        String type = mContext.getContentResolver().getType(ResultsContract.ResultEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.gohar.hydra.app/weather
        assertEquals(ResultsContract.ResultEntry.CONTENT_TYPE, type);

        String testLatitude = "31.2";
        String testLongitude = "74.3";
        // content://com.example.gohar.hydra.app/result/31.2/74.3
        type = mContext.getContentResolver().getType(
                ResultsContract.ResultEntry.buildResultLocation(testLatitude, testLongitude));
        // vnd.android.cursor.dir/com.example.gohar.hydra.app/result
        assertEquals(ResultsContract.ResultEntry.CONTENT_TYPE, type);

        String testDate = "2014-06-12";
        // content://com.example.gohar.hydra.app/result/31.2/74.3/2014-06-12
        type = mContext.getContentResolver().getType(
                ResultsContract.ResultEntry.buildResultLocationWithDate(testLatitude, testLongitude, testDate));
        // vnd.android.cursor.item/com.example.gohar.hydra.app/weather
        assertEquals(ResultsContract.ResultEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.gohar.hydra.app/location/
        type = mContext.getContentResolver().getType(ResultsContract.LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.gohar.hydra.app/location
        assertEquals(ResultsContract.LocationEntry.CONTENT_TYPE, type);

        // content://com.example.gohar.hydra.app/location/1
        type = mContext.getContentResolver().getType(ResultsContract.LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.gohar.hydra.app/location
        assertEquals(ResultsContract.LocationEntry.CONTENT_ITEM_TYPE, type);
    }
}
