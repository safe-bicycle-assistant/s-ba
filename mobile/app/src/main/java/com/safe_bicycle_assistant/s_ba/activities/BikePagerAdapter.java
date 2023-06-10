package com.safe_bicycle_assistant.s_ba.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.safe_bicycle_assistant.s_ba.R;

import java.util.ArrayList;
import java.util.List;

public class BikePagerAdapter extends RecyclerView.Adapter<BikePagerAdapter.BikeViewHolder> {
    static final String TAG = "//*BikePagerAdapter*//";// MainActivity 호출을 위한 요청 코드
    private ArrayList<String> slideTexts;

    public BikePagerAdapter(ArrayList<String> slideTexts) {
        this.slideTexts = slideTexts;
        Log.d(TAG, "BikePagerAdapter: 5");
    }

    @NonNull
    @Override
    public BikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager_childview, parent, false);
        Log.d(TAG, "onCreateViewHolder: 6");
        return new BikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BikeViewHolder holder, int position) {
        String slideText = slideTexts.get(position);
        holder.textView.setText(slideText);
        Log.d(TAG, "onBindViewHolder: 7");
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: 8");
        return slideTexts.size();
    }

    static class BikeViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        BikeViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            Log.d(TAG, "BikeViewHolder: 9");
        }
    }
}

