package com.agrirakshak.drone.drone;

public interface DroneController {

    void connect();

    void disconnect();

    void startMission();

    void stopMission();

    boolean isConnected();

    boolean isMissionRunning();

    String getStatus();
}