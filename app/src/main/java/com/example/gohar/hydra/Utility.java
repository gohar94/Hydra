package com.example.gohar.hydra;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.gohar.hydra.data.ResultContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by goharirfan on 7/22/15.
 */
public class Utility {

    private static final String LOG_TAG = Utility.class.getSimpleName();

    // outputs a string array with {latitude, longitude}
    public static String[] getPrefferedLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String latitude = prefs.getString(context.getString(R.string.pref_location_latitude_key),
                context.getString(R.string.pref_location_latitude_default));
        String longitude = prefs.getString(context.getString(R.string.pref_location_longitude_key),
                context.getString(R.string.pref_location_longitude_default));
        return new String[]{latitude, longitude};
    }

    public static String getPlantDate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String plantDate = prefs.getString(context.getString(R.string.pref_plant_date), Utility.getCurrentDate());
        return plantDate;
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        return currentDate;
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    public static String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /**
     * Prepare the attributes max and min temperature for presentation.
     */
    public static String formatMaxMinTemp(double max, double min) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(max);
        long roundedLow = Math.round(min);

        String highLowStr = roundedHigh + "/" + roundedLow + "°C";
        return highLowStr;
    }

    /**
     * Prepare the attributes max and min temperature for presentation.
     */
    public static String formatTemp(double temp) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long rounded = Math.round(temp);

        String str = rounded + "°C";
        return str;
    }

    /**
     * Prepare the attribute having mm as unit for presentation.
     */
    public static String formatMillimeter(double precip) {
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
    public static String formatPercentage(double precip) {
        long rounded = Math.round(precip);

        String mFinal = rounded + "%";
        return mFinal;
    }

    /**
     * Prepare the attribute having m/s as unit for presentation.
     */
    public static String formatMeterPerSecond(double precip) {
        long rounded = Math.round(precip);

        String mFinal = rounded + "m/s";
        return mFinal;
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, String dateStr) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Date todayDate = new Date();
        String todayStr = Utility.getCurrentDate();
        Date inputDate = ResultContract.getDateFromDb(dateStr);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (todayStr.equals(dateStr)) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateStr)));
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, 7);
            String weekFutureString = ResultContract.getDbDateString(cal.getTime());

            if (dateStr.compareTo(weekFutureString) < 0) {
                // If the input date is less than a week in the future, just return the day name.
                return getDayName(context, dateStr);
            } else {
                // Otherwise, use the form "Mon Jun 3"
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(inputDate);
            }
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return
     */
    public static String getDayName(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.
            if (ResultContract.getDbDateString(todayDate).equals(dateStr)) {
                return context.getString(R.string.today);
            } else {
                // If the date is set for tomorrow, the format is "Tomorrow".
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, 1);
                Date tomorrowDate = cal.getTime();
                if (ResultContract.getDbDateString(tomorrowDate).equals(
                        dateStr)) {
                    return context.getString(R.string.tomorrow);
                } else {
                    // Otherwise, the format is just the day of the week (e.g "Wednesday".
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    return dayFormat.format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // It couldn't process the date correctly.
            return "";
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
            String monthDayString = monthDayFormat.format(inputDate);
            return monthDayString;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // TODO consult hydra team
    public static int getIconResourceForWeatherCondition(String conditionCode, boolean isColored) {
        if (conditionCode == null || conditionCode.equals("null") || conditionCode.equals("") || conditionCode.length() == 0) {
            Log.v(LOG_TAG, "Using default image");
            return R.drawable.art_clear; // TODO default image should be changed maybe?
        }
        char code1 = conditionCode.charAt(0);
        char code2 = conditionCode.charAt(1);
        char code3 = conditionCode.charAt(2); // wind not being user right now

        if (code1 == '1' || code1 == '2' || code1 == '6' || code1 == '7') {
            if (isColored)
                return R.drawable.art_clear;
            else
                return R.drawable.ic_clear;
        } else if (code1 == '3' || code1 == '4' || code1 == '5' || code1 == '8' || code1 == '9' || code1 == 'A') {
            if (code2 == '1') {
                if (isColored)
                    return R.drawable.art_clouds;
                else
                    return R.drawable.ic_cloudy;
            } else if (code2 == '2' || code2 == '3') {
                if (isColored)
                    return R.drawable.art_rain;
                else
                    return R.drawable.ic_rain;
            } else {
                if (isColored)
                    return R.drawable.art_storm;
                else
                    return R.drawable.ic_storm;
            }
        }

        return R.drawable.art_clear;
    }
}
