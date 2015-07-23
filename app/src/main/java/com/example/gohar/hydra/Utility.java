package com.example.gohar.hydra;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by goharirfan on 7/22/15.
 */
public class Utility {

    // outputs a string array with {latitude, longitude}
    public static String[] getPrefferedLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String latitude = prefs.getString(context.getString(R.string.pref_location_latitude_key),
                context.getString(R.string.pref_location_latitude_default));
        String longitude = prefs.getString(context.getString(R.string.pref_location_longitude_key),
                context.getString(R.string.pref_location_longitude_default));
        return new String[]{latitude, longitude};
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        return currentDate;
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private static String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /**
     * Prepare the attributes max and min temperature for presentation.
     */
    private static String formatMaxMinTemp(double max, double min) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(max);
        long roundedLow = Math.round(min);

        String highLowStr = roundedHigh + "/" + roundedLow + "°C";
        return highLowStr;
    }

    /**
     * Prepare the attribute having mm as unit for presentation.
     */
    private static String formatMillimeter(double precip) {
        long rounded = Math.round(precip);

        String mFinal = rounded + "mm";
        return mFinal;
    }

    /**
     * Prepare the attribute having wh/m2 as unit for presentation.
     */
    private static String formatWattHours(double precip) {
        long rounded = Math.round(precip);

        String mFinal = rounded + "wh/m2";
        return mFinal;
    }

    /**
     * Prepare the attribute having % as unit for presentation.
     */
    private static String formatPercentage(double precip) {
        long rounded = Math.round(precip);

        String mFinal = rounded + "%";
        return mFinal;
    }

    /**
     * Prepare the attribute having m/s as unit for presentation.
     */
    private static String formatMeterPerSecond(double precip) {
        long rounded = Math.round(precip);

        String mFinal = rounded + "m/s";
        return mFinal;
    }
}
