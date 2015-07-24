package com.example.gohar.hydra;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gohar.hydra.data.ResultContract;

/**
 * Created by goharirfan on 7/23/15.
 */
public class DetailFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    public static final int DETAIL_LOADER = 0;
    public static final String DATE_KEY = "forecast_date";
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
            ResultContract.ResultEntry.COLUMN_PRECIP,
            ResultContract.ResultEntry.COLUMN_ACC_PRECIP,
            ResultContract.ResultEntry.COLUMN_ACC_PRECIP_PRIOR_YEAR,
            ResultContract.ResultEntry.COLUMN_ACC_PRECIP_3_YEAR_AVERAGE,
            ResultContract.ResultEntry.COLUMN_ACC_PRECIP_LONG_TERM_AVERAGE,
            ResultContract.ResultEntry.COLUMN_SOLAR,
            ResultContract.ResultEntry.COLUMN_MIN_HUMIDITY,
            ResultContract.ResultEntry.COLUMN_MAX_HUMIDITY,
            ResultContract.ResultEntry.COLUMN_MORN_WIND,
            ResultContract.ResultEntry.COLUMN_MAX_WIND,
            ResultContract.ResultEntry.COLUMN_GDD,
            ResultContract.ResultEntry.COLUMN_ACC_GDD,
            ResultContract.ResultEntry.COLUMN_ACC_GDD_PRIOR_YEAR,
            ResultContract.ResultEntry.COLUMN_ACC_GDD_3_YEAR_AVERAGE,
            ResultContract.ResultEntry.COLUMN_ACC_GDD_LONG_TERM_AVERAGE,
            ResultContract.ResultEntry.COLUMN_PET,
            ResultContract.ResultEntry.COLUMN_ACC_PET,
            ResultContract.ResultEntry.COLUMN_PPET,
            ResultContract.ResultEntry.COLUMN_CONDITIONS_COND_CODE,
            ResultContract.ResultEntry.COLUMN_CONDITIONS_COND_TEXT,
            ResultContract.LocationEntry.COLUMN_LATITUDE,
            ResultContract.LocationEntry.COLUMN_LONGITUDE
    };


    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String DETAIL_SHARE_HASHTAG = " #HydraApp";
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
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mMaxHumidityView;
    private TextView mMinHumidityView;
    private TextView mMaxWindView;
    private TextView mMornWindView;
    private TextView mPressureView;
    private TextView mPrecip;
    private TextView mAccPrecip;
    private TextView mAccPrecipPriorYear;
    private TextView mAccPrecip3YearAverage;
    private TextView mAccPrecipLongTermAverage;
    private TextView mSolar;
    private TextView mGdd;
    private TextView mAccGdd;
    private TextView mAccGddPriorYear;
    private TextView mAccGdd3YearAverage;
    private TextView mAccGddLongTermAverage;
    private TextView mPet;
    private TextView mAccPet;
    private TextView mPpet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mMaxHumidityView = (TextView) rootView.findViewById(R.id.detail_max_humidity_textview);
        mMinHumidityView = (TextView) rootView.findViewById(R.id.detail_min_humidity_textview);
        mMaxWindView = (TextView) rootView.findViewById(R.id.detail_max_wind_textview);
        mMornWindView = (TextView) rootView.findViewById(R.id.detail_morn_wind_textview);
        mPrecip = (TextView) rootView.findViewById(R.id.detail_precip_textview);
        mAccPrecip = (TextView) rootView.findViewById(R.id.detail_accPrecip_textview);
        mAccPrecipPriorYear = (TextView) rootView.findViewById(R.id.detail_accPrecipPriorYear_textview);
        mAccPrecip3YearAverage = (TextView) rootView.findViewById(R.id.detail_accPrecip3YearAverage_textview);
        mAccPrecipLongTermAverage = (TextView) rootView.findViewById(R.id.detail_accPrecipLongTermAverage_textview);
        mSolar = (TextView) rootView.findViewById(R.id.detail_solar_textview);
        mGdd = (TextView) rootView.findViewById(R.id.detail_gdd_textview);
        mAccGdd = (TextView) rootView.findViewById(R.id.detail_accGdd_textview);
        mAccGddPriorYear = (TextView) rootView.findViewById(R.id.detail_accGddPriorYear_textview);
        mAccGdd3YearAverage = (TextView) rootView.findViewById(R.id.detail_accGdd3YearAverage_textview);
        mAccGddLongTermAverage = (TextView) rootView.findViewById(R.id.detail_accGddLongTermAverage_textview);
        mPet = (TextView) rootView.findViewById(R.id.detail_pet_textview);
        mAccPet = (TextView) rootView.findViewById(R.id.detail_accPet_textview);
        mPpet = (TextView) rootView.findViewById(R.id.detail_ppet_textview);

        return rootView;
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        String dateString = data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_DATE));
        mDateView.setText(Utility.getFormattedMonthDay(getActivity(), dateString));
        mFriendlyDateView.setText(Utility.getFriendlyDayString(getActivity(), dateString));

        boolean isDetail = true;
        mIconView.setImageResource(Utility.getIconResourceForWeatherCondition(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_CONDITIONS_COND_CODE)), isDetail));

        Double maxTemperature = data.getDouble(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_MAX_TEMP));
        mHighTempView.setText(Utility.formatTemp(maxTemperature));

        Double minTemperature = data.getDouble(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_MIN_TEMP));
        mLowTempView.setText(Utility.formatTemp(minTemperature));

        Double maxHumidity = data.getDouble(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_MAX_HUMIDITY));
        mMaxHumidityView.setText(Utility.formatPercentage(maxHumidity));

        Double minHumidity = data.getDouble(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_MIN_HUMIDITY));
        mMinHumidityView.setText(Utility.formatPercentage(minHumidity));

        Double mornWind = data.getDouble(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_MORN_WIND));
        mMornWindView.setText(Utility.formatMeterPerSecond(mornWind));

        Double maxWind = data.getDouble(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_MAX_WIND));
        mMaxWindView.setText(Utility.formatMeterPerSecond(maxWind));

        String description = data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_CONDITIONS_COND_TEXT));
        mDescriptionView.setText(description);

        mPrecip.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_PRECIP)));
        mAccPrecip.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_ACC_PRECIP)));
        mAccPrecipPriorYear.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_ACC_PRECIP_PRIOR_YEAR)));
        mAccPrecip3YearAverage.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_ACC_PRECIP_3_YEAR_AVERAGE)));
        mAccPrecipLongTermAverage.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_ACC_PRECIP_LONG_TERM_AVERAGE)));
        mSolar.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_SOLAR)));
        mGdd.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_GDD)));
        mAccGdd.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_ACC_GDD)));
        mAccGddPriorYear.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_ACC_GDD_PRIOR_YEAR)));
        mAccGdd3YearAverage.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_ACC_GDD_3_YEAR_AVERAGE)));
        mAccGddLongTermAverage.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_ACC_GDD_LONG_TERM_AVERAGE)));
        mPet.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_PET)));
        mAccPet.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_ACC_PET)));
        mPpet.setText(data.getString(data.getColumnIndex(ResultContract.ResultEntry.COLUMN_PPET)));

        // We still need this for the share intent
        resultString = String.format("%s\n %s - %s/%s", dateString, description, Utility.formatTemp(maxTemperature), Utility.formatTemp(minTemperature));

        Log.v(LOG_TAG, "Forecast String: " + resultString);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareDetailIntent());
        }
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