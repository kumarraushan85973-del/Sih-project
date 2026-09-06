package com.agrirakshak.drone.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.agrirakshak.drone.R;
import com.agrirakshak.drone.database.AppDatabase;
import com.agrirakshak.drone.database.ScanEntity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 2001;

    private MapView mapView;
    private FusedLocationProviderClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure OpenStreetMap
        Configuration.getInstance().setUserAgentValue(
                getPackageName()
        );

        setContentView(R.layout.activity_map);

        // Find map
        mapView = findViewById(R.id.map);

        // Configure map tiles
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setUseDataConnection(true);
        mapView.setMultiTouchControls(true);

        // Default location: India
        GeoPoint defaultLocation =
                new GeoPoint(20.5937, 78.9629);

        mapView.getController().setZoom(5.0);
        mapView.getController().setCenter(
                defaultLocation
        );

        // Location service
        locationClient =
                LocationServices.getFusedLocationProviderClient(
                        this
                );

        // Load saved scan locations
        loadSavedScans();

        // Get current location
        requestLocationPermission();
    }

    private void requestLocationPermission() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );

            return;
        }

        getCurrentLocation();
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        locationClient
                .getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        double latitude =
                                location.getLatitude();

                        double longitude =
                                location.getLongitude();

                        showCurrentLocation(
                                latitude,
                                longitude
                        );

                    } else {

                        Toast.makeText(
                                MapActivity.this,
                                "Unable to get current location",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            MapActivity.this,
                            "Location error: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void showCurrentLocation(
            double latitude,
            double longitude
    ) {

        GeoPoint currentLocation =
                new GeoPoint(
                        latitude,
                        longitude
                );

        // Move map to current location
        mapView.getController().setZoom(17.0);

        mapView.getController().animateTo(
                currentLocation
        );

        // Create current location marker
        Marker marker =
                new Marker(mapView);

        marker.setPosition(
                currentLocation
        );

        marker.setTitle(
                "Current Field Location"
        );

        marker.setSnippet(
                String.format(
                        Locale.getDefault(),
                        "Latitude: %.6f\nLongitude: %.6f",
                        latitude,
                        longitude
                )
        );

        mapView.getOverlays().add(marker);

        mapView.invalidate();

        Toast.makeText(
                this,
                "Current location found",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void loadSavedScans() {

        Executors
                .newSingleThreadExecutor()
                .execute(() -> {

                    AppDatabase db =
                            AppDatabase.getInstance(
                                    MapActivity.this
                            );

                    List<ScanEntity> scans =
                            db.scanDao().getAllScans();

                    runOnUiThread(() -> {

                        for (ScanEntity scan : scans) {

                            // Ignore scans without GPS
                            if (scan.latitude == 0
                                    && scan.longitude == 0) {
                                continue;
                            }

                            // Scan GPS location
                            GeoPoint scanLocation =
                                    new GeoPoint(
                                            scan.latitude,
                                            scan.longitude
                                    );

                            // Create marker
                            Marker marker =
                                    new Marker(mapView);

                            marker.setPosition(
                                    scanLocation
                            );

                            // Disease name
                            marker.setTitle(
                                    scan.disease
                            );

                            // Scan information
                            marker.setSnippet(
                                    String.format(
                                            Locale.getDefault(),
                                            "Status: %s\nConfidence: %.2f%%\nDate: %s\nLocation: %.6f, %.6f",
                                            scan.status,
                                            scan.confidence,
                                            scan.dateTime,
                                            scan.latitude,
                                            scan.longitude
                                    )
                            );

                            mapView
                                    .getOverlays()
                                    .add(marker);
                        }

                        mapView.invalidate();
                    });
                });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode ==
                LOCATION_PERMISSION_REQUEST) {

            if (grantResults.length > 0
                    &&
                    grantResults[0]
                            == PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation();

            } else {

                Toast.makeText(
                        this,
                        "Location permission is required",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {

        if (mapView != null) {
            mapView.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (mapView != null) {
            mapView.onDetach();
        }

        super.onDestroy();
    }
}