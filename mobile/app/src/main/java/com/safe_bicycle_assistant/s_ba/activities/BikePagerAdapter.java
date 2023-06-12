package com.safe_bicycle_assistant.s_ba.activities;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.safe_bicycle_assistant.s_ba.R;

import java.util.ArrayList;

public class BikePagerAdapter extends RecyclerView.Adapter<BikePagerAdapter.BikeViewHolder> {
    static final String TAG = "//*BikePagerAdapter*//";// MainActivity 호출을 위한 요청 코드
    private ArrayList<String> slideTexts;

    public BikePagerAdapter(ArrayList<String> slideTexts) {
        this.slideTexts = slideTexts;
    }

    @NonNull
    @Override
    public BikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager_childview, parent, false);
        return new BikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BikeViewHolder holder, int position) {
        String slideText = slideTexts.get(position);
        holder.textView.setText(slideText);
    }

    @Override
    public int getItemCount() {
        return slideTexts.size();
    }

    static class BikeViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        BikeViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }
}

