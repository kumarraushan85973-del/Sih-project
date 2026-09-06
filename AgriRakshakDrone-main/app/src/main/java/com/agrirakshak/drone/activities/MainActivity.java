package com.agrirakshak.drone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.agrirakshak.drone.R;
import com.agrirakshak.drone.database.AppDatabase;
import com.agrirakshak.drone.database.ScanEntity;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView txtHealthyZones;
    private TextView txtSuspectedZones;
    private TextView txtTotalScans;

    // Recent scan views
    private TextView txtRecentDisease;
    private TextView txtRecentConfidence;
    private TextView txtRecentStatus;
    private TextView txtRecentDate;
    private TextView txtRecentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // =========================
        // DASHBOARD STATISTICS
        // =========================
        txtHealthyZones = findViewById(R.id.txtHealthyZones);
        txtSuspectedZones = findViewById(R.id.txtSuspectedZones);
        txtTotalScans = findViewById(R.id.txtTotalScans);

        // =========================
        // RECENT SCAN
        // =========================
        txtRecentDisease = findViewById(R.id.txtRecentDisease);
        txtRecentConfidence = findViewById(R.id.txtRecentConfidence);
        txtRecentStatus = findViewById(R.id.txtRecentStatus);
        txtRecentDate = findViewById(R.id.txtRecentDate);
        txtRecentLocation = findViewById(R.id.txtRecentLocation);

        // Load database statistics
        loadDashboardStatistics();


        // =========================
        // SCAN FIELD
        // =========================
        findViewById(R.id.cardScan).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    ScanActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // BOTTOM SCAN
        // =========================
        findViewById(R.id.navScan).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    ScanActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // SCAN HISTORY
        // =========================
        findViewById(R.id.cardHistory).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    HistoryActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // FIELD MAP CARD
        // =========================
        findViewById(R.id.cardMap).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    MapActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // BOTTOM MAP
        // =========================
        findViewById(R.id.navMap).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    MapActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // DRONE SCAN CARD
        // =========================
        findViewById(R.id.cardDrone).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    DroneActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // BOTTOM DRONE
        // =========================
        findViewById(R.id.navDrone).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    DroneActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // PROFILE BUTTON
        // =========================
        findViewById(R.id.btnProfile).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    ProfileActivity.class
            );

            startActivity(intent);
        });
    }


    // =========================================================
    // LOAD DASHBOARD STATISTICS + RECENT SCAN
    // =========================================================
    private void loadDashboardStatistics() {

        Executors.newSingleThreadExecutor().execute(() -> {

            AppDatabase db =
                    AppDatabase.getInstance(MainActivity.this);

            List<ScanEntity> scans =
                    db.scanDao().getAllScans();

            int totalScans = scans.size();

            int healthyZones = 0;
            int suspectedZones = 0;

            for (ScanEntity scan : scans) {

                if (scan.status != null
                        && scan.status.equalsIgnoreCase("HEALTHY")) {

                    healthyZones++;

                } else {

                    suspectedZones++;
                }
            }

            // Latest scan
            ScanEntity latestScan = null;

            if (!scans.isEmpty()) {
                latestScan = scans.get(0);
            }

            int finalHealthyZones = healthyZones;
            int finalSuspectedZones = suspectedZones;
            ScanEntity finalLatestScan = latestScan;

            runOnUiThread(() -> {

                // =========================
                // STATISTICS
                // =========================

                txtTotalScans.setText(
                        String.valueOf(totalScans)
                );

                txtHealthyZones.setText(
                        String.valueOf(finalHealthyZones)
                );

                txtSuspectedZones.setText(
                        String.valueOf(finalSuspectedZones)
                );


                // =========================
                // RECENT SCAN
                // =========================

                if (finalLatestScan == null) {

                    txtRecentDisease.setText(
                            "No scans yet"
                    );

                    txtRecentConfidence.setText(
                            "Confidence: --"
                    );

                    txtRecentStatus.setText(
                            "Status: --"
                    );

                    txtRecentDate.setText(
                            "Date: --"
                    );

                    txtRecentLocation.setText(
                            "Location: --"
                    );

                    return;
                }


                // Disease
                txtRecentDisease.setText(
                        finalLatestScan.disease
                );


                // Confidence
                txtRecentConfidence.setText(
                        String.format(
                                Locale.getDefault(),
                                "Confidence: %.2f%%",
                                finalLatestScan.confidence
                        )
                );


                // Status
                txtRecentStatus.setText(
                        "Status: " + finalLatestScan.status
                );


                // Date
                txtRecentDate.setText(
                        "Date: " + finalLatestScan.dateTime
                );


                // GPS Location
                txtRecentLocation.setText(
                        String.format(
                                Locale.getDefault(),
                                "📍 %.6f, %.6f",
                                finalLatestScan.latitude,
                                finalLatestScan.longitude
                        )
                );
            });
        });
    }


    // =========================================================
    // REFRESH WHEN RETURNING TO DASHBOARD
    // =========================================================
    @Override
    protected void onResume() {

        super.onResume();

        if (txtTotalScans != null) {
            loadDashboardStatistics();
        }
    }
}