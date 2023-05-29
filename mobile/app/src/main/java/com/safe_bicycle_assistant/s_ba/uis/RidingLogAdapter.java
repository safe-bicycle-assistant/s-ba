package com.safe_bicycle_assistant.s_ba.uis;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.Utils;
import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;

public class RidingLogAdapter extends CursorAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public RidingLogAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View v = mLayoutInflater.inflate(R.layout.riding_log_listview,viewGroup,false);
        return v;
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        long time_millis = cursor.getLong(RidingDB.TIME);
        int length = cursor.getInt(RidingDB.LENGTH);
        double avgSpeed = cursor.getDouble(RidingDB.AVERAGE_SPEED);
        TextView timeTextView = view.findViewById(R.id.riding_log_listview_time);
        TextView lengthTextView = view.findViewById(R.id.riding_log_listview_length);
        TextView avgSpeedTextView = view.findViewById(R.id.riding_log_listview_avgspeed);
        timeTextView.setText("Workout Time : "+ Utils.DateToString(Utils.longToDate(time_millis)));
        lengthTextView.setText("Riding Length : " + length + "km");
        avgSpeedTextView.setText("Average Speed : " + avgSpeed+"km/h");
    }
}
