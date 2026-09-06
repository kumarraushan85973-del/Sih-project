package com.agrirakshak.drone.activities;

import com.agrirakshak.drone.adapters.ScanHistoryAdapter;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrirakshak.drone.R;
import com.agrirakshak.drone.database.AppDatabase;
import com.agrirakshak.drone.database.ScanEntity;

import java.util.List;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerHistory;
    private TextView txtEmptyHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerHistory = findViewById(R.id.recyclerHistory);
        txtEmptyHistory = findViewById(R.id.txtEmptyHistory);

        recyclerHistory.setLayoutManager(
                new LinearLayoutManager(this)
        );

        loadHistory();
    }

    private void loadHistory() {

        Executors.newSingleThreadExecutor().execute(() -> {

            AppDatabase db =
                    AppDatabase.getInstance(HistoryActivity.this);

            List<ScanEntity> scans =
                    db.scanDao().getAllScans();

            runOnUiThread(() -> {

                if (scans.isEmpty()) {

                    txtEmptyHistory.setVisibility(
                            TextView.VISIBLE
                    );

                    recyclerHistory.setVisibility(
                            RecyclerView.GONE
                    );

                } else {

                    txtEmptyHistory.setVisibility(
                            TextView.GONE
                    );

                    recyclerHistory.setVisibility(
                            RecyclerView.VISIBLE
                    );

                    ScanHistoryAdapter adapter =
                            new ScanHistoryAdapter(scans);

                    recyclerHistory.setAdapter(adapter);
                }
            });
        });
    }
}