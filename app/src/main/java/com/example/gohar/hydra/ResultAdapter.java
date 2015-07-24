package com.example.gohar.hydra;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by goharirfan on 7/23/15.
 */
public class ResultAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final String LOG_TAG = ResultAdapter.class.getSimpleName();

    public ResultAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_results_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_results;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                // Get weather icon
                if (cursor.getString(ResultsFragment.COL_RESULT_CONDITIONS_COND_CODE) == null) {
                    Log.v(LOG_TAG, "conditions code not present");
                    break;
                }
                boolean isColor = true;
                viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
                        cursor.getString(ResultsFragment.COL_RESULT_CONDITIONS_COND_CODE), isColor));
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                // Get weather icon
                if (cursor.getString(ResultsFragment.COL_RESULT_CONDITIONS_COND_CODE) == null) {
                    Log.v(LOG_TAG, "conditions code not present");
                    break;
                }
                boolean isColor = false;
                viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
                        cursor.getString(ResultsFragment.COL_RESULT_CONDITIONS_COND_CODE), isColor));
                break;
            }
        }

        // Read result icon ID from cursor
        int resultID = cursor.getInt(ResultsFragment.COL_RESULT_ID);

        // Read date from cursor
        String dateString = cursor.getString(ResultsFragment.COL_RESULT_DATE);
        dateString = dateString.split("T")[0];
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateString));

//        // Read weather forecast from cursor
//        String description = cursor.getString(ResultsFragment.COL);
//        // Find TextView and set weather forecast on it
//        TextView descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
//        descriptionView.setText(description);

        double high = cursor.getDouble(ResultsFragment.COL_RESULT_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemp(high));

        double low = cursor.getDouble(ResultsFragment.COL_RESULT_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemp(low));
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }

}
