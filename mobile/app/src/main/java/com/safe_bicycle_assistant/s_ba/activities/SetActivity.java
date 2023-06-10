package com.safe_bicycle_assistant.s_ba.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.safe_bicycle_assistant.s_ba.R;

public class SetActivity extends AppCompatActivity {
    static final String TAG = "//*SetActivity*//";// SetActivity 호출을 위한 요청 코드
    private static final int MAX_NICKNAMES=5;
    public static final int REQUEST_CODE = 1;
    public static final String EXTRA_BIKE_NAME = "bike_name";

    private EditText editText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        Log.d(TAG, "onCreate: 10");

        editText = findViewById(R.id.editText);
        saveButton = findViewById(R.id.RegButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bikeName = editText.getText().toString().trim();
                Log.d(TAG, "onClick: 11");
                if (bikeName.trim().isEmpty()) {
                    Toast.makeText(SetActivity.this, "등록할 자전거를 입력하세요", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onClick: 12");
                }
                else {
                    Intent intent = new Intent();
                    intent.putExtra("bikename", bikeName);
                    setResult(RESULT_OK, intent);
                    Log.d(TAG, "onClick: 13");

                    finish();
                }
            }
        });
        Log.d(TAG, "onCreate: 15");
    }
}

