package com.safe_bicycle_assistant.s_ba.log_fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.db_helpers.ManagementDB;
import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;
import com.safe_bicycle_assistant.s_ba.uis.ManagementLogAdapter;
import com.safe_bicycle_assistant.s_ba.uis.RidingLogAdapter;

public class RidingLogFragment extends Fragment {
    public final static String TAG= "RidingLogFragment";



    static RidingDB ridingDatabaseHelper;
    SQLiteDatabase Ridingdb;
    static ManagementDB managementDatabaseHelper;
    SQLiteDatabase Managementdb;
    ListView listView;
    String bicycleName;

    public RidingLogFragment(String bicycleName) {
        this.bicycleName = bicycleName;
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.riding_log_fragment_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        managementDatabaseHelper = new ManagementDB(getContext(),1);
        Managementdb = managementDatabaseHelper.getWritableDatabase();
        managementDatabaseHelper.onCreate(Managementdb);
        switch(item.getItemId()) {
            case R.id.ViewAll: {
                Cursor ridingCursor =ridingDatabaseHelper.getAllDataToCursor(bicycleName);
                listView.setAdapter(new RidingLogAdapter(getContext(), ridingCursor, true));
                ridingCursor.moveToFirst();
                int totalDistance = 0;
                while(ridingCursor.moveToNext())
                {
                    totalDistance += ridingCursor.getInt(RidingDB.LENGTH);
                    TextView tv = getView().findViewById(R.id.totalDistanceTextView);
                    tv.setText(""+totalDistance+" km");
                }
//                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                        ManagementLogDetailsFragment managementLogDetailsFragment = new ManagementLogDetailsFragment(i);
////                FrameLayout container = getView().findViewById(R.id.fragment_container);
//                        FragmentManager manager = getActivity().getSupportFragmentManager();
//                        FragmentTransaction ft = manager.beginTransaction();
//                        ft.replace(R.id.fragment_container, managementLogDetailsFragment);
//                        ft.addToBackStack(null);
//                        ft.commit();
//                    }
//                });
                return true;
            }
            case R.id.ViewAfterLatestManagement: {
                Cursor cursor = managementDatabaseHelper.getAllDataToCursor(bicycleName);
                cursor.moveToFirst();
                do
                {
                    int fixedBit = cursor.getInt(ManagementDB.CHANGE);
                    if((fixedBit & ManagementDB.TYRES) != 0)
                    {
                        Cursor ridingCursor = ridingDatabaseHelper.getAllDataAfterTime(cursor.getLong(ManagementDB.TIME));
                        listView.setAdapter(new RidingLogAdapter(getContext(), ridingCursor, true));
                        ridingCursor.moveToFirst();
                        int totalDistance = 0;
                        while(ridingCursor.moveToNext())
                        {
                            totalDistance += ridingCursor.getInt(RidingDB.LENGTH);
                            TextView tv = getView().findViewById(R.id.totalDistanceTextView);
                            tv.setText(""+totalDistance+" km");
                        }
                        return true;
                    }
                }while(cursor.moveToNext());
                //no tyre replacement
                Cursor ridingCursor =ridingDatabaseHelper.getAllDataToCursor(bicycleName);
                listView.setAdapter(new RidingLogAdapter(getContext(), ridingCursor, true));
                ridingCursor.moveToFirst();
                int totalDistance = 0;
                while(ridingCursor.moveToNext())
                {
                    totalDistance += ridingCursor.getInt(RidingDB.LENGTH);
                    TextView tv = getView().findViewById(R.id.totalDistanceTextView);
                    tv.setText(""+totalDistance+" km");
                }
                return false;
            }

        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        listView = getView().findViewById(R.id.ridingListView);

        ridingDatabaseHelper = new RidingDB(getContext(),1);
        Ridingdb = ridingDatabaseHelper.getWritableDatabase();
        ridingDatabaseHelper.onCreate(Ridingdb);
//        for(int i = 0; i< 10; i++)
//            ridingDatabaseHelper.insert(System.currentTimeMillis(),i+1,30.34,90.04,"HI",34.03,100.4);

        Cursor ridingCursor =ridingDatabaseHelper.getAllDataToCursor(bicycleName);
        listView.setAdapter(new RidingLogAdapter(getContext(), ridingCursor, true));
        ridingCursor.moveToFirst();
        int totalDistance = 0;
        while(ridingCursor.moveToNext())
        {
            totalDistance += ridingCursor.getInt(RidingDB.LENGTH);
            TextView tv = getView().findViewById(R.id.totalDistanceTextView);
            tv.setText(""+totalDistance+" km");
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: "+i+l);
                RidingLogDetailsFragment ridingLogFragment = new RidingLogDetailsFragment(i,bicycleName);
//                FrameLayout container = getView().findViewById(R.id.fragment_container);
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.fragment_container,ridingLogFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
//        listView.setAdapter(new ArrayAdapter<String>(getContext(),R.layout.riding_log_listview,ridingDatabaseHelper.getAllData()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_riding_log, container, false);
    }
}
