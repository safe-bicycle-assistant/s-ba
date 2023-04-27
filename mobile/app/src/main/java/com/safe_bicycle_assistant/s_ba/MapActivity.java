package com.safe_bicycle_assistant.s_ba;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class MapActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        requestPermissionsIfNecessary(
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                }
        );

        setContentView(R.layout.activity_map);
        map = findViewById(R.id.map);

        GeoPoint initialPoint = getCurrentGeoPoint();
        initialize(initialPoint);

        GeoPoint endPoint = new GeoPoint(37.5, 127.06);
        searchRoute(initialPoint, endPoint);
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        ArrayList<String> permissionsToRequest = new ArrayList<>(
                Arrays.asList(permissions).subList(0, grantResults.length)
        );

        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void initialize(GeoPoint initialPoint) {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(17.0);
        mapController.setCenter(initialPoint);

        Marker marker = new Marker(map);
        marker.setOnMarkerClickListener((m, v) -> false);
        marker.setPosition(initialPoint);
        map.getOverlays().add(marker);
    }

    private void searchRoute(GeoPoint from, GeoPoint to) {
        RoadManager roadManager = new OSRMRoadManager(this, getPackageName());
        ((OSRMRoadManager) roadManager).setMean(OSRMRoadManager.MEAN_BY_BIKE);

        if (BuildConfig.PROFILE == "prod") {
            // NOTE: 더 나은 자전거 경로를 검색하지만, 사용량 제한이 있는 API이므로 개발 환경에서는 되도록 호출하지 않는다.
            roadManager = new GraphHopperRoadManager(BuildConfig.GRAPHHOPPER_API_KEY, false);
            roadManager.addRequestOption("profile=bike");
        }

        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(from);
        waypoints.add(to);

        Road road = roadManager.getRoad(waypoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        roadOverlay.getOutlinePaint().setStrokeWidth(20.0f);
        map.getOverlays().add(roadOverlay);

        map.invalidate();
    }

    private GeoPoint getCurrentGeoPoint() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        @SuppressLint("MissingPermission") Location defaultLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        AtomicReference<GeoPoint> point = new AtomicReference<>(new GeoPoint(defaultLocation.getLatitude(), defaultLocation.getLongitude()));

        return point.get();
    }
}
