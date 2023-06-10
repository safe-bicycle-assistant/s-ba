package com.safe_bicycle_assistant.s_ba.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.safe_bicycle_assistant.s_ba.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "//*MainActivity*//";// MainActivity 호출을 위한 요청 코드
    private static final int REQUEST_CODE_SET_NICKNAME = 1;
    private static final int MAX_BIKES = 5;
    private static int NUM_PAGES = 0;
    private Button mapButton, manageButton, setButton, btnToggle;
    private ViewPager2 viewPager2;
    private FragmentStateAdapter pagerAdapter;
    private ArrayList<TextView> textViews = new ArrayList<>();
    private ArrayList<String> nicknames = new ArrayList<>();
//    private BikePagerAdapter bikePagerAdapter;
//    private ArrayList<Bike> bikes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: 1");

        mapButton = findViewById(R.id.mapButton);
        manageButton = findViewById(R.id.manageButton);
        setButton = findViewById(R.id.setButton);

//        bikes = new ArrayList<>();
//        bikePagerAdapter = new BikePagerAdapter(bikes, this);
//        viewPager.setAdapter(bikePagerAdapter);

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ManagementActivity.class);
                startActivity(intent);
            }
        });

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nicknames.size() >= MAX_BIKES) {
                    // Maximum number of bikes reached
                    // Show a message or handle the error
                    return;
                }
                Intent intent = new Intent(MainActivity.this, SetActivity.class);
                startActivityForResult(intent, SetActivity.REQUEST_CODE);
                Log.d(TAG, "onClick: 2");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SET_NICKNAME && resultCode == RESULT_OK) {
            String nickname = data.getStringExtra("bikename");
            if (nickname != null) {
                nicknames.add(nickname);

                if (textViews.size() <= 5) { //자전거 최대 5개까지 등록 가능
                    TextView textView = new TextView(this);
                    textView.setText(nickname);
                    textViews.add(textView);
                    updateContentView();
                }
            }
            Log.d(TAG, "onActivityResult: 3 " +nicknames);

        }
    }

//    private void setupViewPager(){
//        BikePagerAdapter slideAdapter = new BikePagerAdapter(nicknames);
//        viewPager2.setAdapter(slideAdapter);
//        Log.d(TAG, "setupViewPager: 4");
//    }
    @Override
    public void onBackPressed() {
        if (viewPager2.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager2.setCurrentItem(viewPager2.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            return new ScreenSlidePageFragment();
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    private void updateContentView() {
        if(textViews.size() != 0)
        {
            viewPager2 = findViewById(R.id.pager);

            viewPager2.setAdapter(new BikePagerAdapter(nicknames));

            switch (textViews.size()){
                case 1: // 1대 등록
                    //SetActivity.java에서 등록한 닉네임과 bike.png파일이 ViewPager2(viewpager_childview.xml)에 담겨서 MainActivity에 저장
                    // Instantiate a ViewPager2 and a PagerAdapter.
                    NUM_PAGES = 1;
//                    textView.setText(nicknames.get(0));
//                    pagerAdapter = new ScreenSlidePagerAdapter(this);
//                    viewPager2.setAdapter(pagerAdapter);
                    break;
                case 2:
                    NUM_PAGES = 2;
//                    pagerAdapter = new ScreenSlidePagerAdapter(this);
//                    viewPager2.setAdapter(pagerAdapter);
                    break;
                case 3:
                    NUM_PAGES = 3;
//                    pagerAdapter = new ScreenSlidePagerAdapter(this);
//                    viewPager2.setAdapter(pagerAdapter);
                    break;
                case 4:
                    NUM_PAGES = 4;
//                    pagerAdapter = new ScreenSlidePagerAdapter(this);
//                    viewPager2.setAdapter(pagerAdapter);
                    break;
                case 5:
                    NUM_PAGES = 5;
//                    pagerAdapter = new ScreenSlidePagerAdapter(this);
//                    viewPager2.setAdapter(pagerAdapter);
                    break;
            }
        }
        else{
            setContentView(R.layout.activity_main);
        }

        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager2.setAdapter(pagerAdapter);

        setButton = findViewById(R.id.setButton);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textViews.size()<5){
                    Intent intent = new Intent(MainActivity.this, SetActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SET_NICKNAME);
                }
                else {
                    Toast.makeText(MainActivity.this, "하나를 삭제하신 후, 다시 시도하십시오.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnToggle = findViewById(R.id.btnToggle);
        btnToggle.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (viewPager2.getOrientation() == ViewPager2.ORIENTATION_VERTICAL) {
                    btnToggle.setText("가로로 슬라이드");
                    viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                    Log.d(TAG, "onClick: 가로");
                }else {
                    btnToggle.setText("세로로 슬라이드");
                    viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
                    Log.d(TAG, "onClick: 세로");
                }
            }
        });

//        viewPager2.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                int position = viewPager2.getCurrentItem();
////                showDeleteDialog(position);
//                return true;
//            }
//        });
    }



//    private void showDeleteDialog(final int position) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Delete Bike")
//                .setMessage("Are you sure you want to delete this bike?")
//                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        bikes.remove(position);
//                        bikePagerAdapter.notifyDataSetChanged();
//                    }
//                })
//                .setNegativeButton("Cancel", null)
//                .create()
//                .show();
//    }
}
