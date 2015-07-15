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

    private ArrayAdapter<String> resultsAdapter;
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

        // Find the list view to populate
        ListView listView = (ListView) rootView.findViewById(R.id.listview_results);
        // Assign the adapter to the list view
        listView.setAdapter(resultsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String result = resultsAdapter.getItem(position);
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
        private String formatMaxMin(double max, double min) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(max);
            long roundedLow = Math.round(min);

            String highLowStr = roundedHigh + "/" + roundedLow + "°C";
            return highLowStr;
        }

        /**
         * Prepare the attribute precip for presentation.
         */
        private String formatPrecip(double precip) {
            long roundedPrecip = Math.round(precip);

            String finalPrecip = roundedPrecip + "mm";
            return finalPrecip;
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

            // These are the names of the JSON objects that need to be extracted.
            final String DAILY_ATTRIBUTES = "dailyAttributes";
            final String MAX_TEMPERATURE = "maxTemperature";
            final String MIN_TEMPERATURE = "minTemperature";
            final String PRECIP = "precip";
            final String DATE = "date";

            JSONArray resultsArray = new JSONArray(resultJsonStr);

            String[] resultStrs = new String[resultsArray.length()];
            for (int i = 0; i < resultsArray.length(); i++) {
                String maxAndMin = "";
                String precipitation = "";
                String date = "";
                Double maxTemperature = Double.NaN;
                Double minTemperature = Double.NaN;;
                Double precip = Double.NaN;

                // Get the JSON object representing the day
                JSONObject result = resultsArray.getJSONObject(i);

                if (result.has(DAILY_ATTRIBUTES)) {
                    JSONObject dailyAttributes = result.getJSONObject(DAILY_ATTRIBUTES);

                    if (dailyAttributes.getString(MAX_TEMPERATURE) != "null")
                        maxTemperature = dailyAttributes.getDouble(MAX_TEMPERATURE);

                    if (dailyAttributes.getString(MIN_TEMPERATURE) != "null")
                        minTemperature = dailyAttributes.getDouble(MIN_TEMPERATURE);

                    if (dailyAttributes.getString(PRECIP) != "null")
                        precip = dailyAttributes.getDouble(PRECIP);

                    maxAndMin = formatMaxMin(maxTemperature, minTemperature);
                    precipitation = formatPrecip(precip);
                } else {
                    String errorMessage = "Incomplete information received!";
                    resultStrs[i] = errorMessage;
                    Log.e(LOG_TAG, errorMessage);
                }

                if (result.getString(DATE) != "null")
                    date = result.getString(DATE);

                resultStrs[i] = date + " - " + precipitation + " - " + maxAndMin;
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

        private String[] getDataFromAPI(String latitude, String longitude, String date, String token) {
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
                final String LATITUDE = "latitude";
                final String LONGITUDE = "longitude";
                final String START_DATE = "startDate";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(LATITUDE, latitude)
                        .appendQueryParameter(LONGITUDE, longitude)
                        .appendQueryParameter(START_DATE, date)
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

        @Override
        protected void onPostExecute(String[] results) {
            if (results != null) {
                resultsAdapter.clear();
                for (String result : results) {
                    resultsAdapter.add(result);
                }
            }
        }
    }
}