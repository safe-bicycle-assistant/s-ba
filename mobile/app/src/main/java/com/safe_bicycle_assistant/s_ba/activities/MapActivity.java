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
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.safe_bicycle_assistant.s_ba.BuildConfig;
import com.safe_bicycle_assistant.s_ba.map_fragments.AddressesBottomSheetFragment;
import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.map_fragments.RouteBottomSheetFragment;
import com.safe_bicycle_assistant.s_ba.models.AddressFor;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class MapActivity extends AppCompatActivity implements AddressesBottomSheetFragment.MapBottomSheetListener, RouteBottomSheetFragment.RouteBottomSheetListener {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private final GeoPoint DEFAULT_POINT = new GeoPoint(37.2831f, 127.0448f);

    private MapView map = null;
    private EditText editTextFrom = null;
    private EditText editTextTo = null;

    private AddressesBottomSheetFragment addressesBottomSheetFragment = null;
    private RouteBottomSheetFragment routeBottomSheetFragment = null;

    private final AtomicReference<GeoPoint> current = new AtomicReference<>();
    private GeoPoint from = this.DEFAULT_POINT;
    private GeoPoint to = null;

    private LocationManager locationManager = null;
    private LocationListener locationListener = location -> {};

    private enum DefinedOverlay {
        HERE("here"),
        TO("to"),
        FROM("from"),
        ROUTE("route");

        public final String value;

        DefinedOverlay(String value) {
            this.value = value;
        }
    }

    @Override
    @SuppressLint("MissingPermission")
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
        this.map = findViewById(R.id.map);

        this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        this.current.set(getLastKnownGeoPoint());
        this.from = this.current.get().clone();
        initialize(this.current.get());

        this.editTextFrom = findViewById(R.id.editTextFrom);
        this.editTextFrom.setText(extractAddressText(getAddressByPoint(this.from)));
        this.editTextFrom.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                showAddressesBottomSheet(AddressFor.FROM, this.editTextFrom);
            }
            return true;
        });

        this.editTextTo = findViewById(R.id.editTextTo);
        this.editTextTo.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                showAddressesBottomSheet(AddressFor.TO, this.editTextTo);
            }
            return true;
        });

        Button buttonCurrentLocation = findViewById(R.id.buttonCurrentLocation);
        buttonCurrentLocation.setOnClickListener((v) -> this.moveToPoint(getCurrentGeoPoint()));
    }

    @Override
    public void onResume() {
        super.onResume();
        this.map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.map.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        ArrayList<String> permissionsToRequest = new ArrayList<>(
                Arrays.asList(permissions).subList(0, grantResults.length)
        );

        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    this.REQUEST_PERMISSIONS_REQUEST_CODE);
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
                    this.REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void initialize(GeoPoint initialPoint) {
        this.map.setTileSource(TileSourceFactory.MAPNIK);
        this.map.setMultiTouchControls(true);

        IMapController mapController = this.map.getController();
        mapController.setZoom(17.0);
        mapController.setCenter(initialPoint);

        Marker marker = getDefaultMarker(DefinedOverlay.HERE.value, initialPoint);
        this.map.getOverlays().add(marker);

        this.locationListener = location ->
                current.set(new GeoPoint(location.getLatitude(), location.getLongitude()));

        this.map.invalidate();
    }

    private Road searchRoute() {
        if (this.from == null || this.to == null) return null;

        RoadManager roadManager = new OSRMRoadManager(this, getPackageName());
        ((OSRMRoadManager) roadManager).setMean(OSRMRoadManager.MEAN_BY_BIKE);

        if (BuildConfig.PROFILE == "prod") {
            // NOTE: 더 나은 자전거 경로를 검색하지만, 사용량 제한이 있는 API이므로 개발 환경에서는 되도록 호출하지 않는다.
            roadManager = new GraphHopperRoadManager(BuildConfig.GRAPHHOPPER_API_KEY, false);
            roadManager.addRequestOption("profile=bike");
        }

        ArrayList<GeoPoint> waypoints = new ArrayList<>(Arrays.asList(from, to));
        return roadManager.getRoad(waypoints);
    }

    private void drawRoute(Road road) {
        removePolyline(DefinedOverlay.ROUTE.value);
        removeMarker(DefinedOverlay.FROM.value);
        removeMarker(DefinedOverlay.TO.value);

        this.map.getOverlays().addAll(
                Arrays.asList(
                        getDefaultMarker(DefinedOverlay.FROM.value, from),
                        getDefaultMarker(DefinedOverlay.TO.value, to)
                )
        );


        Polyline routeOverlay = RoadManager.buildRoadOverlay(road);
        routeOverlay.setId(DefinedOverlay.ROUTE.value);
        routeOverlay.getOutlinePaint().setStrokeWidth(20.0f);
        this.map.getOverlays().add(routeOverlay);

        this.map.zoomToBoundingBox(applyOffsets(road.mBoundingBox), true);

        this.map.invalidate();
    }

    private void moveToPoint(GeoPoint point) {
        removeMarker(DefinedOverlay.HERE.value);
        this.map.getOverlays().add(getDefaultMarker(DefinedOverlay.HERE.value, point));
        this.map.getController().animateTo(point);
    }

    @SuppressLint("MissingPermission")
    private GeoPoint getCurrentGeoPoint() {
        this.locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this.locationListener, null);
        return this.current.get();
    }

    @SuppressLint("MissingPermission")
    private GeoPoint getLastKnownGeoPoint() {
        Location location = this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            return new GeoPoint(location.getLatitude(), location.getLongitude());
        }
        return DEFAULT_POINT;
    }

    private List<Address> getAddressesByName(String locationName) {
        try {
            IGeoPoint center = this.map.getMapCenter();
            GeocoderNominatim geocoder = new GeocoderNominatim(Locale.KOREAN, getPackageName());
            return geocoder.getFromLocationName(
                    locationName,
                    15,
                    center.getLatitude() - 1,
                    center.getLongitude() - 1,
                    center.getLatitude() + 1,
                    center.getLongitude() + 1
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Address getAddressByPoint(GeoPoint point) {
        try {
            GeocoderNominatim geocoder = new GeocoderNominatim(Locale.KOREAN, getPackageName());
            return geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1).get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private void showAddressesBottomSheet(AddressFor addressFor, EditText view) {
        List<Address> addresses = getAddressesByName(view.getText().toString());
        this.addressesBottomSheetFragment = new AddressesBottomSheetFragment();

        Bundle args = new Bundle();
        args.putInt("addressFor", addressFor.toValue());
        args.putParcelableArrayList("addresses", (ArrayList<Address>) addresses);
        this.addressesBottomSheetFragment.setArguments(args);

        this.addressesBottomSheetFragment.show(getSupportFragmentManager(), "mapBottomSheet");
    }

    private void showRouteBottomSheet(Road road) {
        this.routeBottomSheetFragment = new RouteBottomSheetFragment();

        this.routeBottomSheetFragment.setCancelable(false);

        Bundle args = new Bundle();
        args.putParcelable("road", road);
        this.routeBottomSheetFragment.setArguments(args);

        this.routeBottomSheetFragment.show(getSupportFragmentManager(), "routeBottomSheet");
    }

    private Marker getDefaultMarker(String id, GeoPoint point) {
        Marker marker = new Marker(this.map);
        marker.setOnMarkerClickListener((m, v) -> false);
        marker.setPosition(point);
        marker.setId(id);
        return marker;
    }

    private void removeMarker(String id) {
        for (int i = 0; i < this.map.getOverlays().size(); i++) {
            Overlay overlay = this.map.getOverlays().get(i);
            if (overlay instanceof Marker && ((Marker) overlay).getId().equals(id)) {
                this.map.getOverlays().remove(overlay);
                break;
            }
        }
    }

    private void removePolyline(String id) {
        for (int i = 0; i < this.map.getOverlays().size(); i++) {
            Overlay overlay = this.map.getOverlays().get(i);
            if (overlay instanceof Polyline && ((Polyline) overlay).getId().equals(id)) {
                this.map.getOverlays().remove(overlay);
                break;
            }
        }
    }

    private String extractAddressText(Address address) {
        if (address != null) {
            return address.getExtras().get("display_name").toString().trim();
        } else {
            return "";
        }
    }

    private BoundingBox applyOffsets(BoundingBox box) {
        double northOffset = 0.04;
        double eastOffset = 0.02;
        double southOffset = 0.02;
        double westOffset = 0.02;

        return new BoundingBox(
                box.getActualNorth() + northOffset,
                box.getLonEast() + eastOffset,
                box.getActualSouth() - southOffset,
                box.getLonWest() - westOffset
        );
    }

    @Override
    public void onAddressSelected(Address address, AddressFor addressFor) {
        GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());
        if (addressFor == AddressFor.FROM) {
            this.from = point;
            this.editTextFrom.setText(extractAddressText(getAddressByPoint(this.from)));
        } else if (addressFor == AddressFor.TO) {
            this.to = point;
            this.editTextTo.setText(extractAddressText(getAddressByPoint(this.to)));
        }

        if (this.addressesBottomSheetFragment != null) {
            this.addressesBottomSheetFragment.dismiss();
            this.addressesBottomSheetFragment = null;
        }

        if (this.to != null) {
            if (this.from == null) {
                this.to = this.current.get();
                this.editTextTo.setText(extractAddressText(getAddressByPoint(this.to)));
            }

            Road road = searchRoute();
            drawRoute(road);
            showRouteBottomSheet(road);
        }
    }

    @Override
    public void onStartDriving() {
    }

    @Override
    public void onCancelRoute() {
        this.routeBottomSheetFragment.dismiss();
    }
}
