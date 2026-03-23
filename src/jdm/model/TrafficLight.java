package jdm.model;

public enum TrafficLight {
    GREEN("🟢 Stable - No immediate action needed"),
    YELLOW("🟡 Monitoring Required - Follow-up recommended"),
    RED("🔴 Critical - Urgent clinical attention needed");

    private final String message;

    TrafficLight(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}