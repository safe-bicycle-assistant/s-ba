package com.safe_bicycle_assistant.s_ba.activities;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.safe_bicycle_assistant.s_ba.R;

public class ViewHolderPage extends RecyclerView.ViewHolder {

    private ImageView bikeImage;
    private TextView bikeName;

    Bike data;

    ViewHolderPage(View itemView) {
        super(itemView);
        bikeImage = itemView.findViewById(R.id.bikeImage);
        bikeName = itemView.findViewById(R.id.bikeName);
    }

    public void onBind(Bike data){
        this.data = data;

        bikeImage.setImageResource(data.getImageResId());
        bikeName.setText(data.getName());
    }
}
