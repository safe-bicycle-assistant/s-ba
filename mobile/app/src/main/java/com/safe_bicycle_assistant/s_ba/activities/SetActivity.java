//package com.safe_bicycle_assistant.s_ba.activities;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.safe_bicycle_assistant.s_ba.R;
//
//import java.util.ArrayList;
//
//public class SetActivity extends AppCompatActivity
//{
//    static final String TAG = "***********SetActivity**********";// SetActivity 호출을 위한 요청 코드
//
//    private static final int MAX_NICKNAMES = 5; //최대 등록 가능한 자전거 닉네임 수 : 5개
//
//    private EditText bikeNameEditText;
//    private Button registerButton;
//    private ArrayList<String> nicknames;
//    private SpinnableImageViewListener listener; // SpinnableImageViewListener 인터페이스의 참조 선언
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_set);
//
//        bikeNameEditText = findViewById(R.id.editText);
//        registerButton = findViewById(R.id.RegButton);
//        nicknames = new ArrayList<>();
//
//        registerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String bikeName = bikeNameEditText.getText().toString().trim();
//
//                if (bikeName.trim().isEmpty()) {
//                    Toast.makeText(SetActivity.this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    nicknames.add(bikeName);
//                    Intent intent = new Intent();
//                    intent.putExtra("bikename", bikeName);
//                    setResult(RESULT_OK, intent);
//                    Log.d(TAG, "onClick: "+intent);
//
////                    // SpinnableImageViewListener 인터페이스의 참조를 MainActivity로 전달
////                    if (listener != null) {
////                        listener.onRotationChanged(0); // 임의의 회전 각도 전달
////                    }
//
//                    finish();
//                }
//            }
//        });
//    }
//}






////다시다시다시//////
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

import java.util.ArrayList;

public class SetActivity extends AppCompatActivity {
    static final String TAG = "//*SetActivity*//";// SetActivity 호출을 위한 요청 코드
    private static final int MAX_NICKNAMES=5;
    public static final int REQUEST_CODE = 1;
    public static final String EXTRA_BIKE_NAME = "bike_name";

    private EditText editText;
    private Button saveButton;
//    private ArrayList<String> nicknames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        Log.d(TAG, "onCreate: 10");

        editText = findViewById(R.id.editText);
        saveButton = findViewById(R.id.RegButton);
//        nicknames=new ArrayList<>();

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
//                    nicknames.add(bikeName);
                    Intent intent = new Intent();
                    intent.putExtra("bikename", bikeName);
                    setResult(RESULT_OK, intent);
                    Log.d(TAG, "onClick: 13");
//                    Log.d(TAG, "onClick: "+intent);

                    finish();
                    Log.d(TAG, "onClick: 14");
                }
            }
        });
        Log.d(TAG, "onCreate: 15");
    }
}

