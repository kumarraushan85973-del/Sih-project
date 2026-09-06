package com.agrirakshak.drone.drone;

public class MockDroneController implements DroneController {

    private boolean connected = false;
    private boolean missionRunning = false;

    @Override
    public void connect() {
        connected = true;
    }

    @Override
    public void disconnect() {
        connected = false;
        missionRunning = false;
    }

    @Override
    public void startMission() {
        if (connected) {
            missionRunning = true;
        }
    }

    @Override
    public void stopMission() {
        missionRunning = false;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean isMissionRunning() {
        return missionRunning;
    }

    @Override
    public String getStatus() {

        if (!connected) {
            return "DISCONNECTED";
        }

        if (missionRunning) {
            return "MISSION RUNNING";
        }

        return "CONNECTED";
    }
}