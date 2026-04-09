package jdm.service;

import jdm.alert.Alert;
import jdm.alert.CriticalAlert;
import jdm.alert.MonitoringAlert;
import jdm.alert.StableAlert;
import jdm.model.*;

public class MonitoringService {

    // Thresholds (example values, adjust based on clinical guidelines)
    private static final double CMAS_RED_THRESHOLD = 20.0;
    private static final double CMAS_YELLOW_THRESHOLD = 35.0;

    private static final double CXCL10_RED_THRESHOLD = 1000.0;
    private static final double CXCL10_YELLOW_THRESHOLD = 500.0;

    private static final double GAL9_RED_THRESHOLD = 100.0;
    private static final double GAL9_YELLOW_THRESHOLD = 50.0;

    private static final double TNFR2_RED_THRESHOLD = 2000.0;
    private static final double TNFR2_YELLOW_THRESHOLD = 1000.0;

    public TrafficLight calculateStatus(Patient patient) {
        if (patient == null || patient.getMeasurements().isEmpty()) {
            return TrafficLight.GREEN; // Default to stable if no data
        }

        TrafficLight worstStatus = TrafficLight.GREEN;

        // Check latest CMAS
        CMASMeasurement latestCmas = patient.getLatestCmas();
        if (latestCmas != null) {
            double score = latestCmas.getValue();
            if (score < CMAS_RED_THRESHOLD) {
                return TrafficLight.RED; // Critical
            } else if (score < CMAS_YELLOW_THRESHOLD) {
                worstStatus = TrafficLight.YELLOW;
            }
        }

        // Check latest biomarkers
        BiomarkerMeasurement cxcl10 = patient.getLatestBiomarker("CXCL10");
        if (cxcl10 != null) {
            double value = cxcl10.getValue();
            if (value > CXCL10_RED_THRESHOLD) {
                return TrafficLight.RED;
            } else if (value > CXCL10_YELLOW_THRESHOLD) {
                worstStatus = TrafficLight.YELLOW;
            }
        }

        BiomarkerMeasurement gal9 = patient.getLatestBiomarker("Galectin-9");
        if (gal9 != null) {
            double value = gal9.getValue();
            if (value > GAL9_RED_THRESHOLD) {
                return TrafficLight.RED;
            } else if (value > GAL9_YELLOW_THRESHOLD) {
                worstStatus = TrafficLight.YELLOW;
            }
        }

        BiomarkerMeasurement tnfr2 = patient.getLatestBiomarker("TNFR2");
        if (tnfr2 != null) {
            double value = tnfr2.getValue();
            if (value > TNFR2_RED_THRESHOLD) {
                return TrafficLight.RED;
            } else if (value > TNFR2_YELLOW_THRESHOLD) {
                worstStatus = TrafficLight.YELLOW;
            }
        }

        return worstStatus;
    }

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
