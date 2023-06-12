package com.safe_bicycle_assistant.s_ba.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.safe_bicycle_assistant.s_ba.ActivityAIDL;
import com.safe_bicycle_assistant.s_ba.ConnectionServiceAIDL;
import com.safe_bicycle_assistant.s_ba.R;
import com.safe_bicycle_assistant.s_ba.Services.ConnectionService;
import com.safe_bicycle_assistant.s_ba.db_helpers.RidingDB;
import com.safe_bicycle_assistant.s_ba.managers.MapManager;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class NavigationActivity extends AppCompatActivity implements SensorEventListener {
    private final String TAG = "NavigationActivity";
    private MapView map;
    private MapManager mapManager;
    private IMapController mapController;
    private SensorManager sensorManager;
    private final Gson gson = new Gson();

    private boolean hasAccSensor = false;
    private boolean hasMagSensor = false;
    private final float[] accelerometers = new float[3];
    private final float[] magnetics = new float[3];
    private float orientation = 0.0f;

    private float lengthPassed = 0;
    private float meanSpeed = 0;
    private float meanCadence = 0;
    private float maxSpeed = 0;
    private float maxCadence = 0;
    private String bicycleName;

    private final List<String> pointPassed = new ArrayList<>();

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

                if (detection >= DETECTION_THRESHOLD) {
                    imageWarning.setVisibility(View.VISIBLE);
                } else {
                    imageWarning.setVisibility(View.INVISIBLE);
                }

                if (cadence > maxCadence) maxCadence = cadence;
                meanCadence = (meanCadence + cadence) / 2;
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

    private Marker hereMarker = null;

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
        this.bicycleName = getIntent().getStringExtra("name");
        this.mapController = this.map.getController();

        this.imageWarning = findViewById(R.id.imageWarning);
        this.textCadence = findViewById(R.id.textCadence);
        this.pointPassed.add(this.gson.toJson(this.mapManager.from));
        TextView textSpeed = findViewById(R.id.textSpeed);
        LocationListener listener = location -> {
            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            double distance = geoPoint.distanceToAsDouble(this.mapManager.current.get());
            this.lengthPassed += distance;
            if (distance > 1) this.pointPassed.add(this.gson.toJson(geoPoint));

            this.mapManager.current.set(geoPoint);
            animateMarker(this.hereMarker, this.mapManager.current.get());
            this.mapController.animateTo(this.mapManager.current.get());

            int speed = (int) (location.getSpeed() * 3.6);
            if (speed > this.maxSpeed) this.maxSpeed = speed;
            this.meanSpeed = (this.meanSpeed + speed) / 2;
            textSpeed.setText(speed + "km/h");
        };

        this.mapManager.trackCurrentGeoPoint(listener);
        this.setupSensors();

        Button buttonStopDriving = findViewById(R.id.buttonStopDriving);
        buttonStopDriving.setOnClickListener((v) -> this.stopDriving());

        Intent intent = new Intent(getApplicationContext(), ConnectionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        initialize(this.mapManager.current.get());
    }
    private void initialize(GeoPoint initialPoint) {
        this.map.setTileSource(TileSourceFactory.MAPNIK);
        this.map.setMultiTouchControls(true);

        this.hereMarker = getBasicMarker(DefinedOverlay.HERE.value, R.drawable.directed_location, initialPoint);
        this.map.getOverlays().add(this.hereMarker);

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
        routeOverlay.getOutlinePaint().setARGB(255, 0, 139, 236);
        routeOverlay.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
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
        if (this.lengthPassed < 1) {
            this.meanSpeed = 0;
            this.maxSpeed = 0;
            this.meanCadence = 0;
            this.maxCadence = 0;
        }

        try {
            Log.d(TAG, "stopDriving: ");
            RidingDB dbhelper = new RidingDB(this, 1);
            String encoded = this.gson.toJson(this.pointPassed);
            dbhelper.insert(System.currentTimeMillis(), (int) this.lengthPassed,
                    this.meanSpeed,this.meanCadence, encoded, this.maxSpeed, this.maxCadence,this.bicycleName);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            Log.d(TAG, "stopDriving: Exception");
            // Do nothing
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("라이딩 끝!")
                .setMessage(
                        "주행 거리: " + meterToText(this.lengthPassed) + "\n" +
                        "평균 속력: " + String.format("%.1f", this.meanSpeed) + "km/h\n" +
                        "최고 속력: " + String.format("%.1f", this.maxSpeed) + "km/h\n" +
                        "평균 케이던스: " + String.format("%.1f", this.meanCadence) + "RPM\n" +
                        "최고 케이던스: " + String.format("%.1f", this.maxCadence) + "RPM\n"
                )
                .setPositiveButton("확인", (d, w) -> this.finish())
                .show();
    }

    private String meterToText(float meter) {
        if (meter >= 1000) {
            return String.format("%.1f", meter / 1000.f) + "km";
        }
        return String.format("%.1f", meter) + "m";
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

    private void animateMarker(final Marker marker, final GeoPoint toPosition) {
        Handler handler = new Handler();
        long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toPixels(marker.getPosition(), null);
        IGeoPoint startGeoPoint = proj.fromPixels(startPoint.x, startPoint.y);
        long duration = 500;
        LinearInterpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.getLongitude() + (1 - t) * startGeoPoint.getLongitude();
                double lat = t * toPosition.getLatitude() + (1 - t) * startGeoPoint.getLatitude();
                marker.setPosition(new GeoPoint(lat, lng));
                if (t < 1.0) handler.postDelayed(this, 15);
                map.postInvalidate();
            }
        });
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

            final int ORIENTATION_THRESHOLD = 5;
            float currentOrientation = this.orientation;
            float newOrientation = 360 - yaw;
            if (currentOrientation < 1 || Math.abs(currentOrientation - newOrientation) > ORIENTATION_THRESHOLD) {
                this.mapController.animateTo(null, null, 200L, newOrientation);
                this.orientation = newOrientation;
            }

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
