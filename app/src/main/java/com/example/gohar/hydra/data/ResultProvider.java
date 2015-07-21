package com.example.gohar.hydra.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Gohar on 17/07/15.
 */
public class ResultProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ResultDBHelper mOpenHelper;

    private static final int RESULT = 100;
    private static final int RESULT_WITH_LOCATION = 101;
    private static final int RESULT_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static final SQLiteQueryBuilder sResultByLocationQueryBuilder;

    static{
        sResultByLocationQueryBuilder = new SQLiteQueryBuilder();
        sResultByLocationQueryBuilder.setTables(
                ResultContract.ResultEntry.TABLE_NAME + " INNER JOIN " +
                        ResultContract.LocationEntry.TABLE_NAME +
                        " ON " + ResultContract.ResultEntry.TABLE_NAME +
                        "." + ResultContract.ResultEntry.COLUMN_LOC_KEY +
                        " = " + ResultContract.LocationEntry.TABLE_NAME +
                        "." + ResultContract.LocationEntry._ID);
    }

    private static final String sLocationSelection =
            ResultContract.LocationEntry.TABLE_NAME+
                    "." + ResultContract.LocationEntry.COLUMN_LATITUDE + " = ? AND " +
            ResultContract.LocationEntry.COLUMN_LONGITUDE + " = ? ";
    private static final String sLocationWithStartDateSelection =
            ResultContract.LocationEntry.TABLE_NAME+
                    "." + ResultContract.LocationEntry.COLUMN_LATITUDE + " = ? AND " +
                    ResultContract.LocationEntry.COLUMN_LONGITUDE + " = ? AND " +
                    ResultContract.ResultEntry.COLUMN_DATE + " >= ? ";
    private static final String sLocationWithDateSelection =
            ResultContract.LocationEntry.TABLE_NAME+
                    "." + ResultContract.LocationEntry.COLUMN_LATITUDE + " = ? AND " +
                    ResultContract.LocationEntry.COLUMN_LONGITUDE + " = ? AND " +
                    ResultContract.ResultEntry.COLUMN_DATE + " = ? ";

    private Cursor getResultByLocation(Uri uri, String[] projection, String sortOrder) {
        String latitude = ResultContract.ResultEntry.getLatitudeFromUri(uri);
        String longitude = ResultContract.ResultEntry.getLongitudeFromUri(uri);
        String startDate = ResultContract.ResultEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sLocationSelection;
            selectionArgs = new String[]{latitude, longitude};
        } else {
            selectionArgs = new String[]{latitude, longitude, startDate};
            selection = sLocationWithStartDateSelection;
        }

        return sResultByLocationQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getResultByLocationWithDate(Uri uri, String[] projection, String sortOrder) {
        String latitude = ResultContract.ResultEntry.getLatitudeFromUri(uri);
        String longitude = ResultContract.ResultEntry.getLongitudeFromUri(uri);
        String date = ResultContract.ResultEntry.getDateFromUri(uri);

        return sResultByLocationQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationWithDateSelection,
                new String[]{latitude, longitude, date},
                null,
                null,
                sortOrder
        );
    }

    private static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ResultContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, ResultContract.PATH_RESULT, RESULT);
        matcher.addURI(authority, ResultContract.PATH_RESULT + "/*/*", RESULT_WITH_LOCATION); // latitude/longitude
        matcher.addURI(authority, ResultContract.PATH_RESULT + "/*/*/*", RESULT_WITH_LOCATION_AND_DATE); // latitude/longitude/date

        matcher.addURI(authority, ResultContract.PATH_LOCATION, LOCATION);
        matcher.addURI(authority, ResultContract.PATH_LOCATION + "/#", LOCATION_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ResultDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "result/*/*/*"
            case RESULT_WITH_LOCATION_AND_DATE:
            {
                retCursor = getResultByLocationWithDate(uri, projection, sortOrder);
                break;
            }

            // "result/*/*"
            // start date is passed as a query parameter
            case RESULT_WITH_LOCATION: {
                retCursor = getResultByLocation(uri, projection, sortOrder);
                break;
            }
            // "result"
            case RESULT: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ResultContract.ResultEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location/*"
            case LOCATION_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ResultContract.LocationEntry.TABLE_NAME,
                        projection,
                        ResultContract.LocationEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location"
            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ResultContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case RESULT_WITH_LOCATION_AND_DATE:
                return ResultContract.ResultEntry.CONTENT_ITEM_TYPE;
            case RESULT_WITH_LOCATION:
                return ResultContract.ResultEntry.CONTENT_TYPE;
            case RESULT:
                return ResultContract.ResultEntry.CONTENT_TYPE;
            case LOCATION:
                return ResultContract.LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return ResultContract.LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case RESULT: {
                long _id = db.insert(ResultContract.ResultEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 )
                    returnUri = ResultContract.ResultEntry.buildResultUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                long _id = db.insert(ResultContract.LocationEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 )
                    returnUri = ResultContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case RESULT:
                rowsDeleted = db.delete(
                        ResultContract.ResultEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LOCATION:
                rowsDeleted = db.delete(
                        ResultContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case RESULT:
                rowsUpdated = db.update(ResultContract.ResultEntry.TABLE_NAME, contentValues, selection,
                        selectionArgs);
                break;
            case LOCATION:
                rowsUpdated = db.update(ResultContract.LocationEntry.TABLE_NAME, contentValues, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
