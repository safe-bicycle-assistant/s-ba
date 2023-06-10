package com.safe_bicycle_assistant.s_ba.log_fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.db_helpers.ManagementDB;
import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;
import com.safe_bicycle_assistant.s_ba.uis.ManagementLogAdapter;

public class ManagementLogFragment extends Fragment {

    static ManagementDB managementDatabaseHelper;
    SQLiteDatabase db;
    ListView listView;



    @Override
    public void onStart() {
        super.onStart();
        listView = getView().findViewById(R.id.managementListView);

        managementDatabaseHelper = new ManagementDB(getContext(),1);
        db = managementDatabaseHelper.getWritableDatabase();
        managementDatabaseHelper.onCreate(db);
//        for(int i = 0; i< 10; i++)
//            managementDatabaseHelper.insert(System.currentTimeMillis(),ManagementDB.BRAKES | ManagementDB.TYRES);
        listView.setAdapter(new ManagementLogAdapter(getContext(),managementDatabaseHelper.getAllDataToCursor(),true));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ManagementLogDetailsFragment managementLogDetailsFragment = new ManagementLogDetailsFragment(i);
//                FrameLayout container = getView().findViewById(R.id.fragment_container);
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.fragment_container,managementLogDetailsFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        Button addButton = getView().findViewById(R.id.buttonAdd);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment  Fragment = new ManagementLogAddFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.fragment_container,Fragment);
                ft.commit();

            }
        });
//        listView.setAdapter(new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,managementDatabaseHelper.getAllData()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_management_log, container, false);
    }
}
