package com.safe_bicycle_assistant.s_ba.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.safe_bicycle_assistant.s_ba.R;

public class ScreenSlidePageFragment extends Fragment {
    static final String TAG = "*//*Screen*//*";// 호출을 위한 요청 코드
    private String nickname;

    public ScreenSlidePageFragment(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewpager_childview, container, false);
        Log.d(TAG, "onCreateView: "+view.findViewById(R.id.bikeName).toString());
        TextView textView = view.findViewById(R.id.bikeName);
        textView.setText(nickname);
        return view;
    }
}
