package com.example.gohar.hydra.data;

import android.provider.BaseColumns;

/**
 * Created by Gohar on 16/07/15.
 */

/**
 * Defines table and column names for the weather database.
 */
public class ResultsContract {

    /* Inner class that defines the table contents of the location table */
    public static final class LocationEntry implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "location";

        // The location setting string is what will be sent to openweathermap
        // as the location query.
        public static final String COLUMN_LOCATION_SETTING = "location_setting";

        // Human readable location string, provided by the API.  Because for styling,
        // "Mountain View" is more recognizable than 94043.
        // TODO add support for this = maybe use some third party service to get city names
        public static final String COLUMN_CITY_NAME = "city_name";

        // In order to uniquely pinpoint the location on the map when we launch the
        // map intent, we store the latitude and longitude as returned by openweathermap.
        public static final String COLUMN_LATITUDE = Constants.LATITUDE;
        public static final String COLUMN_LONGITUDE = Constants.LONGITUDE;
    }

    /* Inner class that defines the table contents of the results table */
    public static final class ResultEntry implements BaseColumns {

        public static final String TABLE_NAME = "results";

        // Column with the foreign key into the location table.
        public static final String COLUMN_LOC_KEY = "location_id";
        // Date, stored as Text with format yyyy-MM-dd
        public static final String COLUMN_START_DATE = Constants.START_DATE;

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

    }
}
