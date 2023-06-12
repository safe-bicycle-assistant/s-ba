package com.safe_bicycle_assistant.s_ba.log_fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import com.safe_bicycle_assistant.s_ba.db_helpers.ManagementDB;
import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;
import com.safe_bicycle_assistant.s_ba.uis.RidingLogAdapter;

public class ManagementLogDetailsFragment extends Fragment {
    int index;

    final static String TAG= "RidingLogFragment";
    static ManagementDB managementDatabaseHelper;
    SQLiteDatabase db;
    ImageView toolsView;
    TextView tyresView;
    TextView timeView;
    TextView wheelsView;
    TextView brakesView;
    String bicycleName;


    public ManagementLogDetailsFragment(int index,String bicycleName) {
        this.index = index;
        this.bicycleName = bicycleName;
    }
    @Override
    public void onStart() {
        super.onStart();
        toolsView = getView().findViewById(R.id.mapImageView);
        timeView = getView().findViewById(R.id.timeTextView);
        wheelsView = getView().findViewById(R.id.wheelsTextView);
        tyresView = getView().findViewById(R.id.tyresTextView);
        brakesView = getView().findViewById(R.id.brakesTextView);

        managementDatabaseHelper = new ManagementDB(getContext(),1);
        Cursor c = managementDatabaseHelper.getDataByIndex(index,bicycleName);
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
//            String bitmap = c.getString(5);
//            mapView.setImageBitmap(Utils.string2Bitmap(bitmap));
            timeView.setText(""+Utils.DateToString( Utils.longToDate(c.getLong(0))));
            int managementBit = c.getInt(ManagementDB.CHANGE);
            if ((managementBit&ManagementDB.TYRES) != 0)
                tyresView.setText("O");
            else
                tyresView.setText("X");

            if ((managementBit&ManagementDB.WHEELS) != 0)
                wheelsView.setText("O");
            else
                wheelsView.setText("X");

            if ((managementBit&ManagementDB.BRAKES) != 0)
                brakesView.setText("O");
            else
                brakesView.setText("X");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_management_log_details, container, false);
    }


}
