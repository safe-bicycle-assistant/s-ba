package com.safe_bicycle_assistant.s_ba.log_fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.safe_bicycle_assistant.s_ba.R;

public class MenuFragment extends Fragment {
    ImageView managementView;
    ImageView ridingView;
    String bicycleName;
    public MenuFragment(String bicycleName){
        this.bicycleName = bicycleName;
    }
    @Override
    public void onStart() {
        super.onStart();
        managementView = getView().findViewById(R.id.managementView);
        ridingView = getView().findViewById(R.id.ridingView);
        managementView.setImageResource(R.drawable.management_icon);
        ridingView.setImageResource(R.drawable.riding_icon);
        managementView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ManagementLogFragment managementLogFragment = new ManagementLogFragment(bicycleName);
//                FrameLayout container = getView().findViewById(R.id.fragment_container);
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.fragment_container,managementLogFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        ridingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RidingLogFragment ridingLogFragment = new RidingLogFragment(bicycleName);
//                FrameLayout container = getView().findViewById(R.id.fragment_container);
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.fragment_container,ridingLogFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }
}
