package com.safe_bicycle_assistant.s_ba.log_fragments;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;

public class RidingLogFragment extends Fragment {
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
            ridingDatabaseHelper.insert(System.currentTimeMillis(),i+1,30.34,90.04);

        listView.setAdapter(new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,ridingDatabaseHelper.getAllData()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_riding_log, container, false);
    }
}
