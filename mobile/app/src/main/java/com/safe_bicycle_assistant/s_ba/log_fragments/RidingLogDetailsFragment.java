package com.safe_bicycle_assistant.s_ba.log_fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.Utils;
import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;
import com.safe_bicycle_assistant.s_ba.uis.RidingLogAdapter;

public class RidingLogDetailsFragment extends Fragment {
    int index;

    final static String TAG= "RidingLogFragment";
    static RidingDB ridingDatabaseHelper;
    SQLiteDatabase db;
    ImageView mapView;
    TextView timeView;
    TextView distanceView;
    TextView maxSpeedView;
    TextView avgSpeedView;
    TextView maxCadenceView;
    TextView avgCadenceView;

    public RidingLogDetailsFragment(int index) {
        this.index = index;
    }
    @Override
    public void onStart() {
        super.onStart();
        mapView = getView().findViewById(R.id.mapImageView);
        timeView = getView().findViewById(R.id.timeTextView);
        distanceView = getView().findViewById(R.id.lengthTextView);
        maxSpeedView = getView().findViewById(R.id.maxSpeedTextView);
        avgCadenceView = getView().findViewById(R.id.avgCadenceTextView);
        avgSpeedView = getView().findViewById(R.id.avgSpeedTextView);
        maxCadenceView = getView().findViewById(R.id.maxCadenceTextView);
        ridingDatabaseHelper = new RidingDB(getContext(),1);
        Cursor c = ridingDatabaseHelper.getDataByIndex(index);
//        Cursor c = ridingDatabaseHelper.getAllDataToCursor();
//        c.moveToFirst();
//        for(int i = 0; i< index; i++)
//        {
//            c.moveToNext();
//        }
        Log.d(TAG, Utils.longToDate(c.getInt(1)).toString());
        if(c == null)
        {

        }
        else
        {
            String bitmap = c.getString(RidingDB.MAP);
            mapView.setImageBitmap(Utils.string2Bitmap(bitmap));
            timeView.setText(""+Utils.DateToString( Utils.longToDate(c.getLong(0))));
            distanceView.setText(""+c.getInt(RidingDB.LENGTH)+" km");
            maxSpeedView.setText(""+c.getDouble(RidingDB.MAX_SPEED)+" km/h");
            avgSpeedView.setText(""+c.getDouble(RidingDB.AVERAGE_SPEED)+" km/h");
            avgCadenceView.setText(""+c.getDouble(RidingDB.AVERAGE_CADENCE)+" rpm");
            maxCadenceView.setText(""+c.getDouble(RidingDB.MAX_CADENCE)+" rpm");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_riding_log_details, container, false);
    }
}
