package com.agrirakshak.drone.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrirakshak.drone.R;
import com.agrirakshak.drone.activities.ResultActivity;
import com.agrirakshak.drone.database.ScanEntity;

import java.util.List;
import java.util.Locale;

public class ScanHistoryAdapter
        extends RecyclerView.Adapter<ScanHistoryAdapter.ScanViewHolder> {

    private final List<ScanEntity> scanList;

    public ScanHistoryAdapter(List<ScanEntity> scanList) {
        this.scanList = scanList;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_scan_history,
                        parent,
                        false
                );

        return new ScanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ScanViewHolder holder,
            int position) {

        ScanEntity scan = scanList.get(position);

        // =========================
        // DISEASE
        // =========================
        if (scan.disease != null && !scan.disease.isEmpty()) {

            holder.txtDisease.setText(
                    scan.disease
            );

        } else {

            holder.txtDisease.setText(
                    "Unknown"
            );
        }


        // =========================
        // STATUS
        // =========================
        if (scan.status != null) {

            holder.txtStatus.setText(
                    scan.status
            );

            if (scan.status.equalsIgnoreCase("HEALTHY")) {

                holder.txtStatus.setTextColor(
                        Color.rgb(46, 125, 50)
                );

            } else {

                holder.txtStatus.setTextColor(
                        Color.rgb(211, 47, 47)
                );
            }

        } else {

            holder.txtStatus.setText(
                    "UNKNOWN"
            );

            holder.txtStatus.setTextColor(
                    Color.rgb(107, 114, 128)
            );
        }


        // =========================
        // CONFIDENCE
        // =========================
        holder.txtConfidence.setText(
                String.format(
                        Locale.getDefault(),
                        "Confidence: %.2f%%",
                        scan.confidence
                )
        );


        // =========================
        // DATE
        // =========================
        if (scan.dateTime != null) {

            holder.txtDateTime.setText(
                    scan.dateTime
            );

        } else {

            holder.txtDateTime.setText(
                    "Date unavailable"
            );
        }


        // =========================
        // GPS LOCATION
        // =========================
        if (scan.latitude != 0
                || scan.longitude != 0) {

            holder.txtLocation.setText(
                    String.format(
                            Locale.getDefault(),
                            "📍 %.6f, %.6f",
                            scan.latitude,
                            scan.longitude
                    )
            );

        } else {

            holder.txtLocation.setText(
                    "📍 Location unavailable"
            );
        }


        // =========================
        // OPEN RESULT DETAILS
        // =========================
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    v.getContext(),
                    ResultActivity.class
            );

            intent.putExtra(
                    "status",
                    scan.status
            );

            intent.putExtra(
                    "disease",
                    scan.disease
            );

            intent.putExtra(
                    "confidence",
                    scan.confidence
            );

            intent.putExtra(
                    "recommendation",
                    scan.recommendation
            );

            v.getContext().startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return scanList.size();
    }


    // =========================================================
    // VIEW HOLDER
    // =========================================================
    static class ScanViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtDisease;
        TextView txtStatus;
        TextView txtConfidence;
        TextView txtDateTime;
        TextView txtLocation;

        public ScanViewHolder(
                @NonNull View itemView) {

            super(itemView);

            txtDisease =
                    itemView.findViewById(
                            R.id.txtHistoryDisease
                    );

            txtStatus =
                    itemView.findViewById(
                            R.id.txtHistoryStatus
                    );

            txtConfidence =
                    itemView.findViewById(
                            R.id.txtHistoryConfidence
                    );

            txtDateTime =
                    itemView.findViewById(
                            R.id.txtHistoryDateTime
                    );

            txtLocation =
                    itemView.findViewById(
                            R.id.txtHistoryLocation
                    );
        }
    }
}