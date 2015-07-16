package com.example.gohar.hydra;

/**
 * Created by Gohar on 03/07/15.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.gohar.hydra.data.Constants;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class ResultsFragment extends Fragment {

    // only contains the dates
    private ArrayAdapter<String> resultsAdapter;

    // contains the detail corresponding to each element in resultsAdapter
    private ArrayList<String> resultsDetails;
    private static String oauthToken = null;
    private final String LOG_TAG = ResultsFragment.class.getSimpleName();

    public ResultsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line for the fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.resultsfragment, menu);
    }

    public void updateResults() {
        FetchResultsTask fetchResultsTask = new FetchResultsTask();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String latitude = prefs.getString(getString(R.string.pref_location_latitude_key),
                getString(R.string.pref_location_latitude_default));
        String longitude = prefs.getString(getString(R.string.pref_location_longitude_key),
                getString(R.string.pref_location_longitude_default));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());

        Log.v(LOG_TAG, "Fetching data for date = " + currentDate);

        fetchResultsTask.execute(latitude, longitude, currentDate);
        return;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateResults();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateResults();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // ArrayList of data to display on the list
        List<String> results = new ArrayList<String>();

        // Adapter of data to display on the list
        resultsAdapter = new ArrayAdapter<String>(
                // Current context
                getActivity(),
                // Layout file of list item
                R.layout.list_item_results,
                // TextView on the list item to populate
                R.id.list_item_results_textview,
                // Data to display on the list
                results);

        resultsDetails = new ArrayList<String>();

        // Find the list view to populate
        ListView listView = (ListView) rootView.findViewById(R.id.listview_results);
        // Assign the adapter to the list view
        listView.setAdapter(resultsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String result = resultsDetails.get(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class).
                        putExtra(Intent.EXTRA_TEXT, result);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class FetchResultsTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchResultsTask.class.getSimpleName();

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }

        /**
         * Prepare the attributes max and min temperature for presentation.
         */
        private String formatMaxMinTemp(double max, double min) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(max);
            long roundedLow = Math.round(min);

            String highLowStr = roundedHigh + "/" + roundedLow + "°C";
            return highLowStr;
        }

        /**
         * Prepare the attribute having mm as unit for presentation.
         */
        private String formatMillimeter(double precip) {
            long rounded = Math.round(precip);

            String mFinal = rounded + "mm";
            return mFinal;
        }

        /**
         * Prepare the attribute having wh/m2 as unit for presentation.
         */
        private String formatWattHours(double precip) {
            long rounded = Math.round(precip);

            String mFinal = rounded + "wh/m2";
            return mFinal;
        }

        /**
         * Prepare the attribute having % as unit for presentation.
         */
        private String formatPercentage(double precip) {
            long rounded = Math.round(precip);

            String mFinal = rounded + "%";
            return mFinal;
        }

        /**
         * Prepare the attribute having m/s as unit for presentation.
         */
        private String formatMeterPerSecond(double precip) {
            long rounded = Math.round(precip);

            String mFinal = rounded + "m/s";
            return mFinal;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String resultJsonStr)
                throws JSONException {

            JSONArray resultsArray = new JSONArray(resultJsonStr);

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

                // Get the JSON object representing the day
                JSONObject result = resultsArray.getJSONObject(i);

                if (result.has(Constants.DAILY_ATTRIBUTES)) {
                    JSONObject dailyAttributes = result.getJSONObject(Constants.DAILY_ATTRIBUTES);

                    if (dailyAttributes.has(Constants.MAX_TEMPERATURE) && dailyAttributes.getString(Constants.MAX_TEMPERATURE) != "null")
                        maxTemperature = dailyAttributes.getDouble(Constants.MAX_TEMPERATURE);

                    if (dailyAttributes.has(Constants.MIN_TEMPERATURE) && dailyAttributes.getString(Constants.MIN_TEMPERATURE) != "null")
                        minTemperature = dailyAttributes.getDouble(Constants.MIN_TEMPERATURE);

                    if (dailyAttributes.has(Constants.PRECIP) && dailyAttributes.getString(Constants.PRECIP) != "null")
                        precip = dailyAttributes.getDouble(Constants.PRECIP);

                    if (dailyAttributes.has(Constants.ACC_PRECIP) && dailyAttributes.getString(Constants.ACC_PRECIP) != "null")
                        accPrecip = dailyAttributes.getDouble(Constants.ACC_PRECIP);

                    if (dailyAttributes.has(Constants.ACC_PRECIP_PRIOR_YEAR) && dailyAttributes.getString(Constants.ACC_PRECIP_PRIOR_YEAR) != "null")
                        accPrecipPriorYear = dailyAttributes.getDouble(Constants.ACC_PRECIP_PRIOR_YEAR);

                    if (dailyAttributes.has(Constants.ACC_PRECIP_3_YEAR_AVERAGE) && dailyAttributes.getString(Constants.ACC_PRECIP_3_YEAR_AVERAGE) != "null")
                        accPrecip3YearAverage = dailyAttributes.getDouble(Constants.ACC_PRECIP_3_YEAR_AVERAGE);

                    if (dailyAttributes.has(Constants.ACC_PRECIP_LONG_TERM_AVERAGE) && dailyAttributes.getString(Constants.ACC_PRECIP_LONG_TERM_AVERAGE) != "null")
                        accPrecipLongTermAverage = dailyAttributes.getDouble(Constants.ACC_PRECIP_LONG_TERM_AVERAGE);

                    if (dailyAttributes.has(Constants.SOLAR) && dailyAttributes.getString(Constants.SOLAR) != "null")
                        solar = dailyAttributes.getDouble(Constants.SOLAR);

                    if (dailyAttributes.has(Constants.MIN_HUMIDITY) && dailyAttributes.getString(Constants.MIN_HUMIDITY) != "null")
                        minHumidity = dailyAttributes.getDouble(Constants.MIN_HUMIDITY);

                    if (dailyAttributes.has(Constants.MAX_HUMIDITY) && dailyAttributes.getString(Constants.MAX_HUMIDITY) != "null")
                        maxHumidity = dailyAttributes.getDouble(Constants.MAX_HUMIDITY);

                    if (dailyAttributes.has(Constants.MORN_WIND) && dailyAttributes.getString(Constants.MORN_WIND) != "null")
                        mornWind = dailyAttributes.getDouble(Constants.MORN_WIND);

                    if (dailyAttributes.has(Constants.MAX_WIND) && dailyAttributes.getString(Constants.MAX_WIND) != "null")
                        maxWind = dailyAttributes.getDouble(Constants.MAX_WIND);

                    if (dailyAttributes.has(Constants.GDD) && dailyAttributes.getString(Constants.GDD) != "null")
                        gdd = dailyAttributes.getDouble(Constants.GDD);

                    if (dailyAttributes.has(Constants.ACC_GDD) && dailyAttributes.getString(Constants.ACC_GDD) != "null")
                        accGdd = dailyAttributes.getDouble(Constants.ACC_GDD);

                    if (dailyAttributes.has(Constants.ACC_GDD_PRIOR_YEAR) && dailyAttributes.getString(Constants.ACC_GDD_PRIOR_YEAR) != "null")
                        accGddPriorYear = dailyAttributes.getDouble(Constants.ACC_GDD_PRIOR_YEAR);

                    if (dailyAttributes.has(Constants.ACC_GDD_3_YEAR_AVERAGE) && dailyAttributes.getString(Constants.ACC_GDD_3_YEAR_AVERAGE) != "null")
                        accGdd3YearAverage = dailyAttributes.getDouble(Constants.ACC_GDD_3_YEAR_AVERAGE);

                    if (dailyAttributes.has(Constants.ACC_GDD_LONG_TERM_AVERAGE) && dailyAttributes.getString(Constants.ACC_GDD_LONG_TERM_AVERAGE) != "null")
                        accGddLongTermAverage = dailyAttributes.getDouble(Constants.ACC_GDD_LONG_TERM_AVERAGE);

                    if (dailyAttributes.has(Constants.PET) && dailyAttributes.getString(Constants.PET) != "null")
                        pet = dailyAttributes.getDouble(Constants.PET);

                    if (dailyAttributes.has(Constants.ACC_PET) && dailyAttributes.getString(Constants.ACC_PET) != "null")
                        accPet = dailyAttributes.getDouble(Constants.ACC_PET);

                    if (dailyAttributes.has(Constants.PPET) && dailyAttributes.getString(Constants.PPET) != "null")
                        ppet = dailyAttributes.getDouble(Constants.PPET);

                    if (result.has(Constants.DATE) && result.getString(Constants.DATE) != "null")
                        date = result.getString(Constants.DATE);

                    // formatting the variables for presentation and appending to string
                    resultStrs[i] = "Date = " + date + "\n";
                    resultStrs[i]+= "Max/Min Temperature = " + formatMaxMinTemp(maxTemperature, minTemperature) + "\n";
                    resultStrs[i]+= "Precipitation = " + formatMillimeter(precip) + "\n";
                    resultStrs[i]+= "Total accumulated precipitation from start date of the requested period = " + formatMillimeter(accPrecip) + "\n";
                    resultStrs[i]+= "Total accumulated precipitation for the same date range in the prior year = " + formatMillimeter(accPrecipPriorYear) + "\n";
                    resultStrs[i]+= "Avg. total accumulated precipitation for the same date range over prior 3 years = " + formatMillimeter(accPrecip3YearAverage) + "\n";
                    resultStrs[i]+= "Avg. total accumulated precipitation for the input date range over [up to] the past 10 years = " + formatMillimeter(accPrecipLongTermAverage) + "\n";
                    resultStrs[i]+= "Summation of total solar energy received during day = " + formatWattHours(solar) + "\n";
                    resultStrs[i]+= "Lowest % relative humidity recorded for day = " + formatPercentage(minHumidity) + "\n";
                    resultStrs[i]+= "Highest % relative humidity recorded for day = " + formatPercentage(maxHumidity) + "\n";
                    resultStrs[i]+= "Morning’s highest wind speed = " + formatMeterPerSecond(mornWind) + "\n";
                    resultStrs[i]+= "Day’s highest wind speed = " + formatMeterPerSecond(maxWind) + "\n";
                    resultStrs[i]+= "Growing Degree Days (# of heat units achieved per day) = " + Math.round(gdd) + "\n";
                    resultStrs[i]+= "Total accumulated GDDs from start date of the requested period = " + Math.round(accGdd) + "\n";
                    resultStrs[i]+= "Total accumulated GDDs for the same date range in the prior year = " + Math.round(accGddPriorYear) + "\n";
                    resultStrs[i]+= "Avg. total accumulated GDDs for the same date range over the prior 3 years = " + Math.round(accGdd3YearAverage) + "\n";
                    resultStrs[i]+= "Avg. total accumulated GDDs for the input date range over [up to] the past 10 years = " + Math.round(accGddLongTermAverage) + "\n";
                    resultStrs[i]+= "Potential Evapotranspiration for each day = " + formatMillimeter(pet) + "\n";
                    resultStrs[i]+= "Accumulated PET from the start date to each day in the date range = " + formatMillimeter(accPet) + "\n";
                    resultStrs[i]+= "P/PET or Precipitation over PET, for determining potential crop water stress = " + ppet;

                } else {
                    String errorMessage = "Incomplete information received!";
                    resultStrs[i] = errorMessage;
                    Log.e(LOG_TAG, errorMessage);
                }
            }

            return resultStrs;

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

        private String[] getDataFromAPI(String latitude, String longitude, String startDate, String token) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the API query
                final String FORECAST_BASE_URL =
                        "https://api.awhere.com/v1/weather";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(Constants.LATITUDE, latitude)
                        .appendQueryParameter(Constants.LONGITUDE, longitude)
                        .appendQueryParameter(Constants.START_DATE, startDate)
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
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to the server and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                Log.v(LOG_TAG, "Respond Code is " + responseCode);

                if (responseCode == 401) {
                    // Authorization token invalid
                    String[] results = {"401"};
                    return results;
                }

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
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
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Response from server received!");
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
                return getWeatherDataFromJson(forecastJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected String[] doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            if (oauthToken == null) {
                oauthToken = getOAuthToken();

                if (oauthToken == null) {
                    // Could not obtain oauth token
                    Log.e(LOG_TAG, "Could not obtain token");
                    Toast.makeText(getActivity(), R.string.toast_error_connection, Toast.LENGTH_SHORT).show();
                    return null;
                }

                Log.v(LOG_TAG, "No existing token was present, new token is = " + oauthToken);
            }

            String[] results = getDataFromAPI(params[0], params[1], params[2], oauthToken);

            if (results == null) {
                return null;
            } else if (results[0] == "401") {
                // When the token has expired and we get exception from getDataFromAPI method
                oauthToken = getOAuthToken();
                Log.v(LOG_TAG, "Token has expired, new token is = " + oauthToken);
                results = getDataFromAPI(params[0], params[1], params[2], oauthToken);
            } else {
                Log.v(LOG_TAG, "Using same token = " + oauthToken);
            }

            return results;
        }

        // Formatting string to get date from API result response
        private String getDateFromResult(String result) {
            // input date is in the format e.g. "Date = 2015-07-09T00:00:00"
            String temp = result.split("Max/Min")[0];
            temp = temp.split("Date = ")[1];
            temp = temp.split("T")[0];
            return temp;
        }

        @Override
        protected void onPostExecute(String[] results) {
            if (results != null) {
                resultsAdapter.clear();
                for (String result : results) {
                    resultsDetails.add(result);
                    String temp = getDateFromResult(result);
                    Log.v(LOG_TAG, temp);
                    resultsAdapter.add(temp);
                }
            }
        }
    }
}