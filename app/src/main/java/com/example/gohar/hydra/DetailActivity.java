package com.example.gohar.hydra;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.gohar.hydra.data.ResultContract;


public class DetailActivity extends ActionBarActivity {

    public static final String DATE_KEY = "forecast_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

        public static final int DETAIL_LOADER = 0;
        private static final String LATITUDE_KEY = "latitude";
        private static final String LONGITUDE_KEY = "longitude";
        private String mLatitude;
        private String mLongitude;
        private ShareActionProvider mShareActionProvider;

        String[] DETAILS_COLUMNS = {
                ResultContract.ResultEntry.TABLE_NAME + "." + ResultContract.ResultEntry._ID,
                ResultContract.ResultEntry.COLUMN_DATE,
                ResultContract.ResultEntry.COLUMN_MAX_TEMP,
                ResultContract.ResultEntry.COLUMN_MIN_TEMP,
                ResultContract.LocationEntry.COLUMN_LATITUDE,
                ResultContract.LocationEntry.COLUMN_LONGITUDE
        };


        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final String DETAIL_SHARE_HASHTAG = " #Hydra";
        private String resultString;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
            if (intent == null || !intent.hasExtra(DATE_KEY)) {
                return null;
            }
            String date = intent.getStringExtra(DATE_KEY);

            // Sort order:  Ascending, by date.
            String sortOrder = ResultContract.ResultEntry.COLUMN_DATE + " ASC";

            mLatitude = Utility.getPrefferedLocation(getActivity())[0];
            mLongitude = Utility.getPrefferedLocation(getActivity())[1];
            Uri weatherForLocationUri = ResultContract.ResultEntry.buildResultLocationWithDate(
                    mLatitude, mLongitude, date);
            Log.v(LOG_TAG, weatherForLocationUri.toString());

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    weatherForLocationUri,
                    DETAILS_COLUMNS,
                    null,
                    null,
                    sortOrder
            );
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mLatitude != null &&
                    !mLatitude.equals(Utility.getPrefferedLocation(getActivity())[0])) {
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
                Log.v(LOG_TAG, "changed latitude");
            } else if (mLongitude != null &&
                    !mLongitude.equals(Utility.getPrefferedLocation(getActivity())[1])) {
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
                Log.v(LOG_TAG, "changed longitude");
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (null != mLongitude)
                outState.putString(LONGITUDE_KEY, mLongitude);

            if (null != mLatitude)
                outState.putString(LATITUDE_KEY, mLatitude);

            super.onSaveInstanceState(outState);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v(LOG_TAG, "In onLoadFinished");
            if (!data.moveToFirst()) { return; }

            String dateString = data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_DATE));
            ((TextView) getView().findViewById(R.id.detail_date_textview))
                    .setText(dateString);

            String maxTemperature = data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_MAX_TEMP));
            ((TextView) getView().findViewById(R.id.detail_high_textview))
                    .setText(maxTemperature);

            String minTemperature = data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_MIN_TEMP));
            ((TextView) getView().findViewById(R.id.detail_low_textview))
                    .setText(minTemperature);

            // We still need this for the share intent
            resultString = String.format("%s - %s/%s", dateString, maxTemperature, minTemperature);

            Log.v(LOG_TAG, "Forecast String: " + resultString);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareDetailIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_detail, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            if (savedInstanceState != null) {
                mLatitude = savedInstanceState.getString(LATITUDE_KEY);
                mLongitude = savedInstanceState.getString(LONGITUDE_KEY);
            }
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);

            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent.
            mShareActionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // Attach an intent to this ShareActionProvider.  You can update this at any time,
            // like when the user selects a new piece of data they might like to share.
            if (mShareActionProvider != null ) {
                mShareActionProvider.setShareIntent(createShareDetailIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null?");
            }
        }

        private Intent createShareDetailIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            // So that we get back to Hydra app after we are done with the share app
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    resultString + DETAIL_SHARE_HASHTAG);
            return shareIntent;
        }
    }
}
