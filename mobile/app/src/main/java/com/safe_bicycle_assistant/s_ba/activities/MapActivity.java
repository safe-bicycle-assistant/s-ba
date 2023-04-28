package com.safe_bicycle_assistant.s_ba.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.safe_bicycle_assistant.s_ba.BuildConfig;
import com.safe_bicycle_assistant.s_ba.map_fragments.AddressesBottomSheetFragment;
import com.safe_bicycle_assistant.s_ba.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class MapActivity extends AppCompatActivity implements AddressesBottomSheetFragment.MapBottomSheetListener {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    private GeoPoint currentPoint = new GeoPoint(0.f, 0.f);
    private AddressesBottomSheetFragment addressesBottomSheetFragment = null;

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

        currentPoint = getCurrentGeoPoint();
        initialize(currentPoint);

        EditText editTextDestination = findViewById(R.id.editTextDestination);
        editTextDestination.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                List<Address> addresses = geocode(editTextDestination.getText().toString());
                addressesBottomSheetFragment = new AddressesBottomSheetFragment();

                Bundle args = new Bundle();
                args.putParcelableArrayList("addresses", (ArrayList<Address>) addresses);
                addressesBottomSheetFragment.setArguments(args);

                addressesBottomSheetFragment.show(getSupportFragmentManager(), "mapBottomSheet");
            }

            return true;
        });
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

        map.invalidate();
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
        map.getOverlays().clear();
        map.getOverlays().add(roadOverlay);

        map.invalidate();
    }

    @SuppressLint("MissingPermission")
    private GeoPoint getCurrentGeoPoint() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Location defaultLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        AtomicReference<GeoPoint> point = new AtomicReference<>();
        if (defaultLocation != null) {
            point.set(new GeoPoint(defaultLocation.getLatitude(), defaultLocation.getLongitude()));
        }

        final LocationListener locationListener = location -> point.set(new GeoPoint(location.getLatitude(), location.getLongitude()));
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

        return point.get();
    }

    private List<Address> geocode(String locationName) {
        try {
            GeocoderNominatim geocoder = new GeocoderNominatim(Locale.KOREAN, getPackageName());
            return geocoder.getFromLocationName(
                    locationName,
                    15,
                    currentPoint.getLatitude() - 3,
                    currentPoint.getLongitude() - 3,
                    currentPoint.getLatitude() + 3,
                    currentPoint.getLongitude() + 3
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void onAddressSelected(Address address) {
        GeoPoint endPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
        searchRoute(currentPoint, endPoint);

        if (addressesBottomSheetFragment != null) {
            addressesBottomSheetFragment.dismiss();
            addressesBottomSheetFragment = null;
        }
    }
}
