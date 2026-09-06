package com.agrirakshak.drone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.agrirakshak.drone.R;

public class ScanActivity extends AppCompatActivity {

    Button btnCamera;
    Button btnGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan);

        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);

        btnCamera.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            ScanActivity.this,
                            CameraActivity.class
                    );

            startActivity(intent);
        });

        btnGallery.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ScanActivity.this,
                    UploadActivity.class
            );

            startActivity(intent);

        });
    }
}