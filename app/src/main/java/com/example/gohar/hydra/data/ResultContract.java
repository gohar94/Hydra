package com.example.gohar.hydra.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Gohar on 16/07/15.
 */

/**
 * Defines table and column names for the weather database.
 */
public class ResultContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.gohar.hydra.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_RESULT = "result";
    public static final String PATH_LOCATION = "location";

    /* Inner class that defines the table contents of the location table */
    public static final class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        // Table name
        public static final String TABLE_NAME = "location";

        // Human readable location string, provided by the API.  Because for styling,
        // "Mountain View" is more recognizable than 94043.
        // TODO add support for this = maybe use some third party service to get city names
        public static final String COLUMN_CITY_NAME = "city_name";

        // In order to uniquely pinpoint the location on the map when we launch the
        // map intent, we store the latitude and longitude
        public static final String COLUMN_LATITUDE = Constants.LATITUDE;
        public static final String COLUMN_LONGITUDE = Constants.LONGITUDE;

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the results table */
    public static final class ResultEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_RESULT).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_RESULT;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_RESULT;

        public static final String TABLE_NAME = "results";

        // Column with the foreign key into the location table.
        public static final String COLUMN_LOC_KEY = "location_id";
        // Date, stored as Text with format yyyy-MM-dd
        public static final String COLUMN_DATE = Constants.DATE;

        // Min and max temperatures for the day (stored as floats)
        public static final String COLUMN_MIN_TEMP = Constants.MIN_TEMPERATURE;
        public static final String COLUMN_MAX_TEMP = Constants.MAX_TEMPERATURE;

        // Precipitation data
        // All of this is in (mm)
        public static final String COLUMN_PRECIP = Constants.PRECIP;
        public static final String COLUMN_ACC_PRECIP = Constants.ACC_PRECIP;
        public static final String COLUMN_ACC_PRECIP_PRIOR_YEAR = Constants.ACC_PRECIP_PRIOR_YEAR;
        public static final String COLUMN_ACC_PRECIP_3_YEAR_AVERAGE = Constants.ACC_PRECIP_3_YEAR_AVERAGE;
        public static final String COLUMN_ACC_PRECIP_LONG_TERM_AVERAGE = Constants.ACC_PRECIP_LONG_TERM_AVERAGE;

        // Units = watt hours/sq. meter [wh/m2]
        public static final String COLUMN_SOLAR = Constants.SOLAR;

        // Humidity data
        // Units = %
        public static final String COLUMN_MIN_HUMIDITY = Constants.MIN_HUMIDITY;
        public static final String COLUMN_MAX_HUMIDITY = Constants.MAX_HUMIDITY;

        // Wind data
        // Units = meters/sec [m/s]
        public static final String COLUMN_MORN_WIND = Constants.MORN_WIND;
        public static final String COLUMN_MAX_WIND = Constants.MAX_WIND;

        // GDD data (growing degree days)
        // Unit = heat units
        public static final String COLUMN_GDD = Constants.GDD;
        public static final String COLUMN_ACC_GDD = Constants.ACC_GDD;
        public static final String COLUMN_ACC_GDD_PRIOR_YEAR = Constants.ACC_GDD_PRIOR_YEAR;
        public static final String COLUMN_ACC_GDD_3_YEAR_AVERAGE = Constants.ACC_GDD_3_YEAR_AVERAGE;
        // unit is in mm? Not sure
        public static final String COLUMN_ACC_GDD_LONG_TERM_AVERAGE = Constants.ACC_GDD_LONG_TERM_AVERAGE;

        // Potential evapotranspiration data or PET
        // Unit = mm
        public static final String COLUMN_PET = Constants.PET;
        public static final String COLUMN_ACC_PET = Constants.ACC_PET;

        // This is a ratio
        public static final String COLUMN_PPET = Constants.PPET;

        public static Uri buildResultUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildResultLocation(String latitude, String longitude) {
            return CONTENT_URI.buildUpon().appendPath(latitude).appendPath(longitude).build();
        }

        // for matching on all dates after or equal to start date
        public static Uri buildResultLocationWithStartDate(
                String latitude, String longitude, String startDate) {
            return CONTENT_URI.buildUpon().appendPath(latitude).appendPath(longitude)
                    .appendQueryParameter(Constants.START_DATE, startDate).build();
        }

        // for matching with exact date
        public static Uri buildResultLocationWithDate(String latitude, String longitude, String date) {
            return CONTENT_URI.buildUpon().appendPath(latitude).appendPath(longitude).appendPath(date).build();
        }

        public static String getLatitudeFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getLongitudeFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(3);
        }

        public static String getStartDateFromUri(Uri uri) {
            return uri.getQueryParameter(Constants.START_DATE);
        }

    }
}
