package com.safe_bicycle_assistant.s_ba.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

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

public class NavigationActivity extends AppCompatActivity implements SensorEventListener {

    private MapView map;
    private MapManager mapManager;
    private IMapController mapController;
    private SensorManager sensorManager;

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

        TextView textSpeed = findViewById(R.id.textSpeed);
        LocationListener listener = location -> {
            this.mapManager.current.set(new GeoPoint(location.getLatitude(), location.getLongitude()));
            this.mapController.setCenter(mapManager.current.get());
            Marker marker = getBasicMarker(DefinedOverlay.HERE.value, org.osmdroid.library.R.drawable.person, this.mapManager.current.get());
            removeMarker(DefinedOverlay.HERE.value);
            this.map.getOverlays().add(marker);
            textSpeed.setText((int)location.getSpeed() + "km/h");
        };

        this.mapManager.trackCurrentGeoPoint(listener);

        this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }

        initialize(this.mapManager.current.get());
    }
    private void initialize(GeoPoint initialPoint) {
        this.map.setTileSource(TileSourceFactory.MAPNIK);
        this.map.setMultiTouchControls(true);

        Marker marker = getBasicMarker(DefinedOverlay.HERE.value, org.osmdroid.library.R.drawable.person, initialPoint);
        this.map.getOverlays().add(marker);

        this.mapController.setZoom(19.3);
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
    public void onSensorChanged(SensorEvent event) {
        final float[] accelerometers = new float[3];
        final float[] magnetics = new float[3];

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, accelerometers, 0, event.values.length);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, magnetics, 0, event.values.length);
                break;
        }

        float[] r = new float[9];
        float[] i = new float[9];
        SensorManager.getRotationMatrix(r, i, accelerometers, magnetics);

        float[] angles = new float[3];
        SensorManager.getOrientation(r, angles);

        float yaw = (float) Math.toDegrees(angles[0]);
        if (yaw < 0) yaw += 360;

        this.map.setMapOrientation(360 - yaw);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Do nothing
    }

    @Override
    public void onResume() {
        super.onResume();
        this.map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        this.sensorManager.unregisterListener(this);

        this.map.onPause();
    }
}
