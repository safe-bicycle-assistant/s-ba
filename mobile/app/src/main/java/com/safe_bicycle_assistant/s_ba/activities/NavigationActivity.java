package com.safe_bicycle_assistant.s_ba.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.safe_bicycle_assistant.s_ba.ActivityAIDL;
import com.safe_bicycle_assistant.s_ba.ConnectionServiceAIDL;
import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.Services.ConnectionService;
import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;
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

import java.util.ArrayList;
import java.util.List;

public class NavigationActivity extends AppCompatActivity implements SensorEventListener {

    private MapView map;
    private MapManager mapManager;
    private IMapController mapController;
    private SensorManager sensorManager;

    private boolean hasAccSensor = false;
    private boolean hasMagSensor = false;
    private final float[] accelerometers = new float[3];
    private final float[] magnetics = new float[3];

    private Thread stopwatch;
    private int timeSpent = 0;
    private float lengthPassed = 0;
    private float curSpeed = 0;
    private float curCadence = 0;
    private float accSpeed = 0;
    private float accCadence = 0;
    private float maxSpeed = 0;
    private float maxCadence = 0;

    private List<GeoPoint> pointPassed = new ArrayList<>();

    TextView textCadence = null;
    ImageView imageWarning = null;

    ConnectionServiceAIDL mConnectionServiceAIDL = null;

    private final IBinder ActivityBinder = new ActivityAIDL.Stub() {
        @Override
        public void testConnection() { }

        @Override
        public void setTexts(float cadence, int detection) {
            runOnUiThread(() -> {
                final int DETECTION_THRESHOLD = 50000;

                if (detection >= DETECTION_THRESHOLD && imageWarning.getVisibility() == View.INVISIBLE) {
                    imageWarning.setVisibility(View.VISIBLE);
                } else if (imageWarning.getVisibility() == View.VISIBLE) {
                    imageWarning.setVisibility(View.INVISIBLE);
                }

                if (cadence > maxCadence) maxCadence = cadence;
                curCadence = cadence;
                textCadence.setText(String.format("%.1f", cadence) + "RPM");
            });
        }
    };

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mConnectionServiceAIDL = ConnectionServiceAIDL.Stub.asInterface(iBinder);
            Bundle bundle = new Bundle();
            bundle.putBinder("key", ActivityBinder);
            try {
                mConnectionServiceAIDL.reverseConnection(bundle);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mConnectionServiceAIDL = null;
        }
    };

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

        this.imageWarning = findViewById(R.id.imageWarning);
        this.textCadence = findViewById(R.id.textCadence);
        TextView textSpeed = findViewById(R.id.textSpeed);
        LocationListener listener = location -> {
            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            double distance = geoPoint.distanceToAsDouble(this.mapManager.current.get());
            this.lengthPassed += distance;
            if (distance > 2) this.pointPassed.add(geoPoint);

            this.mapManager.current.set(geoPoint);
            this.mapController.setCenter(mapManager.current.get());

            Marker marker = getBasicMarker(DefinedOverlay.HERE.value, R.drawable.directed_location, this.mapManager.current.get());
            removeMarker(DefinedOverlay.HERE.value);
            this.map.getOverlays().add(marker);

            int speed = (int) location.getSpeed();
            if (speed > maxSpeed) maxSpeed = speed;
            curSpeed = speed;
            textSpeed.setText(speed + "km/h");
        };

        this.mapManager.trackCurrentGeoPoint(listener);
        this.setupSensors();

        Button buttonStopDriving = findViewById(R.id.buttonStopDriving);
        buttonStopDriving.setOnClickListener((v) -> this.stopDriving());

        Intent intent = new Intent(getApplicationContext(), ConnectionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        initialize(this.mapManager.current.get());

        this.stopwatch = new Thread(this::onEverySecond);
        this.stopwatch.start();
    }
    private void initialize(GeoPoint initialPoint) {
        this.map.setTileSource(TileSourceFactory.MAPNIK);
        this.map.setMultiTouchControls(true);

        Marker marker = getBasicMarker(DefinedOverlay.HERE.value, R.drawable.directed_location, initialPoint);
        this.map.getOverlays().add(marker);

        this.mapController.setZoom(19.3);
        this.mapController.setCenter(initialPoint);

        drawRoute(this.mapManager.road);

        this.map.invalidate();
    }

    private void drawRoute(Road road) {
        this.map.getOverlays().add(
                getBasicMarker(DefinedOverlay.TO.value, R.drawable.marker_red, mapManager.to)
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

    private void setupSensors() {
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
    }

    private void stopDriving() {
        if (this.stopwatch != null) {
            this.stopwatch.interrupt();
            this.stopwatch = null;
        }

        float meanSpeed = this.accSpeed / timeSpent;
        float meanCadence = (this.accCadence / timeSpent) * 60;

        if (this.lengthPassed >= 10) {
            try {
                RidingDB dbhelper = new RidingDB(this, 1);
                SQLiteDatabase db = dbhelper.getWritableDatabase();
                dbhelper.onCreate(db);
                dbhelper.insert(System.currentTimeMillis(), (int) this.lengthPassed, meanSpeed, meanCadence);
            } catch (Exception ignored) {
                // Do nothing
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("라이딩 끝!")
                .setMessage(
                        "주행 시간: " + secToText(timeSpent) + "\n" +
                        "주행 거리: " + String.format("%.1f", this.lengthPassed) + "m\n" +
                        "평균 속력: " + String.format("%.1f", meanSpeed) + "km/h\n" +
                        "최고 속력: " + String.format("%.1f", this.maxSpeed) + "km/h\n" +
                        "평균 케이던스: " + String.format("%.1f", meanCadence) + "RPM\n" +
                        "최고 케이던스: " + String.format("%.1f", this.maxCadence) + "RPM\n"
                )
                .setPositiveButton("확인", (d, w) -> this.finish())
                .show();
    }

    private String secToText(int sec) {
        if (sec >= 3600) {
            String h = (int) (sec / 3600) + "시간";
            if (sec % 3600 > 0) {
                String m = (int) (sec % 3600 / 60) + "분";
                return h + " " + m;
            }
            return h;
        }  else if (sec > 60) {
            return (int) (sec / 60) + "분";
        }
        return sec + "초";
    }

    private void onEverySecond() {
        try {
            while (true) {
                this.timeSpent += 1;
                this.accSpeed += curSpeed;
                this.accCadence += curCadence;
                Thread.sleep(1000);
            }
        } catch (Exception ignored) {
            // Do nothing
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, this.accelerometers, 0, event.values.length);
                hasAccSensor = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, this.magnetics, 0, event.values.length);
                hasMagSensor = true;
                break;
        }

        if (hasAccSensor && hasMagSensor) {
            float[] r = new float[9];
            float[] i = new float[9];
            SensorManager.getRotationMatrix(r, i, this.accelerometers, this.magnetics);

            float[] angles = new float[3];
            SensorManager.getOrientation(r, angles);

            float yaw = (float) Math.toDegrees(angles[0]);
            if (yaw < 0) yaw += 360;

            this.mapController.animateTo(null, null, 200L, 360 - yaw);

            hasAccSensor = false;
            hasMagSensor = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Do nothing
    }

    @Override
    public void onResume() {
        super.onResume();
        this.map.onResume();
        this.setupSensors();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        this.sensorManager.unregisterListener(this);
        try {
            this.mConnectionServiceAIDL.destroyService();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        this.map.onDetach();
    }
}
