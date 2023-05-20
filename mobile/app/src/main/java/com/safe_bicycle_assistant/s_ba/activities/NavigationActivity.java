package com.safe_bicycle_assistant.s_ba.activities;

import android.content.Context;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.managers.MapManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.Arrays;

public class NavigationActivity extends AppCompatActivity {

    private MapView map;
    private MapManager mapManager;
    private IMapController mapController;

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

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_navigation);

        this.map = findViewById(R.id.map);

        this.mapManager = new MapManager(this);
        this.mapManager.from = getIntent().getParcelableExtra("from");
        this.mapManager.to = getIntent().getParcelableExtra("to");
        this.mapManager.road = getIntent().getParcelableExtra("road");

        this.mapController = this.map.getController();
        LocationListener listener = location -> {
            this.mapManager.current.set(new GeoPoint(location.getLatitude(), location.getLongitude()));
            this.mapController.setCenter(mapManager.current.get());
            Marker marker = getBasicMarker(DefinedOverlay.HERE.value, org.osmdroid.library.R.drawable.person, this.mapManager.current.get());
            removeMarker(DefinedOverlay.HERE.value);
            this.map.getOverlays().add(marker);
        };

        this.mapManager.trackCurrentGeoPoint(listener);

        initialize(this.mapManager.current.get());
    }

    private void initialize(GeoPoint initialPoint) {
        this.map.setTileSource(TileSourceFactory.MAPNIK);
        this.map.setMultiTouchControls(true);

        Marker marker = getBasicMarker(DefinedOverlay.HERE.value, org.osmdroid.library.R.drawable.person, initialPoint);
        this.map.getOverlays().add(marker);

        this.mapController.setZoom(20.0);
        this.mapController.setCenter(initialPoint);

        drawRoute(this.mapManager.road);

        this.map.invalidate();
    }

    private void drawRoute(Road road) {
        this.map.getOverlays().addAll(
                Arrays.asList(
                        getBasicMarker(DefinedOverlay.FROM.value, org.osmdroid.library.R.drawable.marker_default, mapManager.from),
                        getBasicMarker(DefinedOverlay.TO.value, org.osmdroid.library.R.drawable.marker_default, mapManager.to)
                )
        );

        Polyline routeOverlay = RoadManager.buildRoadOverlay(road);
        routeOverlay.setId(DefinedOverlay.ROUTE.value);
        routeOverlay.getOutlinePaint().setStrokeWidth(20.0f);
        this.map.getOverlays().add(routeOverlay);
    }

    private Marker getBasicMarker(String id, int icon, GeoPoint point) {
        Marker marker = new Marker(this.map);
        marker.setIcon(AppCompatResources.getDrawable(this, icon));
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
}
