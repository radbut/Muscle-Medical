package jdm.alert;

public class CriticalAlert extends Alert {

    @Override
    public String getMessage() {
        return "CRITICAL: Immediate clinical attention required.";
    }

    @Override
    public String getColor() {
        return "RED";
    }
}
