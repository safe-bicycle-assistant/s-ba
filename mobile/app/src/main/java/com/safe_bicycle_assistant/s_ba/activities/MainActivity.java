package com.safe_bicycle_assistant.s_ba.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;
import android.os.Bundle;
import android.view.View;

import com.safe_bicycle_assistant.s_ba.R;




public class MainActivity extends AppCompatActivity {
    ListView listView;
    static RidingDB ridingDatabaseHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bt = findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ManagementActivity.class);
                startActivity(intent);
            }
        });

        Button bt2 = findViewById(R.id.button2);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),TestActivity.class);
                startActivity(intent);
            }
        });
        Button bt3= findViewById(R.id.button_map);
        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
                MainActivity.this.startActivity(mapIntent);
            }
        });

        ImageView img = findViewById(R.id.imageView);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //use SpinnableImageView Class
            }
        });

    }

//    public void openMap(View view) {
//
//    }
}

