package jdm.service;

import jdm.alert.Alert;
import jdm.alert.CriticalAlert;
import jdm.alert.MonitoringAlert;
import jdm.alert.StableAlert;
import jdm.model.Patient;
import jdm.model.TrafficLight;

public class MonitoringService {

    public Alert getAlertForStatus(TrafficLight status) {
        if (status == null) {
            return new StableAlert();
        }
        return switch (status) {
            case GREEN -> new StableAlert();
            case YELLOW -> new MonitoringAlert();
            case RED -> new CriticalAlert();
        };
    }

    public Alert getAlertForPatient(Patient patient) {
        if (patient == null || patient.getStatus() == null) {
            return new StableAlert();
        }
        return getAlertForStatus(patient.getStatus());
    }
}
