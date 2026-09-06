package com.agrirakshak.drone.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationUtils {

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String message);
    }

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    public static void getCurrentLocation(
            Activity activity,
            LocationCallback callback
    ) {

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );

            callback.onLocationError(
                    "Location permission required"
            );

            return;
        }

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(activity);

        client.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        callback.onLocationReceived(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                    } else {

                        callback.onLocationError(
                                "Unable to get current location"
                        );
                    }
                })
                .addOnFailureListener(e ->
                        callback.onLocationError(
                                "Location error: " + e.getMessage()
                        )
                );
    }
}