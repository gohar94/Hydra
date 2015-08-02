package com.example.gohar.hydra;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.gohar.hydra.data.Constants;
import com.example.gohar.hydra.data.ResultContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by goharirfan on 7/22/15.
 */
public class FetchResultsTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchResultsTask.class.getSimpleName();
    private boolean DEBUG = false;
    // only contains the dates
//    private ArrayAdapter<String> resultsAdapter;
    private final Context mContext;

    // contains the detail corresponding to each element in resultsAdapter
//    private ArrayList<String> resultsDetails;
    private static String oauthToken = null;

    public static ArrayList<String> dates = new ArrayList<String>();
    public static ArrayList<Double> etcList = new ArrayList<Double>();

    public FetchResultsTask(Context context) {
        mContext = context;
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param latitude the latitude of the city
     * @param longitude the longitude of the city
     * @return the row ID of the added location.
     */
    private long addLocation(String latitude, String longitude) {
//        String cityName; // can be taken in input of function

        Log.v(LOG_TAG, "inserting co-ord: " + latitude + ", " + longitude);

        // First, check if the location with this city name exists in the db
        Cursor cursor = mContext.getContentResolver().query(
                ResultContract.LocationEntry.CONTENT_URI,
                new String[]{ResultContract.LocationEntry._ID},
                ResultContract.LocationEntry.COLUMN_LATITUDE + " = ? AND " +
                        ResultContract.LocationEntry.COLUMN_LONGITUDE + " = ? ",
                new String[]{latitude, longitude},
                null);

        if (cursor.moveToFirst()) {
            Log.v(LOG_TAG, "Found it in the database!");
            int locationIdIndex = cursor.getColumnIndex(ResultContract.LocationEntry._ID);
            long temp = cursor.getLong(locationIdIndex);
            cursor.close();
            return temp;
        } else {
            cursor.close();
            Log.v(LOG_TAG, "Didn't find it in the database, inserting now!");
            ContentValues locationValues = new ContentValues();
//            locationValues.put(LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(ResultContract.LocationEntry.COLUMN_LATITUDE, latitude);
            locationValues.put(ResultContract.LocationEntry.COLUMN_LONGITUDE, longitude);

            Uri locationInsertUri = mContext.getContentResolver()
                    .insert(ResultContract.LocationEntry.CONTENT_URI, locationValues);

            return ContentUris.parseId(locationInsertUri);
        }
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getWeatherDataFromJson(String resultJsonStr, long locationID)
            throws JSONException {

        JSONArray resultsArray = new JSONArray(resultJsonStr);

        // Get and insert the new results into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(resultsArray.length());

        dates.clear();
        etcList.clear();

        String[] resultStrs = new String[resultsArray.length()];
        for (int i = 0; i < resultsArray.length(); i++) {
            String date = "";
            Double maxTemperature = Double.NaN;
            Double minTemperature = Double.NaN;;
            Double precip = Double.NaN;
            Double accPrecip = Double.NaN;
            Double accPrecipPriorYear = Double.NaN;
            Double accPrecip3YearAverage = Double.NaN;
            Double accPrecipLongTermAverage = Double.NaN;
            Double solar = Double.NaN;
            Double minHumidity = Double.NaN;
            Double maxHumidity = Double.NaN;
            Double mornWind = Double.NaN;
            Double maxWind = Double.NaN;
            Double gdd = Double.NaN;
            Double accGdd = Double.NaN;
            Double accGddPriorYear = Double.NaN;
            Double accGdd3YearAverage = Double.NaN;
            Double accGddLongTermAverage = Double.NaN;
            Double pet = Double.NaN;
            Double accPet = Double.NaN;
            Double ppet = Double.NaN;
            String cond_type = "";
            String cond_text = "";

            // Get the JSON object representing the day
            JSONObject result = resultsArray.getJSONObject(i);
            ContentValues resultValues = new ContentValues();

            resultValues.put(ResultContract.ResultEntry.COLUMN_LOC_KEY, locationID);

            if (result.has(Constants.DAILY_ATTRIBUTES)) {
                JSONObject dailyAttributes = result.getJSONObject(Constants.DAILY_ATTRIBUTES);

                if (dailyAttributes.has(Constants.MAX_TEMPERATURE) && dailyAttributes.getString(Constants.MAX_TEMPERATURE) != "null") {
                    maxTemperature = dailyAttributes.getDouble(Constants.MAX_TEMPERATURE);
                    resultValues.put(Constants.MAX_TEMPERATURE, maxTemperature);
                }

                if (dailyAttributes.has(Constants.MIN_TEMPERATURE) && dailyAttributes.getString(Constants.MIN_TEMPERATURE) != "null") {
                    minTemperature = dailyAttributes.getDouble(Constants.MIN_TEMPERATURE);
                    resultValues.put(Constants.MIN_TEMPERATURE, minTemperature);
                }

                if (dailyAttributes.has(Constants.PRECIP) && dailyAttributes.getString(Constants.PRECIP) != "null") {
                    precip = dailyAttributes.getDouble(Constants.PRECIP);
                    resultValues.put(Constants.PRECIP, precip);
                }

                if (dailyAttributes.has(Constants.ACC_PRECIP) && dailyAttributes.getString(Constants.ACC_PRECIP) != "null") {
                    accPrecip = dailyAttributes.getDouble(Constants.ACC_PRECIP);
                    resultValues.put(Constants.ACC_PRECIP, accPrecip);
                }

                if (dailyAttributes.has(Constants.ACC_PRECIP_PRIOR_YEAR) && dailyAttributes.getString(Constants.ACC_PRECIP_PRIOR_YEAR) != "null") {
                    accPrecipPriorYear = dailyAttributes.getDouble(Constants.ACC_PRECIP_PRIOR_YEAR);
                    resultValues.put(Constants.ACC_PRECIP_PRIOR_YEAR, accPrecipPriorYear);
                }

                if (dailyAttributes.has(Constants.ACC_PRECIP_3_YEAR_AVERAGE) && dailyAttributes.getString(Constants.ACC_PRECIP_3_YEAR_AVERAGE) != "null") {
                    accPrecip3YearAverage = dailyAttributes.getDouble(Constants.ACC_PRECIP_3_YEAR_AVERAGE);
                    resultValues.put(Constants.ACC_PRECIP_3_YEAR_AVERAGE, accPrecip3YearAverage);
                }

                if (dailyAttributes.has(Constants.ACC_PRECIP_LONG_TERM_AVERAGE) && dailyAttributes.getString(Constants.ACC_PRECIP_LONG_TERM_AVERAGE) != "null") {
                    accPrecipLongTermAverage = dailyAttributes.getDouble(Constants.ACC_PRECIP_LONG_TERM_AVERAGE);
                    resultValues.put(Constants.ACC_PRECIP_LONG_TERM_AVERAGE, accPrecipLongTermAverage);
                }

                if (dailyAttributes.has(Constants.SOLAR) && dailyAttributes.getString(Constants.SOLAR) != "null") {
                    solar = dailyAttributes.getDouble(Constants.SOLAR);
                    resultValues.put(Constants.SOLAR, solar);
                }

                if (dailyAttributes.has(Constants.MIN_HUMIDITY) && dailyAttributes.getString(Constants.MIN_HUMIDITY) != "null") {
                    minHumidity = dailyAttributes.getDouble(Constants.MIN_HUMIDITY);
                    resultValues.put(Constants.MIN_HUMIDITY, minHumidity);
                }

                if (dailyAttributes.has(Constants.MAX_HUMIDITY) && dailyAttributes.getString(Constants.MAX_HUMIDITY) != "null") {
                    maxHumidity = dailyAttributes.getDouble(Constants.MAX_HUMIDITY);
                    resultValues.put(Constants.MAX_HUMIDITY, maxHumidity);
                }

                if (dailyAttributes.has(Constants.MORN_WIND) && dailyAttributes.getString(Constants.MORN_WIND) != "null") {
                    mornWind = dailyAttributes.getDouble(Constants.MORN_WIND);
                    resultValues.put(Constants.MORN_WIND, mornWind);
                }

                if (dailyAttributes.has(Constants.MAX_WIND) && dailyAttributes.getString(Constants.MAX_WIND) != "null") {
                    maxWind = dailyAttributes.getDouble(Constants.MAX_WIND);
                    resultValues.put(Constants.MAX_WIND, maxWind);
                }

                if (dailyAttributes.has(Constants.GDD) && dailyAttributes.getString(Constants.GDD) != "null") {
                    gdd = dailyAttributes.getDouble(Constants.GDD);
                    resultValues.put(Constants.GDD, gdd);
                }

                if (dailyAttributes.has(Constants.ACC_GDD) && dailyAttributes.getString(Constants.ACC_GDD) != "null") {
                    accGdd = dailyAttributes.getDouble(Constants.ACC_GDD);
                    resultValues.put(Constants.ACC_GDD, accGdd);
                }

                if (dailyAttributes.has(Constants.ACC_GDD_PRIOR_YEAR) && dailyAttributes.getString(Constants.ACC_GDD_PRIOR_YEAR) != "null") {
                    accGddPriorYear = dailyAttributes.getDouble(Constants.ACC_GDD_PRIOR_YEAR);
                    resultValues.put(Constants.ACC_PRECIP_PRIOR_YEAR, accGddPriorYear);
                }

                if (dailyAttributes.has(Constants.ACC_GDD_3_YEAR_AVERAGE) && dailyAttributes.getString(Constants.ACC_GDD_3_YEAR_AVERAGE) != "null") {
                    accGdd3YearAverage = dailyAttributes.getDouble(Constants.ACC_GDD_3_YEAR_AVERAGE);
                    resultValues.put(Constants.ACC_PRECIP_3_YEAR_AVERAGE, accGdd3YearAverage);
                }

                if (dailyAttributes.has(Constants.ACC_GDD_LONG_TERM_AVERAGE) && dailyAttributes.getString(Constants.ACC_GDD_LONG_TERM_AVERAGE) != "null") {
                    accGddLongTermAverage = dailyAttributes.getDouble(Constants.ACC_GDD_LONG_TERM_AVERAGE);
                    resultValues.put(Constants.ACC_PRECIP_LONG_TERM_AVERAGE, accGddLongTermAverage);
                }

                if (dailyAttributes.has(Constants.PET) && dailyAttributes.getString(Constants.PET) != "null") {
                    pet = dailyAttributes.getDouble(Constants.PET);
                    resultValues.put(Constants.PET, pet);
                }

                if (dailyAttributes.has(Constants.ACC_PET) && dailyAttributes.getString(Constants.ACC_PET) != "null") {
                    accPet = dailyAttributes.getDouble(Constants.ACC_PET);
                    resultValues.put(Constants.ACC_PET, accPet);
                }

                if (dailyAttributes.has(Constants.PPET) && dailyAttributes.getString(Constants.PPET) != "null") {
                    ppet = dailyAttributes.getDouble(Constants.PPET);
                    resultValues.put(Constants.PPET, ppet);
                }

                if (result.has(Constants.DATE) && result.getString(Constants.DATE) != "null") {
                    date = result.getString(Constants.DATE);
                    resultValues.put(Constants.DATE, date);
                }

                Double kc = new Double(0);

                if (gdd != null && pet != null) {
                    if (gdd >= 0 && gdd <= 1593) {
                        kc = (0.0005336 * gdd) + 0.3;
                    } else if (gdd > 1593 && gdd <= 1825) {
                        kc = 1.15;
                    } else {
                        kc = 2.95 - (0.00098425 * gdd);
                    }
                    Double etc = pet*kc;
                    dates.add(date);
                    etcList.add(etc);
                }

                boolean parseConditions = false;
                if (result.has(Constants.CONDITIONS)) {
                    JSONArray conditionsArray = result.getJSONArray(Constants.CONDITIONS);

                    if (conditionsArray.length() == 0) {
                        i++;
                        if (i < resultsArray.length()) {
                            result = resultsArray.getJSONObject(i);
                            if (result.has(Constants.DAILY_ATTRIBUTES)) {
                                i--;
                            } else {
                                parseConditions = true;
                            }
                        } else {
                            i--;
                        }
                    } else {
                        parseConditions = true;
                    }

                    if (parseConditions == true) {
                        conditionsArray = result.getJSONArray(Constants.CONDITIONS);
                        Log.e(LOG_TAG, "parsing conditions");
                        Log.v(LOG_TAG, "conditions length " + conditionsArray.length());

                        for (int j = 0; j < conditionsArray.length(); j++) {
                            JSONObject conditions = conditionsArray.getJSONObject(j);
                            if (conditions.has(Constants.CONDITIONS_COND_CODE) && conditions.getString(Constants.CONDITIONS_COND_CODE) != "null") {
                                cond_type = conditions.getString(Constants.CONDITIONS_COND_CODE);
                                resultValues.put(Constants.CONDITIONS_COND_CODE, cond_type);
                                Log.v(LOG_TAG, "inserting conditions code " + cond_type);
                            }

                            if (conditions.has(Constants.CONDITIONS_COND_TEXT) && conditions.getString(Constants.CONDITIONS_COND_TEXT) != "null") {
                                cond_text = conditions.getString(Constants.CONDITIONS_COND_TEXT);
                                resultValues.put(Constants.CONDITIONS_COND_TEXT, cond_text);
                            }
                        }
                    }
                }

                cVVector.add(resultValues);

                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    int rowsInserted = mContext.getContentResolver()
                            .bulkInsert(ResultContract.ResultEntry.CONTENT_URI, cvArray);
                    Log.v(LOG_TAG, "inserted " + rowsInserted + " rows of weather data");
                    // Use a DEBUG variable to gate whether or not you do this, so you can easily
                    // turn it on and off, and so that it's easy to see what you can rip out if
                    // you ever want to remove it.
                    if (DEBUG) {
                        Cursor weatherCursor = mContext.getContentResolver().query(
                                ResultContract.ResultEntry.CONTENT_URI,
                                null,
                                null,
                                null,
                                null
                        );

                        if (weatherCursor.moveToFirst()) {
                            ContentValues resultValues2 = new ContentValues();
                            DatabaseUtils.cursorRowToContentValues(weatherCursor, resultValues2);
                            Log.v(LOG_TAG, "Query succeeded! **********");
                            for (String key : resultValues2.keySet()) {
                                Log.v(LOG_TAG, key + ": " + resultValues2.getAsString(key));
                            }
                        } else {
                            Log.v(LOG_TAG, "Query failed! :( **********");
                        }

                        weatherCursor.close();
                    }
                }

            } else {
                String errorMessage = "Incomplete information received!";
                resultStrs[i] = errorMessage;
                Log.e(LOG_TAG, errorMessage);
            }
        }

        return;

    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private String getOAuthToken() {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String oauthTokenJsonStr = null;

        try {
            // Construct the URL for the API query
            Uri builtUri = Uri.parse(Constants.OAUTH_API_BASE_URL).buildUpon()
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            // Create hashmap of post parameters
            HashMap<String, String> postDataParams = new HashMap<>();
            postDataParams.put(Constants.OAUTH_GRANT_TYPE, Constants.OAUTH_CLIENT_CREDENTIALS);

            // Create the request to the server and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setReadTimeout(15000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestProperty("Authorization", "Basic " + Constants.BASE64_ENCODED_CREDENTIAL);
            urlConnection.setRequestProperty("Content-Type", Constants.OAUTH_HEADER_CONTENT_TYPE);

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                Log.e(LOG_TAG, "input stream null");
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a LOT easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                Log.e(LOG_TAG, "buffer length 0");
                return null;
            }
            oauthTokenJsonStr = buffer.toString();
            Log.v(LOG_TAG, "OAauth response from server " + oauthTokenJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            JSONObject tokenObject = new JSONObject(oauthTokenJsonStr);
            return tokenObject.getString("access_token");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        // This will only happen if there was an error getting or parsing the OAuth token.
        return null;
    }

    private boolean getDataFromAPI(String latitude, String longitude, String startDate, String token) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.

        String plantDate = Utility.getPlantDate(mContext);
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        long locationID = addLocation(latitude, longitude);

        try {
            // Construct the URL for the API query
            final String FORECAST_BASE_URL =
                    "https://api.awhere.com/v1/weather";

            // TODO fix here
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(Constants.LATITUDE, latitude)
                    .appendQueryParameter(Constants.LONGITUDE, longitude)
                    .appendQueryParameter(Constants.START_DATE, startDate)
                    .appendQueryParameter("plantDate", plantDate)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.MIN_TEMPERATURE)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.MAX_TEMPERATURE)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.PRECIP)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.ACC_PRECIP)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.ACC_PRECIP_PRIOR_YEAR)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.ACC_PRECIP_3_YEAR_AVERAGE)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.ACC_PRECIP_LONG_TERM_AVERAGE)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.SOLAR)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.MIN_HUMIDITY)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.MAX_HUMIDITY)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.MORN_WIND)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.MAX_WIND)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.GDD)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.ACC_GDD)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.ACC_GDD_PRIOR_YEAR)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.ACC_GDD_3_YEAR_AVERAGE)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.ACC_GDD_LONG_TERM_AVERAGE)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.PET)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.ACC_PET)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.PPET)
                    .appendQueryParameter(Constants.ATTRIBUTE, Constants.CONDITIONS)
                    .appendQueryParameter(Constants.INTERVALS, Constants.INTERVALS_VALUE)
                    .appendQueryParameter(Constants.CONDITIONS_TYPE, Constants.CONDITIONS_TYPE_VALUE)
                    .appendQueryParameter(Constants.UTC_OFFSET, Constants.UTC_OFFSET_VALUE)
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            // Create the request to the server and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            String responseMessage = urlConnection.getResponseMessage();
            Log.v(LOG_TAG, "Respond Code is " + responseCode + " - " + responseMessage);

            if (responseCode != 200) {
                // Authorization token invalid
                Log.v(LOG_TAG, "Respond Code is " + responseCode + " - " + responseMessage);
                return false;
            }

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return false;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a LOT easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return false;
            }
            forecastJsonStr = buffer.toString();
            Log.v(LOG_TAG, "Response from server received!");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            Log.v(LOG_TAG, "response = " + forecastJsonStr);
            getWeatherDataFromJson(forecastJsonStr, locationID);
            return true;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        // This will only happen if there was an error getting or parsing the forecast.
        return false;
    }

    @Override
    protected Void doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            Log.e(LOG_TAG, "params 0");
            return null;
        }

        if (oauthToken == null) {
            oauthToken = getOAuthToken();

            if (oauthToken == null) {
                // Could not obtain oauth token
                Log.e(LOG_TAG, "Could not obtain token");
                return null;
            }

            Log.v(LOG_TAG, "No existing token was present, new token is = " + oauthToken);
        }

        // Params: latitude, longitude, startdate, plantdate, token
        boolean result = getDataFromAPI(params[0], params[1], params[2], oauthToken);

        if (!result) { // some error code returned from API
            // When the token has expired and we get exception from getDataFromAPI method
            oauthToken = getOAuthToken();
            Log.v(LOG_TAG, "Token has expired, new token is = " + oauthToken);
            getDataFromAPI(params[0], params[1], params[2], oauthToken);
        } else {
            Log.v(LOG_TAG, "Using same token = " + oauthToken);
        }
        return null;
    }

    // Formatting string to get date from API result response
    private String getDateFromResult(String result) {
        // input date is in the format e.g. "Date = 2015-07-09T00:00:00"
        String temp = result.split("Max/Min")[0];
        temp = temp.split("Date = ")[1];
        temp = temp.split("T")[0];
        return temp;
    }

}
