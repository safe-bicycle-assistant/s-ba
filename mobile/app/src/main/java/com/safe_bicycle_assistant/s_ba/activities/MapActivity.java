package com.safe_bicycle_assistant.s_ba.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.safe_bicycle_assistant.s_ba.managers.MapManager;
import com.safe_bicycle_assistant.s_ba.map_fragments.AddressesBottomSheetFragment;
import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.map_fragments.RouteBottomSheetFragment;
import com.safe_bicycle_assistant.s_ba.models.AddressFor;
import com.safe_bicycle_assistant.s_ba.utils.PermissionUtil;

import org.osmdroid.api.IMapController;
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
import java.util.List;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements
        AddressesBottomSheetFragment.MapBottomSheetListener,
        RouteBottomSheetFragment.RouteBottomSheetListener {

    private MapView map;
    private EditText editTextFrom;
    private EditText editTextTo;

    private AddressesBottomSheetFragment addressesBottomSheetFragment;
    private RouteBottomSheetFragment routeBottomSheetFragment;

    private MapManager mapManager;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        PermissionUtil.requestPermissions(
                Arrays.asList(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                this
        );

        setContentView(R.layout.activity_map);

        this.map = findViewById(R.id.map);
        this.mapManager = new MapManager(ctx);

        initialize(mapManager.current.get());

        this.editTextFrom = findViewById(R.id.editTextFrom);
        this.editTextFrom.setText(mapManager.searchAddressTextBy(mapManager.from));
        this.editTextFrom.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                showAddressesBottomSheet(AddressFor.FROM, this.editTextFrom);
                return true;
            }
            return false;
        });

        this.editTextTo = findViewById(R.id.editTextTo);
        this.editTextTo.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                showAddressesBottomSheet(AddressFor.TO, this.editTextTo);
                return true;
            }
            return false;
        });

        FloatingActionButton buttonCurrentLocation = findViewById(R.id.buttonCurrentLocation);
        buttonCurrentLocation.setOnClickListener((v) ->
                moveToPoint(mapManager.fetchCurrentGeoPoint()));
    }

    private void initialize(GeoPoint initialPoint) {
        this.map.setTileSource(TileSourceFactory.MAPNIK);
        this.map.setMultiTouchControls(true);

        IMapController mapController = this.map.getController();
        mapController.setZoom(17.0);
        mapController.setCenter(initialPoint);

        Marker marker = getBasicMarker(DefinedOverlay.HERE.value, R.drawable.location, initialPoint);
        this.map.getOverlays().add(marker);

        this.map.invalidate();
    }

    private void drawRoute(Road road) {
        removePolyline(DefinedOverlay.ROUTE.value);
        removeMarker(DefinedOverlay.FROM.value);
        removeMarker(DefinedOverlay.TO.value);

        this.map.getOverlays().addAll(
                Arrays.asList(
                        getBasicMarker(DefinedOverlay.FROM.value, R.drawable.marker_green, mapManager.from),
                        getBasicMarker(DefinedOverlay.TO.value, R.drawable.marker_red, mapManager.to)
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
        this.map.getOverlays().add(getBasicMarker(DefinedOverlay.HERE.value, R.drawable.location, point));
        this.map.getController().animateTo(point);
    }

    private void showAddressesBottomSheet(AddressFor addressFor, EditText view) {
        try {
            ArrayList<Address> addresses = (ArrayList<Address>) mapManager.searchAddressesBy(
                    view.getText().toString(), mapManager.current.get());
            this.addressesBottomSheetFragment = new AddressesBottomSheetFragment();

            Bundle args = new Bundle();
            args.putInt("addressFor", addressFor.toValue());
            args.putParcelableArrayList("addresses", addresses);
            this.addressesBottomSheetFragment.setArguments(args);

            this.addressesBottomSheetFragment.show(getSupportFragmentManager(), "mapBottomSheet");
        } catch (Exception ignored) {
            // Do nothing
        }
    }

    private void showRouteBottomSheet(Road road) {
        this.routeBottomSheetFragment = new RouteBottomSheetFragment();

        Bundle args = new Bundle();
        args.putParcelable("road", road);
        this.routeBottomSheetFragment.setArguments(args);

        this.routeBottomSheetFragment.show(getSupportFragmentManager(), "routeBottomSheet");
    }

    private Marker getBasicMarker(String id, int icon, GeoPoint point) {
        Marker marker = new Marker(this.map);
        marker.setOnMarkerClickListener((m, v) -> false);
        marker.setPosition(point);
        marker.setId(id);
        if (!Objects.equals(id, DefinedOverlay.HERE.value)) {
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        }
        marker.setIcon(AppCompatResources.getDrawable(this, icon));
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

    private BoundingBox applyOffsets(BoundingBox box) {
        double northOffset = 0.02;
        double eastOffset = 0.01;
        double southOffset = 0.02;
        double westOffset = 0.01;

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
            mapManager.from = point;
            this.editTextFrom.setText(mapManager.searchAddressTextBy(mapManager.from));
        } else if (addressFor == AddressFor.TO) {
            mapManager.to = point;
            this.editTextTo.setText(mapManager.searchAddressTextBy(mapManager.to));
        }

        if (this.addressesBottomSheetFragment != null) {
            this.addressesBottomSheetFragment.dismiss();
            this.addressesBottomSheetFragment = null;
        }

        if (mapManager.to != null) {
            if (mapManager.from == null) {
                mapManager.from = mapManager.current.get();
                this.editTextTo.setText(mapManager.searchAddressTextBy(mapManager.from));
            }

            this.mapManager.road = mapManager.searchRoute(mapManager.to, mapManager.from);
            drawRoute(this.mapManager.road);
            showRouteBottomSheet(this.mapManager.road);
        }
    }

    @Override
    public void onStartDriving() {
        this.routeBottomSheetFragment.dismiss();
        Intent navigationIntent = new Intent(this, NavigationActivity.class);
        navigationIntent.putExtra("to", (Parcelable) this.mapManager.to);
        navigationIntent.putExtra("from", (Parcelable) this.mapManager.from);
        navigationIntent.putExtra("road", this.mapManager.road);
        startActivity(navigationIntent);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.requestPermissions(
                Arrays.asList(permissions).subList(0, grantResults.length),
                this
        );
    }
}
