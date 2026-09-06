package com.agrirakshak.drone.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scans")
public class ScanEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String status;
    public String disease;
    public double confidence;
    public String recommendation;
    public String dateTime;

    public double latitude;
    public double longitude;

    public ScanEntity(
            String status,
            String disease,
            double confidence,
            String recommendation,
            String dateTime,
            double latitude,
            double longitude
    ) {
        this.status = status;
        this.disease = disease;
        this.confidence = confidence;
        this.recommendation = recommendation;
        this.dateTime = dateTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}