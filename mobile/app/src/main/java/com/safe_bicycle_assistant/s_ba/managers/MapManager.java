package com.safe_bicycle_assistant.s_ba.managers;

import static android.content.Context.LOCATION_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.safe_bicycle_assistant.s_ba.BuildConfig;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class MapManager {
    private final GeoPoint DEFAULT_POINT = new GeoPoint(37.2831f, 127.0448f);

    private final Context context;

    public final AtomicReference<GeoPoint> current = new AtomicReference<>();
    public GeoPoint from;
    public GeoPoint to = null;

    private final LocationManager locationManager;
    private final LocationListener locationListener;

    public MapManager(Context context) {
        this.context = context;

        this.locationManager = (LocationManager) this.context.getSystemService(LOCATION_SERVICE);
        this.locationListener = location ->
                current.set(new GeoPoint(location.getLatitude(), location.getLongitude()));

        this.current.set(this.fetchLastKnownGeoPoint());
        this.from = this.current.get().clone();
    }

    public Road searchRoute(GeoPoint from, GeoPoint to) {
        if (from == null || to == null) return null;

        RoadManager roadManager = new OSRMRoadManager(this.context, this.context.getPackageName());
        ((OSRMRoadManager) roadManager).setMean(OSRMRoadManager.MEAN_BY_BIKE);

        if (BuildConfig.PROFILE == "prod") {
            // NOTE: 더 나은 자전거 경로를 검색하지만, 사용량 제한이 있는 API이므로 개발 환경에서는 되도록 호출하지 않는다.
            roadManager = new GraphHopperRoadManager(BuildConfig.GRAPHHOPPER_API_KEY, false);
            roadManager.addRequestOption("profile=bike");
        }

        ArrayList<GeoPoint> waypoints = new ArrayList<>(Arrays.asList(from, to));
        return roadManager.getRoad(waypoints);
    }

    public List<Address> searchAddressesBy(String locationName, IGeoPoint point) {
        try {
            GeocoderNominatim geocoder = new GeocoderNominatim(Locale.KOREAN, this.context.getPackageName());
            return geocoder.getFromLocationName(
                    locationName,
                    15,
                    point.getLatitude() - 1,
                    point.getLongitude() - 1,
                    point.getLatitude() + 1,
                    point.getLongitude() + 1
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Address searchAddressBy(GeoPoint point) {
        try {
            GeocoderNominatim geocoder = new GeocoderNominatim(Locale.KOREAN, this.context.getPackageName());
            return geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1).get(0);
        } catch (Exception e) {
            return null;
        }
    }

    public String searchAddressTextBy(GeoPoint point) {
        return this.extractAddressText(this.searchAddressBy(point));
    }


    @SuppressLint("MissingPermission")
    public GeoPoint fetchCurrentGeoPoint() {
        this.locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this.locationListener, null);
        return this.current.get();
    }

    @SuppressLint("MissingPermission")
    public GeoPoint fetchLastKnownGeoPoint() {
        Location location = this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            return new GeoPoint(location.getLatitude(), location.getLongitude());
        }
        return this.DEFAULT_POINT;
    }

    private String extractAddressText(Address address) {
        if (address != null) {
            return address.getExtras().get("display_name").toString().trim();
        } else {
            return "";
        }
    }
}
