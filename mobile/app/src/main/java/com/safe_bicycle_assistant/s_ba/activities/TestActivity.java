package com.safe_bicycle_assistant.s_ba.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.Services.ConnectionService;

public class TestActivity extends AppCompatActivity {
    TextView cadenceTextView;
    TextView detectionTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        cadenceTextView = findViewById(R.id.cadenceTextView);
        detectionTextView = findViewById(R.id.detectionTextView);
        Intent intent = new Intent(getApplicationContext(), ConnectionService.class);
        Bundle bundle = new Bundle();

        startService(intent);
    }
}
