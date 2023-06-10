package com.safe_bicycle_assistant.s_ba.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.safe_bicycle_assistant.s_ba.R;

public class ScreenSlidePageFragment extends Fragment {
    static final String TAG = "*//*Screen*//*";// 호출을 위한 요청 코드
    private int position;
    private String nickname;

    public ScreenSlidePageFragment(String nickname,int position) {
        this.nickname = nickname;
        this.position = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.viewpager_childview, container, false);
//        Log.d(TAG, "onCreateView: "+view.findViewById(R.id.bikeName).toString());
        TextView textView = view.findViewById(R.id.bikeName);
        ImageView imageView = view.findViewById(R.id.bikeImage); // ImageView 가져오기
        textView.setText(nickname);

        Log.d(TAG, "**/onCreateView: "+view.findViewById(R.id.bikeImage).toString());
        Log.d(TAG, "**/onCreateView: "+view.findViewById(R.id.bikeImage));

        // 각 슬라이드에 맞는 ImageView의 색상 설정
        int color = getColorForPosition(position); // 슬라이드 위치에 따른 색상 결정
        imageView.setColorFilter(color); // ImageView 색상 변경

        return view;
    }

    private int getColorForPosition(int position)
    {
        // 슬라이드 위치에 따라 적절한 색상을 반환하는 로직 구현
        int color;
        switch (position) {
            case 0:
                color = Color.RED;
                break;
            case 1:
                color = Color.BLUE;
                break;
            case 2:
                color = Color.GREEN;
                break;
            case 3:
                color = Color.YELLOW;
                break;
            default:
                color = Color.LTGRAY;
                break;
        }

        return color;
    }
}
