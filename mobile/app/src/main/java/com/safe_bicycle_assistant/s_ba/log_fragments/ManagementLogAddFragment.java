package com.safe_bicycle_assistant.s_ba.log_fragments;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.Utils;
import com.safe_bicycle_assistant.s_ba.db_helpers.ManagementDB;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ManagementLogAddFragment extends DialogFragment {
    TextView tyreView;
    TextView wheelView;
    TextView brakesView;
    DatePicker datePicker;
    Button addButton;
    TimePicker timePicker;
    String bicycleName;

    public ManagementLogAddFragment(String bicycleName) {
        this.bicycleName = bicycleName;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        tyreView = getView().findViewById(R.id.tyresView);
        wheelView = getView().findViewById(R.id.wheelsView);
        brakesView = getView().findViewById(R.id.brakesView);
        addButton = getView().findViewById(R.id.buttonAdd);
        datePicker = getView().findViewById(R.id.datePicker);
        timePicker = getView().findViewById(R.id.timePicker);

        tyreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tyreView.getText().toString().equals("X"))
                    tyreView.setText("O");
                else
                    tyreView.setText("X");
            }
        });
        wheelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(wheelView.getText().toString().equals("X"))
                    wheelView.setText("O");
                else
                    wheelView.setText("X");
            }
        });
        brakesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(brakesView.getText().toString().equals("X"))
                    brakesView.setText("O");
                else
                    brakesView.setText("X");
            }
        });

        Calendar c = Calendar.getInstance();
        datePicker.updateDate(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DATE));
//        timePicker.setIs24HourView(true);
        timePicker.setHour(c.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(c.get(Calendar.MINUTE));

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(datePicker.getYear(),datePicker.getMonth(),datePicker.getDayOfMonth(),timePicker.getHour(),timePicker.getMinute());
                long millis = Utils.dateToLong(calendar.getTime());
                int fixBit = 0;
                if(brakesView.getText().toString().equals("O"))
                    fixBit = fixBit | ManagementDB.BRAKES;
                if(wheelView.getText().toString().equals("O"))
                    fixBit = fixBit | ManagementDB.WHEELS;
                if(tyreView.getText().toString().equals("O"))
                    fixBit = fixBit | ManagementDB.TYRES;
                ManagementDB managementDB = new ManagementDB(getContext(),1);

                managementDB.insert(millis,fixBit,bicycleName);
                FragmentManager fm = getActivity().getSupportFragmentManager();
//                fm.beginTransaction().remove(ManagementLogAddFragment.this).commit();
                fm.beginTransaction().replace(R.id.fragment_container,new ManagementLogFragment(bicycleName)).commit();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_management_log_add, container, false);
    }
}
