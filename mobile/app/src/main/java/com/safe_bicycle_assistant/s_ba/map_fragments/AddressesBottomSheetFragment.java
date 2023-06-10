package com.safe_bicycle_assistant.s_ba.map_fragments;

import android.annotation.SuppressLint;
import android.location.Address;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.models.AddressFor;

import java.util.ArrayList;

public class AddressesBottomSheetFragment extends BottomSheetDialogFragment {
    private MapBottomSheetListener mListener;
    private AddressFor addressFor = AddressFor.UNKNOWN;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addresses_bottom_sheet, viewGroup, false);

        ListView listView = view.findViewById(R.id.listViewSearchResult);
        listView.setOnTouchListener((v, event) -> {
            int action = event.getAction();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }

            v.onTouchEvent(event);
            return true;
        });

        listView.setOnItemClickListener((adapterView, listItemView, position, id) ->
                this.mListener.onAddressSelected((Address) adapterView.getAdapter().getItem(position), this.addressFor));

        Bundle args = getArguments();
        if (args != null) {
            this.addressFor = AddressFor.from(args.getInt("addressFor"));
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
                            ((TextView) convertView.findViewById(android.R.id.text1)).setText(headings[0].trim());
                            ((TextView) convertView.findViewById(android.R.id.text2)).setText(headings[1].trim());

                            return convertView;
                        }
                    }
            );
        }

        this.mListener = (MapBottomSheetListener) getContext();

        return view;
    }

    public interface MapBottomSheetListener {
        void onAddressSelected(Address address, AddressFor addressFor);
    }
}
