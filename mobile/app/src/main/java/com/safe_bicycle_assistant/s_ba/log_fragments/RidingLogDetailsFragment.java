package com.safe_bicycle_assistant.s_ba.log_fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.Utils;
import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;
import com.safe_bicycle_assistant.s_ba.uis.RidingLogAdapter;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Arrays;

public class RidingLogDetailsFragment extends Fragment {
    int index;

    final static String TAG= "RidingLogFragment";
    static RidingDB ridingDatabaseHelper;
    SQLiteDatabase db;
    MapView mapView;
    TextView timeView;
    TextView distanceView;
    TextView maxSpeedView;
    TextView avgSpeedView;
    TextView maxCadenceView;
    TextView avgCadenceView;
    String bicycleName;
    private final Gson gson = new Gson();

    public RidingLogDetailsFragment(int index,String bicycleName) {
        this.index = index;
        this.bicycleName = bicycleName;
    }
    @Override
    public void onStart() {
        super.onStart();
        mapView = getView().findViewById(R.id.mapImageView);
        timeView = getView().findViewById(R.id.timeTextView);
        distanceView = getView().findViewById(R.id.lengthTextView);
        maxSpeedView = getView().findViewById(R.id.maxSpeedTextView);
        avgCadenceView = getView().findViewById(R.id.avgCadenceTextView);
        avgSpeedView = getView().findViewById(R.id.avgSpeedTextView);
        maxCadenceView = getView().findViewById(R.id.maxCadenceTextView);
        ridingDatabaseHelper = new RidingDB(getContext(),1);
        Cursor c = ridingDatabaseHelper.getDataByIndex(index,bicycleName);
//        Cursor c = ridingDatabaseHelper.getAllDataToCursor();
//        c.moveToFirst();
//        for(int i = 0; i< index; i++)
//        {
//            c.moveToNext();
//        }
        Log.d(TAG, Utils.longToDate(c.getInt(1)).toString());
        if(c == null)
        {

        }
        else
        {
            drawRoute(gson.fromJson(c.getString(RidingDB.MAP), new TypeToken<ArrayList<String>>(){}.getType()));
            timeView.setText(""+Utils.DateToString( Utils.longToDate(c.getLong(0))));
            distanceView.setText(""+c.getInt(RidingDB.LENGTH)+" km");
            maxSpeedView.setText(""+c.getDouble(RidingDB.MAX_SPEED)+" km/h");
            avgSpeedView.setText(""+c.getDouble(RidingDB.AVERAGE_SPEED)+" km/h");
            avgCadenceView.setText(""+c.getDouble(RidingDB.AVERAGE_CADENCE)+" rpm");
            maxCadenceView.setText(""+c.getDouble(RidingDB.MAX_CADENCE)+" rpm");
        }
    }

    private void drawRoute(ArrayList<String> pathJson) {
        if (pathJson.size() > 1) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);

            GeoPoint firstPoint = gson.fromJson(pathJson.get(0), GeoPoint.class);
            GeoPoint lastPoint = gson.fromJson(pathJson.get(pathJson.size() - 1), GeoPoint.class);
            mapView.getOverlays().addAll(
                    Arrays.asList(
                            getBasicMarker("from", R.drawable.marker_green, firstPoint),
                            getBasicMarker("to", R.drawable.marker_red, lastPoint)
                    )
            );

            Polyline line = new Polyline(mapView);
            for (String json : pathJson) line.addPoint(gson.fromJson(json, GeoPoint.class));
            line.getOutlinePaint().setStrokeWidth(20.f);
            line.getOutlinePaint().setARGB(255, 0, 139, 236);
            line.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
            mapView.getOverlays().add(line);

            IMapController mapController = mapView.getController();
            mapController.setCenter(firstPoint);
            mapController.setZoom(15.0);
//            mapView.zoomToBoundingBox(line.getBounds(), false);
            mapView.invalidate();
        }
    }

    private Marker getBasicMarker(String id, int icon, GeoPoint point) {
        Marker marker = new Marker(mapView);
        marker.setIcon(AppCompatResources.getDrawable(getContext(), icon));
        marker.setOnMarkerClickListener((m, v) -> false);
        marker.setPosition(point);
        marker.setId(id);
        return marker;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_riding_log_details, container, false);
    }
}
