package com.agrirakshak.drone.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agrirakshak.drone.R;
import com.agrirakshak.drone.drone.DroneController;
import com.agrirakshak.drone.drone.MockDroneController;

public class DroneActivity extends AppCompatActivity {

    private DroneController droneController;

    private TextView txtDroneStatus;
    private TextView txtDroneDetails;
    private TextView txtMissionStatus;
    private TextView txtMissionProgress;
    private TextView txtGpsStatus;
    private TextView txtAltitude;
    private TextView txtBattery;
    private TextView txtScans;
    private TextView txtCaptureStatus;

    private ProgressBar progressMission;

    private Button btnConnectDrone;
    private Button btnStartMission;
    private Button btnStopMission;
    private Button btnCaptureImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_drone);

        // Mock drone controller for demo
        droneController = new MockDroneController();

        // Connect UI elements
        txtDroneStatus = findViewById(R.id.txtDroneStatus);
        txtDroneDetails = findViewById(R.id.txtDroneDetails);
        txtMissionStatus = findViewById(R.id.txtMissionStatus);
        txtMissionProgress = findViewById(R.id.txtMissionProgress);
        txtGpsStatus = findViewById(R.id.txtGpsStatus);
        txtAltitude = findViewById(R.id.txtAltitude);
        txtBattery = findViewById(R.id.txtBattery);
        txtScans = findViewById(R.id.txtScans);
        txtCaptureStatus = findViewById(R.id.txtCaptureStatus);

        progressMission = findViewById(R.id.progressMission);

        btnConnectDrone = findViewById(R.id.btnConnectDrone);
        btnStartMission = findViewById(R.id.btnStartMission);
        btnStopMission = findViewById(R.id.btnStopMission);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);

        updateDroneUI();

        // CONNECT DRONE
        btnConnectDrone.setOnClickListener(v -> {

            if (!droneController.isConnected()) {

                droneController.connect();

                Toast.makeText(
                        DroneActivity.this,
                        "Drone connected successfully",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                droneController.disconnect();

                Toast.makeText(
                        DroneActivity.this,
                        "Drone disconnected",
                        Toast.LENGTH_SHORT
                ).show();
            }

            updateDroneUI();
        });

        // START MISSION
        btnStartMission.setOnClickListener(v -> {

            if (!droneController.isConnected()) {

                Toast.makeText(
                        DroneActivity.this,
                        "Please connect the drone first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            droneController.startMission();

            Toast.makeText(
                    DroneActivity.this,
                    "Mission started",
                    Toast.LENGTH_SHORT
            ).show();

            updateDroneUI();
        });

        // STOP MISSION
        btnStopMission.setOnClickListener(v -> {

            droneController.stopMission();

            Toast.makeText(
                    DroneActivity.this,
                    "Mission stopped",
                    Toast.LENGTH_SHORT
            ).show();

            updateDroneUI();
        });

        // CAPTURE IMAGE
        btnCaptureImage.setOnClickListener(v -> {

            if (!droneController.isConnected()) {

                Toast.makeText(
                        DroneActivity.this,
                        "Connect the drone first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            txtCaptureStatus.setText(
                    "📸 Image captured successfully"
            );

            txtScans.setText("Scans completed: 1");

            Toast.makeText(
                    DroneActivity.this,
                    "Crop image captured",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void updateDroneUI() {

        String status = droneController.getStatus();

        txtDroneStatus.setText(status);

        if (droneController.isConnected()) {

            txtDroneDetails.setText(
                    "Demo drone connected and ready"
            );

            btnConnectDrone.setText(
                    "Disconnect Drone"
            );

            txtGpsStatus.setText(
                    "GPS: Connected"
            );

            txtAltitude.setText(
                    "Altitude: 25 m"
            );

            txtBattery.setText(
                    "Battery: 92%"
            );

        } else {

            txtDroneDetails.setText(
                    "No drone connected"
            );

            btnConnectDrone.setText(
                    "Connect Drone"
            );

            txtGpsStatus.setText(
                    "GPS: Not Connected"
            );

            txtAltitude.setText(
                    "Altitude: --"
            );

            txtBattery.setText(
                    "Battery: --"
            );
        }

        if (droneController.isMissionRunning()) {

            txtMissionStatus.setText(
                    "MISSION RUNNING"
            );

            txtMissionProgress.setText(
                    "Mission in progress..."
            );

            progressMission.setProgress(50);

        } else {

            txtMissionStatus.setText(
                    "MISSION READY"
            );

            txtMissionProgress.setText(
                    "No active mission"
            );

            progressMission.setProgress(0);
        }
    }
}