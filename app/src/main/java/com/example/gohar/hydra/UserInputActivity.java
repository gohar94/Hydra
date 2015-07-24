package com.example.gohar.hydra;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class UserInputActivity extends Activity {

    Button mButton;
    EditText mEdit;
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
                mEdit = (EditText) findViewById(R.id.edit_user_input_planting_date);
                String plantDate = mEdit.getText().toString();
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(UserInputActivity.this);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(getString(R.string.pref_plant_date), plantDate);
                editor.apply();
                Log.v(LOG_TAG, "done " + plantDate);
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                startActivity(i);
            }
        });
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
}