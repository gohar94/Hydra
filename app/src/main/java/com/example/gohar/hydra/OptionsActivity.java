package com.example.gohar.hydra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class OptionsActivity extends ActionBarActivity {
    Button btnShowLocation;
    Button btnOpenMaps;

    // GPSTracker class
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        btnOpenMaps= (Button) findViewById(R.id.btnOpenMaps);

        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                gps = new GPSTracker(OptionsActivity.this);

                // check if GPS enabled
                if (gps.canGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();


                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(OptionsActivity.this);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("latitude", Double.toString(latitude));
                    editor.putString("longitude", String.valueOf(longitude));
                    editor.apply();


                    // \n is for new line
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                    Intent i=new Intent(getBaseContext(), MainActivity.class);
                    startActivity(i);
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

            }
        });

        // show location button click event
        btnOpenMaps.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // \n is for new line
                Toast.makeText(getApplicationContext(), "Open Maps clicked", Toast.LENGTH_SHORT).show();
                Intent i=new Intent(getBaseContext(), MapsActivity.class);
                startActivity(i);
            }
        });


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);
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
}
