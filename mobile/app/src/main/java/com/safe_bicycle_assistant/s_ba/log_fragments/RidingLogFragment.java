package com.safe_bicycle_assistant.s_ba.log_fragments;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;
import com.safe_bicycle_assistant.s_ba.uis.RidingLogAdapter;

public class RidingLogFragment extends Fragment {
    public final static String TAG= "RidingLogFragment";



    static RidingDB ridingDatabaseHelper;
    SQLiteDatabase db;
    ListView listView;
    @Override
    public void onStart() {
        super.onStart();
        listView = getView().findViewById(R.id.ridingListView);

        ridingDatabaseHelper = new RidingDB(getContext(),1);
        db = ridingDatabaseHelper.getWritableDatabase();
        ridingDatabaseHelper.onCreate(db);
        for(int i = 0; i< 10; i++)
            ridingDatabaseHelper.insert(System.currentTimeMillis(),i+1,30.34,90.04,"HI",34.03,100.4);

        listView.setAdapter(new RidingLogAdapter(getContext(), ridingDatabaseHelper.getAllDataToCursor(), true));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: "+i+l);
                RidingLogDetailsFragment ridingLogFragment = new RidingLogDetailsFragment(i);
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
