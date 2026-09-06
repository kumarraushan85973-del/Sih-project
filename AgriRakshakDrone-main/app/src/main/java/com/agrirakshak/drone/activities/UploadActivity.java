package com.agrirakshak.drone.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.agrirakshak.drone.R;
import com.agrirakshak.drone.api.ApiService;
import com.agrirakshak.drone.api.RetrofitClient;
import com.agrirakshak.drone.models.PredictionResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private TextView txtFileName;
    private Button btnSelect;
    private Button btnRemove;
    private Button btnUpload;

    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {

                            selectedImageUri = uri;

                            imgPreview.setImageURI(uri);

                            txtFileName.setText(
                                    getFileName(uri)
                            );

                            btnRemove.setVisibility(
                                    View.VISIBLE
                            );

                            btnUpload.setVisibility(
                                    View.VISIBLE
                            );
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload);

        imgPreview =
                findViewById(R.id.imgPreview);

        txtFileName =
                findViewById(R.id.txtFileName);

        btnSelect =
                findViewById(R.id.btnSelect);

        btnRemove =
                findViewById(R.id.btnRemove);

        btnUpload =
                findViewById(R.id.btnUpload);

        // Select image
        btnSelect.setOnClickListener(v -> {

            imagePicker.launch("image/*");

        });

        // Remove image
        btnRemove.setOnClickListener(v -> {

            selectedImageUri = null;

            imgPreview.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );

            txtFileName.setText(
                    "No image selected"
            );

            btnRemove.setVisibility(
                    View.GONE
            );

            btnUpload.setVisibility(
                    View.GONE
            );
        });

        // Upload and scan
        btnUpload.setOnClickListener(v -> {

            if (selectedImageUri == null) {

                Toast.makeText(
                        UploadActivity.this,
                        "Please select an image first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            uploadImage();
        });
    }

    private void uploadImage() {

        btnUpload.setEnabled(false);
        btnSelect.setEnabled(false);
        btnRemove.setEnabled(false);

        btnUpload.setText(
                "Scanning..."
        );

        try {

            // Convert Android Uri to File
            File imageFile =
                    createFileFromUri(
                            selectedImageUri
                    );

            if (!imageFile.exists()
                    || imageFile.length() == 0) {

                showError(
                        "Unable to read selected image"
                );

                return;
            }

            // Create request body
            RequestBody requestFile =
                    RequestBody.create(
                            MediaType.parse("image/jpeg"),
                            imageFile
                    );

            // IMPORTANT:
            // FastAPI expects field name "file"
            MultipartBody.Part imagePart =
                    MultipartBody.Part.createFormData(
                            "file",
                            imageFile.getName(),
                            requestFile
                    );

            // Retrofit API
            ApiService apiService =
                    RetrofitClient.getApiService();

            Call<PredictionResult> call =
                    apiService.predictDisease(
                            imagePart
                    );

            call.enqueue(
                    new Callback<PredictionResult>() {

                        @Override
                        public void onResponse(
                                Call<PredictionResult> call,
                                Response<PredictionResult> response
                        ) {

                            resetButton();

                            if (response.isSuccessful()
                                    && response.body() != null) {

                                PredictionResult result =
                                        response.body();

                                openResultScreen(
                                        result
                                );

                            } else {

                                String errorMessage =
                                        "Server error: "
                                                + response.code();

                                if (response.errorBody()
                                        != null) {

                                    try {

                                        String serverError =
                                                response
                                                        .errorBody()
                                                        .string();

                                        errorMessage +=
                                                "\n" + serverError;

                                    } catch (Exception ignored) {
                                    }
                                }

                                Toast.makeText(
                                        UploadActivity.this,
                                        errorMessage,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<PredictionResult> call,
                                Throwable t
                        ) {

                            resetButton();

                            Toast.makeText(
                                    UploadActivity.this,
                                    "AI connection failed:\n"
                                            + t.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

        } catch (Exception e) {

            resetButton();

            Toast.makeText(
                    this,
                    "Image processing error:\n"
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void openResultScreen(
            PredictionResult result
    ) {

        Intent intent =
                new Intent(
                        UploadActivity.this,
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

        startActivity(intent);
    }

    private void resetButton() {

        btnUpload.setEnabled(true);
        btnSelect.setEnabled(true);
        btnRemove.setEnabled(true);

        btnUpload.setText(
                "Upload & Scan"
        );
    }

    private void showError(
            String message
    ) {

        resetButton();

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    private File createFileFromUri(
            Uri uri
    ) throws Exception {

        String fileName =
                getFileName(uri);

        if (fileName == null
                || fileName.trim().isEmpty()) {

            fileName =
                    "crop_image.jpg";
        }

        // Avoid problematic file names
        fileName =
                fileName.replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );

        File file =
                new File(
                        getCacheDir(),
                        fileName
                );

        InputStream inputStream =
                getContentResolver()
                        .openInputStream(uri);

        if (inputStream == null) {

            throw new Exception(
                    "Could not open image"
            );
        }

        FileOutputStream outputStream =
                new FileOutputStream(file);

        byte[] buffer =
                new byte[8192];

        int length;

        while ((length =
                inputStream.read(buffer)) != -1) {

            outputStream.write(
                    buffer,
                    0,
                    length
            );
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return file;
    }

    private String getFileName(
            Uri uri
    ) {

        String result =
                "crop_image.jpg";

        if ("content".equals(
                uri.getScheme()
        )) {

            Cursor cursor =
                    getContentResolver()
                            .query(
                                    uri,
                                    null,
                                    null,
                                    null,
                                    null
                            );

            if (cursor != null) {

                int nameIndex =
                        cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME
                        );

                if (nameIndex >= 0
                        && cursor.moveToFirst()) {

                    String name =
                            cursor.getString(
                                    nameIndex
                            );

                    if (name != null
                            && !name.isEmpty()) {

                        result = name;
                    }
                }

                cursor.close();
            }
        }

        return result;
    }
}