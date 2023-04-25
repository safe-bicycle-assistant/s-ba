package com.safe_bicycle_assistant.s_ba.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.safe_bicycle_assistant.s_ba.log_fragments.MenuFragment;
import com.safe_bicycle_assistant.s_ba.R;

public class ManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        MenuFragment menuFragment = new MenuFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(R.id.fragment_container,menuFragment).commit();
    }
}
