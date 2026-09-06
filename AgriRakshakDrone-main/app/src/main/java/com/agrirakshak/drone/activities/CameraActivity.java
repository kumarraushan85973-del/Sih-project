package com.agrirakshak.drone.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.agrirakshak.drone.R;
import com.agrirakshak.drone.api.ApiService;
import com.agrirakshak.drone.api.RetrofitClient;
import com.agrirakshak.drone.models.PredictionResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private TextView txtPlaceholder;

    private Button btnCapture;
    private Button btnRetake;
    private Button btnScanAI;

    private Uri imageUri;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            openCamera();
                        } else {
                            Toast.makeText(
                                    CameraActivity.this,
                                    R.string.camera_perm_required,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            showCapturedImage();
                        } else {
                            Toast.makeText(
                                    CameraActivity.this,
                                    R.string.photo_capture_cancelled,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        imagePreview = findViewById(R.id.imagePreview);
        txtPlaceholder = findViewById(R.id.txtPlaceholder);

        btnCapture = findViewById(R.id.btnCapture);
        btnRetake = findViewById(R.id.btnRetake);
        btnScanAI = findViewById(R.id.btnScanAI);

        btnCapture.setOnClickListener(v ->
                checkCameraPermission()
        );

        btnRetake.setOnClickListener(v -> {
            resetPreview();
            checkCameraPermission();
        });

        btnScanAI.setOnClickListener(v -> {

            if (imageUri == null) {
                Toast.makeText(
                        CameraActivity.this,
                        R.string.capture_first,
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            scanImageWithAI();
        });
    }

    private void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            openCamera();

        } else {

            cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
            );
        }
    }

    private void openCamera() {

        Intent cameraIntent =
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(
                getPackageManager()
        ) == null) {

            Toast.makeText(
                    this,
                    R.string.no_camera_app,
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        File photoFile;

        try {

            photoFile = createImageFile();

        } catch (IOException e) {

            Toast.makeText(
                    this,
                    R.string.unable_create_image,
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        imageUri =
                FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        photoFile
                );

        cameraIntent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                imageUri
        );

        cameraIntent.addFlags(
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_READ_URI_PERMISSION
        );

        cameraLauncher.launch(cameraIntent);
    }

    private File createImageFile()
            throws IOException {

        String timeStamp =
                new SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.getDefault()
                ).format(new Date());

        String imageFileName =
                "AGRIRAKSHAK_" + timeStamp + "_";

        File storageDir =
                getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES
                );

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void showCapturedImage() {

        if (imageUri == null) {
            return;
        }

        imagePreview.setImageURI(imageUri);

        txtPlaceholder.setVisibility(
                TextView.GONE
        );

        btnCapture.setText(
                R.string.photo_captured
        );

        btnRetake.setVisibility(
                Button.VISIBLE
        );

        btnScanAI.setVisibility(
                Button.VISIBLE
        );

        Toast.makeText(
                this,
                R.string.photo_captured_success,
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================================================
    // AI SCAN
    // =========================================================

    private void scanImageWithAI() {

        btnScanAI.setEnabled(false);
        btnRetake.setEnabled(false);

        btnScanAI.setText(
                "AI Scanning..."
        );

        Toast.makeText(
                this,
                "Sending image to AI...",
                Toast.LENGTH_SHORT
        ).show();

        ExecutorsHelper.run(() -> {

            File uploadFile = null;

            try {

                uploadFile =
                        copyUriToCache(imageUri);

                File finalUploadFile = uploadFile;

                runOnUiThread(() -> {

                    sendImageToServer(
                            finalUploadFile
                    );
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    resetScanButtons();

                    Toast.makeText(
                            CameraActivity.this,
                            "Unable to prepare image: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    // =========================================================
    // COPY IMAGE TO CACHE
    // =========================================================

    private File copyUriToCache(Uri uri)
            throws IOException {

        File cacheFile =
                new File(
                        getCacheDir(),
                        "agrirakshak_scan.jpg"
                );

        InputStream inputStream =
                getContentResolver()
                        .openInputStream(uri);

        if (inputStream == null) {
            throw new IOException(
                    "Unable to open image"
            );
        }

        FileOutputStream outputStream =
                new FileOutputStream(cacheFile);

        byte[] buffer = new byte[8192];

        int length;

        while ((length = inputStream.read(buffer)) > 0) {

            outputStream.write(
                    buffer,
                    0,
                    length
            );
        }

        inputStream.close();
        outputStream.close();

        return cacheFile;
    }

    // =========================================================
    // SEND IMAGE TO FASTAPI
    // =========================================================

    private void sendImageToServer(
            File imageFile
    ) {

        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("image/jpeg"),
                        imageFile
                );

        MultipartBody.Part body =
                MultipartBody.Part.createFormData(
                        "file",
                        imageFile.getName(),
                        requestFile
                );

        ApiService apiService =
                RetrofitClient.getApiService();

        Call<PredictionResult> call =
                apiService.predictDisease(body);

        call.enqueue(
                new Callback<PredictionResult>() {

                    @Override
                    public void onResponse(
                            Call<PredictionResult> call,
                            Response<PredictionResult> response
                    ) {

                        resetScanButtons();

                        if (response.isSuccessful()
                                && response.body() != null) {

                            PredictionResult result =
                                    response.body();

                            openResultScreen(result);

                        } else {

                            Toast.makeText(
                                    CameraActivity.this,
                                    "AI server returned an error: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<PredictionResult> call,
                            Throwable t
                    ) {

                        resetScanButtons();

                        Toast.makeText(
                                CameraActivity.this,
                                "Cannot connect to AI server.\n"
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =========================================================
    // OPEN RESULT SCREEN
    // =========================================================

    private void openResultScreen(
            PredictionResult result
    ) {

        Intent intent =
                new Intent(
                        CameraActivity.this,
                        ResultActivity.class
                );

        intent.putExtra(
                "status",
                result.getStatus()
        );

        intent.putExtra(
                "disease",
                result.getDisease()
        );

        intent.putExtra(
                "confidence",
                result.getConfidence()
        );

        intent.putExtra(
                "recommendation",
                result.getRecommendation()
        );

        // Pass captured image URI
        if (imageUri != null) {

            intent.putExtra(
                    "imageUri",
                    imageUri.toString()
            );
        }

        startActivity(intent);
    }

    // =========================================================
    // RESET BUTTONS
    // =========================================================

    private void resetScanButtons() {

        btnScanAI.setEnabled(true);
        btnRetake.setEnabled(true);

        btnScanAI.setText(
                "Scan with AI"
        );
    }

    // =========================================================
    // RESET PREVIEW
    // =========================================================

    private void resetPreview() {

        imagePreview.setImageDrawable(null);

        txtPlaceholder.setVisibility(
                TextView.VISIBLE
        );

        btnCapture.setText(
                R.string.capture_photo
        );

        btnRetake.setVisibility(
                Button.GONE
        );

        btnScanAI.setVisibility(
                Button.GONE
        );

        imageUri = null;
    }

    // =========================================================
    // SIMPLE BACKGROUND EXECUTOR
    // =========================================================

    private static class ExecutorsHelper {

        static void run(Runnable runnable) {

            java.util.concurrent.Executors
                    .newSingleThreadExecutor()
                    .execute(runnable);
        }
    }
}