package com.agrirakshak.drone.models;

public class PredictionResult {

    private String status;
    private String disease;
    private double confidence;
    private String recommendation;

    public String getStatus() {
        return status;
    }

    public String getDisease() {
        return disease;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getRecommendation() {
        return recommendation;
    }
}