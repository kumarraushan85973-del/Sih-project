package com.agrirakshak.drone.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agrirakshak.drone.R;
import com.agrirakshak.drone.database.AppDatabase;
import com.agrirakshak.drone.database.ScanEntity;
import com.agrirakshak.drone.utils.LocationUtils;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ResultActivity extends AppCompatActivity {

    private ImageView imgResult;

    private TextView txtStatus;
    private TextView txtDisease;
    private TextView txtConfidence;
    private TextView txtRecommendation;

    private ProgressBar progressConfidence;

    private Button btnSaveReport;
    private Button btnScanAnother;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_result);


        // =====================================================
        // FIND VIEWS
        // =====================================================

        imgResult =
                findViewById(R.id.imgResult);

        txtStatus =
                findViewById(R.id.txtStatus);

        txtDisease =
                findViewById(R.id.txtDisease);

        txtConfidence =
                findViewById(R.id.txtConfidence);

        txtRecommendation =
                findViewById(R.id.txtRecommendation);

        progressConfidence =
                findViewById(R.id.progressConfidence);

        btnSaveReport =
                findViewById(R.id.btnSaveReport);

        btnScanAnother =
                findViewById(R.id.btnScanAnother);


        // =====================================================
        // LOAD CAPTURED IMAGE
        // =====================================================

        loadCapturedImage();


        // =====================================================
        // GET AI PREDICTION RESULT
        // =====================================================

        String status =
                getIntent().getStringExtra("status");

        String disease =
                getIntent().getStringExtra("disease");

        double confidence =
                getIntent().getDoubleExtra(
                        "confidence",
                        0
                );

        String recommendation =
                getIntent().getStringExtra(
                        "recommendation"
                );


        // =====================================================
        // DISPLAY STATUS
        // =====================================================

        if (status != null
                && !status.isEmpty()) {

            txtStatus.setText(status);

        } else {

            txtStatus.setText("UNKNOWN");
        }


        // =====================================================
        // DISPLAY DISEASE
        // =====================================================

        if (disease != null
                && !disease.isEmpty()) {

            txtDisease.setText(disease);

        } else {

            txtDisease.setText("Unknown");
        }


        // =====================================================
        // DISPLAY CONFIDENCE
        // =====================================================

        txtConfidence.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f%%",
                        confidence
                )
        );


        // Keep progress between 0 and 100
        int confidenceProgress =
                (int) Math.max(
                        0,
                        Math.min(
                                100,
                                confidence
                        )
                );

        progressConfidence.setProgress(
                confidenceProgress
        );


        // =====================================================
        // DISPLAY RECOMMENDATION
        // =====================================================

        if (recommendation != null
                && !recommendation.isEmpty()) {

            txtRecommendation.setText(
                    recommendation
            );

        } else {

            txtRecommendation.setText(
                    "No recommendation available."
            );
        }


        // =====================================================
        // SCAN ANOTHER CROP
        // =====================================================

        btnScanAnother.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            ResultActivity.this,
                            ScanActivity.class
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            );

            startActivity(intent);

            finish();
        });


        // =====================================================
        // SAVE REPORT
        // =====================================================

        btnSaveReport.setOnClickListener(v -> {

            btnSaveReport.setEnabled(false);

            LocationUtils.getCurrentLocation(
                    ResultActivity.this,
                    new LocationUtils.LocationCallback() {

                        @Override
                        public void onLocationReceived(
                                double latitude,
                                double longitude
                        ) {

                            // =================================
                            // CURRENT DATE AND TIME
                            // =================================

                            String dateTime =
                                    new SimpleDateFormat(
                                            "dd MMM yyyy, hh:mm a",
                                            Locale.getDefault()
                                    ).format(
                                            new Date()
                                    );


                            // =================================
                            // CREATE SCAN RECORD
                            // =================================

                            ScanEntity scan =
                                    new ScanEntity(
                                            status,
                                            disease,
                                            confidence,
                                            recommendation,
                                            dateTime,
                                            latitude,
                                            longitude
                                    );


                            // =================================
                            // SAVE TO ROOM DATABASE
                            // =================================

                            Executors
                                    .newSingleThreadExecutor()
                                    .execute(() -> {

                                        try {

                                            AppDatabase db =
                                                    AppDatabase.getInstance(
                                                            ResultActivity.this
                                                    );

                                            db.scanDao()
                                                    .insertScan(scan);


                                            runOnUiThread(() -> {

                                                btnSaveReport
                                                        .setEnabled(true);

                                                Toast.makeText(
                                                        ResultActivity.this,
                                                        "Report saved with GPS location",
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                            });

                                        } catch (Exception e) {

                                            runOnUiThread(() -> {

                                                btnSaveReport
                                                        .setEnabled(true);

                                                Toast.makeText(
                                                        ResultActivity.this,
                                                        "Unable to save report",
                                                        Toast.LENGTH_LONG
                                                ).show();

                                            });
                                        }
                                    });
                        }


                        @Override
                        public void onLocationError(
                                String message
                        ) {

                            btnSaveReport.setEnabled(true);

                            Toast.makeText(
                                    ResultActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        });
    }


    // =========================================================
    // LOAD CAPTURED IMAGE
    // =========================================================

    private void loadCapturedImage() {

        String imageUriString =
                getIntent().getStringExtra(
                        "imageUri"
                );


        if (imageUriString == null
                || imageUriString.isEmpty()) {

            Toast.makeText(
                    this,
                    "No captured image received",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        try {

            Uri imageUri =
                    Uri.parse(imageUriString);


            InputStream inputStream =
                    getContentResolver()
                            .openInputStream(imageUri);


            if (inputStream == null) {

                Toast.makeText(
                        this,
                        "Unable to open captured image",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            Bitmap bitmap =
                    BitmapFactory.decodeStream(
                            inputStream
                    );


            inputStream.close();


            if (bitmap != null) {

                imgResult.setImageBitmap(
                        bitmap
                );

                imgResult.setScaleType(
                        ImageView.ScaleType.CENTER_CROP
                );

            } else {

                Toast.makeText(
                        this,
                        "Unable to decode captured image",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to display captured image",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}