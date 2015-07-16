package com.example.gohar.hydra.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by Gohar on 17/07/15.
 */
public class ResultProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ResultsDBHelper mOpenHelper;

    private static final int RESULT = 100;
    private static final int RESULT_WITH_LOCATION = 101;
    private static final int RESULT_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ResultsContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, ResultsContract.PATH_RESULT, RESULT);
        matcher.addURI(authority, ResultsContract.PATH_RESULT + "/*/*", RESULT_WITH_LOCATION); // latitude/longitude
        matcher.addURI(authority, ResultsContract.PATH_RESULT + "/*/*/*", RESULT_WITH_LOCATION_AND_DATE); // latitude/longitude/date

        matcher.addURI(authority, ResultsContract.PATH_LOCATION, LOCATION);
        matcher.addURI(authority, ResultsContract.PATH_LOCATION + "/#", LOCATION_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ResultsDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case RESULT_WITH_LOCATION_AND_DATE:
            {
                retCursor = null;
                break;
            }
            // "weather/*"
            case RESULT_WITH_LOCATION: {
                retCursor = null;
                break;
            }
            // "weather"
            case RESULT: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ResultsContract.ResultEntry.TABLE_NAME,
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
                        ResultsContract.LocationEntry.TABLE_NAME,
                        projection,
                        ResultsContract.LocationEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
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
                        ResultsContract.LocationEntry.TABLE_NAME,
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
                return ResultsContract.ResultEntry.CONTENT_ITEM_TYPE;
            case RESULT_WITH_LOCATION:
                return ResultsContract.ResultEntry.CONTENT_TYPE;
            case RESULT:
                return ResultsContract.ResultEntry.CONTENT_TYPE;
            case LOCATION:
                return ResultsContract.LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return ResultsContract.LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
