package com.safe_bicycle_assistant.s_ba;

import android.location.Address;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class MapBottomSheet extends BottomSheetDialogFragment {
    private MapBottomSheetListener mListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_map, viewGroup, false);

        ListView listView = view.findViewById(R.id.listViewSearchResult);
        listView.setOnItemClickListener((adapterView, view1, position, id) ->
                mListener.onAddressSelected((Address) adapterView.getAdapter().getItem(position)));

        Bundle args = getArguments();
        if (args != null) {
            ArrayList<Address> addresses = new ArrayList<>();
            for (Parcelable parcel : args.getParcelableArrayList("addresses")) {
                Address address = (Address) parcel;
                addresses.add(address);
            }

            listView.setAdapter(
                    new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_2, addresses) {
                        @Override
                        public View getView(int position, View view, ViewGroup viewGroup) {
                            View convertView = view;
                            if (convertView == null) {
                                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, viewGroup, false);
                            }

                            Address address = (Address) getItem(position);
                            String[] headings = ((String) address.getExtras().get("display_name")).split(",", 2);
                            ((TextView) convertView.findViewById(android.R.id.text1)).setText(headings[0]);
                            ((TextView) convertView.findViewById(android.R.id.text2)).setText(headings[1]);

                            return convertView;
                        }
                    }
            );
        }

        mListener = (MapBottomSheetListener) getContext();

        return view;
    }

    public interface MapBottomSheetListener {
        void onAddressSelected(Address address);
    }
}
