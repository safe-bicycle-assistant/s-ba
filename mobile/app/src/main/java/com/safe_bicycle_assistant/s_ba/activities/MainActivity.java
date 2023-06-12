package com.safe_bicycle_assistant.s_ba.activities;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.db_helpers.BicycleDB;
import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
    static final String TAG = "//*MainActivity*//";// MainActivity 호출을 위한 요청 코드
    private static final int REQUEST_CODE_SET_NICKNAME = 1;
    private static final int MAX_BIKES = 5;
    private static int NUM_PAGES = 0;
    private Button mapButton, manageButton, setButton, btnToggle;
    private ViewPager2 viewPager2;
    private FragmentStateAdapter pagerAdapter;
    private ArrayList<TextView> textViews = new ArrayList<>();
    private ArrayList<String> nicknames = new ArrayList<>();
    private GestureDetector gestureDetector;
    private ViewPager2.OnPageChangeCallback onPageChangeCallback;
    private static final String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat mDetector;
    private BicycleDB bicycleDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapButton = findViewById(R.id.mapButton);
        manageButton = findViewById(R.id.manageButton);
        setButton = findViewById(R.id.setButton);
        viewPager2 = findViewById(R.id.pager);
        bicycleDB = new BicycleDB(getApplicationContext(),1);
        Cursor cursor = bicycleDB.getAllDateToCursor();
        while(cursor.moveToNext())
            nicknames.add(cursor.getString(BicycleDB.NICKNAME));
        updateContentView();

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: of mapButton up");
                int position = viewPager2.getCurrentItem();
                if(nicknames.size()==0)
                {
                    position=-1;
                }
                Log.d(TAG, "////////onClick: "+position);
                if(position == -1)
                {
                    Toast.makeText(MainActivity.this, "자전거를 먼저 추가한 후 다시 시도하세요", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra("name",nicknames.get(position));
                    startActivity(intent);
                }

            }
        });

        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: of manage button up");
                int position = viewPager2.getCurrentItem();
                if(nicknames.size()==0)
                {
                    position=-1;
                }
                if(position == -1)
                {
                    Toast.makeText(MainActivity.this, "자전거를 먼저 추가한 후 다시 시도하세요", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Log.d(TAG, "////////onClick: "+position);
                    Intent intent = new Intent(MainActivity.this, ManagementActivity.class);
                    intent.putExtra("name",nicknames.get(position));
                    startActivity(intent);
                }

            }
        });

        setButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: of setButton up");
                int position = viewPager2.getCurrentItem();
                if(nicknames.size()==0)
                {
                    position=-1;
                }
                Log.d(TAG, "////////onClick: "+nicknames.size());
                if (nicknames.size() >= MAX_BIKES) {
                    return;
                }
                Intent intent = new Intent(MainActivity.this, SetActivity.class);
                startActivityForResult(intent, SetActivity.REQUEST_CODE);
            }
        });
    }

    //////////?@@@////////////
    private Fragment getFragmentAtPosition(int position) {
        FragmentStateAdapter adapter = (FragmentStateAdapter) viewPager2.getAdapter();
        if (adapter != null) {
            String tag = "f" + viewPager2.getId() + ":a" + adapter.getItemId(position);
            return getSupportFragmentManager().findFragmentByTag(tag);
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (this.mDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(DEBUG_TAG,"onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
        int position = viewPager2.getCurrentItem();
        showDeleteDialog(position);
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX,
                            float distanceY) {
        Log.d(DEBUG_TAG, "onScroll: " + event1.toString() + event2.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        return true;
    }


    //////////?@@@////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SET_NICKNAME && resultCode == RESULT_OK) {
            String nickname = data.getStringExtra("bikename");
            if (nickname != null) {
                nicknames.add(nickname);

                bicycleDB.insert(nickname);

                if (nicknames.size() <= 5) { //자전거 최대 5개까지 등록 가능
                    TextView textView = new TextView(this);
                    textView.setText(nickname);
                    updateContentView();
                }
            }
        }
    }

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
            return new ScreenSlidePageFragment(nicknames.get(position),position);
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }

        @Override
        public long getItemId(int position) {
            return position; // 각 슬라이드의 고유 ID로서 position을 반환
        }
    }

    private void updateContentView() {
        if(nicknames.size() != 0)
        {
//            viewPager2.setAdapter(new BikePagerAdapter(nicknames));

            switch (nicknames.size()){
                case 1: // 1대 등록
                    NUM_PAGES = 1;
//                    Log.d(TAG, "**/updateContentView: "+nicknames);
                    break;
                case 2:
                    NUM_PAGES = 2;
//                    Log.d(TAG, "**/updateContentView: "+nicknames);
                    break;
                case 3:
                    NUM_PAGES = 3;
//                    Log.d(TAG, "updateContentView: "+nicknames);
                    break;
                case 4:
                    NUM_PAGES = 4;
//                    Log.d(TAG, "updateContentView: "+nicknames);
                    break;
                case 5:
                    NUM_PAGES = 5;
//                    Log.d(TAG, "updateContentView: "+nicknames);
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
                Log.d(TAG, "onClick: bottom of setButton bottom;"+nicknames.size());
                if(nicknames.size()<5){
                    Intent intent = new Intent(MainActivity.this, SetActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SET_NICKNAME);
                }
                else {
                    Toast.makeText(MainActivity.this, "하나를 삭제하신 후, 다시 시도하십시오.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mapButton = findViewById(R.id.mapButton);
        manageButton = findViewById(R.id.manageButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: of mapButton bottom");
                int position = viewPager2.getCurrentItem();
                if(nicknames.size()==0)
                {
                    position=-1;
                }
                Log.d(TAG, "////////onClick: "+position);
                if(position == -1)
                {
                    Toast.makeText(MainActivity.this, "자전거를 먼저 추가한 후 다시 시도하세요", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Log.d(TAG, "////////onClick: "+position);
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra("name",nicknames.get(position));
                    startActivity(intent);
                }
            }
        });

        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: of manager button bottom");
                int position = viewPager2.getCurrentItem();
                if(nicknames.size()==0)
                {
                    position=-1;
                }
                if(position == -1)
                {
                    Toast.makeText(MainActivity.this, "자전거를 먼저 추가한 후 다시 시도하세요", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Log.d(TAG, "////////onClick: "+position);
                    Intent intent = new Intent(MainActivity.this, ManagementActivity.class);
                    intent.putExtra("name",nicknames.get(position));
                    startActivity(intent);
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
//                    Log.d(TAG, "onClick: 가로");
                }else {
                    btnToggle.setText("세로로 슬라이드");
                    viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
//                    Log.d(TAG, "onClick: 세로");
                }
            }
        });

        viewPager2 = findViewById(R.id.pager);
        mDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                onLongPress(e);
                int position = viewPager2.getCurrentItem();
                Fragment fragment = getFragmentAtPosition(position);
                Log.d(TAG, "onLongPress: 현재 포지션 "+position);
//                if (fragment instanceof ScreenSlidePageFragment) {
//                    ((ScreenSlidePageFragment) fragment).handleViewPagerLongClick();
//                }
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Fragment fragment = getFragmentAtPosition(position);
                Log.d(TAG, "onPageSelected: 현재 포지션 "+position);
//                if (fragment instanceof ScreenSlidePageFragment) {
//                    ((ScreenSlidePageFragment) fragment).handleViewPagerClick();
//                }
            }
        });



    }


    public void showDeleteDialog(final int position) {
        Log.d(TAG, "showDeleteDialog: ");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Bike");
        builder.setMessage("Are you sure you want to delete this bike?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                textViews.remove(position);
                Log.d(TAG, "onClick: of builder Delete" + nicknames.size());
                bicycleDB.delete(nicknames.get(position));
                nicknames.remove(position);

                updateContentView();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create();
        builder.show();
    }
    private View findViewAt(ViewGroup viewGroup, float x, float y) {
        for(int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                View foundView = findViewAt((ViewGroup) child, x, y);
                if (foundView != null && foundView.isShown()) {
                    return foundView;
                }
            } else {
                int[] location = new int[2];
                child.getLocationOnScreen(location);
                Rect rect = new Rect(location[0], location[1], location[0] + child.getWidth(), location[1] + child.getHeight());
                if (rect.contains((int)x, (int)y)) {
                    return child;
                }
            }
        }

        return null;
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = findViewAt((ViewGroup) getWindow().getDecorView().getRootView(),ev.getX(),ev.getY());

        if(view != null && (view.getId() == R.id.bikeName || view.getId() == R.id.bikeImage))
        {

            if(ev.getPointerCount() == 2 && ev.getAction() == 261)
            {

                showDeleteDialog(viewPager2.getCurrentItem());
            }
        }
        return super.dispatchTouchEvent(ev);

    }
}
