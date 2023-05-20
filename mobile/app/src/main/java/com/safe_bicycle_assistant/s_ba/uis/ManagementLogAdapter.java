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

public class ManagementLogAdapter extends CursorAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public ManagementLogAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View v = mLayoutInflater.inflate(R.layout.management_log_listview,viewGroup,false);
        return v;
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        long time_millis = cursor.getLong(0);
        int fix_flag = cursor.getInt(1);
        TextView timeTextView = view.findViewById(R.id.management_log_listview_time);
        TextView numberTextView = view.findViewById(R.id.management_log_listview_num);
        timeTextView.setText("Management Time : "+ Utils.DateToString(Utils.longToDate(time_millis)));
        int num = (fix_flag | 1) + ((fix_flag | 2)>>1) + ((fix_flag | 2)>>2) ;
        numberTextView.setText("number of parts fixed : " + num);
    }
}
