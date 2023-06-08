package com.safe_bicycle_assistant.s_ba.map_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.safe_bicycle_assistant.s_ba.R;

import org.osmdroid.bonuspack.routing.Road;

public class RouteBottomSheetFragment extends BottomSheetDialogFragment {
    private RouteBottomSheetListener mListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_bottom_sheet, viewGroup, false);

        Button buttonStartDriving  = view.findViewById(R.id.buttonStartDriving);
        buttonStartDriving.setOnClickListener((buttonView) ->
                this.mListener.onStartDriving());

        Bundle args = getArguments();
        if (args != null) {
            Road road = args.getParcelable("road");

            TextView textRouteDuration = view.findViewById(R.id.textRouteDuration);
            textRouteDuration.setText(getDurationText(road.mDuration));

            TextView textRouteDistance = view.findViewById(R.id.textRouteDistance);
            textRouteDistance.setText(getDistanceText(road.mLength));
        }

        this.mListener = (RouteBottomSheetListener) getContext();

        return view;
    }

    public interface RouteBottomSheetListener {
        void onStartDriving();
    }

    private static String getDistanceText(double distance) {
        String result;

        if (distance >= 100.0) {
            result = (int)(distance) + "km";
        } else if (distance >= 1.0) {
            result = Math.round(distance * 10) / 10.0 + "km";
        } else {
            result = (int)(distance * 1000) + "m";
        }

        return result;
    }

    private String getDurationText(double duration) {
        int totalSeconds = (int) duration;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds / 60) - (hours * 60);
        int seconds = (totalSeconds % 60);

        String result = "";
        if (hours != 0) result += hours + "시간 ";
        if (minutes != 0) result += minutes + "분 ";
        if (hours == 0 && minutes == 0) result += seconds + "초 ";

        return result;
    }
}
