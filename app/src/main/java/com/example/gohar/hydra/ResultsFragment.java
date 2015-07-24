package com.example.gohar.hydra;

/**
 * Created by Gohar on 03/07/15.
 */

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.gohar.hydra.data.ResultContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ResultsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = ResultsFragment.class.getSimpleName();

    private ResultAdapter resultsAdapter;

    private static final int FORECAST_LOADER = 0;
    private String mLatitude;
    private String mLongitude;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] DISPLAY_COLUMNS = {
            ResultContract.ResultEntry.TABLE_NAME + "." + ResultContract.ResultEntry._ID,
            ResultContract.ResultEntry.COLUMN_DATE,
            ResultContract.ResultEntry.COLUMN_MAX_TEMP,
            ResultContract.ResultEntry.COLUMN_MIN_TEMP,
            ResultContract.ResultEntry.COLUMN_CONDITIONS_COND_CODE,
            ResultContract.LocationEntry.COLUMN_LATITUDE,
            ResultContract.LocationEntry.COLUMN_LONGITUDE
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_RESULT_ID = 0;
    public static final int COL_RESULT_DATE = 1;
    public static final int COL_RESULT_MAX_TEMP = 2;
    public static final int COL_RESULT_MIN_TEMP = 3;
    public static final int COL_RESULT_CONDITIONS_COND_CODE = 4;
    public static final int COL_LATITUDE = 5;
    public static final int COL_LONGITUDE = 6;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

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
        FetchResultsTask fetchResultsTask = new FetchResultsTask(getActivity());

        // {latitude, longitude}
        String[] location = Utility.getPrefferedLocation(getActivity());
        String currentDate = Utility.getCurrentDate();
        String plantDate = Utility.getPlantDate(getActivity());

        Log.v(LOG_TAG, "Fetching data for date = " + currentDate);

        fetchResultsTask.execute(location[0], location[1], plantDate, currentDate);
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
    public void onResume() {
        super.onResume();
        if (mLatitude != null && !mLatitude.equals(Utility.getPrefferedLocation(getActivity())[0])) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        } else if (mLongitude != null && !mLongitude.equals(Utility.getPrefferedLocation(getActivity())[1])) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateResults();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        resultsAdapter = new ResultAdapter(getActivity(), null, 0);

        // Get a reference to the ListView, and attach this adapter to it.
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_results);
        listView.setAdapter(resultsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = resultsAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .putExtra(DetailActivity.DATE_KEY, cursor.getString(COL_RESULT_DATE));
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by date.
        String sortOrder = ResultContract.ResultEntry.COLUMN_DATE + " ASC";

        String[] location = Utility.getPrefferedLocation(getActivity());
        mLatitude = location[0];
        mLongitude = location[1];

        Uri resultForLocationUri = ResultContract.ResultEntry.buildResultLocationWithStartDate(
                mLatitude, mLongitude, Utility.getCurrentDate());

        Log.v(LOG_TAG, "in on create loader");

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                resultForLocationUri,
                DISPLAY_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        resultsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        resultsAdapter.swapCursor(null);
    }
}