package com.agrirakshak.drone.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.agrirakshak.drone.R;
import com.agrirakshak.drone.database.AppDatabase;

import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private Spinner spinnerLanguage;
    private Button btnResetDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        btnResetDemo = findViewById(R.id.btnResetDemo);

        // Language options
        String[] languages = {
                "English",
                "हिंदी"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        languages
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerLanguage.setAdapter(adapter);

        // Reset demo data
        btnResetDemo.setOnClickListener(v -> {

            Executors.newSingleThreadExecutor().execute(() -> {

                AppDatabase db =
                        AppDatabase.getInstance(
                                ProfileActivity.this
                        );

                db.scanDao().deleteAllScans();

                runOnUiThread(() ->
                        Toast.makeText(
                                ProfileActivity.this,
                                "Demo scan data cleared",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            });
        });
    }
}