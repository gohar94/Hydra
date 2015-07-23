package com.example.gohar.hydra;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by goharirfan on 7/23/15.
 */
public class ResultAdapter extends CursorAdapter {

    public ResultAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_results, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Read result icon ID from cursor
        int resultID = cursor.getInt(ResultsFragment.COL_RESULT_ID);
        // Use placeholder image for now
        ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
//        iconView.setImageResource(R.drawable.ic_launcher);

        // Read date from cursor
        String dateString = cursor.getString(ResultsFragment.COL_RESULT_DATE);
        dateString = dateString.split("T")[0];
        // Find TextView and set formatted date on it
        TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        dateView.setText(Utility.getFriendlyDayString(context, dateString));

//        // Read weather forecast from cursor
//        String description = cursor.getString(ResultsFragment.COL);
//        // Find TextView and set weather forecast on it
//        TextView descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
//        descriptionView.setText(description);

        double high = cursor.getDouble(ResultsFragment.COL_RESULT_MAX_TEMP);
        TextView highView = (TextView) view.findViewById(R.id.list_item_high_textview);
        highView.setText(Utility.formatTemp(high));

        // Read low temperature from cursor
        double low = cursor.getDouble(ResultsFragment.COL_RESULT_MIN_TEMP);
        TextView lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
        lowView.setText(Utility.formatTemp(low));
    }

}
