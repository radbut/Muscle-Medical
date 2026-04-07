package jdm.alert;

public class MonitoringAlert extends Alert {

    @Override
    public String getMessage() {
        return "Patient requires monitoring. Schedule a follow-up.";
    }

    @Override
    public String getColor() {
        return "YELLOW";
    }
}
