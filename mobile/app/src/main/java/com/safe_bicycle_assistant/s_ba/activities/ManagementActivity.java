package com.safe_bicycle_assistant.s_ba.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;

import com.safe_bicycle_assistant.s_ba.log_fragments.MenuFragment;
import com.safe_bicycle_assistant.s_ba.R;

public class ManagementActivity extends AppCompatActivity {
    private String bicycleName;
    private final String TAG = "ManagementActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        bicycleName = getIntent().getStringExtra("name");
        Log.d(TAG, "onCreate: "+ bicycleName);
        MenuFragment menuFragment = new MenuFragment(bicycleName);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(R.id.fragment_container,menuFragment).commit();
    }
}
