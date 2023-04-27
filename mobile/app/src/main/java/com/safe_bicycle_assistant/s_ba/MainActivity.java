package com.safe_bicycle_assistant.s_ba;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openMap(View view) {
        Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
        MainActivity.this.startActivity(mapIntent);
    }
}
