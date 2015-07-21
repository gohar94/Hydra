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
    public static String[] getPrefferedDate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String latitude = prefs.getString(context.getString(R.string.pref_location_latitude_key),
                context.getString(R.string.pref_location_latitude_default));
        String longitude = prefs.getString(context.getString(R.string.pref_location_longitude_key),
                context.getString(R.string.pref_location_longitude_default));
        return new String[]{latitude, longitude};
    }

    public  static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        return currentDate;
    }
}
