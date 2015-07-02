package com.example.gohar.hydra;

/**
 * Created by Gohar on 03/07/15.
 */

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ResultsFragment extends Fragment {

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchResultsTask fetchResultsTask = new FetchResultsTask();
            fetchResultsTask.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Array of data to display on the list
        String[] resultsArray = {
                "Today - Sunny",
                "Tomorrow - Cloudy",
                "Wed - Windy",
                "Thur - Sunny",
                "Fri - Rainy",
                "Sat - Rainy",
                "Sun - Overcast"
        };

        // ArrayList of data to display on the list
        List<String> results = new ArrayList<String>(Arrays.asList(resultsArray));

        // Adapter of data to display on the list
        ArrayAdapter<String> resultsAdapter = new ArrayAdapter<String>(
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

        return rootView;
    }

    public class FetchResultsTask extends AsyncTask<Void, Void, Void> {
        private final String LOG_TAG = FetchResultsTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String resultJsonStr = null;

            try {
                // Construct the URL for the API query
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=Lahore,pk&mode=json&unit=metric&cnt=7");

                // Create the request to the API, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
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
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                resultJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Result from API is: " + resultJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
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
            return null;
        }
    }
}