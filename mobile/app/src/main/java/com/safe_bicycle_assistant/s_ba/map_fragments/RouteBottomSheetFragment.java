package com.safe_bicycle_assistant.s_ba.map_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

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

        Button buttonCancelRoute  = view.findViewById(R.id.buttonCancelRoute);
        buttonCancelRoute.setOnClickListener((buttonView) ->
                this.mListener.onCancelRoute());

        Bundle args = getArguments();
        if (args != null) {
            Road road = args.getParcelable("road");

            TextView textRouteInfo = view.findViewById(R.id.textRouteInfo);
            textRouteInfo.setText(road.getLengthDurationText(getContext(), -1));
        }

        this.mListener = (RouteBottomSheetListener) getContext();

        return view;
    }

    public interface RouteBottomSheetListener {
        void onStartDriving();
        void onCancelRoute();
    }
}
