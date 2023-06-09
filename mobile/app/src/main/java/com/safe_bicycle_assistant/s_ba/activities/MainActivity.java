package com.safe_bicycle_assistant.s_ba.activities;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Intent;
//
//import android.database.sqlite.SQLiteDatabase;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.ListView;
//
//import com.safe_bicycle_assistant.s_ba.R;
//import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;
//import android.os.Bundle;
//import android.view.View;
//
//import com.safe_bicycle_assistant.s_ba.R;
//
//
//
//
//public class MainActivity extends AppCompatActivity {
//    ListView listView;
//    static RidingDB ridingDatabaseHelper;
//    SQLiteDatabase db;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Button bt = findViewById(R.id.button);
//        bt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(),ManagementActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        Button bt2 = findViewById(R.id.button2);
//        bt2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(),TestActivity.class);
//                startActivity(intent);
//            }
//        });
//        Button bt3= findViewById(R.id.button);
//        bt3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
//                MainActivity.this.startActivity(mapIntent);
//            }
//        });
//
//        ImageView img = findViewById(R.id.gear);
//        img.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //use SpinnableImageView Class
//            }
//        });
//
//    }
//
////    public void openMap(View view) {
////
////    }
//}
//



import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.safe_bicycle_assistant.s_ba.R;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SpinnableImageViewListener{
    static final String TAG = "//////////MainActivity//////////////";
    private SpinnableImageView spinnableImageView;
    private static final int REQUEST_CODE_SET_NICKNAME = 1;
    private TextView textView1, textView2, textView3, textView4;
    private ArrayList<TextView> textViews;
    private ArrayList<String> nicknames;
    private Button Button1, Button2, Button3, setButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViews = new ArrayList<>();
        nicknames = new ArrayList<>();

//        //////////////////////////////Activity로 전환하는 버튼////////////////////////////////
//        Button1 = findViewById(R.id.button);
//        Button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, SetActivity.class);
//                startActivityForResult(intent, REQUEST_CODE_SET_NICKNAME);
//            }
//        });
//
//        //////////////////////////////Activity로 전환하는 버튼////////////////////////////////
//        Button2 = findViewById(R.id.button2);
//        Button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, SetActivity.class);
//                startActivityForResult(intent, REQUEST_CODE_SET_NICKNAME);
//            }
//        });
//
//        //////////////////////////////Activity로 전환하는 버튼////////////////////////////////
//        Button3 = findViewById(R.id.button3);
//        Button3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, SetActivity.class);
//                startActivityForResult(intent, REQUEST_CODE_SET_NICKNAME);
//            }
//        });

        //////////////////////////////SetActivity로 전환하는 버튼////////////////////////////////
        setButton = findViewById(R.id.button4);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SetActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SET_NICKNAME);
                Log.d(TAG, "onClick: "+intent);
//                Intent intent = new Intent(MainActivity.this, SetActivity.class);
//                // SpinnableImageViewListener 인터페이스의 참조 전달
//                intent.putExtra("listener", listener);
//                startActivityForResult(intent, REQUEST_CODE_SET_NICKNAME);
            }
        });

        spinnableImageView = findViewById(R.id.gear);
//        spinnableImageView.setSpinnableImageViewListener(this);
        // SpinnableImageViewListener 설정
        spinnableImageView.setSpinnableImageViewListener(new SpinnableImageViewListener() {
            @Override
            public void onRotationChanged(double rotationAngle) {
                handleRotationChanged(rotationAngle);
            }
        });
    }

    ////////////////////////////////회전에 따른 자전거 선택 및 색상 변경////////////////////////////////
    private void handleRotationChanged(double rotationAngle) {
        Log.d(TAG, "onTouch1: "+textViews.size());
        // 손가락 드래그에 따른 이미지 회전 처리
        rotationAngle = spinnableImageView.getRotation();

        if(textViews.size()==1) //TextView 1개
        {
            Log.d(TAG, "onRotationChanged: 1");
            View view = getLayoutInflater().inflate(R.layout.activity_main1, null);
            TextView textView = view.findViewById(R.id.textView1);
            textView.setTextColor(Color.BLACK);
        }
        else if(textViews.size()==2) //TextView 2개
        {
            Log.d(TAG, "onRotationChanged: 2");
            View view = getLayoutInflater().inflate(R.layout.activity_main2, null);
            TextView textView1 = view.findViewById(R.id.textView2);
            TextView textView2 = view.findViewById(R.id.textView3);
            // 회전 각도에 따른 텍스트 색상 변경
            if (rotationAngle >= 0 && rotationAngle < 180) {
                textView1.setTextColor(Color.BLACK);
                textView2.setTextColor(Color.LTGRAY);
            }
            else if (rotationAngle >= 180 && rotationAngle < 360) {
                textView1.setTextColor(Color.BLACK);
                textView2.setTextColor(Color.LTGRAY);
            }
        }
        else if(textViews.size()==3) //TextView 3개
        {
            Log.d(TAG, "onRotationChanged: 3");
            View view = getLayoutInflater().inflate(R.layout.activity_main3, null);
            TextView textView1 = view.findViewById(R.id.textView4);
            TextView textView2 = view.findViewById(R.id.textView5);
            TextView textView3 = view.findViewById(R.id.textView6);
            // 회전 각도에 따른 텍스트 색상 변경
            if (rotationAngle >= 0 && rotationAngle < 120){
                textView1.setTextColor(Color.BLACK);
                textView2.setTextColor(Color.LTGRAY);
            }
            else if (rotationAngle >= 120 && rotationAngle < 240) {
                textView1.setTextColor(Color.LTGRAY);
                textView2.setTextColor(Color.BLACK);
                textView3.setTextColor(Color.LTGRAY);
            }
            else if (rotationAngle >= 240) {
                textView1.setTextColor(Color.LTGRAY);
                textView2.setTextColor(Color.LTGRAY);
                textView3.setTextColor(Color.BLACK);
            }
        }
    }

    @Override
    public void onRotationChanged(double rotationAngle) {
        Log.d(TAG, "onTouch1: "+textViews.size());
        // 손가락 드래그에 따른 이미지 회전 처리
        rotationAngle = spinnableImageView.getRotation();

        if(textViews.size()==1) //TextView 1개
        {
            Log.d(TAG, "onRotationChanged: 1");
            View view = getLayoutInflater().inflate(R.layout.activity_main1, null);
            TextView textView = view.findViewById(R.id.textView1);
            textView.setTextColor(Color.RED);
        }
        else if(textViews.size()==2) //TextView 2개
        {
            Log.d(TAG, "onRotationChanged: 2");
            View view = getLayoutInflater().inflate(R.layout.activity_main2, null);
            TextView textView1 = view.findViewById(R.id.textView2);
            TextView textView2 = view.findViewById(R.id.textView3);
            // 회전 각도에 따른 텍스트 색상 변경
            if (rotationAngle >= 0 && rotationAngle < 180) {
                textView1.setTextColor(Color.BLACK);
                textView2.setTextColor(Color.LTGRAY);
            }
            else if (rotationAngle >= 180 && rotationAngle < 360) {
                textView1.setTextColor(Color.BLACK);
                textView2.setTextColor(Color.LTGRAY);
            }
        }
        else if(textViews.size()==3) //TextView 3개
        {
            Log.d(TAG, "onRotationChanged: 3");
            View view = getLayoutInflater().inflate(R.layout.activity_main3, null);
            TextView textView1 = view.findViewById(R.id.textView4);
            TextView textView2 = view.findViewById(R.id.textView5);
            TextView textView3 = view.findViewById(R.id.textView6);
            // 회전 각도에 따른 텍스트 색상 변경
            if (rotationAngle >= 0 && rotationAngle < 120){
                textView1.setTextColor(Color.BLACK);
                textView2.setTextColor(Color.LTGRAY);
            }
            else if (rotationAngle >= 120 && rotationAngle < 240) {
                textView1.setTextColor(Color.LTGRAY);
                textView2.setTextColor(Color.BLACK);
                textView3.setTextColor(Color.LTGRAY);
            }
            else if (rotationAngle >= 240) {
                textView1.setTextColor(Color.LTGRAY);
                textView2.setTextColor(Color.LTGRAY);
                textView3.setTextColor(Color.BLACK);
            }
        }
    }

    ////////////////////////////////자전거 추가에 따른 액티비티 화면////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SET_NICKNAME && resultCode == RESULT_OK) {
            String nickname = data.getStringExtra("bikename");
            if (nickname != null) {
                nicknames.add(nickname);

                if (textViews.size() <= 3) { //자전거 최대 3개까지 등록 가능
                    TextView textView = new TextView(this);
                    textView.setText(nickname);
                    textViews.add(textView);
                    updateContentView();
                }
            }
        }
    }


    ////////////////////////////////자전거 등록 수////////////////////////////////
    private void updateContentView() {
        switch (textViews.size()) {
            case 1: //자전거 1대
                setContentView(R.layout.activity_main1);
                spinnableImageView = findViewById(R.id.gear);
//        spinnableImageView.setSpinnableImageViewListener(this);
                // SpinnableImageViewListener 설정
                spinnableImageView.setSpinnableImageViewListener(new SpinnableImageViewListener() {
                    @Override
                    public void onRotationChanged(double rotationAngle) {
                        handleRotationChanged(rotationAngle);
                    }
                });
                textView1 = findViewById(R.id.textView1);
                textView1.setText(nicknames.get(0));
                break;
            case 2: //자전거 4대
                setContentView(R.layout.activity_main2);
                spinnableImageView = findViewById(R.id.gear);
//        spinnableImageView.setSpinnableImageViewListener(this);
                // SpinnableImageViewListener 설정
                spinnableImageView.setSpinnableImageViewListener(new SpinnableImageViewListener() {
                    @Override
                    public void onRotationChanged(double rotationAngle) {
                        handleRotationChanged(rotationAngle);
                    }
                });
                textView2 = findViewById(R.id.textView2);
                textView3 = findViewById(R.id.textView3);
                textView2.setText(nicknames.get(0));
                textView3.setText(nicknames.get(1));
                break;
            case 3: //자전거 3대
                setContentView(R.layout.activity_main3);
                spinnableImageView = findViewById(R.id.gear);
//        spinnableImageView.setSpinnableImageViewListener(this);
                // SpinnableImageViewListener 설정
                spinnableImageView.setSpinnableImageViewListener(new SpinnableImageViewListener() {
                    @Override
                    public void onRotationChanged(double rotationAngle) {
                        handleRotationChanged(rotationAngle);
                    }
                });
                textView2 = findViewById(R.id.textView4);
                textView3 = findViewById(R.id.textView5);
                textView4 = findViewById(R.id.textView6);
                textView2.setText(nicknames.get(0));
                textView3.setText(nicknames.get(1));
                textView4.setText(nicknames.get(2));
                break;
        }

        ////////////////////////////////설정 버튼 누르면 다시 SetActivity 화면으로 전환////////////////////////////////
        setButton = findViewById(R.id.button4);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textViews.size() < 3) {
                    Intent intent = new Intent(MainActivity.this, SetActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SET_NICKNAME);
                    Log.d(TAG, "onClick2: "+intent);
                } else {
                    Toast.makeText(MainActivity.this, "하나를 삭제하신 후, 다시 시도하십시오.", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    ////////////////////////////////등록한 자전거 삭제(TextView LongClick)////////////////////////////////
    private void deleteTextView() {
        if(textViews.size()==1) //등록 자전거 1대인 경우
        {
            View view = getLayoutInflater().inflate(R.layout.activity_main1, null);
            TextView textView = view.findViewById(R.id.textView1);

            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int longClickId = v.getId();
                    showDeleteDialog(longClickId);
                    return true;
                }
            });
        }
        else if(textViews.size()==2) //등록 자전거 2대인 경우
        {
            View view = getLayoutInflater().inflate(R.layout.activity_main2, null);
            TextView textView1 = view.findViewById(R.id.textView2);
            TextView textView2 = view.findViewById(R.id.textView3);

            textView1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int longClickId = v.getId();
                    showDeleteDialog(longClickId);
                    return true;
                }
            });
            textView2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int longClickId = v.getId();
                    showDeleteDialog(longClickId);
                    return true;
                }
            });

        }
        else if(textViews.size()==3) //등록 자전거 3대인 경우
        {
            Log.d(TAG, "onRotationChanged: 3");
            View view = getLayoutInflater().inflate(R.layout.activity_main3, null);
            TextView textView1 = view.findViewById(R.id.textView4);
            TextView textView2 = view.findViewById(R.id.textView5);
            TextView textView3 = view.findViewById(R.id.textView6);

            textView1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int longClickId = v.getId();
                    showDeleteDialog(longClickId);
                    return true;
                }
            });
            textView2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int longClickId = v.getId();
                    showDeleteDialog(longClickId);
                    return true;
                }
            });
            textView3.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int longClickId = v.getId();
                    showDeleteDialog(longClickId);
                    return true;
                }
            });
        }
    }

    ////////////////////////////////삭제를 위해 long click한 경우에 뜨는 dialog////////////////////////////////
    private void showDeleteDialog(int longClickId) {
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setIcon(R.mipmap.ic_launcher);//알림창 아이콘 설정
        dialog.setTitle("삭제");
        dialog.setMessage("해당 자전거를 삭제할까요?"); //알림창 메세지 설정

//        //알림창 닫기
//        dialog.setNeutralButton("닫기", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(getApplicationContext(),"창을 닫습니다",Toast.LENGTH_SHORT).show();
//            }
//        });

        //알림창 예
        dialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"삭제가 완료되었습니다.",Toast.LENGTH_LONG).show();

                ////////////////////////////////////////arraylist에서 선택학 textView 제거////////////////////////////////////////
                switch (textViews.size()) {
                    case 1:
                        View view1 = getLayoutInflater().inflate(R.layout.activity_main1, null);
                        TextView text1 = view1.findViewById(R.id.textView1);
                        if(longClickId==text1.getId())
                        {
                            textViews.remove(0);
                            nicknames.remove(0);
                        }
                        setContentView(R.layout.activity_main);
                        break;

                    case 2:
                        View view2 = getLayoutInflater().inflate(R.layout.activity_main2, null);
                        TextView text2 = view2.findViewById(R.id.textView2);
                        TextView text3 = view2.findViewById(R.id.textView3);
                        if(longClickId==text2.getId())
                        {
                            textViews.remove(0);
                            nicknames.remove(0);
                        }
                        else if(longClickId==text3.getId())
                        {
                            textViews.remove(1);
                            nicknames.remove(1);
                        }
                        setContentView(R.layout.activity_main1);
                        break;

                    case 3:
                        View view3 = getLayoutInflater().inflate(R.layout.activity_main3, null);
                        TextView text4 = view3.findViewById(R.id.textView4);
                        TextView text5 = view3.findViewById(R.id.textView5);
                        TextView text6 = view3.findViewById(R.id.textView6);
                        if(longClickId==text4.getId())
                        {
                            textViews.remove(0);
                            nicknames.remove(0);
                        }
                        else if(longClickId==text5.getId())
                        {
                            textViews.remove(1);
                            nicknames.remove(1);
                        }
                        else if(longClickId==text5.getId())
                        {
                            textViews.remove(2);
                            nicknames.remove(2);
                        }
                        setContentView(R.layout.activity_main2);
                        break;
                }
                ////////////////////////////////////////////////////////////////////////////////////////////////////////

            }
        });

        //알림창 아니오
        dialog.setNegativeButton("취소되었습니다.",null);//아무 이벤트 발생하지 않게 하기위하여 null로 설정

        dialog.show();

    }

}
