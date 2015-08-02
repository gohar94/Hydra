package com.example.gohar.hydra;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;


public class UserInputActivity extends Activity {

    Button mButton;
    EditText mEdit;
    EditText mEditSoilMoisture;
    TextView mText;
    String latitude;
    String longitude;
    private final String LOG_TAG = ResultsFragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_input);

        mButton = (Button)findViewById(R.id.button_submit_user_input);

        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mEditSoilMoisture = (EditText) findViewById(R.id.edit_user_input_soil_moisture);
                String soilMoisture = mEditSoilMoisture.getText().toString();

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(UserInputActivity.this);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(getString(R.string.pref_soil_moisture), soilMoisture);

                editor.apply();
                Log.v(LOG_TAG, "Soil Moisture chosen is = " + soilMoisture);
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                startActivity(i);
            }
        });
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_input, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private final String LOG_TAG = ResultsFragment.class.getSimpleName();

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            String plantDate = "";
            String strYear = new String(Integer.toString(year));
            String strMonth = new String(Integer.toString(month));
            String strDay = new String(Integer.toString(day));

            // padding with 0 to make format YYYY-MM-DD
            if (month < 10) {
                strMonth = "0" + strMonth;
            }

            if (day < 10) {
                strDay = "0" + strDay;
            }

            plantDate = strYear + "-" + strMonth + "-" + strDay;

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(getString(R.string.pref_plant_date), plantDate);
            editor.apply();
            Log.v(LOG_TAG, "Date Picked = " + plantDate);
        }
    }
}