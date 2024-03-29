package com.example.gohar.hydra;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private final String LOG_TAG = MapsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        if (mMap == null) {
            // Happens when play services are not installed/updated
            // This takes to the page to install/update them and prevents from crashing
            Toast.makeText(this, R.string.toast_error_no_map, Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Map is null");
            return;
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(final LatLng point) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle(R.string.alert_map_title);
                builder.setMessage(getString(R.string.alert_map_latitude) + ": " + point.latitude + "\n" + getString(R.string.alert_map_longitude) + ": " + point.longitude);

                // Set up the buttons
                builder.setPositiveButton(R.string.alert_map_ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(getString(R.string.pref_location_latitude_key), Double.toString(point.latitude));
                        editor.putString(getString(R.string.pref_location_longitude_key), String.valueOf(point.longitude));
                        editor.apply();
                        Intent i = new Intent(getBaseContext(), UserInputActivity.class);
                        startActivity(i);
                    }
                });
                builder.setNegativeButton(R.string.alert_map_cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

    }

//    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.

            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(31, 71), 5.0f) );
    }

}
