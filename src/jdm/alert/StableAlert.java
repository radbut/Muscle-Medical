package jdm.alert;

public class StableAlert extends Alert {

    @Override
    public String getMessage() {
        return "Patient is stable. No action required.";
    }

    @Override
    public String getColor() {
        return "GREEN";
    }
}
