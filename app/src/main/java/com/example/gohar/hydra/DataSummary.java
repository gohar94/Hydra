package com.example.gohar.hydra;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Arrays;


public class DataSummary extends Activity {

    private final String LOG_TAG = DataSummary.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_summary);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        DataPoint[] points = new DataPoint[7];
        double soilMoisture = Utility.getSoilMoisture(this);

        for (int i = 0; i < 7; i++) {
            double val = 0;
            if (i == 0 ) {
                val = soilMoisture;
            } else {
                val = soilMoisture-FetchResultsTask.etcList.get(i);
                soilMoisture = soilMoisture-FetchResultsTask.etcList.get(i);
            }
            points[i] = new DataPoint(new Double(i).doubleValue(), val);
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(points);
        graph.setTitle("Changes in Soil Moisture Level");

        // use static labels for horizontal and vertical labels
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        Object[] objectDates = FetchResultsTask.dates.toArray();
        String[] stringArray = Arrays.copyOf(objectDates, Math.min(objectDates.length, 7), String[].class);
        String[] stringArrayFormated = new String[7];
        for (int i = 0; i < Math.min(objectDates.length, 7); i++) {
            stringArrayFormated[i] = Utility.getDayName(this, stringArray[i]);
        }
        staticLabelsFormatter.setHorizontalLabels(stringArrayFormated);
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        graph.addSeries(series);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_data_summary, menu);
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
